package com.xunfang.manufacture.service;

import com.xunfang.common.core.web.domain.AjaxResult;
import com.xunfang.common.core.web.page.TableDataInfo;
import com.xunfang.manufacture.domain.XfProductFamily;
import javax.servlet.http.HttpServletRequest;

public interface IXfProductFamilyService {
    XfProductFamily selectXfProductFamilyById(String id) throws Exception;
    TableDataInfo selectXfProductFamilyList(XfProductFamily family, HttpServletRequest request) throws Exception;
    AjaxResult insertXfProductFamily(XfProductFamily family) throws Exception;
    AjaxResult updateXfProductFamily(XfProductFamily family) throws Exception;
    AjaxResult deleteXfProductFamilyByIds(String[] ids) throws Exception;
}
