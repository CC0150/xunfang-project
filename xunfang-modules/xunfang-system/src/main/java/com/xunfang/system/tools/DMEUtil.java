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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

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

    public static final String apiCustomService = "/rdm/basic/api/customservice/";//高代码编码前缀
    public static final String tenantApiService = "/rdm/common/api/Tenant/";//租户API前缀
    public static final String apiExecute = "/dynamic/api/";//全量API前缀


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

}
