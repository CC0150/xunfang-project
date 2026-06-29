package com.xunfang.manufacture.service.impl;

import com.alibaba.fastjson2.JSONReader;
import com.xunfang.common.core.utils.DateUtils;
import com.xunfang.common.core.web.domain.AjaxResult;
import com.xunfang.common.core.web.page.TableDataInfo;
import com.xunfang.manufacture.domain.XfUnit;
import com.xunfang.manufacture.service.IXfPartUnitService;
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

/**
 * 单位管理 — 服务实现
 * DME 实体：XfPartUnit_20
 */
@Service
public class XfPartUnitServiceImpl implements IXfPartUnitService
{
    @Autowired
    private DMEUtil dmeUtil;

    @Override
    public XfUnit selectXfPartUnitById(String id) throws Exception {
        TokenAndProject tap = dmeUtil.getToken();
        String url = DMEUtil.projectUrl + DMEUtil.apiExecute + "/XfPartUnit_20/get";
        JSONObject p = new JSONObject(); p.put("id", id);
        JSONObject pj = new JSONObject(); pj.put("params", p);
        String res = RequestUtil.requestsPost(url, pj.toString(), tap.getToken());
        JSONArray arr = new JSONObject(res).getJSONArray("data");
        return com.alibaba.fastjson2.JSON.parseObject(arr.getJSONObject(0).toString(), XfUnit.class, JSONReader.Feature.SupportSmartMatch);
    }

    @Override
    public TableDataInfo selectXfPartUnitList(XfUnit xfUnit, HttpServletRequest request) throws Exception {
        String ps = request.getParameter("pageSize"); if (ps == null || ps.isEmpty()) ps = "10";
        String pn = request.getParameter("pageNum"); if (pn == null || pn.isEmpty()) pn = "1";
        String url = DMEUtil.projectUrl + DMEUtil.apiExecute + "/XfPartUnit_20/find/" + ps + "/" + pn;

        JSONObject params = new JSONObject();
        params.put("characterSet", "UTF8"); params.put("decrypt", false);
        JSONObject filter = new JSONObject();
        JSONArray conditions = new JSONArray();

        if (xfUnit.getUnitName() != null && !xfUnit.getUnitName().isEmpty())
            addCond(conditions, "unitName", xfUnit.getUnitName(), "like");

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
        List<XfUnit> rows = dataArr != null ? com.alibaba.fastjson2.JSON.parseArray(dataArr.toString(), XfUnit.class, JSONReader.Feature.SupportSmartMatch) : Collections.emptyList();
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
        } catch (org.json.JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public AjaxResult batchInsertXfPartUnit(List<XfUnit> list) throws Exception {
        int ok = 0;
        for (XfUnit u : list) {
            u.setCreateTime(DateUtils.getNowDate());
            TokenAndProject tap = dmeUtil.getToken();
            String url = DMEUtil.projectUrl + DMEUtil.apiExecute + "/XfPartUnit_20/create";
            JSONObject params = new JSONObject();
            params.put("unitCode", u.getUnitCode());
            params.put("unitName", u.getUnitName());
            params.put("createTime", DMEUtil.dateToUTCString(u.getCreateTime()));
            JSONObject pj = new JSONObject(); pj.put("params", params);
            String res = RequestUtil.requestsPost(url, pj.toString(), tap.getToken());
            if ("SUCCESS".equals(new JSONObject(res).get("result").toString())) ok++;
        }
        return AjaxResult.success("成功新增 " + ok + "/" + list.size() + " 条");
    }

    @Override
    public AjaxResult updateXfPartUnit(XfUnit xfUnit) throws Exception {
        xfUnit.setUpdateTime(DateUtils.getNowDate());
        TokenAndProject tap = dmeUtil.getToken();
        String url = DMEUtil.projectUrl + DMEUtil.apiExecute + "/XfPartUnit_20/update";
        JSONObject params = new JSONObject();
        params.put("id", xfUnit.getId());
        params.put("unitCode", xfUnit.getUnitCode());
        params.put("unitName", xfUnit.getUnitName());
        params.put("updateTime", DMEUtil.dateToUTCString(xfUnit.getUpdateTime()));
        JSONObject pj = new JSONObject(); pj.put("params", params);
        String res = RequestUtil.requestsPost(url, pj.toString(), tap.getToken());
        return "SUCCESS".equals(new JSONObject(res).get("result").toString()) ? AjaxResult.success() : AjaxResult.error();
    }

    @Override
    public AjaxResult deleteXfPartUnitByIds(String[] ids) throws Exception {
        TokenAndProject tap = dmeUtil.getToken();
        String url = DMEUtil.projectUrl + DMEUtil.apiExecute + "/XfPartUnit_20/batchDelete";
        JSONArray idArr = new JSONArray(); for (String id : ids) idArr.put(id);
        JSONObject params = new JSONObject(); params.put("ids", idArr);
        JSONObject pj = new JSONObject(); pj.put("params", params);
        String res = RequestUtil.requestsPost(url, pj.toString(), tap.getToken());
        return "SUCCESS".equalsIgnoreCase(String.valueOf(new JSONObject(res).get("result"))) ? AjaxResult.success() : AjaxResult.error();
    }
}
