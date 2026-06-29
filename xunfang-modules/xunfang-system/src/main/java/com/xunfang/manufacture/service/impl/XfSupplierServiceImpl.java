package com.xunfang.manufacture.service.impl;

import com.xunfang.common.core.utils.DateUtils;
import com.xunfang.common.core.web.domain.AjaxResult;
import com.xunfang.common.core.web.page.TableDataInfo;
import com.xunfang.manufacture.domain.XfSupplier;
import com.xunfang.manufacture.service.IXfSupplierService;
import com.xunfang.system.tools.DMEUtil;
import com.xunfang.system.tools.RequestUtil;
import com.xunfang.system.tools.TokenAndProject;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.fastjson2.JSONReader;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;

/**
 * 供应商Service业务层处理
 *
 * @author xunfang
 * @date 2025-09-12
 */
@Service
public class XfSupplierServiceImpl implements IXfSupplierService
{
    @Autowired
    private DMEUtil dmeUtil;

    /**
     * 查询供应商信息
     *
     * @param id 供应商信息主键
     * @return 供应商信息
     */
    @Override
    public XfSupplier selectXfSupplierById(String id) throws Exception
    {
        TokenAndProject tap = dmeUtil.getToken();
        String token = tap.getToken();
        String url = DMEUtil.projectUrl + DMEUtil.apiExecute + "/Xfsupplier_20/get";
        JSONObject paramsJson = new JSONObject();
        JSONObject params = new JSONObject();
        params.put("id", id);
        paramsJson.put("params", params);
        String res = RequestUtil.requestsPost(url, paramsJson.toString(), token);
        JSONObject jsonObject = new JSONObject(res);
        JSONArray jsonArray = jsonObject.getJSONArray("data");
        String str = jsonArray.toString();
        return com.alibaba.fastjson2.JSON.parseObject(jsonArray.getJSONObject(0).toString(), XfSupplier.class, JSONReader.Feature.SupportSmartMatch);
    }

