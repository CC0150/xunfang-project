package com.xunfang.manufacture.service.impl;

import com.xunfang.common.core.utils.DateUtils;
import com.xunfang.common.core.web.domain.AjaxResult;
import com.xunfang.common.core.web.page.TableDataInfo;
import com.xunfang.manufacture.domain.XfPurchaseOrder;
import com.xunfang.manufacture.service.IXfPurchaseOrderService;
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
 * 采购订单Service业务层处理
 *
 * @author xunfang
 * @date 2025-09-12
 */
@Service
public class XfPurchaseOrderServiceImpl implements IXfPurchaseOrderService
{
    @Autowired
    private DMEUtil dmeUtil;

    /**
     * 查询采购订单信息
     *
     * @param id 采购订单主键
     * @return 采购订单信息
     */
    @Override
    public XfPurchaseOrder selectXfPurchaseOrderById(String id) throws Exception
    {
        TokenAndProject tap = dmeUtil.getToken();
        String url = DMEUtil.projectUrl + DMEUtil.apiExecute + "/XfPurchaseOrder_20/get";
        JSONObject paramsJson = new JSONObject();
        JSONObject params = new JSONObject();
        params.put("id", id);
        paramsJson.put("params", params);
        String res = RequestUtil.requestsPost(url, paramsJson.toString(), tap.getToken());
        JSONObject jsonObject = new JSONObject(res);
        JSONArray jsonArray = jsonObject.getJSONArray("data");
        return com.alibaba.fastjson2.JSON.parseObject(jsonArray.getJSONObject(0).toString(), XfPurchaseOrder.class, JSONReader.Feature.SupportSmartMatch);
    }

