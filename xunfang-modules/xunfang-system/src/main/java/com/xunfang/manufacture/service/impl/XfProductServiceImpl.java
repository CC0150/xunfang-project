package com.xunfang.manufacture.service.impl;

import com.xunfang.common.core.utils.DateUtils;
import com.xunfang.common.core.web.domain.AjaxResult;
import com.xunfang.common.core.web.page.TableDataInfo;
import com.xunfang.manufacture.domain.XfVersionProduct;
import com.xunfang.manufacture.service.IXfProductService;
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
public class XfProductServiceImpl implements IXfProductService
{
    @Autowired
    private DMEUtil dmeUtil;
    private static final String ENTITY = "XfProduct01";

    private String pickAlias(Object enumObj, String defaultVal) {
        if (enumObj instanceof JSONObject) {
            JSONObject eo = (JSONObject) enumObj;
            String v = eo.optString("alias", null);
            if (v == null || v.isEmpty()) v = eo.optString("enName", null);
            if (v != null && !v.isEmpty()) return v;
        }
        if (enumObj instanceof String) { String s = (String) enumObj; if (!s.isEmpty() && !"null".equalsIgnoreCase(s)) return s; }
        return defaultVal;
    }

    private JSONObject getRaw(String id) throws Exception {
        TokenAndProject tap = dmeUtil.getToken();
        String url = DMEUtil.projectUrl + DMEUtil.apiExecute + "/" + ENTITY + "/get";
        JSONObject p = new JSONObject(); p.put("id", id);
        JSONObject pj = new JSONObject(); pj.put("params", p);
        String res = RequestUtil.requestsPost(url, pj.toString(), tap.getToken());
        JSONArray arr = new JSONObject(res).optJSONArray("data");
        if (arr == null || arr.length() == 0) throw new RuntimeException("DME get failed for id=" + id);
        return arr.getJSONObject(0);
    }

    private String normalizeWorkingState(JSONObject obj) {
        JSONObject wsObj = obj.optJSONObject("workingState");
        String ws = null;
        if (wsObj != null) ws = wsObj.optString("alias", wsObj.optString("enName", null));
        if (ws == null || ws.isEmpty()) ws = obj.optString("workingState", null);
        if (ws == null || ws.isEmpty()) return "CHECKED_IN";
        String u = ws.toUpperCase();
        if ("INWORK".equals(u)) return "CHECKED_OUT";
        if ("CHECKED_IN".equals(u)) return "CHECKED_IN";
        if ("CHECKED_OUT".equals(u)) return "CHECKED_OUT";
        return u;
    }

    private void addCond(JSONArray conditions, String name, String value, String operator) {
        try {
            JSONObject c = new JSONObject(); c.put("conditionName", name);
            JSONArray v = new JSONArray(); v.put(value);
            c.put("conditionValues", v); c.put("ignoreStr", false); c.put("multi", false); c.put("operator", operator);
            conditions.put(c);
        } catch (org.json.JSONException e) { throw new RuntimeException(e); }
    }