    /**
     * 查询供应商列表
     *
     * @param xfSupplier 供应商信息
     * @param request HTTP请求
     * @return 分页数据
     */
    @Override
    public TableDataInfo selectXfSupplierList(XfSupplier xfSupplier, HttpServletRequest request) throws Exception
    {
        String pageSize = request.getParameter("pageSize");
        String pageNum = request.getParameter("pageNum");
        if (pageSize == null || pageSize.isEmpty()) pageSize = "10";
        if (pageNum == null || pageNum.isEmpty()) pageNum = "1";

        String url = DMEUtil.projectUrl + DMEUtil.apiExecute + "/Xfsupplier_20/find/" + pageSize + "/" + pageNum;

        JSONObject paramsJson = new JSONObject();
        JSONObject params = new JSONObject();
        params.put("characterSet", "UTF8");
        params.put("decrypt", false);

        JSONObject filter = new JSONObject();
        JSONArray conditions = new JSONArray();

        // 供应商编码 like
        if (xfSupplier.getSupplierCode() != null && !xfSupplier.getSupplierCode().isEmpty())
        {
            JSONObject c = new JSONObject();
            c.put("conditionName", "supplierCode");
            JSONArray v = new JSONArray();
            v.put(xfSupplier.getSupplierCode());
            c.put("conditionValues", v);
            c.put("ignoreStr", false);
            c.put("multi", false);
            c.put("operator", "like");
            conditions.put(c);
        }

        // 供应商名称 like
        if (xfSupplier.getSupplierName() != null && !xfSupplier.getSupplierName().isEmpty())
        {
            JSONObject c = new JSONObject();
            c.put("conditionName", "supplierName");
            JSONArray v = new JSONArray();
            v.put(xfSupplier.getSupplierName());
            c.put("conditionValues", v);
            c.put("ignoreStr", false);
            c.put("multi", false);
            c.put("operator", "like");
            conditions.put(c);
        }

        // 供应商类型 =
        if (xfSupplier.getSupplierType() != null && !xfSupplier.getSupplierType().isEmpty())
        {
            JSONObject c = new JSONObject();
            c.put("conditionName", "supplierType");
            JSONArray v = new JSONArray();
            v.put(xfSupplier.getSupplierType());
            c.put("conditionValues", v);
            c.put("ignoreStr", false);
            c.put("multi", false);
            c.put("operator", "=");
            conditions.put(c);
        }

        // 合作状态 =
        if (xfSupplier.getCooperativeStatus() != null && !xfSupplier.getCooperativeStatus().isEmpty())
        {
            JSONObject c = new JSONObject();
            c.put("conditionName", "cooperativeStatus");
            JSONArray v = new JSONArray();
            v.put(xfSupplier.getCooperativeStatus());
            c.put("conditionValues", v);
            c.put("ignoreStr", false);
            c.put("multi", false);
            c.put("operator", "=");
            conditions.put(c);
        }

        filter.put("conditions", conditions);
        filter.put("ignoreStr", false);
        filter.put("joiner", "and");
        filter.put("multi", false);

        params.put("filter", filter);
        params.put("isNeedTotal", true);
        params.put("isPresentAll", true);
        params.put("publicData", "INCLUDE_PUBLIC_DATA");
        params.put("sorts", new JSONArray());

        paramsJson.put("params", params);

        TokenAndProject tap = dmeUtil.getToken();
        String token = tap.getToken();
        String res = RequestUtil.requestsPost(url, paramsJson.toString(), token);

        if (res == null || res.isEmpty())
        {
            throw new RuntimeException("iDME返回为空");
        }

        JSONObject root = new JSONObject(res);
        String result = root.getString("result");
        if (!"SUCCESS".equalsIgnoreCase(result))
        {
            throw new RuntimeException("查询失败：" + root.toString());
        }

        JSONArray dataArr = root.optJSONArray("data");
        List<XfSupplier> rows = Collections.emptyList();
        if (dataArr != null)
        {
            rows = com.alibaba.fastjson2.JSON.parseArray(dataArr.toString(), XfSupplier.class, JSONReader.Feature.SupportSmartMatch);
        }

        long total = rows.size();
        JSONObject pageInfo = root.optJSONObject("pageInfo");
        if (pageInfo != null && pageInfo.has("totalRows"))
        {
            long tr = pageInfo.getLong("totalRows");
            if (tr > 0) total = tr;
        }

        TableDataInfo tab = new TableDataInfo();
        tab.setCode(200);
        tab.setRows(rows);
        tab.setTotal(total);
        return tab;
    }

    /**
     * 新增供应商
     *
     * @param xfSupplier 供应商信息
     * @return 结果
     */
    @Override
    public AjaxResult insertXfSupplier(XfSupplier xfSupplier) throws Exception
    {
        xfSupplier.setCreateTime(DateUtils.getNowDate());
        TokenAndProject tap = dmeUtil.getToken();
        String token = tap.getToken();
        String url = DMEUtil.projectUrl + DMEUtil.apiExecute + "/Xfsupplier_20/create";

        JSONObject paramsJson = new JSONObject();
        JSONObject params = new JSONObject();
        params.put("supplierCode", xfSupplier.getSupplierCode());
        params.put("supplierName", xfSupplier.getSupplierName());
        params.put("linkMan", xfSupplier.getLinkMan());
        params.put("linkPhone", xfSupplier.getLinkPhone());
        params.put("linkEmail", xfSupplier.getLinkEmail());
        params.put("supplierType", xfSupplier.getSupplierType());
        params.put("address", xfSupplier.getAddress());
        params.put("scopeOfSupply", xfSupplier.getScopeOfSupply());
        params.put("cooperativeStatus", xfSupplier.getCooperativeStatus());
        params.put("remark", xfSupplier.getRemark());
        params.put("createTime", DMEUtil.dateToUTCString(xfSupplier.getCreateTime()));
        paramsJson.put("params", params);

        String res = RequestUtil.requestsPost(url, paramsJson.toString(), token);
        System.out.println("res:" + res);
        JSONObject resJson = new JSONObject(res);
        Object result = resJson.get("result");
        if ("SUCCESS".equals(result.toString()))
        {
            return AjaxResult.success();
        }
        else
        {
            return AjaxResult.error();
        }
    }

