package com.xunfang.manufacture.service.impl;

import com.alibaba.fastjson2.JSONReader;
import com.xunfang.common.core.utils.DateUtils;
import com.xunfang.common.core.web.domain.AjaxResult;
import com.xunfang.common.core.web.page.TableDataInfo;
import com.xunfang.manufacture.domain.XfPart;
import com.xunfang.manufacture.service.IXfPartService;
import com.xunfang.system.tools.DMEUtil;
import com.xunfang.system.tools.RequestUtil;
import com.xunfang.system.tools.TokenAndProject;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;

@Service
public class XfPartServiceImpl implements IXfPartService
{
    @Autowired
    private DMEUtil dmeUtil;

    private String getEnumVal(JSONObject obj, String field, String defaultVal) {
        JSONObject enumObj = obj.optJSONObject(field);
        if (enumObj != null) {
            String v = enumObj.optString("alias", null);
            if (v == null || v.isEmpty()) v = enumObj.optString("enName", null);
            if (v == null || v.isEmpty()) v = defaultVal;
            return v;
        }
        String s = obj.optString(field, null);
        if (s == null || s.isEmpty() || "null".equalsIgnoreCase(s)) return defaultVal;
        return s;
    }

    private JSONObject getRawPart(String id) throws Exception {
        TokenAndProject tap = dmeUtil.getToken();
        String url = DMEUtil.projectUrl + DMEUtil.apiExecute + "/XfPart01_20/get";
        JSONObject p = new JSONObject(); p.put("id", id);
        JSONObject pj = new JSONObject(); pj.put("params", p);
        String res = RequestUtil.requestsPost(url, pj.toString(), tap.getToken());
        return new JSONObject(res).getJSONArray("data").getJSONObject(0);
    }

    private JSONObject refObj(String id, String className) throws Exception {
        JSONObject obj = new JSONObject();
        obj.put("id", id != null ? id : "");
        obj.put("className", className);
        return obj;
    }

    private JSONObject masterRef(String id) throws Exception {
        JSONObject obj = new JSONObject();
        obj.put("id", id != null ? id : "");
        obj.put("className", "XfPart01_20");
        return obj;
    }

    @Override
    public XfPart selectXfPartById(String id) throws Exception {
        TokenAndProject tap = dmeUtil.getToken();
        String url = DMEUtil.projectUrl + DMEUtil.apiExecute + "/XfPart01_20/get";
        JSONObject params = new JSONObject();
        params.put("id", id);
        JSONObject paramsJson = new JSONObject();
        paramsJson.put("params", params);
        String res = RequestUtil.requestsPost(url, paramsJson.toString(), tap.getToken());
        JSONObject jsonObject = new JSONObject(res);
        JSONArray jsonArray = jsonObject.getJSONArray("data");
        JSONObject item = flattenEnumFields(jsonArray.getJSONObject(0));
        return com.alibaba.fastjson2.JSON.parseObject(item.toString(), XfPart.class, JSONReader.Feature.SupportSmartMatch);
    }

    // 把DME枚举对象拍平成字符串值
    private JSONObject flattenEnumFields(JSONObject obj) throws Exception {
        String[] enumFields = {"status", "partType", "purchaseOrManufacture"};
        for (String f : enumFields) {
            JSONObject enumObj = obj.optJSONObject(f);
            if (enumObj != null) {
                String v = enumObj.optString("alias", null);
                if (v == null || v.isEmpty()) v = enumObj.optString("enName", null);
                if (v != null && !v.isEmpty()) obj.put(f, v);
            }
        }
        return obj;
    }