    private String uploadFile(MultipartFile file, String token) throws Exception {
        String uploadUrl = DMEUtil.projectUrl.replace("/services", "") + "/api/v2/file/uploadFile";
        java.io.File tmp = java.io.File.createTempFile("xf_prod_", "_" + file.getOriginalFilename());
        try {
            file.transferTo(tmp);
            String boundary = "----XFP" + System.currentTimeMillis();
            java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
            bos.write(("--" + boundary + "\r\n").getBytes("UTF-8"));
            bos.write(("Content-Disposition: form-data; name=\"file\"; filename=\"" + file.getOriginalFilename() + "\"\r\n").getBytes("UTF-8"));
            bos.write("Content-Type: application/octet-stream\r\n\r\n".getBytes("UTF-8"));
            java.io.FileInputStream fis = new java.io.FileInputStream(tmp);
            byte[] buf = new byte[8192]; int n;
            while ((n = fis.read(buf)) != -1) bos.write(buf, 0, n);
            fis.close();
            bos.write(("\r\n--" + boundary + "--\r\n").getBytes("UTF-8"));
            java.net.URL u = new java.net.URL(uploadUrl);
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) u.openConnection();
            conn.setRequestMethod("POST"); conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            conn.setRequestProperty("X-Auth-Token", token);
            conn.getOutputStream().write(bos.toByteArray());
            java.io.BufferedReader rdr = new java.io.BufferedReader(new java.io.InputStreamReader(conn.getInputStream(), "UTF-8"));
            StringBuilder sb = new StringBuilder(); String line;
            while ((line = rdr.readLine()) != null) sb.append(line);
            rdr.close();
            JSONObject ur = new JSONObject(sb.toString());
            if ("SUCCESS".equalsIgnoreCase(ur.optString("result", ""))) {
                JSONArray da = ur.optJSONArray("data");
                if (da != null && da.length() > 0) return da.getJSONObject(0).optString("id", "");
            }
            throw new RuntimeException("Upload failed: " + sb);
        } finally { tmp.delete(); }
    }

    // ====== CRUD ======

    @Override
    public XfVersionProduct selectXfProductById(String id) throws Exception {
        JSONObject raw = getRaw(id);
        return mapToProduct(raw);
    }

    private XfVersionProduct mapToProduct(JSONObject raw) {
        XfVersionProduct p = new XfVersionProduct();
        p.setId(raw.optString("id"));
        p.setProductName(raw.optString("productName"));
        p.setProductFamily(raw.optString("productFamily"));
        p.setCategory(raw.optString("category"));
        p.setSpecificationModels(raw.optString("specificationModels"));
        p.setProductDescribe(raw.optString("productDescribe"));
        p.setDisplayVersion(raw.optString("iteration", raw.optString("version", null)));
        p.setUiWorkingState(normalizeWorkingState(raw));

        JSONObject master = raw.optJSONObject("master");
        if (master != null) p.setMasterId(master.optString("id"));

        // 生命周期状态名
        JSONObject lcState = raw.optJSONObject("lifecycleState");
        if (lcState != null) p.setLifecycleStateName(lcState.optString("alias", lcState.optString("enName", "")));

        // 附件
        JSONArray extArr = raw.optJSONArray("extAttrs");
        if (extArr != null && extArr.length() > 0) {
            List<Map<String, Object>> list = new ArrayList<>();
            for (int i = 0; i < extArr.length(); i++) {
                try { list.add(jsonObjToMap(extArr.getJSONObject(i))); } catch (org.json.JSONException e) {}
            }
            p.setExtAttrs(list);
            p.extractFileName();
        }
        String ct = raw.optString("createTime", raw.optString("lastUpdateTime", null));
        if (ct != null && !ct.isEmpty()) {
            try { p.setCreateTime(DMEUtil.dmeDateToExamDate(ct)); } catch (Exception ignored) {}
        }
        return p;
    }

    @Override
    public TableDataInfo selectXfProductList(XfVersionProduct product, HttpServletRequest request) throws Exception {
        String ps = request.getParameter("pageSize"); if (ps == null || ps.isEmpty()) ps = "10";
        String pn = request.getParameter("pageNum"); if (pn == null || pn.isEmpty()) pn = "1";
        String url = DMEUtil.projectUrl + DMEUtil.apiExecute + "/" + ENTITY + "/find/" + ps + "/" + pn;

        JSONObject params = new JSONObject();
        params.put("characterSet", "UTF8"); params.put("decrypt", false);
        params.put("isPresentAll", true); params.put("isNeedTotal", true);
        params.put("publicData", "INCLUDE_PUBLIC_DATA");
        params.put("sorts", new JSONArray());

        JSONObject filter = new JSONObject();
        JSONArray conditions = new JSONArray();
        addCond(conditions, "latest", "true", "=");
        if (product.getProductName() != null && !product.getProductName().isEmpty())
            addCond(conditions, "productName", product.getProductName(), "like");
        if (product.getProductFamily() != null && !product.getProductFamily().isEmpty())
            addCond(conditions, "productFamily", product.getProductFamily(), "like");
        if (product.getCategory() != null && !product.getCategory().isEmpty())
            addCond(conditions, "category", product.getCategory(), "=");
        filter.put("conditions", conditions); filter.put("ignoreStr", false); filter.put("joiner", "and"); filter.put("multi", false);
        params.put("filter", filter);

        JSONObject pj = new JSONObject(); pj.put("params", params);
        TokenAndProject tap = dmeUtil.getToken();
        String res = RequestUtil.requestsPost(url, pj.toString(), tap.getToken());
        JSONObject root = new JSONObject(res);
        if (!"SUCCESS".equalsIgnoreCase(root.optString("result", ""))) throw new RuntimeException("Query failed");

        JSONArray dataArr = root.optJSONArray("data");
        List<XfVersionProduct> rows = new ArrayList<>();
        if (dataArr != null) {
            for (int i = 0; i < dataArr.length(); i++) rows.add(mapToProduct(dataArr.getJSONObject(i)));
        }
        long total = rows.size();
        JSONObject pi = root.optJSONObject("pageInfo");
        if (pi != null && pi.has("totalRows")) { long tr = pi.getLong("totalRows"); if (tr > 0) total = tr; }
        TableDataInfo t = new TableDataInfo(); t.setCode(200); t.setRows(rows); t.setTotal(total);
        return t;
    }

    @Override
    public AjaxResult insertXfProduct(XfVersionProduct p, MultipartFile file) throws Exception {
        p.setCreateTime(DateUtils.getNowDate());
        TokenAndProject tap = dmeUtil.getToken();
        String fileId = null;
        if (file != null && !file.isEmpty()) fileId = uploadFile(file, tap.getToken());

        String url = DMEUtil.projectUrl + DMEUtil.apiExecute + "/" + ENTITY + "/create";
        JSONObject params = new JSONObject();
        params.put("productName", p.getProductName());
        params.put("productFamily", p.getProductFamily());
        params.put("category", p.getCategory());
        params.put("specificationModels", nvl(p.getSpecificationModels()));
        params.put("productDescribe", nvl(p.getProductDescribe()));
        params.put("createTime", DMEUtil.dateToUTCString(p.getCreateTime()));
        params.put("master", new JSONObject());
        params.put("branch", new JSONObject());
        if (p.getLifecycleTemplate() != null) params.put("lifecycleTemplate", new JSONObject(p.getLifecycleTemplate()));
        if (p.getLifecycleState() != null) params.put("lifecycleState", new JSONObject(p.getLifecycleState()));
        if (fileId != null) {
            JSONArray ext = new JSONArray();
            JSONObject fa = new JSONObject(); fa.put("name", "File");
            JSONObject fv = new JSONObject(); fv.put("id", fileId); fv.put("fileName", file.getOriginalFilename());
            fa.put("value", fv); ext.put(fa); params.put("extAttrs", ext);
        }
        JSONObject pj = new JSONObject(); pj.put("params", params);
        String r = RequestUtil.requestsPost(url, pj.toString(), tap.getToken());
        return "SUCCESS".equals(new JSONObject(r).optString("result", "")) ? AjaxResult.success() : AjaxResult.error();
    }

    @Override
    public AjaxResult updateXfProduct(XfVersionProduct p, MultipartFile file) throws Exception {
        p.setUpdateTime(DateUtils.getNowDate());
        TokenAndProject tap = dmeUtil.getToken();
        JSONObject existing = getRaw(p.getId());
        String fileId = null;
        if (file != null && !file.isEmpty()) fileId = uploadFile(file, tap.getToken());

        String url = DMEUtil.projectUrl + DMEUtil.apiExecute + "/" + ENTITY + "/update";
        JSONObject params = new JSONObject();
        params.put("id", p.getId());
        params.put("productName", nvlOr(p.getProductName(), existing.optString("productName")));
        params.put("productFamily", nvlOr(p.getProductFamily(), existing.optString("productFamily")));
        params.put("category", nvlOr(p.getCategory(), existing.optString("category")));
        params.put("specificationModels", nvlOr(p.getSpecificationModels(), existing.optString("specificationModels")));
        params.put("productDescribe", nvlOr(p.getProductDescribe(), existing.optString("productDescribe")));
        params.put("updateTime", DMEUtil.dateToUTCString(p.getUpdateTime()));
        params.put("master", existing.optJSONObject("master") != null ? existing.getJSONObject("master") : new JSONObject());
        params.put("branch", existing.optJSONObject("branch") != null ? existing.getJSONObject("branch") : new JSONObject());

        boolean wantClear = Boolean.TRUE.equals(p.getClearFile());
        if (fileId != null) {
            JSONArray ext = new JSONArray();
            JSONObject fa = new JSONObject(); fa.put("name", "File");
            JSONObject fv = new JSONObject(); fv.put("id", fileId); fv.put("fileName", file.getOriginalFilename());
            fa.put("value", fv); ext.put(fa); params.put("extAttrs", ext);
        } else if (wantClear) {
            JSONArray ext = new JSONArray();
            JSONObject fa = new JSONObject(); fa.put("name", "File");
            fa.put("value", new JSONObject()); ext.put(fa); params.put("extAttrs", ext);
        }
        JSONObject pj = new JSONObject(); pj.put("params", params);
        String r = RequestUtil.requestsPost(url, pj.toString(), tap.getToken());
        return "SUCCESS".equals(new JSONObject(r).optString("result", "")) ? AjaxResult.success() : AjaxResult.error();
    }

    // ====== 检出/检入/状态更新 ======

    @Override
    public AjaxResult checkOut(XfVersionProduct p) throws Exception {
        TokenAndProject tap = dmeUtil.getToken();
        String url = DMEUtil.projectUrl + DMEUtil.apiExecute + "/" + ENTITY + "/checkout";
        JSONObject params = new JSONObject();
        params.put("masterId", p.getMasterId());
        params.put("workCopyType", p.getWorkCopyType() != null ? p.getWorkCopyType() : "BOTH");
        if (p.getLifecycleTemplate() != null) params.put("lifecycleTemplate", new JSONObject(p.getLifecycleTemplate()));
        if (p.getLifecycleState() != null) params.put("lifecycleState", new JSONObject(p.getLifecycleState()));
        JSONObject pj = new JSONObject(); pj.put("params", params);
        String r = RequestUtil.requestsPost(url, pj.toString(), tap.getToken());
        return "SUCCESS".equalsIgnoreCase(new JSONObject(r).optString("result", "")) ? AjaxResult.success() : AjaxResult.error();
    }

    @Override
    public AjaxResult checkIn(XfVersionProduct p) throws Exception {
        TokenAndProject tap = dmeUtil.getToken();
        String url = DMEUtil.projectUrl + DMEUtil.apiExecute + "/" + ENTITY + "/checkin";
        JSONObject params = new JSONObject();
        params.put("masterId", p.getMasterId());
        params.put("viewNo", p.getViewNo() != null ? p.getViewNo() : "");
        if (p.getLifecycleTemplate() != null) params.put("lifecycleTemplate", new JSONObject(p.getLifecycleTemplate()));
        if (p.getLifecycleState() != null) params.put("lifecycleState", new JSONObject(p.getLifecycleState()));
        JSONObject pj = new JSONObject(); pj.put("params", params);
        String r = RequestUtil.requestsPost(url, pj.toString(), tap.getToken());
        return "SUCCESS".equalsIgnoreCase(new JSONObject(r).optString("result", "")) ? AjaxResult.success() : AjaxResult.error();
    }

    @Override
    public AjaxResult updateLifecycleStatus(XfVersionProduct p) throws Exception {
        TokenAndProject tap = dmeUtil.getToken();
        String url = DMEUtil.projectUrl + DMEUtil.apiExecute + "/" + ENTITY + "/update";
        JSONObject existing = getRaw(p.getId());
        JSONObject params = new JSONObject();
        params.put("id", p.getId());
        params.put("productName", existing.optString("productName"));
        params.put("productFamily", existing.optString("productFamily"));
        params.put("category", existing.optString("category"));
        params.put("specificationModels", existing.optString("specificationModels"));
        params.put("productDescribe", existing.optString("productDescribe"));
        params.put("updateTime", DMEUtil.dateToUTCString(DateUtils.getNowDate()));
        params.put("master", existing.optJSONObject("master") != null ? existing.getJSONObject("master") : new JSONObject());
        params.put("branch", existing.optJSONObject("branch") != null ? existing.getJSONObject("branch") : new JSONObject());
        if (p.getLifecycleTemplate() != null) params.put("lifecycleTemplate", new JSONObject(p.getLifecycleTemplate()));
        if (p.getLifecycleState() != null) params.put("lifecycleState", new JSONObject(p.getLifecycleState()));
        JSONObject pj = new JSONObject(); pj.put("params", params);
        String r = RequestUtil.requestsPost(url, pj.toString(), tap.getToken());
        return "SUCCESS".equalsIgnoreCase(new JSONObject(r).optString("result", "")) ? AjaxResult.success() : AjaxResult.error();
    }

    // ====== 删除 ======

    @Override
    public AjaxResult deleteXfProductByMasterIds(String[] masterIds) throws Exception {
        TokenAndProject tap = dmeUtil.getToken();
        String url = DMEUtil.projectUrl + DMEUtil.apiExecute + "/" + ENTITY + "/batchDelete";
        JSONArray arr = new JSONArray(); for (String id : masterIds) arr.put(id);
        JSONObject params = new JSONObject(); params.put("masterIds", arr);
        JSONObject pj = new JSONObject(); pj.put("params", params);
        String r = RequestUtil.requestsPost(url, pj.toString(), tap.getToken());
        return "SUCCESS".equalsIgnoreCase(new JSONObject(r).optString("result", "")) ? AjaxResult.success() : AjaxResult.error();
    }

    @Override
    public AjaxResult deleteXfProductByMasterId(String masterId) throws Exception {
        TokenAndProject tap = dmeUtil.getToken();
        String url = DMEUtil.projectUrl + DMEUtil.apiExecute + "/" + ENTITY + "/delete";
        JSONObject params = new JSONObject(); params.put("masterId", masterId);
        JSONObject pj = new JSONObject(); pj.put("params", params);
        String r = RequestUtil.requestsPost(url, pj.toString(), tap.getToken());
        return "SUCCESS".equalsIgnoreCase(new JSONObject(r).optString("result", "")) ? AjaxResult.success() : AjaxResult.error();
    }

    // helpers
    private String nvl(String s) { return s != null ? s : ""; }
    private String nvlOr(String s, String def) { return (s != null && !s.isEmpty()) ? s : def; }

    private Map<String, Object> jsonObjToMap(JSONObject obj) {
        Map<String, Object> m = new HashMap<>();
        if (obj == null) return m;
        Iterator<String> keys = obj.keys();
        while (keys.hasNext()) { String k = keys.next(); m.put(k, obj.opt(k)); }
        return m;
    }
}
