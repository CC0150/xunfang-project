package com.xunfang.manufacture.service.impl;

import com.alibaba.fastjson2.JSONReader;
import com.xunfang.common.core.web.domain.AjaxResult;
import com.xunfang.common.core.web.page.TableDataInfo;
import com.xunfang.manufacture.domain.XfProductFamily;
import com.xunfang.manufacture.service.IXfProductFamilyService;
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
public class XfProductFamilyServiceImpl implements IXfProductFamilyService
{
    @Autowired
    private DMEUtil dmeUtil;

    @Override
    public XfProductFamily selectXfProductFamilyById(String id) throws Exception {
        TokenAndProject tap = dmeUtil.getToken();
        String url = DMEUtil.projectUrl + DMEUtil.apiExecute + "/XfProductFamily/get";
        JSONObject p = new JSONObject(); p.put("id", id);
        JSONObject pj = new JSONObject(); pj.put("params", p);
        String res = RequestUtil.requestsPost(url, pj.toString(), tap.getToken());
        JSONArray arr = new JSONObject(res).getJSONArray("data");
        return com.alibaba.fastjson2.JSON.parseObject(arr.getJSONObject(0).toString(), XfProductFamily.class, JSONReader.Feature.SupportSmartMatch);
    }

    @Override
    public TableDataInfo selectXfProductFamilyList(XfProductFamily family, HttpServletRequest request) throws Exception {
        String ps = request.getParameter("pageSize"); if (ps == null || ps.isEmpty()) ps = "10";
        String pn = request.getParameter("pageNum"); if (pn == null || pn.isEmpty()) pn = "1";
        String url = DMEUtil.projectUrl + DMEUtil.apiExecute + "/XfProductFamily/find/" + ps + "/" + pn;

        JSONObject params = new JSONObject();
        params.put("characterSet", "UTF8"); params.put("decrypt", false);
        JSONObject filter = new JSONObject();
        JSONArray conditions = new JSONArray();

        if (family.getProductFamilyNameCn() != null && !family.getProductFamilyNameCn().isEmpty())
            addCond(conditions, "productFamilyNameCn", family.getProductFamilyNameCn(), "like");
        if (family.getProductFamilyNameEn() != null && !family.getProductFamilyNameEn().isEmpty())
            addCond(conditions, "productFamilyNameEn", family.getProductFamilyNameEn(), "like");

        filter.put("conditions", conditions); filter.put("ignoreStr", false); filter.put("joiner", "and"); filter.put("multi", false);
        params.put("filter", filter);
        params.put("isNeedTotal", true); params.put("isPresentAll", true);
        params.put("publicData", "INCLUDE_PUBLIC_DATA");
        params.put("sorts", new JSONArray());
        JSONObject pj = new JSONObject(); pj.put("params", params);

        TokenAndProject tap = dmeUtil.getToken();
        String res = RequestUtil.requestsPost(url, pj.toString(), tap.getToken());
        JSONObject root = new JSONObject(res);
        if (!"SUCCESS".equalsIgnoreCase(root.getString("result"))) throw new RuntimeException("Query failed");

        JSONArray dataArr = root.optJSONArray("data");
        List<XfProductFamily> rows = dataArr != null ? com.alibaba.fastjson2.JSON.parseArray(dataArr.toString(), XfProductFamily.class, JSONReader.Feature.SupportSmartMatch) : Collections.emptyList();
        long total = rows.size();
        JSONObject pi = root.optJSONObject("pageInfo");
        if (pi != null && pi.has("totalRows")) { long tr = pi.getLong("totalRows"); if (tr > 0) total = tr; }

        TableDataInfo tab = new TableDataInfo(); tab.setCode(200); tab.setRows(rows); tab.setTotal(total); return tab;
    }

    private void addCond(JSONArray conditions, String name, String value, String operator) {
        try {
            JSONObject c = new JSONObject(); c.put("conditionName", name);
            JSONArray v = new JSONArray(); v.put(value);
            c.put("conditionValues", v); c.put("ignoreStr", false); c.put("multi", false); c.put("operator", operator);
            conditions.put(c);
        } catch (org.json.JSONException e) { throw new RuntimeException(e); }
    }

    @Override
    public AjaxResult insertXfProductFamily(XfProductFamily family) throws Exception {
        TokenAndProject tap = dmeUtil.getToken();
        String url = DMEUtil.projectUrl + DMEUtil.apiExecute + "/XfProductFamily/create";
        JSONObject params = new JSONObject();
        params.put("productFamilyNameCn", family.getProductFamilyNameCn());
        params.put("productFamilyNameEn", family.getProductFamilyNameEn());
        if (family.getDescription() != null) params.put("description", family.getDescription());
        JSONObject pj = new JSONObject(); pj.put("params", params);
        String res = RequestUtil.requestsPost(url, pj.toString(), tap.getToken());
        return "SUCCESS".equals(new JSONObject(res).get("result").toString()) ? AjaxResult.success() : AjaxResult.error();
    }

    @Override
    public AjaxResult updateXfProductFamily(XfProductFamily family) throws Exception {
        TokenAndProject tap = dmeUtil.getToken();
        String url = DMEUtil.projectUrl + DMEUtil.apiExecute + "/XfProductFamily/update";
        JSONObject params = new JSONObject();
        params.put("id", family.getId());
        params.put("productFamilyNameCn", family.getProductFamilyNameCn());
        params.put("productFamilyNameEn", family.getProductFamilyNameEn());
        if (family.getDescription() != null) params.put("description", family.getDescription());
        JSONObject pj = new JSONObject(); pj.put("params", params);
        String res = RequestUtil.requestsPost(url, pj.toString(), tap.getToken());
        return "SUCCESS".equals(new JSONObject(res).get("result").toString()) ? AjaxResult.success() : AjaxResult.error();
    }

    @Override
    public AjaxResult deleteXfProductFamilyByIds(String[] ids) throws Exception {
        TokenAndProject tap = dmeUtil.getToken();
        String url = DMEUtil.projectUrl + DMEUtil.apiExecute + "/XfProductFamily/batchDelete";
        JSONArray idArr = new JSONArray(); for (String id : ids) idArr.put(id);
        JSONObject params = new JSONObject(); params.put("ids", idArr);
        JSONObject pj = new JSONObject(); pj.put("params", params);
        String res = RequestUtil.requestsPost(url, pj.toString(), tap.getToken());
        return "SUCCESS".equalsIgnoreCase(String.valueOf(new JSONObject(res).get("result"))) ? AjaxResult.success() : AjaxResult.error();
    }
}
