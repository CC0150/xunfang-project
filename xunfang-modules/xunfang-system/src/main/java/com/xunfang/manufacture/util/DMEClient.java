package com.xunfang.manufacture.util;

import com.alibaba.fastjson2.JSONReader;
import com.xunfang.system.tools.DMEUtil;
import com.xunfang.system.tools.RequestUtil;
import com.xunfang.system.tools.TokenAndProject;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * DME API 调用封装 — 统一 Token 获取、请求发送、响应解析
 */
@Component
public class DMEClient {

    @Autowired
    private DMEUtil dmeUtil;

    /** POST 请求并返回 SUCCESS/FAIL */
    public boolean execute(String entityPath, JSONObject params) throws Exception {
        TokenAndProject tap = dmeUtil.getToken();
        String url = DMEUtil.projectUrl + DMEUtil.apiExecute + entityPath;
        JSONObject pj = new JSONObject(); pj.put("params", params);
        String res = RequestUtil.requestsPost(url, pj.toString(), tap.getToken());
        JSONObject root = new JSONObject(res);
        return "SUCCESS".equalsIgnoreCase(root.optString("result", ""));
    }

    /** POST 请求并返回完整 JSON 响应 */
    public JSONObject executeWithResponse(String entityPath, JSONObject params) throws Exception {
        TokenAndProject tap = dmeUtil.getToken();
        String url = DMEUtil.projectUrl + DMEUtil.apiExecute + entityPath;
        JSONObject pj = new JSONObject(); pj.put("params", params);
        String res = RequestUtil.requestsPost(url, pj.toString(), tap.getToken());
        return new JSONObject(res);
    }

    /** GET 单个实体，返回 data[0] */
    public JSONObject getOne(String entityName, String id) throws Exception {
        JSONObject p = new JSONObject(); p.put("id", id);
        JSONObject root = executeWithResponse("/" + entityName + "/get", p);
        JSONArray arr = root.getJSONArray("data");
        return arr.getJSONObject(0);
    }

    /** 分页查询 find/{pageSize}/{pageNum} */
    public JSONObject find(String entityName, JSONObject filterParams, int pageNum, int pageSize) throws Exception {
        JSONObject params = new JSONObject();
        params.put("characterSet", "UTF8");
        params.put("decrypt", false);
        params.put("filter", filterParams);
        params.put("isNeedTotal", true);
        params.put("isPresentAll", true);
        params.put("publicData", "INCLUDE_PUBLIC_DATA");
        params.put("sorts", new JSONArray());
        String url = "/" + entityName + "/find/" + pageSize + "/" + pageNum;
        return executeWithResponse(url, params);
    }

    /** 拍平 DME 枚举字段（从 JSONObject 提取 alias/enName） */
    public static void flattenEnum(JSONObject obj, String field) throws Exception {
        JSONObject enumObj = obj.optJSONObject(field);
        if (enumObj != null) {
            String v = enumObj.optString("alias", null);
            if (v == null || v.isEmpty()) v = enumObj.optString("enName", null);
            if (v != null && !v.isEmpty()) obj.put(field, v);
        }
    }

    /** 拍平多个枚举字段 */
    public static JSONObject flattenEnums(JSONObject obj, String... fields) throws Exception {
        for (String f : fields) flattenEnum(obj, f);
        return obj;
    }

    /** 解析 JSONArray 为 Java List */
    public static <T> List<T> parseList(JSONArray arr, Class<T> clazz) {
        if (arr == null) return java.util.Collections.emptyList();
        return com.alibaba.fastjson2.JSON.parseArray(arr.toString(), clazz, JSONReader.Feature.SupportSmartMatch);
    }
}
