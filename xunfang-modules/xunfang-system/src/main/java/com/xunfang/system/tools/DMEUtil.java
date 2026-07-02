package com.xunfang.system.tools;


import com.xunfang.system.tools.RedisCache1;
import com.xunfang.system.tools.TokenAndProject;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

@SuppressWarnings("deprecation")
@Component
public class DMEUtil {
    private static final Logger logger = LoggerFactory.getLogger(DMEUtil.class);
    @Autowired
    RedisCache1 redisCache1;

    private static final String userName = "gzlg020";//用户名
    private static final String password = "Hngy@123456";//密码
    private static final String account = "sziit2024";//账户名
    private static final String hwyProjectName = "cn-north-4";//区域名
    public static final String projectUrl = "http://7ecd0cfa-83e9-493a-b9a1-c0e4d5e80fb4.xdm.runtime.cn-north-4.huaweicloud-idme.com/rdm_5eb684ad0fcc42e0b16ff67259692e7d_app/services";//url前缀
    /** 基础 URL（不带 /services），用于文件上传/下载 */
    public static final String basicUrl = "http://7ecd0cfa-83e9-493a-b9a1-c0e4d5e80fb4.xdm.runtime.cn-north-4.huaweicloud-idme.com/rdm_5eb684ad0fcc42e0b16ff67259692e7d_app";

    public static final String apiCustomService = "/rdm/basic/api/customservice/";//高代码编码前缀
    public static final String tenantApiService = "/rdm/common/api/Tenant/";//租户API前缀
    public static final String apiExecute = "/dynamic/api/";//全量API前缀

    // ===== IDME 文件上传/下载 API 路径 =====
    private static final String API_UPLOAD_FILE = "/rdm/basic/api/upload/uploadFile";
    private static final String API_DOWNLOAD_FILE = "/rdm/basic/api/file/downloadFile";

    // ===== IDME 文件 API 配置 =====
    @Value("${dme.file.applicationId:5eb684ad0fcc42e0b16ff67259692e7d}")
    private String dmeApplicationId;
    public String getApplicationId() { return dmeApplicationId; }

    /** 操作者用户名（IAM 用户名） */
    public static final String usingUserName = "gzlg020";
    /** 操作者用户 ID（IAM 用户 ID） */
    public static final String usingUserId = "019eda0119b874869b7d13b6f0ff25c2";

    /** DME 操作者标识（格式: username userId，与 DME 实体存储格式一致） */
    public static String getOperator() {
        return usingUserName + " " + usingUserId;
    }

    /**
     * 获取DMEapi认证token
     *
     * @return
     * @throws Exception
     */
    @SuppressWarnings({"resource"})
    public TokenAndProject getToken()
            throws Exception {
        String mcName = userName + account;
        logger.debug("token mcName" + mcName);
        String token = (String) redisCache1.get(mcName + "_dmetoken");
        String projectId = (String) redisCache1.get(mcName + "_dmeprojectId");
        if (StringUtils.isBlank(token)) {
            String url = "https://iam.myhuaweicloud.com/v3/auth/tokens";
            StringEntity se = new StringEntity(
                    "{\"auth\":{\"identity\":{\"methods\":[\"password\"],\"password\":{\"user\":{\"name\":\"" + userName
                            + "\",\"domain\":{\"name\":\"" + account + "\"},\"password\":\"" + password
                            + "\"}}},\"scope\":{\"project\":{\"name\":\"" + hwyProjectName + "\"}}}}",
                    "UTF-8");
            HttpPost post = new HttpPost(url);
            post.setHeader("User-Agent", "Mozilla/5.0");
            post.setHeader("Content-Type", "application/json;charset=utf8");
            post.setEntity(se);
            HttpClient client = new DefaultHttpClient();
            HttpResponse response = null;
            JSONObject resjo = null;
            String res = "";
            response = client.execute(post);
            token = response.getFirstHeader("X-Subject-Token").getValue();
            BufferedReader httpinput = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            res = httpinput.lines().collect(Collectors.joining());
            logger.debug("token res" + res);
            resjo = new JSONObject(res);
            projectId = resjo.getJSONObject("token").getJSONObject("project").getString("id");
            ClientConnectionManager ku = client.getConnectionManager();
            ku.closeExpiredConnections();
            ku.shutdown();
            redisCache1.set(mcName + "_dmetoken", token, 1 * 60 * 60);
            redisCache1.set(mcName + "_dmeprojectId", projectId, 1 * 60 * 60);
        }
        return new TokenAndProject(token, projectId);
    }

