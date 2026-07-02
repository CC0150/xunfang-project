package com.xunfang.manufacture.controller;

import com.xunfang.common.core.web.controller.BaseController;
import com.xunfang.common.core.web.domain.AjaxResult;
import com.xunfang.common.core.web.page.TableDataInfo;
import com.xunfang.manufacture.domain.XfVersionPart;
import com.xunfang.manufacture.service.IXfPartService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/part")
public class XfPartController extends BaseController
{
    private static final Logger log = LoggerFactory.getLogger(XfPartController.class);

    @Resource
    private IXfPartService xfPartService;

    /** 查询详情 */
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id) {
        try {
            return success(xfPartService.selectXfPartById(id));
        } catch (Exception e) {
            log.error("查询Part详情失败, id={}", id, e);
            return AjaxResult.error("查询Part详情失败: " + e.getMessage());
        }
    }

    /** 分页列表 */
    @GetMapping("/list")
    public TableDataInfo list(XfVersionPart xfPart, HttpServletRequest request) {
        try {
            return xfPartService.selectXfPartList(xfPart, request);
        } catch (Exception e) {
            log.error("查询Part列表失败", e);
            TableDataInfo rsp = new TableDataInfo();
            rsp.setCode(500);
            rsp.setMsg("查询Part列表失败: " + e.getMessage());
            return rsp;
        }
    }

    /** 新增（multipart：data JSON + file 可选） */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AjaxResult add(@RequestPart("data") XfVersionPart xfPart,
                          @RequestPart(value = "file", required = false) MultipartFile file) {
        try {
            return xfPartService.insertXfPart(xfPart, file);
        } catch (Exception e) {
            log.error("新增Part失败", e);
            return AjaxResult.error("新增Part失败: " + e.getMessage());
        }
    }

    /** 修改（multipart：附件三态） */
    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AjaxResult edit(@RequestPart("data") XfVersionPart xfPart,
                           @RequestPart(value = "file", required = false) MultipartFile file) {
        try {
            return xfPartService.updateXfPart(xfPart, file);
        } catch (Exception e) {
            log.error("修改Part失败", e);
            return AjaxResult.error("修改Part失败: " + e.getMessage());
        }
    }

    /** 检出 */
    @PutMapping("/checkout")
    public AjaxResult checkout(@RequestBody XfVersionPart xfVersionPart) {
        try {
            return xfPartService.checkOut(xfVersionPart);
        } catch (Exception e) {
            log.error("检出Part失败", e);
            return AjaxResult.error("检出Part失败: " + e.getMessage());
        }
    }

    /** 检入 */
    @PutMapping("/checkin")
    public AjaxResult checkin(@RequestBody XfVersionPart xfVersionPart) {
        try {
            return xfPartService.checkIn(xfVersionPart);
        } catch (Exception e) {
            log.error("检入Part失败", e);
            return AjaxResult.error("检入Part失败: " + e.getMessage());
        }
    }

    /** 批量删除（ids 为逗号分隔的 masterId） */
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids) {
        try {
            return xfPartService.deleteXfPartByMasterIds(ids);
        } catch (Exception e) {
            log.error("批量删除Part失败", e);
            return AjaxResult.error("批量删除Part失败: " + e.getMessage());
        }
    }

    /** 单个删除（按 masterId） */
    @DeleteMapping("/delete/{id}")
    public AjaxResult removeOne(@PathVariable("id") String id) {
        try {
            return xfPartService.deleteXfPartByMasterId(id);
        } catch (Exception e) {
            log.error("删除Part失败, id={}", id, e);
            return AjaxResult.error("删除Part失败: " + e.getMessage());
        }
    }

    /** 文件下载代理 — 通过 IDME 流式下载 */
    @GetMapping("/file/download")
    public void fileDownload(
            @RequestParam("file_id") String fileId,
            @RequestParam(value = "model_name", defaultValue = "XfPart01_20") String modelName,
            @RequestParam(value = "instance_id", required = false) String instanceId,
            @RequestParam(value = "attribute_name", defaultValue = "File_20") String attributeName,
            @RequestParam(value = "filename", required = false) String filename,
            HttpServletResponse response) {
        try {
            xfPartService.downloadFile(fileId, modelName, instanceId, attributeName, filename, response);
        } catch (Exception e) {
            log.error("文件下载失败, fileId={}", fileId, e);
            try {
                response.setStatus(500);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"msg\":\"文件下载失败: " + e.getMessage() + "\"}");
            } catch (Exception ignored) {}
        }
    }
}
