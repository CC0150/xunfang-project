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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;

@Service
public class XfProductFamilyServiceImpl implements IXfProductFamilyService
{
    private static final Logger log = LoggerFactory.getLogger(XfProductFamilyServiceImpl.class);
    @Autowired
    private DMEUtil dmeUtil;

    private static final String ENTITY = "XfProductFamily_20";

    private JSONObject callDmeApi(String url, JSONObject body, String token) throws Exception {
        String res = RequestUtil.requestsPost(url, body.toString(), token);
        if (res == null || res.trim().isEmpty())
            throw new RuntimeException("DME接口响应为空，url=" + url);
        try { return new JSONObject(res); }
        catch (org.json.JSONException e) {
            log.error("DME响应JSON解析失败，url={}", url, e);
            throw new RuntimeException("DME接口返回数据格式异常", e);
        }
    }

    @Override
    public XfProductFamily selectXfProductFamilyById(String id) throws Exception {
        TokenAndProject tap = dmeUtil.getToken();
        String url = DMEUtil.projectUrl + DMEUtil.apiExecute + "/" + ENTITY + "/get";
        JSONObject p = new JSONObject(); p.put("id", id);
        JSONObject pj = new JSONObject(); pj.put("params", p);
        JSONObject root = callDmeApi(url, pj, tap.getToken());
        JSONArray arr = root.getJSONArray("data");
        return com.alibaba.fastjson2.JSON.parseObject(arr.getJSONObject(0).toString(), XfProductFamily.class, JSONReader.Feature.SupportSmartMatch);
    }

    @Override
    public TableDataInfo selectXfProductFamilyList(XfProductFamily family, HttpServletRequest request) throws Exception {
        String ps = request.getParameter("pageSize"); if (ps == null || ps.isEmpty()) ps = "10";
        String pn = request.getParameter("pageNum"); if (pn == null || pn.isEmpty()) pn = "1";
        String url = DMEUtil.projectUrl + DMEUtil.apiExecute + "/" + ENTITY + "/find/" + ps + "/" + pn;

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
        params.put("publicData", "INCLUDE_PUBLIC_DATA"); params.put("sorts", new JSONArray());
        JSONObject pj = new JSONObject(); pj.put("params", params);

        TokenAndProject tap = dmeUtil.getToken();
        JSONObject root = callDmeApi(url, pj, tap.getToken());
        if (!"SUCCESS".equalsIgnoreCase(root.optString("result"))) throw new RuntimeException("查询产品族失败");

        JSONArray dataArr = root.optJSONArray("data");
        List<XfProductFamily> rows = dataArr != null
                ? com.alibaba.fastjson2.JSON.parseArray(dataArr.toString(), XfProductFamily.class, JSONReader.Feature.SupportSmartMatch)
                : Collections.emptyList();
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
        String url = DMEUtil.projectUrl + DMEUtil.apiExecute + "/" + ENTITY + "/create";
        JSONObject params = new JSONObject();
        params.put("productFamilyNameCn", family.getProductFamilyNameCn());
        params.put("productFamilyNameEn", family.getProductFamilyNameEn());
        if (family.getDescription() != null) params.put("description", family.getDescription());
        JSONObject pj = new JSONObject(); pj.put("params", params);
        JSONObject root = callDmeApi(url, pj, tap.getToken());
        return "SUCCESS".equals(root.optString("result")) ? AjaxResult.success()
                : AjaxResult.error(root.optString("error_msg", "创建产品族失败"));
    }

    @Override
    public AjaxResult updateXfProductFamily(XfProductFamily family) throws Exception {
        TokenAndProject tap = dmeUtil.getToken();
        String url = DMEUtil.projectUrl + DMEUtil.apiExecute + "/" + ENTITY + "/update";
        JSONObject params = new JSONObject();
        params.put("id", family.getId());
        params.put("productFamilyNameCn", family.getProductFamilyNameCn());
        params.put("productFamilyNameEn", family.getProductFamilyNameEn());
        if (family.getDescription() != null) params.put("description", family.getDescription());
        JSONObject pj = new JSONObject(); pj.put("params", params);
        JSONObject root = callDmeApi(url, pj, tap.getToken());
        return "SUCCESS".equals(root.optString("result")) ? AjaxResult.success()
                : AjaxResult.error(root.optString("error_msg", "修改产品族失败"));
    }

    @Override
    public AjaxResult deleteXfProductFamilyById(String id) throws Exception {
        TokenAndProject tap = dmeUtil.getToken();
        String url = DMEUtil.projectUrl + DMEUtil.apiExecute + "/" + ENTITY + "/delete";
        JSONObject params = new JSONObject(); params.put("id", id);
        JSONObject pj = new JSONObject(); pj.put("params", params);
        JSONObject root = callDmeApi(url, pj, tap.getToken());
        return "SUCCESS".equalsIgnoreCase(root.optString("result")) ? AjaxResult.success()
                : AjaxResult.error(root.optString("error_msg", "删除产品族失败"));
    }

    @Override
    public AjaxResult deleteXfProductFamilyByIds(String[] ids) throws Exception {
        TokenAndProject tap = dmeUtil.getToken();
        String url = DMEUtil.projectUrl + DMEUtil.apiExecute + "/" + ENTITY + "/batchDelete";
        JSONArray idArr = new JSONArray(); for (String id : ids) idArr.put(id);
        JSONObject params = new JSONObject(); params.put("ids", idArr);
        JSONObject pj = new JSONObject(); pj.put("params", params);
        JSONObject root = callDmeApi(url, pj, tap.getToken());
        return "SUCCESS".equalsIgnoreCase(root.optString("result")) ? AjaxResult.success()
                : AjaxResult.error(root.optString("error_msg", "批量删除产品族失败"));
    }
}