    // date类型转UTC格式
    public static String dateToUTCString(Date date) {
        if (date == null ) {
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'.000+0000'");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(date);
    }

    //dme时间转换为考试date
    public static Date dmeDateToExamDate(String dmeT) throws Exception {
        if (StringUtils.isEmpty(dmeT)) {
            return null;
        }
        // DME 时间可能带任意毫秒值（.000 或 .520），统一处理
        String normalized = dmeT.replaceAll("\\.\\d{3}\\+", ".000+");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'.000+0000'");
        //需要GMT+8  个小时时差
        Calendar c = Calendar.getInstance();
        c.setTime(sdf.parse(normalized));
        c.add(Calendar.HOUR, 8);
        return c.getTime();
    }

    /**
     * @param res    响应结果JSON类型
     * @param idname 主键名
     * @return
     * @throws Exception Map<String,String>
     * @Title: analysisReqResult
     * @Description: TODO(DME请求结果返回主键id)
     * @author 刘念
     * @date 2023年3月31日 上午9:58:20
     */
    public static Map<String, String> analysisReqResult(String res, String idname) throws Exception {
        Map<String, String> resMap = new HashMap<String, String>();
        if (null != res) {
            JSONObject jsonObject = new JSONObject(res);
            if ("SUCCESS".equals(jsonObject.getString("result"))) {
                resMap.put("result", "SUCCESS");
                JSONArray jsonArray = jsonObject.getJSONArray("data");
                JSONObject data = jsonArray.getJSONObject(0);
                resMap.put("id", data.getString(idname));
            } else {
                resMap.put("result", jsonObject.getString("result"));
                JSONArray jsonArray = jsonObject.getJSONArray("errors");
                JSONObject data = jsonArray.getJSONObject(0);
                resMap.put("code", data.getString("code"));
                resMap.put("message", data.getString("message"));
                resMap.put("detailMessage", data.getString("detailMessage"));
            }
        } else {
            resMap.put("result", "error");
            resMap.put("message", "请求结果返回为空");
        }
        return resMap;
    }

    // ==================== IDME 文件上传/下载 API ====================

    /**
     * 上传文件到 IDME 平台
     *
     * @param modelName     模型名称（如 XfPart01_20）
     * @param applicationId 应用 ID
     * @param attributeName 属性名称（如 file）
     * @param file          上传的文件
     * @param instanceId    实体实例 ID（可选，用于关联文件到实体）
     * @param token         DME 认证 token
     * @return DME 上传响应 JSONObject
     * @throws Exception 网络或 API 异常
     */
    public JSONObject uploadFile(String modelName, String applicationId,
                                 String attributeName, MultipartFile file,
                                 String instanceId, String token) throws Exception {
        String originalName = file.getOriginalFilename();
        if (originalName == null) originalName = "file";

        // 构建 URL（所有参数在 Query 中）
        StringBuilder urlBuilder = new StringBuilder(projectUrl)
                .append(API_UPLOAD_FILE)
                .append("?modelName=").append(URLEncoder.encode(modelName, "UTF-8"))
                .append("&attributeName=").append(URLEncoder.encode(attributeName, "UTF-8"))
                .append("&applicationId=").append(URLEncoder.encode(applicationId, "UTF-8"))
                .append("&modelNumber=").append(URLEncoder.encode(modelName, "UTF-8"))
                .append("&encrypted=true")
                .append("&autoResolution=true")
                .append("&exaAttr=1");
        if (instanceId != null && !instanceId.isEmpty()) {
            urlBuilder.append("&instanceId=").append(URLEncoder.encode(instanceId, "UTF-8"));
        }

        String boundary = "----DmeFormBoundary" + System.currentTimeMillis();
        HttpURLConnection conn = (HttpURLConnection) new URL(urlBuilder.toString()).openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("X-Auth-Token", token);
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        conn.setConnectTimeout(30000);
        conn.setReadTimeout(120000);

        // 写入 multipart body
        try (OutputStream os = conn.getOutputStream();
             PrintWriter writer = new PrintWriter(new OutputStreamWriter(os, "UTF-8"), true)) {

            writer.append("--").append(boundary).append("\r\n");
            writer.append("Content-Disposition: form-data; name=\"files\"; filename=\"")
                  .append(originalName).append("\"\r\n");
            writer.append("Content-Type: ").append(file.getContentType() != null
                  ? file.getContentType() : "application/octet-stream").append("\r\n");
            writer.append("\r\n");
            writer.flush();
            os.write(file.getBytes());
            os.flush();
            writer.append("\r\n");
            writer.append("--").append(boundary).append("--\r\n");
            writer.flush();
        }

        // 读取响应
        int status = conn.getResponseCode();
        InputStream responseStream = (status >= 200 && status < 300)
                ? conn.getInputStream() : conn.getErrorStream();
        StringBuilder sb = new StringBuilder();
        if (responseStream != null) {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(responseStream, "UTF-8"))) {
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
            }
        }
        conn.disconnect();
        String responseBody = sb.toString();

