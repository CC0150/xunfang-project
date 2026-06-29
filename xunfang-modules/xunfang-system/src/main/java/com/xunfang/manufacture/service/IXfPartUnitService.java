package com.xunfang.manufacture.service;

import com.xunfang.common.core.web.domain.AjaxResult;
import com.xunfang.common.core.web.page.TableDataInfo;
import com.xunfang.manufacture.domain.XfUnit;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 单位管理 — 服务层接口
 */
public interface IXfPartUnitService {
    /** 查询详情 */
    XfUnit selectXfPartUnitById(String id) throws Exception;

    /** 分页查询（unitName like） */
    TableDataInfo selectXfPartUnitList(XfUnit xfUnit, HttpServletRequest request) throws Exception;

    /** 批量新增 */
    AjaxResult batchInsertXfPartUnit(List<XfUnit> list) throws Exception;

    /** 修改 */
    AjaxResult updateXfPartUnit(XfUnit xfUnit) throws Exception;

    /** 批量删除 */
    AjaxResult deleteXfPartUnitByIds(String[] ids) throws Exception;
}
