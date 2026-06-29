package com.xunfang.manufacture.service.impl;

import com.xunfang.common.core.utils.DateUtils;
import com.xunfang.common.core.web.domain.AjaxResult;
import com.xunfang.common.core.web.page.TableDataInfo;
import com.xunfang.manufacture.domain.XfVersionPart;
import com.xunfang.manufacture.service.IXfPartService;
import com.xunfang.system.tools.DMEUtil;
import com.xunfang.system.tools.RequestUtil;
import com.xunfang.system.tools.TokenAndProject;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Service
public class XfPartServiceImpl implements IXfPartService
{
    @Autowired
    private DMEUtil dmeUtil;

    // ==================== 工具方法 ====================

    /** 从 DME 枚举对象提取 alias 值 */
    private String pickAlias(Object enumObj, String defaultVal) {
        if (enumObj instanceof JSONObject) {
            JSONObject eo = (JSONObject) enumObj;
            String v = eo.optString("alias", null);
            if (v == null || v.isEmpty()) v = eo.optString("enName", null);
            if (v != null && !v.isEmpty()) return v;
        }
        if (enumObj instanceof String) {
            String s = (String) enumObj;
            if (!s.isEmpty() && !"null".equalsIgnoreCase(s)) return s;
        }
        return defaultVal;
    }

    /** GET 单个 Part 原始数据 */
    private JSONObject getRawPart(String id) throws Exception {
        TokenAndProject tap = dmeUtil.getToken();
        String url = DMEUtil.projectUrl + DMEUtil.apiExecute + "/XfPart01_20/get";
        JSONObject p = new JSONObject(); p.put("id", id);
        JSONObject pj = new JSONObject(); pj.put("params", p);
        String res = RequestUtil.requestsPost(url, pj.toString(), tap.getToken());
        JSONObject root = new JSONObject(res);
        JSONArray dataArr = root.optJSONArray("data");
        if (dataArr == null || dataArr.length() == 0) {
            throw new RuntimeException("DME查询失败，id=" + id + "，响应: " + root.toString());
        }
        return dataArr.getJSONObject(0);
    }

    /** 拍平枚举字段为字符串值 */
    private JSONObject flattenEnums(JSONObject obj) {
        String[] fields = {"status", "partType", "purchaseOrManufacture", "workingState"};
        for (String f : fields) {
            try {
                Object enumVal = obj.opt(f);
                if (enumVal instanceof JSONObject) {
                    obj.put(f, pickAlias(enumVal, ""));
                }
            } catch (org.json.JSONException ignored) {}
        }
        return obj;
    }

    /** 归一化工作状态：INWORK → CHECKED_OUT */
    private String normalizeWorkingState(JSONObject obj) {
        // 优先从 workingState alias 获取
        String ws = null;
        JSONObject wsObj = obj.optJSONObject("workingState");
        if (wsObj != null) {
            ws = wsObj.optString("alias", wsObj.optString("enName", null));
        }
        if (ws == null || ws.isEmpty()) {
            ws = obj.optString("workingState", null);
        }
        if (ws == null || ws.isEmpty()) return "CHECKED_IN";
        String u = ws.toUpperCase();
        if ("INWORK".equals(u)) return "CHECKED_OUT";
        if ("CHECKED_IN".equals(u)) return "CHECKED_IN";
        if ("CHECKED_OUT".equals(u)) return "CHECKED_OUT";
        return u;
    }