    /**
     * 查询采购订单列表
     *
     * @param xfPurchaseOrder 采购订单信息
     * @param request HTTP请求
     * @return 分页数据
     */
    @Override
    public TableDataInfo selectXfPurchaseOrderList(XfPurchaseOrder xfPurchaseOrder, HttpServletRequest request) throws Exception
    {
        String pageSize = request.getParameter("pageSize");
        String pageNum = request.getParameter("pageNum");
        if (pageSize == null || pageSize.isEmpty()) pageSize = "10";
        if (pageNum == null || pageNum.isEmpty()) pageNum = "1";

        String url = DMEUtil.projectUrl + DMEUtil.apiExecute + "/XfPurchaseOrder_20/find/" + pageSize + "/" + pageNum;

        // ---- 组装请求体 ----
        JSONObject paramsJson = new JSONObject();
        JSONObject params = new JSONObject();
        params.put("characterSet", "UTF8");
        params.put("decrypt", false);
        params.put("isPresentAll", false);
        params.put("isNeedTotal", true);
        params.put("publicData", "INCLUDE_PUBLIC_DATA");

        // 条件
        JSONObject filter = new JSONObject();
        JSONArray conditions = new JSONArray();

        // 采购订单号 like
        if (xfPurchaseOrder.getPurchaseOrderCode() != null && !xfPurchaseOrder.getPurchaseOrderCode().isEmpty())
        {
            JSONObject c = new JSONObject();
            c.put("conditionName", "purchaseOrderCode");
            JSONArray v = new JSONArray();
            v.put(xfPurchaseOrder.getPurchaseOrderCode());
            c.put("conditionValues", v);
            c.put("ignoreStr", false);
            c.put("multi", false);
            c.put("operator", "like");
            conditions.put(c);
        }

        // 供应商名称 like
        if (xfPurchaseOrder.getSupplierName() != null && !xfPurchaseOrder.getSupplierName().isEmpty())
        {
            JSONObject c = new JSONObject();
            c.put("conditionName", "supplierName");
            JSONArray v = new JSONArray();
            v.put(xfPurchaseOrder.getSupplierName());
            c.put("conditionValues", v);
            c.put("ignoreStr", false);
            c.put("multi", false);
            c.put("operator", "like");
            conditions.put(c);
        }

        // 供应商联系人 like
        if (xfPurchaseOrder.getSupplierLinkMan() != null && !xfPurchaseOrder.getSupplierLinkMan().isEmpty())
        {
            JSONObject c = new JSONObject();
            c.put("conditionName", "supplierLinkMan");
            JSONArray v = new JSONArray();
            v.put(xfPurchaseOrder.getSupplierLinkMan());
            c.put("conditionValues", v);
            c.put("ignoreStr", false);
            c.put("multi", false);
            c.put("operator", "like");
            conditions.put(c);
        }

        // 物料编号 like
        if (xfPurchaseOrder.getMaterialCode() != null && !xfPurchaseOrder.getMaterialCode().isEmpty())
        {
            JSONObject c = new JSONObject();
            c.put("conditionName", "materialCode");
            JSONArray v = new JSONArray();
            v.put(xfPurchaseOrder.getMaterialCode());
            c.put("conditionValues", v);
            c.put("ignoreStr", false);
            c.put("multi", false);
            c.put("operator", "like");
            conditions.put(c);
        }

        // 物料名称 like
        if (xfPurchaseOrder.getMaterialName() != null && !xfPurchaseOrder.getMaterialName().isEmpty())
        {
            JSONObject c = new JSONObject();
            c.put("conditionName", "materialName");
            JSONArray v = new JSONArray();
            v.put(xfPurchaseOrder.getMaterialName());
            c.put("conditionValues", v);
            c.put("ignoreStr", false);
            c.put("multi", false);
            c.put("operator", "like");
            conditions.put(c);
        }

        // 订单状态 =
        if (xfPurchaseOrder.getStatus() != null && !xfPurchaseOrder.getStatus().isEmpty())
        {
            JSONObject c = new JSONObject();
            c.put("conditionName", "status");
            JSONArray v = new JSONArray();
            v.put(xfPurchaseOrder.getStatus());
            c.put("conditionValues", v);
            c.put("ignoreStr", false);
            c.put("multi", false);
            c.put("operator", "=");
            conditions.put(c);
        }

        // 采购日期：>= 开始
        if (xfPurchaseOrder.getPurchaseDateStart() != null && !xfPurchaseOrder.getPurchaseDateStart().isEmpty())
        {
            JSONObject c = new JSONObject();
            c.put("conditionName", "purchaseDate");
            JSONArray v = new JSONArray();
            v.put(xfPurchaseOrder.getPurchaseDateStart());
            c.put("conditionValues", v);
            c.put("ignoreStr", false);
            c.put("multi", false);
            c.put("operator", ">=");
            conditions.put(c);
        }

        // 采购日期：<= 结束
        if (xfPurchaseOrder.getPurchaseDateEnd() != null && !xfPurchaseOrder.getPurchaseDateEnd().isEmpty())
        {
            JSONObject c = new JSONObject();
            c.put("conditionName", "purchaseDate");
            JSONArray v = new JSONArray();
            v.put(xfPurchaseOrder.getPurchaseDateEnd());
            c.put("conditionValues", v);
            c.put("ignoreStr", false);
            c.put("multi", false);
            c.put("operator", "<=");
            conditions.put(c);
        }

        filter.put("conditions", conditions);
        filter.put("ignoreStr", false);
        filter.put("joiner", "and");
        filter.put("multi", false);

        // 一定要把 filter 放进 params，否则条件不生效
        params.put("filter", filter);

        // 排序
        params.put("orderBy", "purchaseDate");
        params.put("sort", "DESC");

        paramsJson.put("params", params);

        // ---- HTTP 调用 + token ----
        TokenAndProject tap = dmeUtil.getToken();
        String token = tap.getToken();
        String res = RequestUtil.requestsPost(url, paramsJson.toString(), token);

        if (res == null || res.isEmpty())
        {
            throw new RuntimeException("iDME 返回为空");
        }

        // ---- 手动解析 JSON ----
        JSONObject root = new JSONObject(res);
        String result = root.getString("result");
        if (result != null && !"SUCCESS".equalsIgnoreCase(result))
        {
            Object errors = root.opt("errors");
            throw new RuntimeException("查询失败: " + (errors == null ? res : errors.toString()));
        }

        JSONArray dataArr = root.optJSONArray("data");
        List<XfPurchaseOrder> rows = Collections.emptyList();
        if (dataArr != null)
        {
            rows = com.alibaba.fastjson2.JSON.parseArray(dataArr.toString(), XfPurchaseOrder.class, JSONReader.Feature.SupportSmartMatch);
        }

        long total = (rows == null ? 0L : rows.size());
        JSONObject pageInfo = root.optJSONObject("pageInfo");
        if (pageInfo != null && pageInfo.has("totalRows"))
        {
            total = pageInfo.getLong("totalRows");
        }

        // ---- 组装分页返回 ----
        TableDataInfo tab = new TableDataInfo();
        tab.setCode(200);
        tab.setRows(rows);
        tab.setTotal(total);
        return tab;
    }