    @Override
    public TableDataInfo selectXfPartList(XfPart xfPart, HttpServletRequest request) throws Exception {
        String pageSize = request.getParameter("pageSize");
        String pageNum = request.getParameter("pageNum");
        if (pageSize == null || pageSize.isEmpty()) pageSize = "10";
        if (pageNum == null || pageNum.isEmpty()) pageNum = "1";

        String url = DMEUtil.projectUrl + DMEUtil.apiExecute + "/XfPart01_20/find/" + pageSize + "/" + pageNum;

        JSONObject paramsJson = new JSONObject();
        JSONObject params = new JSONObject();
        params.put("characterSet", "UTF8");
        params.put("decrypt", false);

        JSONObject filter = new JSONObject();
        JSONArray conditions = new JSONArray();

        if (xfPart.getPartName() != null && !xfPart.getPartName().isEmpty()) {
            addCondition(conditions, "partName", xfPart.getPartName(), "like");
        }
        if (xfPart.getPartType() != null && !xfPart.getPartType().isEmpty()) {
            addCondition(conditions, "partType", xfPart.getPartType(), "=");
        }
        if (xfPart.getStatus() != null && !xfPart.getStatus().isEmpty()) {
            addCondition(conditions, "status", xfPart.getStatus(), "=");
        }
        if (xfPart.getPurchaseOrManufacture() != null && !xfPart.getPurchaseOrManufacture().isEmpty()) {
            addCondition(conditions, "purchaseOrManufacture", xfPart.getPurchaseOrManufacture(), "=");
        }

        filter.put("conditions", conditions);
        filter.put("ignoreStr", false); filter.put("joiner", "and"); filter.put("multi", false);
        params.put("filter", filter);
        params.put("isNeedTotal", true);
        params.put("isPresentAll", true);
        params.put("publicData", "INCLUDE_PUBLIC_DATA");
        params.put("sorts", new JSONArray());
        paramsJson.put("params", params);

        TokenAndProject tap = dmeUtil.getToken();
        String res = RequestUtil.requestsPost(url, paramsJson.toString(), tap.getToken());
        if (res == null || res.isEmpty()) throw new RuntimeException("DME return empty");

        JSONObject root = new JSONObject(res);
        if (!"SUCCESS".equalsIgnoreCase(root.getString("result")))
            throw new RuntimeException("Query failed: " + root.toString());

        JSONArray dataArr = root.optJSONArray("data");
        List<XfPart> rows = Collections.emptyList();
        if (dataArr != null) {
            JSONArray flatArr = new JSONArray();
            for (int i = 0; i < dataArr.length(); i++) {
                flatArr.put(flattenEnumFields(dataArr.getJSONObject(i)));
            }
            rows = com.alibaba.fastjson2.JSON.parseArray(flatArr.toString(), XfPart.class, JSONReader.Feature.SupportSmartMatch);
        }

        long total = rows.size();
        JSONObject pageInfo = root.optJSONObject("pageInfo");
        if (pageInfo != null && pageInfo.has("totalRows")) {
            long tr = pageInfo.getLong("totalRows");
            if (tr > 0) total = tr;
        }

        TableDataInfo tab = new TableDataInfo();
        tab.setCode(200); tab.setRows(rows); tab.setTotal(total);
        return tab;
    }

    private void addCondition(JSONArray conditions, String name, String value, String operator) throws Exception {
        JSONObject c = new JSONObject();
        c.put("conditionName", name);
        JSONArray v = new JSONArray(); v.put(value);
        c.put("conditionValues", v);
        c.put("ignoreStr", false); c.put("multi", false); c.put("operator", operator);
        conditions.put(c);
    }

    @Override
    public AjaxResult insertXfPart(XfPart xfPart) throws Exception {
        xfPart.setCreateTime(DateUtils.getNowDate());
        TokenAndProject tap = dmeUtil.getToken();
        String url = DMEUtil.projectUrl + DMEUtil.apiExecute + "/XfPart01_20/create";

        JSONObject params = new JSONObject();
        params.put("partName", xfPart.getPartName());
        params.put("partNameEn", xfPart.getPartNameEn());
        params.put("partType", xfPart.getPartType());
        params.put("specificationsModel", xfPart.getSpecificationsModel());
        params.put("unit", xfPart.getUnit());
        params.put("status", xfPart.getStatus() != null ? xfPart.getStatus() : "Enable");
        params.put("purchaseOrManufacture", xfPart.getPurchaseOrManufacture());
        params.put("partDeclaration", xfPart.getPartDeclaration());
        // 附件扩展属性
        if (xfPart.getFileId() != null && !xfPart.getFileId().isEmpty()) {
            JSONArray extAttrs = new JSONArray();
            JSONObject fileAttr = new JSONObject();
            fileAttr.put("name", "File_20");
            JSONObject fileValue = new JSONObject();
            fileValue.put("id", xfPart.getFileId());
            fileValue.put("fileName", xfPart.getFileName() != null ? xfPart.getFileName() : "");
            fileAttr.put("value", fileValue);
            extAttrs.put(fileAttr);
            params.put("extAttrs", extAttrs);
        }
        params.put("createTime", DMEUtil.dateToUTCString(xfPart.getCreateTime()));
        params.put("master", masterRef(xfPart.getMasterId()));
        params.put("branch", masterRef(null));
        JSONObject paramsJson = new JSONObject();
        paramsJson.put("params", params);

        String res = RequestUtil.requestsPost(url, paramsJson.toString(), tap.getToken());
        JSONObject resJson = new JSONObject(res);
        if ("SUCCESS".equals(resJson.get("result").toString())) {
            return AjaxResult.success();
        } else {
            return AjaxResult.error(resJson.optString("error_msg", resJson.toString()));
        }
    }

