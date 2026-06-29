package com.xunfang.manufacture.controller;

import com.xunfang.common.core.web.controller.BaseController;
import com.xunfang.common.core.web.domain.AjaxResult;
import com.xunfang.common.core.web.page.TableDataInfo;
import com.xunfang.manufacture.domain.XfSupplier;
import com.xunfang.manufacture.service.IXfSupplierService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 供应商Controller
 *
 * @author xunfang
 * @date 2025-09-12
 */
@RestController
@RequestMapping("/supplier")
public class XfSupplierController extends BaseController
{
    @Resource
    private IXfSupplierService xfSupplierService;

    /**
     * 获取供应商信息详细信息
     */
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id) throws Exception
    {
        return success(xfSupplierService.selectXfSupplierById(id));
    }

    /**
     * 查询供应商信息列表
     */
    @GetMapping("/list")
    public TableDataInfo list(XfSupplier xfSupplier, HttpServletRequest request) throws Exception
    {
        return xfSupplierService.selectXfSupplierList(xfSupplier, request);
    }

    /**
     * 新增供应商信息
     */
    @PostMapping
    public AjaxResult add(@RequestBody XfSupplier xfSupplier) throws Exception
    {
        return xfSupplierService.insertXfSupplier(xfSupplier);
    }

    /**
     * 修改供应商信息
     */
    @PutMapping
    public AjaxResult edit(@RequestBody XfSupplier xfSupplier) throws Exception
    {
        return xfSupplierService.updateXfSupplier(xfSupplier);
    }

    /**
     * 批量删除供应商信息
     */
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids) throws Exception
    {
        return xfSupplierService.deleteXfSupplierByIds(ids);
    }

    /**
     * 删除供应商信息
     */
    @DeleteMapping("/delete/{id}")
    public AjaxResult removeOne(@PathVariable("id") String id) throws Exception
    {
        return xfSupplierService.deleteXfSupplierById(id);
    }
}
