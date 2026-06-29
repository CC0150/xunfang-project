package com.xunfang.manufacture.service;

import com.xunfang.common.core.web.domain.AjaxResult;
import com.xunfang.common.core.web.page.TableDataInfo;
import com.xunfang.manufacture.domain.XfPurchaseOrder;

import javax.servlet.http.HttpServletRequest;

/**
 * 采购订单Service接口
 *
 * @author xunfang
 * @date 2025-09-12
 */
public interface IXfPurchaseOrderService
{
    /**
     * 查询采购订单信息
     *
     * @param id 采购订单主键
     * @return 采购订单信息
     */
    XfPurchaseOrder selectXfPurchaseOrderById(String id) throws Exception;

    /**
     * 查询采购订单列表
     *
     * @param xfPurchaseOrder 采购订单信息
     * @param request HTTP请求
     * @return 分页数据
     */
    TableDataInfo selectXfPurchaseOrderList(XfPurchaseOrder xfPurchaseOrder, HttpServletRequest request) throws Exception;

    /**
     * 新增采购订单
     *
     * @param xfPurchaseOrder 采购订单信息
     * @return 结果
     */
    AjaxResult insertXfPurchaseOrder(XfPurchaseOrder xfPurchaseOrder) throws Exception;

    /**
     * 修改采购订单
     *
     * @param xfPurchaseOrder 采购订单信息
     * @return 结果
     */
    AjaxResult updateXfPurchaseOrder(XfPurchaseOrder xfPurchaseOrder) throws Exception;

    /**
     * 批量删除采购订单
     *
     * @param ids 需要删除的采购订单ID数组
     * @return 结果
     */
    AjaxResult deleteXfPurchaseOrderByIds(String[] ids) throws Exception;

    /**
     * 删除单个采购订单
     *
     * @param id 采购订单ID
     * @return 结果
     */
    AjaxResult deleteXfPurchaseOrderById(String id) throws Exception;
}
