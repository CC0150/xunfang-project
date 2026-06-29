package com.xunfang.manufacture.controller;

import com.xunfang.common.core.web.controller.BaseController;
import com.xunfang.common.core.web.domain.AjaxResult;
import com.xunfang.common.core.web.page.TableDataInfo;
import com.xunfang.manufacture.domain.XfPurchaseOrder;
import com.xunfang.manufacture.service.IXfPurchaseOrderService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 采购订单Controller
 *
 * @author xunfang
 * @date 2025-09-12
 */
@RestController
@RequestMapping("/order")
public class XfPurchaseOrderController extends BaseController
{
    @Resource
    private IXfPurchaseOrderService xfPurchaseOrderService;

    /**
     * 获取采购订单详细信息
     */
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id) throws Exception
    {
        return success(xfPurchaseOrderService.selectXfPurchaseOrderById(id));
    }

    /**
     * 查询采购订单列表
     */
    @GetMapping("/list")
    public TableDataInfo list(XfPurchaseOrder xfPurchaseOrder, HttpServletRequest request) throws Exception
    {
        return xfPurchaseOrderService.selectXfPurchaseOrderList(xfPurchaseOrder, request);
    }

    /**
     * 新增采购订单
     */
    @PostMapping
    public AjaxResult add(@RequestBody XfPurchaseOrder xfPurchaseOrder) throws Exception
    {
        return xfPurchaseOrderService.insertXfPurchaseOrder(xfPurchaseOrder);
    }

    /**
     * 修改采购订单
     */
    @PutMapping
    public AjaxResult edit(@RequestBody XfPurchaseOrder xfPurchaseOrder) throws Exception
    {
        return xfPurchaseOrderService.updateXfPurchaseOrder(xfPurchaseOrder);
    }

    /**
     * 批量删除采购订单
     */
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids) throws Exception
    {
        return xfPurchaseOrderService.deleteXfPurchaseOrderByIds(ids);
    }

    /**
     * 删除单个采购订单
     */
    @DeleteMapping("/delete/{id}")
    public AjaxResult removeOne(@PathVariable("id") String id) throws Exception
    {
        return xfPurchaseOrderService.deleteXfPurchaseOrderById(id);
    }
}
