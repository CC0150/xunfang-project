package com.xunfang.manufacture.service;

import com.xunfang.common.core.web.domain.AjaxResult;
import com.xunfang.common.core.web.page.TableDataInfo;
import com.xunfang.manufacture.domain.XfVersionPart;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Part 管理 服务层接口
 */
public interface IXfPartService {
    /** 按版本 id 查询详情（含附件信息抽取） */
    XfVersionPart selectXfPartById(String id) throws Exception;

    /** 分页查询列表（固定 latest=true） */
    TableDataInfo selectXfPartList(XfVersionPart xfPart, HttpServletRequest request) throws Exception;

    /** 新增（可选文件上传） */
    AjaxResult insertXfPart(XfVersionPart xfPart, MultipartFile file) throws Exception;

    /** 修改（附件三态：替换/删除/保持） */
    AjaxResult updateXfPart(XfVersionPart xfPart, MultipartFile file) throws Exception;

    /** 检出 */
    AjaxResult checkOut(XfVersionPart xfVersionPart) throws Exception;

    /** 检入 */
    AjaxResult checkIn(XfVersionPart xfVersionPart) throws Exception;

    /** 批量删除（按 masterId） */
    AjaxResult deleteXfPartByMasterIds(String[] masterIds) throws Exception;

    /** 单个删除（按 masterId） */
    AjaxResult deleteXfPartByMasterId(String masterId) throws Exception;

    /** 文件下载（流式代理 IDME） */
    void downloadFile(String fileId, String modelName, String instanceId,
                      String attributeName, String filename,
                      HttpServletResponse response) throws Exception;
}
