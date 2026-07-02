package com.xunfang.manufacture.controller;

import com.xunfang.common.core.web.controller.BaseController;
import com.xunfang.common.core.web.domain.AjaxResult;
import com.xunfang.common.core.web.page.TableDataInfo;
import com.xunfang.manufacture.domain.XfVersionProduct;
import com.xunfang.manufacture.service.IXfProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/product")
public class XfProductController extends BaseController
{
    private static final Logger log = LoggerFactory.getLogger(XfProductController.class);

    @Resource
    private IXfProductService xfProductService;

    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id) {
        try { return success(xfProductService.selectXfProductById(id)); }
        catch (Exception e) { log.error("查询产品详情失败, id={}", id, e); return AjaxResult.error("查询产品详情失败: " + e.getMessage()); }
    }

    @GetMapping("/list")
    public TableDataInfo list(XfVersionProduct product, HttpServletRequest request) {
        try { return xfProductService.selectXfProductList(product, request); }
        catch (Exception e) { log.error("查询产品列表失败", e); TableDataInfo rsp = new TableDataInfo(); rsp.setCode(500); rsp.setMsg("查询产品列表失败: " + e.getMessage()); return rsp; }
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AjaxResult add(@RequestPart("data") XfVersionProduct product,
                          @RequestPart(value = "file", required = false) MultipartFile file) {
        try { return xfProductService.insertXfProduct(product, file); }
        catch (Exception e) { log.error("新增产品失败", e); return AjaxResult.error("新增产品失败: " + e.getMessage()); }
    }

    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AjaxResult edit(@RequestPart("data") XfVersionProduct product,
                           @RequestPart(value = "file", required = false) MultipartFile file) {
        try { return xfProductService.updateXfProduct(product, file); }
        catch (Exception e) { log.error("修改产品失败", e); return AjaxResult.error("修改产品失败: " + e.getMessage()); }
    }

    @PutMapping("/checkout")
    public AjaxResult checkout(@RequestBody XfVersionProduct product) {
        try { return xfProductService.checkOut(product); }
        catch (Exception e) { log.error("检出产品失败", e); return AjaxResult.error("检出产品失败: " + e.getMessage()); }
    }

    @PutMapping("/checkin")
    public AjaxResult checkin(@RequestBody XfVersionProduct product) {
        try { return xfProductService.checkIn(product); }
        catch (Exception e) { log.error("检入产品失败", e); return AjaxResult.error("检入产品失败: " + e.getMessage()); }
    }

    @PutMapping("/updateStatus")
    public AjaxResult updateStatus(@RequestBody XfVersionProduct product) {
        try { return xfProductService.updateByAdmin(product); }
        catch (Exception e) { log.error("更新生命周期状态失败", e); return AjaxResult.error("更新生命周期状态失败: " + e.getMessage()); }
    }

    @DeleteMapping("/delete/{masterId}")
    public AjaxResult delOne(@PathVariable("masterId") String masterId) {
        try { return xfProductService.deleteXfProductByMasterId(masterId); }
        catch (Exception e) { log.error("删除产品失败, masterId={}", masterId, e); return AjaxResult.error("删除产品失败: " + e.getMessage()); }
    }

    @DeleteMapping("/batch/{masterIds}")
    public AjaxResult batchDel(@PathVariable("masterIds") String[] masterIds) {
        try { return xfProductService.deleteXfProductByMasterIds(masterIds); }
        catch (Exception e) { log.error("批量删除产品失败", e); return AjaxResult.error("批量删除产品失败: " + e.getMessage()); }
    }

    // 兼容旧前端
    @DeleteMapping("/deleteXfProductByProductId/{id}")
    public AjaxResult removeOne(@PathVariable("id") String id) {
        try { return xfProductService.deleteXfProductByMasterId(id); }
        catch (Exception e) { log.error("删除产品失败, id={}", id, e); return AjaxResult.error("删除产品失败: " + e.getMessage()); }
    }

    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids) {
        try { return xfProductService.deleteXfProductByMasterIds(ids); }
        catch (Exception e) { log.error("批量删除产品失败", e); return AjaxResult.error("批量删除产品失败: " + e.getMessage()); }
    }

    /** 文件下载 */
    @GetMapping("/file/download")
    public void fileDownload(
            @RequestParam("file_id") String fileId,
            @RequestParam(value = "model_name", defaultValue = "XfProduct_20") String modelName,
            @RequestParam(value = "instance_id", required = false) String instanceId,
            @RequestParam(value = "attribute_name", defaultValue = "File_20") String attributeName,
            @RequestParam(value = "filename", required = false) String filename,
            HttpServletResponse response) {
        try {
            xfProductService.downloadFile(fileId, modelName, instanceId, attributeName, filename, response);
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