        if (status != 200) {
            logger.error("DME文件上传失败, status={}, response={}", status, responseBody);
            throw new RuntimeException("DME文件上传失败, HTTP " + status + ": " + responseBody);
        }

        JSONObject result;
        try {
            result = new JSONObject(responseBody);
        } catch (org.json.JSONException e) {
            throw new RuntimeException("DME文件上传响应解析失败: " + responseBody, e);
        }
        if (!"SUCCESS".equals(result.optString("result", ""))) {
            throw new RuntimeException("DME文件上传失败: " + responseBody);
        }
        logger.info("DME文件上传成功, modelName={}, fileName={}", modelName, originalName);
        return result;
    }

    /**
     * 打开 IDME 文件下载连接（返回流式连接，调用方负责关闭）
     *
     * @param fileId        文件 ID
     * @param modelName     模型名称
     * @param attributeName 属性名称
     * @param instanceId    实例 ID
     * @param applicationId 应用 ID
     * @param isMasterAttr  是否为主对象属性
     * @param token         DME 认证 token
     * @return 已连接的 HttpURLConnection，可从 getInputStream() 读取文件内容
     * @throws Exception 连接异常
     */
    public HttpURLConnection openDownloadConnection(String fileId, String modelName,
            String attributeName, String instanceId, String applicationId,
            boolean isMasterAttr, String token) throws Exception {
        StringBuilder urlBuilder = new StringBuilder(projectUrl)
                .append(API_DOWNLOAD_FILE)
                .append("?file_ids=").append(URLEncoder.encode(fileId, "UTF-8"))
                .append("&model_name=").append(URLEncoder.encode(modelName, "UTF-8"))
                .append("&model_number=").append(URLEncoder.encode(modelName, "UTF-8"))
                .append("&instance_id=").append(URLEncoder.encode(instanceId, "UTF-8"))
                .append("&application_id=").append(URLEncoder.encode(applicationId, "UTF-8"))
                .append("&is_master_attr=0")
                .append("&attribute_name=").append(URLEncoder.encode(attributeName, "UTF-8"))
                .append("&download_type=DIRECT_LINK")
                .append("&tenant_id=-1");

        logger.debug("DME下载请求: {}", urlBuilder.toString());

        HttpURLConnection conn = (HttpURLConnection) new URL(urlBuilder.toString()).openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("X-Auth-Token", token);
        conn.setConnectTimeout(30000);
        conn.setReadTimeout(60000);
        conn.setInstanceFollowRedirects(true);
        conn.connect();
        return conn;
    }

    /**
     * 从上传响应中提取文件 ID
     *
     * @param uploadResult uploadFile 返回的 JSONObject
     * @return 文件 ID 字符串，失败返回 null
     */
    public static String extractFileIdFromUploadResponse(JSONObject uploadResult) {
        try {
            JSONArray dataArr = uploadResult.optJSONArray("data");
            if (dataArr != null && dataArr.length() > 0) {
                Object first = dataArr.opt(0);
                // 情况1：data[0] 是 "fileId" 字符串
                if (first instanceof String) {
                    return (String) first;
                }
                // 情况2：data[0] 是 {"id":"fileId"} 对象
                if (first instanceof JSONObject) {
                    JSONObject data = (JSONObject) first;
                    String fileId = data.optString("fileId", null);
                    if (fileId == null || fileId.isEmpty()) {
                        fileId = data.optString("id", null);
                    }
                    if (fileId == null || fileId.isEmpty()) {
                        fileId = data.optString("file_id", null);
                    }
                    return fileId;
                }
            }
        } catch (Exception e) {
            logger.warn("提取文件ID失败: {}", e.getMessage());
        }
        return null;
    }

}
