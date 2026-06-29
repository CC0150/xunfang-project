package com.xunfang.manufacture.controller;

import com.xunfang.common.core.web.controller.BaseController;
import com.xunfang.common.core.web.domain.AjaxResult;
import com.xunfang.common.core.web.page.TableDataInfo;
import com.xunfang.manufacture.domain.XfVersionPart;
import com.xunfang.manufacture.service.IXfPartService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/part")
public class XfPartController extends BaseController
{
    @Resource
    private IXfPartService xfPartService;

    /** 查询详情 */
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id) throws Exception {
        return success(xfPartService.selectXfPartById(id));
    }

    /** 分页列表 */
    @GetMapping("/list")
    public TableDataInfo list(XfVersionPart xfPart, HttpServletRequest request) throws Exception {
        return xfPartService.selectXfPartList(xfPart, request);
    }

    /** 新增（multipart：data JSON + file 可选） */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AjaxResult add(@RequestPart("data") XfVersionPart xfPart,
                          @RequestPart(value = "file", required = false) MultipartFile file) throws Exception {
        return xfPartService.insertXfPart(xfPart, file);
    }

    /** 修改（multipart：附件三态） */
    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AjaxResult edit(@RequestPart("data") XfVersionPart xfPart,
                           @RequestPart(value = "file", required = false) MultipartFile file) throws Exception {
        return xfPartService.updateXfPart(xfPart, file);
    }

    /** 检出 */
    @PutMapping("/checkout")
    public AjaxResult checkout(@RequestBody XfVersionPart xfVersionPart) throws Exception {
        return xfPartService.checkOut(xfVersionPart);
    }

    /** 检入 */
    @PutMapping("/checkin")
    public AjaxResult checkin(@RequestBody XfVersionPart xfVersionPart) throws Exception {
        return xfPartService.checkIn(xfVersionPart);
    }

    /** 批量删除（ids 为逗号分隔的 masterId） */
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids) throws Exception {
        return xfPartService.deleteXfPartByMasterIds(ids);
    }

    /** 单个删除（按 masterId） */
    @DeleteMapping("/delete/{id}")
    public AjaxResult removeOne(@PathVariable("id") String id) throws Exception {
        return xfPartService.deleteXfPartByMasterId(id);
    }
}