    /**
     * 修改供应商
     *
     * @param xfSupplier 供应商信息
     * @return 结果
     */
    @Override
    public AjaxResult updateXfSupplier(XfSupplier xfSupplier) throws Exception
    {
        xfSupplier.setUpdateTime(DateUtils.getNowDate());
        TokenAndProject tap = dmeUtil.getToken();
        String token = tap.getToken();
        String url = DMEUtil.projectUrl + DMEUtil.apiExecute + "/Xfsupplier_20/update";

        JSONObject paramsJson = new JSONObject();
        JSONObject params = new JSONObject();
        params.put("id", xfSupplier.getId());
        params.put("supplierCode", xfSupplier.getSupplierCode());
        params.put("supplierName", xfSupplier.getSupplierName());
        params.put("linkMan", xfSupplier.getLinkMan());
        params.put("linkPhone", xfSupplier.getLinkPhone());
        params.put("linkEmail", xfSupplier.getLinkEmail());
        params.put("supplierType", xfSupplier.getSupplierType());
        params.put("address", xfSupplier.getAddress());
        params.put("scopeOfSupply", xfSupplier.getScopeOfSupply());
        params.put("cooperativeStatus", xfSupplier.getCooperativeStatus());
        params.put("remark", xfSupplier.getRemark());
        params.put("updateTime", DMEUtil.dateToUTCString(xfSupplier.getUpdateTime()));
        paramsJson.put("params", params);

        String res = RequestUtil.requestsPost(url, paramsJson.toString(), token);
        JSONObject resJson = new JSONObject(res);
        Object result = resJson.get("result");
        if ("SUCCESS".equals(result.toString()))
        {
            return AjaxResult.success();
        }
        else
        {
            return AjaxResult.error();
        }
    }

    /**
     * 批量删除供应商
     *
     * @param ids 需要删除的供应商ID数组
     * @return 结果
     */
    @Override
    public AjaxResult deleteXfSupplierByIds(String[] ids) throws Exception
    {
        TokenAndProject tap = dmeUtil.getToken();
        String token = tap.getToken();
        String url = DMEUtil.projectUrl + DMEUtil.apiExecute + "/Xfsupplier_20/batchDelete";

        JSONObject paramsJson = new JSONObject();
        JSONObject params = new JSONObject();
        // 显式构造扁平数组
        JSONArray idArr = new JSONArray();
        for (String id : ids)
        {
            idArr.put(id);
        }
        params.put("ids", idArr);
        paramsJson.put("params", params);

        String res = RequestUtil.requestsPost(url, paramsJson.toString(), token);
        JSONObject resJson = new JSONObject(res);
        Object result = resJson.get("result");
        JSONArray dataArr = resJson.optJSONArray("data");

        if ("SUCCESS".equalsIgnoreCase(String.valueOf(result)))
        {
            // data 有值，取第一个元素作为受影响条数
            int affected = (dataArr != null && dataArr.length() > 0) ? dataArr.getInt(0) : 0;
            if (affected >= 1)
            {
                return AjaxResult.success("成功删除 " + affected + " 条记录");
            }
            else
            {
                return AjaxResult.error("删除未命中任何记录");
            }
        }
        else
        {
            return AjaxResult.error("删除失败: " + res);
        }
    }

    /**
     * 删除单个供应商
     *
     * @param id 供应商ID
     * @return 结果
     */
    @Override
    public AjaxResult deleteXfSupplierById(String id) throws Exception
    {
        TokenAndProject tap = dmeUtil.getToken();
        String token = tap.getToken();
        String url = DMEUtil.projectUrl + DMEUtil.apiExecute + "/Xfsupplier_20/delete";

        JSONObject paramsJson = new JSONObject();
        JSONObject params = new JSONObject();
        params.put("id", id);
        paramsJson.put("params", params);

        String res = RequestUtil.requestsPost(url, paramsJson.toString(), token);
        JSONObject resJson = new JSONObject(res);
        Object result = resJson.get("result");
        if ("SUCCESS".equals(result.toString()))
        {
            return AjaxResult.success();
        }
        else
        {
            return AjaxResult.error();
        }
    }
}
