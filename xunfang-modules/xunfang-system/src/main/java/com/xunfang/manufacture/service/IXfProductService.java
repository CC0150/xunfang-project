package com.xunfang.manufacture.service;

import com.xunfang.common.core.web.domain.AjaxResult;
import com.xunfang.common.core.web.page.TableDataInfo;
import com.xunfang.manufacture.domain.XfVersionProduct;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletRequest;

public interface IXfProductService {
    XfVersionProduct selectXfProductById(String id) throws Exception;
    TableDataInfo selectXfProductList(XfVersionProduct product, HttpServletRequest request) throws Exception;
    AjaxResult insertXfProduct(XfVersionProduct product, MultipartFile file) throws Exception;
    AjaxResult updateXfProduct(XfVersionProduct product, MultipartFile file) throws Exception;
    AjaxResult checkOut(XfVersionProduct product) throws Exception;
    AjaxResult checkIn(XfVersionProduct product) throws Exception;
    AjaxResult updateLifecycleStatus(XfVersionProduct product) throws Exception;
    AjaxResult deleteXfProductByMasterIds(String[] masterIds) throws Exception;
    AjaxResult deleteXfProductByMasterId(String masterId) throws Exception;
}
