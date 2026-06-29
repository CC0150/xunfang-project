package com.xunfang.manufacture.service;

import com.xunfang.common.core.web.domain.AjaxResult;
import com.xunfang.common.core.web.page.TableDataInfo;
import com.xunfang.manufacture.domain.XfUnit;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface IXfPartUnitService {
    XfUnit selectXfPartUnitById(String id) throws Exception;
    TableDataInfo selectXfPartUnitList(XfUnit xfUnit, HttpServletRequest request) throws Exception;
    AjaxResult insertXfPartUnit(XfUnit xfUnit) throws Exception;
    AjaxResult batchInsertXfPartUnit(List<XfUnit> list) throws Exception;
    AjaxResult updateXfPartUnit(XfUnit xfUnit) throws Exception;
    AjaxResult deleteXfPartUnitByIds(String[] ids) throws Exception;
    AjaxResult deleteXfPartUnitById(String id) throws Exception;
}
