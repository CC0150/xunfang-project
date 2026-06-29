package com.xunfang.manufacture.controller;

import com.xunfang.system.tools.DMEUtil;
import com.xunfang.system.tools.RequestUtil;
import com.xunfang.system.tools.TokenAndProject;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 文件下载代理 — 支持本地文件 + iDME 远程文件两种模式
 */
@RestController
@RequestMapping("/file")
public class FileProxyController {

    @Autowired
    private DMEUtil dmeUtil;

    /** 本地文件存储目录（与 xunfang-file 服务共用路径） */
    @Value("${xunfang.file.upload-dir:D:/xunfang/uploadPath}")
    private String uploadDir;

    /**
     * 文件下载
     * <p>本地文件：通过 file_id 从本地目录查找
     * <p>远程 DME 文件：通过 url 参数代理下载
     */
    @GetMapping("/download")
    public void download(
            @RequestParam(value = "model_name", required = false) String modelName,
            @RequestParam(value = "instance_id", required = false) String instanceId,
            @RequestParam(value = "file_id", required = false) String fileId,
            @RequestParam(value = "attribute_name", required = false, defaultValue = "file") String attributeName,
            @RequestParam(value = "filename", required = false) String fileName,
            @RequestParam(value = "url", required = false) String fileUrl,
            HttpServletResponse response) throws Exception {

        // 1. 优先尝试本地文件
        if (fileId != null && !fileId.isEmpty()) {
            Path dir = Paths.get(uploadDir);
            if (Files.exists(dir)) {
                File[] matches = dir.toFile().listFiles((d, name) -> name.startsWith(fileId));
                if (matches != null && matches.length > 0) {
                    File localFile = matches[0];
                    if (fileName != null) {
                        try { fileName = URLDecoder.decode(fileName, "UTF-8"); } catch (Exception ignored) {}
                    }
                    if (fileName == null || fileName.isEmpty()) {
                        fileName = localFile.getName();
                    }
                    response.setContentType("application/octet-stream");
                    response.setHeader("Content-Disposition",
                            "attachment; filename=\"" +
                                    new String(fileName.getBytes("UTF-8"), "ISO-8859-1") + "\"");
                    response.setContentLength((int) localFile.length());
                    try (InputStream is = new FileInputStream(localFile);
                         OutputStream os = response.getOutputStream()) {
                        byte[] buf = new byte[8192];
                        int n;
                        while ((n = is.read(buf)) >= 0) os.write(buf, 0, n);
                    }
                    return;
                }
            }
        }

        // 2. 回退：DME 远程代理下载
        String downloadUrl = resolveDownloadUrl(modelName, instanceId, fileId, attributeName, fileUrl);
        if (downloadUrl == null || downloadUrl.isEmpty()) {
            response.setStatus(404);
            response.getWriter().write("{\"msg\":\"文件不存在\"}");
            return;
        }
        proxyDmeDownload(downloadUrl, fileName, response);
    }

    /** 解析 DME 文件下载 URL */
    private String resolveDownloadUrl(String modelName, String instanceId,
                                       String fileId, String attributeName, String fileUrl) throws Exception {
        if (fileUrl != null && !fileUrl.isEmpty()) {
            return fileUrl;
        }
        if (modelName != null && !modelName.isEmpty()
                && instanceId != null && !instanceId.isEmpty()
                && fileId != null && !fileId.isEmpty()) {
            TokenAndProject tap = dmeUtil.getToken();
            String token = tap.getToken();
            String url = DMEUtil.projectUrl + DMEUtil.apiExecute + "/" + modelName + "/get";
            JSONObject params = new JSONObject();
            params.put("id", instanceId);
            JSONObject body = new JSONObject();
            body.put("params", params);
            String res = RequestUtil.requestsPost(url, body.toString(), token);
            JSONObject root = new JSONObject(res);
            JSONArray dataArr = root.optJSONArray("data");
            if (dataArr != null && dataArr.length() > 0) {
                JSONObject instance = dataArr.getJSONObject(0);
                JSONArray extAttrs = instance.optJSONArray("extAttrs");
                if (extAttrs != null) {
                    for (int i = 0; i < extAttrs.length(); i++) {
                        JSONObject attr = extAttrs.getJSONObject(i);
                        Object valObj = attr.opt("value");
                        if (valObj instanceof JSONArray) {
                            JSONArray values = (JSONArray) valObj;
                            for (int j = 0; j < values.length(); j++) {
                                JSONObject fo = values.getJSONObject(j);
                                if (fileId.equals(fo.optString("id"))) {
                                    return fo.optString("fileDownloadUrl");
                                }
                            }
                        }
                    }
                }
            }
            return null;
        }
        return null;
    }

    /** DME 远程文件代理下载 */
    private void proxyDmeDownload(String fileUrl, String fileName,
                                   HttpServletResponse response) throws Exception {
        TokenAndProject tap = dmeUtil.getToken();
        String token = tap.getToken();

        HttpURLConnection conn = (HttpURLConnection) new URL(fileUrl).openConnection();
        conn.setInstanceFollowRedirects(false);
        conn.setRequestProperty("X-Auth-Token", token);
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(30000);
        conn.connect();

        int status = conn.getResponseCode();
        if (status == 302 || status == 301) {
            String location = conn.getHeaderField("Location");
            conn.disconnect();
            if (location != null) {
                conn = (HttpURLConnection) new URL(location).openConnection();
                conn.setRequestProperty("X-Auth-Token", token);
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(30000);
                conn.connect();
                status = conn.getResponseCode();
            }
        }

        if (status != 200) {
            response.setStatus(status);
            conn.disconnect();
            return;
        }

        if (fileName != null) {
            try { fileName = URLDecoder.decode(fileName, "UTF-8"); } catch (Exception ignored) {}
        }
        String cd = conn.getHeaderField("Content-Disposition");
        if (fileName == null && cd != null) {
            fileName = extractFilename(cd);
        }
        if (fileName == null || fileName.isEmpty()) fileName = "download";

        response.setContentType(conn.getContentType() != null ? conn.getContentType() : "application/octet-stream");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"" + new String(fileName.getBytes("UTF-8"), "ISO-8859-1") + "\"");
        response.setContentLength(conn.getContentLength());

        try (InputStream is = conn.getInputStream();
             OutputStream os = response.getOutputStream()) {
            byte[] buf = new byte[8192];
            int n;
            while ((n = is.read(buf)) >= 0) os.write(buf, 0, n);
        }
        conn.disconnect();
    }

    private String extractFilename(String cd) {
        if (cd == null) return null;
        for (String part : cd.split(";")) {
            part = part.trim();
            if (part.startsWith("filename*=")) {
                String val = part.substring("filename*=".length());
                int idx = val.indexOf("'");
                if (idx >= 0) {
                    int idx2 = val.indexOf("'", idx + 1);
                    if (idx2 >= 0) val = val.substring(idx2 + 1);
                }
                try { return URLDecoder.decode(val, "UTF-8"); } catch (Exception e) { return val; }
            }
        }
        for (String part : cd.split(";")) {
            part = part.trim();
            if (part.startsWith("filename=")) {
                String result = part.substring("filename=".length()).replace("\"", "").trim();
                try { return new String(result.getBytes("ISO-8859-1"), "UTF-8"); } catch (Exception ignored) {}
                return result;
            }
        }
        return null;
    }
}