    @Override
    public AjaxResult updateXfPart(XfPart xfPart) throws Exception {
        xfPart.setUpdateTime(DateUtils.getNowDate());
        TokenAndProject tap = dmeUtil.getToken();

        // 先从DME获取完整数据，保留master/branch引用
        String getUrl = DMEUtil.projectUrl + DMEUtil.apiExecute + "/XfPart01_20/get";
        JSONObject getParams = new JSONObject();
        getParams.put("id", xfPart.getId());
        JSONObject getParamsJson = new JSONObject();
        getParamsJson.put("params", getParams);
        String getRes = RequestUtil.requestsPost(getUrl, getParamsJson.toString(), tap.getToken());
        JSONObject existing = new JSONObject(getRes).getJSONArray("data").getJSONObject(0);

        String url = DMEUtil.projectUrl + DMEUtil.apiExecute + "/XfPart01_20/update";

        JSONObject params = new JSONObject();
        params.put("id", xfPart.getId());
        params.put("partName", xfPart.getPartName() != null ? xfPart.getPartName() : existing.optString("partName"));
        params.put("partNameEn", xfPart.getPartNameEn() != null ? xfPart.getPartNameEn() : existing.optString("partNameEn"));
        params.put("partType", xfPart.getPartType() != null ? xfPart.getPartType() : getEnumVal(existing, "partType", "Ma"));
        params.put("specificationsModel", xfPart.getSpecificationsModel() != null ? xfPart.getSpecificationsModel() : existing.optString("specificationsModel"));
        params.put("unit", xfPart.getUnit() != null ? xfPart.getUnit() : existing.optString("unit"));
        params.put("status", xfPart.getStatus() != null ? xfPart.getStatus() : getEnumVal(existing, "status", "Enable"));
        params.put("purchaseOrManufacture", xfPart.getPurchaseOrManufacture() != null ? xfPart.getPurchaseOrManufacture() : getEnumVal(existing, "purchaseOrManufacture", "Manu"));
        params.put("partDeclaration", xfPart.getPartDeclaration() != null ? xfPart.getPartDeclaration() : existing.optString("partDeclaration"));
        // 附件扩展属性
        if (xfPart.getFileId() != null && !xfPart.getFileId().isEmpty()) {
            JSONArray extAttrs = new JSONArray();
            JSONObject fileAttr = new JSONObject();
            fileAttr.put("name", "File_20");
            JSONObject fileValue = new JSONObject();
            fileValue.put("id", xfPart.getFileId());
            fileValue.put("fileName", xfPart.getFileName() != null ? xfPart.getFileName() : "");
            fileAttr.put("value", fileValue);
            extAttrs.put(fileAttr);
            params.put("extAttrs", extAttrs);
        }
        params.put("updateTime", DMEUtil.dateToUTCString(xfPart.getUpdateTime()));
        // 设置modifier为当前用户
        params.put("modifier", existing.optString("creator", "gzlg020"));
        // 保留原有的master和branch引用
        params.put("master", existing.optJSONObject("master") != null ? existing.getJSONObject("master") : masterRef(null));
        params.put("branch", existing.optJSONObject("branch") != null ? existing.getJSONObject("branch") : masterRef(null));
        JSONObject paramsJson = new JSONObject();
        paramsJson.put("params", params);

        String res = RequestUtil.requestsPost(url, paramsJson.toString(), tap.getToken());
        JSONObject resJson = new JSONObject(res);
        if ("SUCCESS".equals(resJson.get("result").toString())) {
            return AjaxResult.success();
        } else {
            return AjaxResult.error(resJson.optString("error_msg", resJson.toString()));
        }
    }