    /**
     * 新增采购订单
     *
     * @param xfPurchaseOrder 采购订单信息
     * @return 结果
     */
    @Override
    public AjaxResult insertXfPurchaseOrder(XfPurchaseOrder xfPurchaseOrder) throws Exception
    {
        xfPurchaseOrder.setCreateTime(DateUtils.getNowDate());
        TokenAndProject tap = dmeUtil.getToken();
        String url = DMEUtil.projectUrl + DMEUtil.apiExecute + "/XfPurchaseOrder_20/create";

        JSONObject paramsJson = new JSONObject();
        JSONObject params = new JSONObject();
        params.put("purchaseOrderCode", xfPurchaseOrder.getPurchaseOrderCode());
        params.put("purchaseDate", xfPurchaseOrder.getPurchaseDate() != null ?
                DMEUtil.dateToUTCString(xfPurchaseOrder.getPurchaseDate()) : null);
        params.put("supplierName", xfPurchaseOrder.getSupplierName());
        params.put("supplierLinkMan", xfPurchaseOrder.getSupplierLinkMan());
        params.put("materialCode", xfPurchaseOrder.getMaterialCode());
        params.put("materialName", xfPurchaseOrder.getMaterialName());
        params.put("specificationsModels", xfPurchaseOrder.getSpecificationsModels());
        params.put("purchaseQuantity", xfPurchaseOrder.getPurchaseQuantity());
        params.put("unit", xfPurchaseOrder.getUnit());
        params.put("unitPrice", xfPurchaseOrder.getUnitPrice());
        params.put("totalPrice", xfPurchaseOrder.getTotalPrice());
        params.put("status", xfPurchaseOrder.getStatus() != null ? xfPurchaseOrder.getStatus() : "1");
        params.put("remark", xfPurchaseOrder.getRemark());
        params.put("createTime", DMEUtil.dateToUTCString(xfPurchaseOrder.getCreateTime()));
        paramsJson.put("params", params);

        String res = RequestUtil.requestsPost(url, paramsJson.toString(), tap.getToken());
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
     * 修改采购订单
     *
     * @param xfPurchaseOrder 采购订单信息
     * @return 结果
     */
    @Override
    public AjaxResult updateXfPurchaseOrder(XfPurchaseOrder xfPurchaseOrder) throws Exception
    {
        TokenAndProject tap = dmeUtil.getToken();
        String url = DMEUtil.projectUrl + DMEUtil.apiExecute + "/XfPurchaseOrder_20/update";

        JSONObject paramsJson = new JSONObject();
        JSONObject params = new JSONObject();
        params.put("id", xfPurchaseOrder.getId());
        params.put("purchaseOrderCode", xfPurchaseOrder.getPurchaseOrderCode());
        params.put("purchaseDate", xfPurchaseOrder.getPurchaseDate() != null ?
                DMEUtil.dateToUTCString(xfPurchaseOrder.getPurchaseDate()) : null);
        params.put("supplierName", xfPurchaseOrder.getSupplierName());
        params.put("supplierLinkMan", xfPurchaseOrder.getSupplierLinkMan());
        params.put("materialCode", xfPurchaseOrder.getMaterialCode());
        params.put("materialName", xfPurchaseOrder.getMaterialName());
        params.put("specificationsModels", xfPurchaseOrder.getSpecificationsModels());
        params.put("purchaseQuantity", xfPurchaseOrder.getPurchaseQuantity());
        params.put("unit", xfPurchaseOrder.getUnit());
        params.put("unitPrice", xfPurchaseOrder.getUnitPrice());
        params.put("totalPrice", xfPurchaseOrder.getTotalPrice());
        params.put("status", xfPurchaseOrder.getStatus());
        params.put("remark", xfPurchaseOrder.getRemark());
        params.put("updateTime", xfPurchaseOrder.getUpdateTime() != null ?
                DMEUtil.dateToUTCString(xfPurchaseOrder.getUpdateTime()) : DMEUtil.dateToUTCString(new java.util.Date()));
        paramsJson.put("params", params);

        String res = RequestUtil.requestsPost(url, paramsJson.toString(), tap.getToken());
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
     * 批量删除采购订单
     *
     * @param ids 需要删除的采购订单ID数组
     * @return 结果
     */
    @Override
    public AjaxResult deleteXfPurchaseOrderByIds(String[] ids) throws Exception
    {
        TokenAndProject tap = dmeUtil.getToken();
        String url = DMEUtil.projectUrl + DMEUtil.apiExecute + "/XfPurchaseOrder_20/batchDelete";

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

        String res = RequestUtil.requestsPost(url, paramsJson.toString(), tap.getToken());
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
     * 删除单个采购订单
     *
     * @param id 采购订单ID
     * @return 结果
     */
    @Override
    public AjaxResult deleteXfPurchaseOrderById(String id) throws Exception
    {
        TokenAndProject tap = dmeUtil.getToken();
        String url = DMEUtil.projectUrl + DMEUtil.apiExecute + "/XfPurchaseOrder_20/delete";

        JSONObject paramsJson = new JSONObject();
        JSONObject params = new JSONObject();
        params.put("id", id);
        paramsJson.put("params", params);

        String res = RequestUtil.requestsPost(url, paramsJson.toString(), tap.getToken());
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
