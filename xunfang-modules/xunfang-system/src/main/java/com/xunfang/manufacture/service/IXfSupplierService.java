package com.xunfang.manufacture.service;

import com.xunfang.common.core.web.domain.AjaxResult;
import com.xunfang.common.core.web.page.TableDataInfo;
import com.xunfang.manufacture.domain.XfSupplier;

import javax.servlet.http.HttpServletRequest;

/**
 * 供应商Service接口
 *
 * @author xunfang
 * @date 2025-09-12
 */
public interface IXfSupplierService
{
    /**
     * 查询供应商信息
     *
     * @param id 供应商信息主键
     * @return 供应商信息
     */
    XfSupplier selectXfSupplierById(String id) throws Exception;

    /**
     * 查询供应商列表
     *
     * @param xfSupplier 供应商信息
     * @param request HTTP请求
     * @return 分页数据
     */
    TableDataInfo selectXfSupplierList(XfSupplier xfSupplier, HttpServletRequest request) throws Exception;

    /**
     * 新增供应商
     *
     * @param xfSupplier 供应商信息
     * @return 结果
     */
    AjaxResult insertXfSupplier(XfSupplier xfSupplier) throws Exception;

    /**
     * 修改供应商
     *
     * @param xfSupplier 供应商信息
     * @return 结果
     */
    AjaxResult updateXfSupplier(XfSupplier xfSupplier) throws Exception;

    /**
     * 批量删除供应商
     *
     * @param ids 需要删除的供应商ID数组
     * @return 结果
     */
    AjaxResult deleteXfSupplierByIds(String[] ids) throws Exception;

    /**
     * 删除单个供应商
     *
     * @param id 供应商ID
     * @return 结果
     */
    AjaxResult deleteXfSupplierById(String id) throws Exception;
}
