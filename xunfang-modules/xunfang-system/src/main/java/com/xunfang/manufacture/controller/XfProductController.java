package com.xunfang.manufacture.controller;

import com.xunfang.common.core.web.controller.BaseController;
import com.xunfang.common.core.web.domain.AjaxResult;
import com.xunfang.common.core.web.page.TableDataInfo;
import com.xunfang.manufacture.domain.XfVersionProduct;
import com.xunfang.manufacture.service.IXfProductService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/product")
public class XfProductController extends BaseController
{
    @Resource
    private IXfProductService xfProductService;

    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id) throws Exception {
        return success(xfProductService.selectXfProductById(id));
    }

    @GetMapping("/list")
    public TableDataInfo list(XfVersionProduct product, HttpServletRequest request) throws Exception {
        return xfProductService.selectXfProductList(product, request);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AjaxResult add(@RequestPart("data") XfVersionProduct product,
                          @RequestPart(value = "file", required = false) MultipartFile file) throws Exception {
        return xfProductService.insertXfProduct(product, file);
    }

    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AjaxResult edit(@RequestPart("data") XfVersionProduct product,
                           @RequestPart(value = "file", required = false) MultipartFile file) throws Exception {
        return xfProductService.updateXfProduct(product, file);
    }

    @PutMapping("/checkout")
    public AjaxResult checkout(@RequestBody XfVersionProduct product) throws Exception {
        return xfProductService.checkOut(product);
    }

    @PutMapping("/checkin")
    public AjaxResult checkin(@RequestBody XfVersionProduct product) throws Exception {
        return xfProductService.checkIn(product);
    }

    @PutMapping("/updateStatus")
    public AjaxResult updateStatus(@RequestBody XfVersionProduct product) throws Exception {
        return xfProductService.updateLifecycleStatus(product);
    }

    @DeleteMapping("/deleteXfProductByProductId/{id}")
    public AjaxResult removeOne(@PathVariable("id") String id) throws Exception {
        return xfProductService.deleteXfProductByMasterId(id);
    }

    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids) throws Exception {
        return xfProductService.deleteXfProductByMasterIds(ids);
    }
}