    @Override
    public AjaxResult checkoutXfPart(String versionId) throws Exception {
        // 先获取 Part 数据，从中提取 master 引用 ID
        JSONObject existing = getRawPart(versionId);
        JSONObject masterObj = existing.optJSONObject("master");
        String masterId = masterObj != null ? masterObj.optString("id", versionId) : versionId;

        // 调用 DME checkout API
        TokenAndProject tap = dmeUtil.getToken();
        String url = DMEUtil.projectUrl + DMEUtil.apiExecute + "/XfPart01_20/checkout";
        JSONObject params = new JSONObject();
        params.put("masterId", masterId);
        JSONObject pj = new JSONObject(); pj.put("params", params);
        String res = RequestUtil.requestsPost(url, pj.toString(), tap.getToken());
        JSONObject root = new JSONObject(res);
        return "SUCCESS".equalsIgnoreCase(root.optString("result", ""))
                ? AjaxResult.success(root.optString("data", "")) : AjaxResult.error(root.optString("error_msg", res));
    }

    @Override
    public AjaxResult checkinXfPart(String versionId) throws Exception {
        // 先获取工作副本数据，从中提取 master 引用
        JSONObject existing = getRawPart(versionId);
        JSONObject masterObj = existing.optJSONObject("master");
        String masterId = masterObj != null ? masterObj.optString("id", versionId) : versionId;

        // 调用 DME checkin API —— 需要 masterId + id(工作副本) + modifier
        TokenAndProject tap = dmeUtil.getToken();
        String url = DMEUtil.projectUrl + DMEUtil.apiExecute + "/XfPart01_20/checkin";
        JSONObject params = new JSONObject();
        params.put("masterId", masterId);
        params.put("id", versionId);
        params.put("modifier", existing.optString("modifier", existing.optString("creator", "gzlg020")));
        JSONObject pj = new JSONObject(); pj.put("params", params);
        String res = RequestUtil.requestsPost(url, pj.toString(), tap.getToken());
        JSONObject root = new JSONObject(res);
        return "SUCCESS".equalsIgnoreCase(root.optString("result", ""))
                ? AjaxResult.success() : AjaxResult.error(root.optString("error_msg", res));
    }

    @Override
    public AjaxResult deleteXfPartByIds(String[] ids) throws Exception {
        int ok = 0;
        for (String id : ids) {
            AjaxResult r = deleteXfPartById(id);
            if ((int)r.get("code") == 200) ok++;
        }
        return AjaxResult.success("成功删除 " + ok + "/" + ids.length + " 条");
    }

    @Override
    public AjaxResult deleteXfPartById(String id) throws Exception {
        TokenAndProject tap = dmeUtil.getToken();
        // 获取 Master ID
        JSONObject raw = getRawPart(id);
        JSONObject masterObj = raw.optJSONObject("master");
        String masterId = masterObj != null ? masterObj.optString("id", id) : id;
        // DME delete 需要 masterId + modifier
        String url = DMEUtil.projectUrl + DMEUtil.apiExecute + "/XfPart01_20/delete";
        JSONObject params = new JSONObject();
        params.put("masterId", masterId);
        params.put("modifier", raw.optString("modifier", raw.optString("creator", "gzlg020")));
        JSONObject paramsJson = new JSONObject();
        paramsJson.put("params", params);
        String res = RequestUtil.requestsPost(url, paramsJson.toString(), tap.getToken());
        JSONObject resJson = new JSONObject(res);
        if ("SUCCESS".equals(resJson.get("result").toString())) {
            return AjaxResult.success();
        } else {
            return AjaxResult.error(resJson.optString("error_msg", res));
        }
    }
}