    /** JSONObject → Map<String, Object> */
    private Map<String, Object> jsonObjectToMap(JSONObject obj) {
        Map<String, Object> map = new HashMap<>();
        if (obj == null) return map;
        Iterator<String> keys = obj.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            Object val = obj.opt(key);
            if (val instanceof JSONObject) val = jsonObjectToMap((JSONObject) val);
            else if (val instanceof JSONArray) {
                JSONArray arr = (JSONArray) val;
                List<Object> list = new ArrayList<>();
                for (int i = 0; i < arr.length(); i++) {
                    Object item = arr.opt(i);
                    if (item instanceof JSONObject) item = jsonObjectToMap((JSONObject) item);
                    list.add(item);
                }
                val = list;
            }
            map.put(key, val);
        }
        return map;
    }

    /** 构建 DME 过滤条件 */
    private void addCond(JSONArray conditions, String name, String value, String operator) {
        try {
            JSONObject c = new JSONObject();
            c.put("conditionName", name);
            JSONArray v = new JSONArray(); v.put(value);
            c.put("conditionValues", v);
            c.put("ignoreStr", false);
            c.put("multi", false);
            c.put("operator", operator);
            conditions.put(c);
        } catch (org.json.JSONException e) {
            throw new RuntimeException(e);
        }
    }

    // ==================== 查询详情 ====================

    @Override
    public XfVersionPart selectXfPartById(String id) throws Exception {
        JSONObject raw = getRawPart(id);
        flattenEnums(raw);

        XfVersionPart part = new XfVersionPart();
        part.setId(raw.optString("id"));
        part.setPartName(raw.optString("partName"));
        part.setPartNameEn(raw.optString("partNameEn"));
        part.setPartType(pickAlias(raw.opt("partType"), "Ma"));
        part.setSpecificationsModel(raw.optString("specificationsModel"));
        part.setUnit(raw.optString("unit"));
        part.setStatus(pickAlias(raw.opt("status"), "Enable"));
        part.setPurchaseOrManufacture(pickAlias(raw.opt("purchaseOrManufacture"), "Manu"));
        part.setPartDeclaration(raw.optString("partDeclaration"));
        part.setDisplayVersion(raw.optString("iteration", raw.optString("version", null)));

        // 附件
        JSONArray extAttrs = raw.optJSONArray("extAttrs");
        if (extAttrs != null && extAttrs.length() > 0) {
            List<Map<String, Object>> list = new ArrayList<>();
            for (int i = 0; i < extAttrs.length(); i++) {
                JSONObject item = extAttrs.getJSONObject(i);
                list.add(jsonObjectToMap(item));
            }
            part.setExtAttrs(list);
            part.extractFileName();
        }

        // masterId
        JSONObject master = raw.optJSONObject("master");
        if (master != null) part.setMasterId(master.optString("id"));

        // 工作状态归一化
        part.setUiWorkingState(normalizeWorkingState(raw));

        part.setCreator(raw.optString("creator"));
        part.setModifier(raw.optString("modifier"));
        String ct = raw.optString("createTime", null);
        if (ct == null || ct.isEmpty()) ct = raw.optString("lastUpdateTime", null);
        if (ct != null && !ct.isEmpty()) {
            try { part.setCreateTime(DMEUtil.dmeDateToExamDate(ct)); } catch (Exception ignored) {}
        }
        return part;
    }

    // ==================== 列表查询 ====================

    @Override
    public TableDataInfo selectXfPartList(XfVersionPart xfPart, HttpServletRequest request) throws Exception {
        String pageSize = request.getParameter("pageSize");
        String pageNum = request.getParameter("pageNum");
        if (pageSize == null || pageSize.isEmpty()) pageSize = "10";
        if (pageNum == null || pageNum.isEmpty()) pageNum = "1";

        String url = DMEUtil.projectUrl + DMEUtil.apiExecute + "/XfPart01_20/find/" + pageSize + "/" + pageNum;

        JSONObject params = new JSONObject();
        params.put("characterSet", "UTF8");
        params.put("decrypt", false);
        params.put("isPresentAll", true);
        params.put("isNeedTotal", true);
        params.put("publicData", "INCLUDE_PUBLIC_DATA");
        params.put("sorts", new JSONArray());

        // 过滤条件
        JSONObject filter = new JSONObject();
        JSONArray conditions = new JSONArray();

        // 固定 latest=true
        addCond(conditions, "latest", "true", "=");

        if (xfPart.getPartName() != null && !xfPart.getPartName().isEmpty())
            addCond(conditions, "partName", xfPart.getPartName(), "like");
        if (xfPart.getPartType() != null && !xfPart.getPartType().isEmpty())
            addCond(conditions, "partType", xfPart.getPartType(), "=");
        if (xfPart.getStatus() != null && !xfPart.getStatus().isEmpty())
            addCond(conditions, "status", xfPart.getStatus(), "=");
        if (xfPart.getPurchaseOrManufacture() != null && !xfPart.getPurchaseOrManufacture().isEmpty())
            addCond(conditions, "purchaseOrManufacture", xfPart.getPurchaseOrManufacture(), "=");

        filter.put("conditions", conditions);
        filter.put("ignoreStr", false);
        filter.put("joiner", "and");
        filter.put("multi", false);
        params.put("filter", filter);

        JSONObject paramsJson = new JSONObject();
        paramsJson.put("params", params);

        TokenAndProject tap = dmeUtil.getToken();
        String res = RequestUtil.requestsPost(url, paramsJson.toString(), tap.getToken());
        JSONObject root = new JSONObject(res);
        if (!"SUCCESS".equalsIgnoreCase(root.optString("result", "")))
            throw new RuntimeException("Query failed: " + root.toString());

        JSONArray dataArr = root.optJSONArray("data");
        List<XfVersionPart> rows = new ArrayList<>();

        if (dataArr != null) {
            for (int i = 0; i < dataArr.length(); i++) {
                JSONObject item = dataArr.getJSONObject(i);
                flattenEnums(item);

                XfVersionPart row = new XfVersionPart();
                row.setId(item.optString("id"));
                row.setPartName(item.optString("partName"));
                row.setPartNameEn(item.optString("partNameEn"));
                row.setPartType(pickAlias(item.opt("partType"), ""));
                row.setSpecificationsModel(item.optString("specificationsModel"));
                row.setUnit(item.optString("unit"));
                row.setStatus(pickAlias(item.opt("status"), ""));
                row.setPurchaseOrManufacture(pickAlias(item.opt("purchaseOrManufacture"), ""));
                row.setPartDeclaration(item.optString("partDeclaration"));
                // 版本号：DME 返回 iteration 字段
                row.setDisplayVersion(item.optString("iteration", item.optString("version", null)));
                // 创建时间：DME 返回 UTC 字符串，转 Date
                String ct = item.optString("createTime", null);
                if (ct == null || ct.isEmpty()) ct = item.optString("lastUpdateTime", null);
                if (ct != null && !ct.isEmpty()) {
                    try { row.setCreateTime(DMEUtil.dmeDateToExamDate(ct)); } catch (Exception ignored) {}
                }

                // masterId
                JSONObject master = item.optJSONObject("master");
                if (master != null) row.setMasterId(master.optString("id"));

                // 工作状态归一化
                row.setUiWorkingState(normalizeWorkingState(item));

                // 附件信息
                JSONArray extAttrs = item.optJSONArray("extAttrs");
                if (extAttrs != null && extAttrs.length() > 0) {
                    List<Map<String, Object>> list = new ArrayList<>();
                    for (int j = 0; j < extAttrs.length(); j++) {
                        list.add(jsonObjectToMap(extAttrs.getJSONObject(j)));
                    }
                    row.setExtAttrs(list);
                    row.extractFileName();
                }

                rows.add(row);
            }
        }

        long total = rows.size();
        JSONObject pageInfo = root.optJSONObject("pageInfo");
        if (pageInfo != null && pageInfo.has("totalRows")) {
            long tr = pageInfo.getLong("totalRows");
            if (tr > 0) total = tr;
        }

        TableDataInfo tab = new TableDataInfo();
        tab.setCode(200);
        tab.setRows(rows);
        tab.setTotal(total);
        return tab;
    }

    // ==================== 新增（可选文件上传） ====================

    @Override
    public AjaxResult insertXfPart(XfVersionPart xfPart, MultipartFile file) throws Exception {
        xfPart.setCreateTime(DateUtils.getNowDate());
        TokenAndProject tap = dmeUtil.getToken();

        // 如果有文件，先上传到 iDME 获取 fileId
        String fileId = null;
        if (file != null && !file.isEmpty()) {
            fileId = uploadFileToIDME(file, tap.getToken());
        }

        String url = DMEUtil.projectUrl + DMEUtil.apiExecute + "/XfPart01_20/create";
        JSONObject params = new JSONObject();
        params.put("partName", xfPart.getPartName());
        params.put("partNameEn", xfPart.getPartNameEn() != null ? xfPart.getPartNameEn() : "");
        params.put("partType", xfPart.getPartType() != null ? xfPart.getPartType() : "Ma");
        params.put("specificationsModel", xfPart.getSpecificationsModel() != null ? xfPart.getSpecificationsModel() : "");
        params.put("unit", xfPart.getUnit() != null ? xfPart.getUnit() : "");
        params.put("status", xfPart.getStatus() != null ? xfPart.getStatus() : "Enable");
        params.put("purchaseOrManufacture", xfPart.getPurchaseOrManufacture() != null ? xfPart.getPurchaseOrManufacture() : "Manu");
        params.put("partDeclaration", xfPart.getPartDeclaration() != null ? xfPart.getPartDeclaration() : "");
        params.put("createTime", DMEUtil.dateToUTCString(xfPart.getCreateTime()));
        params.put("master", new JSONObject());
        params.put("branch", new JSONObject());

        // 附件扩展属性
        if (fileId != null) {
            JSONArray extAttrs = new JSONArray();
            JSONObject fileAttr = new JSONObject();
            fileAttr.put("name", "File_20");
            JSONObject fileValue = new JSONObject();
            fileValue.put("id", fileId);
            fileValue.put("fileName", file.getOriginalFilename());
            fileAttr.put("value", fileValue);
            extAttrs.put(fileAttr);
            params.put("extAttrs", extAttrs);
        }

        JSONObject pj = new JSONObject(); pj.put("params", params);
        String res = RequestUtil.requestsPost(url, pj.toString(), tap.getToken());
        JSONObject root = new JSONObject(res);
        if ("SUCCESS".equals(root.optString("result", ""))) {
            return AjaxResult.success();
        }
        return AjaxResult.error(root.optString("error_msg", res));
    }

    // ==================== 修改（附件三态） ====================

    @Override
    public AjaxResult updateXfPart(XfVersionPart xfPart, MultipartFile file) throws Exception {
        xfPart.setUpdateTime(DateUtils.getNowDate());
        TokenAndProject tap = dmeUtil.getToken();

        // 获取现有数据保留 master/branch 引用
        JSONObject existing = getRawPart(xfPart.getId());

        // 如有新文件，先上传
        String fileId = null;
        if (file != null && !file.isEmpty()) {
            fileId = uploadFileToIDME(file, tap.getToken());
        }

        String url = DMEUtil.projectUrl + DMEUtil.apiExecute + "/XfPart01_20/update";
        JSONObject params = new JSONObject();
        params.put("id", xfPart.getId());
        params.put("partName", xfPart.getPartName() != null ? xfPart.getPartName() : existing.optString("partName"));
        params.put("partNameEn", xfPart.getPartNameEn() != null ? xfPart.getPartNameEn() : existing.optString("partNameEn"));
        params.put("partType", xfPart.getPartType() != null ? xfPart.getPartType() : pickAlias(existing.opt("partType"), "Ma"));
        params.put("specificationsModel", xfPart.getSpecificationsModel() != null ? xfPart.getSpecificationsModel() : existing.optString("specificationsModel"));
        params.put("unit", xfPart.getUnit() != null ? xfPart.getUnit() : existing.optString("unit"));
        params.put("status", xfPart.getStatus() != null ? xfPart.getStatus() : pickAlias(existing.opt("status"), "Enable"));
        params.put("purchaseOrManufacture", xfPart.getPurchaseOrManufacture() != null ? xfPart.getPurchaseOrManufacture() : pickAlias(existing.opt("purchaseOrManufacture"), "Manu"));
        params.put("partDeclaration", xfPart.getPartDeclaration() != null ? xfPart.getPartDeclaration() : existing.optString("partDeclaration"));
        params.put("updateTime", DMEUtil.dateToUTCString(xfPart.getUpdateTime()));
        params.put("modifier", existing.optString("modifier", existing.optString("creator", "gzlg020")));

        // 保留 master/branch 引用
        params.put("master", existing.optJSONObject("master") != null ? existing.getJSONObject("master") : new JSONObject());
        params.put("branch", existing.optJSONObject("branch") != null ? existing.getJSONObject("branch") : new JSONObject());

        // 附件三态处理
        boolean wantClear = Boolean.TRUE.equals(xfPart.getClearFile());
        if (fileId != null) {
            // A. 替换：新文件
            JSONArray extAttrs = new JSONArray();
            JSONObject fa = new JSONObject();
            fa.put("name", "File_20");
            JSONObject fv = new JSONObject();
            fv.put("id", fileId);
            fv.put("fileName", file.getOriginalFilename());
            fa.put("value", fv);
            extAttrs.put(fa);
            params.put("extAttrs", extAttrs);
        } else if (wantClear) {
            // B. 删除：value 空数组
            JSONArray extAttrs = new JSONArray();
            JSONObject fa = new JSONObject();
            fa.put("name", "File_20");
            fa.put("value", new JSONObject());
            extAttrs.put(fa);
            params.put("extAttrs", extAttrs);
        }
        // C. 保持：不传 extAttrs

        JSONObject pj = new JSONObject(); pj.put("params", params);
        String res = RequestUtil.requestsPost(url, pj.toString(), tap.getToken());
        JSONObject root = new JSONObject(res);
        if ("SUCCESS".equals(root.optString("result", ""))) {
            return AjaxResult.success();
        }
        return AjaxResult.error(root.optString("error_msg", res));
    }

    // ==================== 检出 ====================

    @Override
    public AjaxResult checkOut(XfVersionPart xfVersionPart) throws Exception {
        TokenAndProject tap = dmeUtil.getToken();
        String url = DMEUtil.projectUrl + DMEUtil.apiExecute + "/XfPart01_20/checkout";
        JSONObject params = new JSONObject();
        params.put("masterId", xfVersionPart.getMasterId());
        params.put("workCopyType", xfVersionPart.getWorkCopyType() != null
                ? xfVersionPart.getWorkCopyType() : "BOTH");
        params.put("modifier", "gzlg020");
        JSONObject pj = new JSONObject(); pj.put("params", params);
        String res = RequestUtil.requestsPost(url, pj.toString(), tap.getToken());
        JSONObject root = new JSONObject(res);
        if ("SUCCESS".equalsIgnoreCase(root.optString("result", ""))) {
            return AjaxResult.success();
        }
        return AjaxResult.error(root.optString("error_msg", res));
    }

    // ==================== 检入 ====================

    @Override
    public AjaxResult checkIn(XfVersionPart xfVersionPart) throws Exception {
        TokenAndProject tap = dmeUtil.getToken();
        String url = DMEUtil.projectUrl + DMEUtil.apiExecute + "/XfPart01_20/checkin";
        JSONObject params = new JSONObject();
        params.put("masterId", xfVersionPart.getMasterId());
        params.put("modifier", "gzlg020");
        JSONObject pj = new JSONObject(); pj.put("params", params);
        String res = RequestUtil.requestsPost(url, pj.toString(), tap.getToken());
        JSONObject root = new JSONObject(res);
        if ("SUCCESS".equalsIgnoreCase(root.optString("result", ""))) {
            return AjaxResult.success();
        }
        return AjaxResult.error(root.optString("error_msg", res));
    }

    // ==================== 批量删除（按 masterId） ====================

    @Override
    public AjaxResult deleteXfPartByMasterIds(String[] masterIds) throws Exception {
        TokenAndProject tap = dmeUtil.getToken();
        String url = DMEUtil.projectUrl + DMEUtil.apiExecute + "/XfPart01_20/batchDelete";
        JSONObject params = new JSONObject();
        JSONArray idsArr = new JSONArray();
        for (String id : masterIds) idsArr.put(id);
        params.put("masterIds", idsArr);
        JSONObject pj = new JSONObject(); pj.put("params", params);
        String res = RequestUtil.requestsPost(url, pj.toString(), tap.getToken());
        JSONObject root = new JSONObject(res);
        if ("SUCCESS".equalsIgnoreCase(root.optString("result", ""))) {
            return AjaxResult.success();
        }
        return AjaxResult.error(root.optString("error_msg", res));
    }

    // ==================== 单个删除（按 masterId） ====================

    @Override
    public AjaxResult deleteXfPartByMasterId(String masterId) throws Exception {
        TokenAndProject tap = dmeUtil.getToken();
        String url = DMEUtil.projectUrl + DMEUtil.apiExecute + "/XfPart01_20/delete";
        JSONObject params = new JSONObject();
        params.put("masterId", masterId);
        JSONObject pj = new JSONObject(); pj.put("params", params);
        String res = RequestUtil.requestsPost(url, pj.toString(), tap.getToken());
        JSONObject root = new JSONObject(res);
        if ("SUCCESS".equalsIgnoreCase(root.optString("result", ""))) {
            return AjaxResult.success();
        }
        return AjaxResult.error(root.optString("error_msg", res));
    }

    // ==================== 文件上传到 iDME ====================

    private String uploadFileToIDME(MultipartFile file, String token) throws Exception {
        // 调用 iDME 文件上传接口
        String uploadUrl = DMEUtil.projectUrl.replace("/services", "") + "/api/v2/file/uploadFile";
        // 简化实现：构建 multipart 上传
        java.io.File tmp = java.io.File.createTempFile("xf_upload_", "_" + file.getOriginalFilename());
        try {
            file.transferTo(tmp);
            String boundary = "----XFUpload" + System.currentTimeMillis();
            StringBuilder body = new StringBuilder();
            body.append("--").append(boundary).append("\r\n");
            body.append("Content-Disposition: form-data; name=\"file\"; filename=\"")
                    .append(file.getOriginalFilename()).append("\"\r\n");
            body.append("Content-Type: application/octet-stream\r\n\r\n");

            java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
            bos.write(body.toString().getBytes("UTF-8"));
            java.io.FileInputStream fis = new java.io.FileInputStream(tmp);
            byte[] buf = new byte[8192]; int n;
            while ((n = fis.read(buf)) != -1) bos.write(buf, 0, n);
            fis.close();
            bos.write(("\r\n--" + boundary + "--\r\n").getBytes("UTF-8"));

            java.net.URL u = new java.net.URL(uploadUrl);
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) u.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            conn.setRequestProperty("X-Auth-Token", token);
            conn.getOutputStream().write(bos.toByteArray());
            conn.getOutputStream().flush();

            java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(conn.getInputStream(), "UTF-8"));
            StringBuilder resp = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) resp.append(line);
            reader.close();

            JSONObject uploadRes = new JSONObject(resp.toString());
            if ("SUCCESS".equalsIgnoreCase(uploadRes.optString("result", ""))) {
                JSONArray dataArr = uploadRes.optJSONArray("data");
                if (dataArr != null && dataArr.length() > 0) {
                    return dataArr.getJSONObject(0).optString("id", "");
                }
            }
            throw new RuntimeException("文件上传失败: " + resp);
        } finally {
            tmp.delete();
        }
    }
}
