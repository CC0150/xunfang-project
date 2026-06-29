package com.xunfang.manufacture.service;

import com.xunfang.common.core.web.domain.AjaxResult;
import com.xunfang.common.core.web.page.TableDataInfo;
import com.xunfang.manufacture.domain.XfPart;
import javax.servlet.http.HttpServletRequest;

public interface IXfPartService {
    XfPart selectXfPartById(String id) throws Exception;
    TableDataInfo selectXfPartList(XfPart xfPart, HttpServletRequest request) throws Exception;
    AjaxResult insertXfPart(XfPart xfPart) throws Exception;
    AjaxResult updateXfPart(XfPart xfPart) throws Exception;
    AjaxResult checkoutXfPart(String id) throws Exception;
    AjaxResult checkinXfPart(String id) throws Exception;
    AjaxResult deleteXfPartByIds(String[] ids) throws Exception;
    AjaxResult deleteXfPartById(String id) throws Exception;
}
