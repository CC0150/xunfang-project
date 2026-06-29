package com.xunfang.manufacture.controller;

import com.xunfang.system.tools.DMEUtil;
import com.xunfang.system.tools.TokenAndProject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;

/**
 * 附件直链下载代理 — 处理 DME 文件下载（200/302、中文文件名、ISO-8859-1）
 */
@RestController
@RequestMapping("/file")
public class FileProxyController {

    @Autowired
    private DMEUtil dmeUtil;

    @GetMapping("/download")
    public void download(@RequestParam("url") String fileUrl,
                         @RequestParam(value = "name", required = false) String fileName,
                         HttpServletResponse response) throws Exception {
        if (fileUrl == null || fileUrl.isEmpty()) {
            response.setStatus(404);
            return;
        }
        TokenAndProject tap = dmeUtil.getToken();
        String token = tap.getToken();

        // 打开连接（不自动跟随重定向）
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
                conn.connect();
                status = conn.getResponseCode();
            }
        }

        if (status != 200) {
            response.setStatus(status);
            conn.disconnect();
            return;
        }

        // 文件名处理
        if (fileName != null) {
            try { fileName = URLDecoder.decode(fileName, "UTF-8"); } catch (Exception ignored) {}
        }
        String contentDisposition = conn.getHeaderField("Content-Disposition");
        if (fileName == null && contentDisposition != null) {
            // 从 Content-Disposition 提取 filename
            for (String part : contentDisposition.split(";")) {
                part = part.trim();
                if (part.startsWith("filename*=")) {
                    fileName = part.substring(part.indexOf("'") + 1);
                    if (fileName.contains("'")) fileName = fileName.substring(fileName.indexOf("'") + 1);
                    try { fileName = URLDecoder.decode(fileName, "UTF-8"); } catch (Exception ignored) {}
                } else if (part.startsWith("filename=")) {
                    fileName = part.substring(9).replace("\"", "");
                    try { fileName = new String(fileName.getBytes("ISO-8859-1"), "UTF-8"); } catch (Exception ignored) {}
                }
            }
        }
        if (fileName == null) fileName = "download";

        response.setContentType(conn.getContentType() != null ? conn.getContentType() : "application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=\"" +
                new String(fileName.getBytes("UTF-8"), "ISO-8859-1") + "\"");
        response.setContentLength(conn.getContentLength());

        try (InputStream is = conn.getInputStream();
             OutputStream os = response.getOutputStream()) {
            byte[] buf = new byte[8192]; int n;
            while ((n = is.read(buf)) >= 0) os.write(buf, 0, n);
        }
        conn.disconnect();
    }
}
