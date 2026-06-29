package com.xunfang.manufacture.controller;

import com.xunfang.common.core.web.controller.BaseController;
import com.xunfang.common.core.web.domain.AjaxResult;
import com.xunfang.common.core.web.page.TableDataInfo;
import com.xunfang.manufacture.domain.XfProductFamily;
import com.xunfang.manufacture.service.IXfProductFamilyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/family")
public class XfProductFamilyController extends BaseController
{
    private static final Logger log = LoggerFactory.getLogger(XfProductFamilyController.class);

    @Resource
    private IXfProductFamilyService xfProductFamilyService;

    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id) {
        try { return success(xfProductFamilyService.selectXfProductFamilyById(id)); }
        catch (Exception e) { log.error("查询产品族详情失败, id={}", id, e); return AjaxResult.error("查询产品族详情失败: " + e.getMessage()); }
    }

    @GetMapping("/list")
    public TableDataInfo list(XfProductFamily family, HttpServletRequest request) {
        try { return xfProductFamilyService.selectXfProductFamilyList(family, request); }
        catch (Exception e) { log.error("查询产品族列表失败", e); TableDataInfo rsp = new TableDataInfo(); rsp.setCode(500); rsp.setMsg("查询产品族列表失败: " + e.getMessage()); return rsp; }
    }

    @PostMapping
    public AjaxResult add(@RequestBody XfProductFamily family) {
        try { return xfProductFamilyService.insertXfProductFamily(family); }
        catch (Exception e) { log.error("新增产品族失败", e); return AjaxResult.error("新增产品族失败: " + e.getMessage()); }
    }

    @PutMapping
    public AjaxResult edit(@RequestBody XfProductFamily family) {
        try { return xfProductFamilyService.updateXfProductFamily(family); }
        catch (Exception e) { log.error("修改产品族失败", e); return AjaxResult.error("修改产品族失败: " + e.getMessage()); }
    }

    @DeleteMapping("/delete/{id}")
    public AjaxResult removeOne(@PathVariable("id") String id) {
        try { return xfProductFamilyService.deleteXfProductFamilyById(id); }
        catch (Exception e) { log.error("删除产品族失败, id={}", id, e); return AjaxResult.error("删除产品族失败: " + e.getMessage()); }
    }

    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids) {
        try { return xfProductFamilyService.deleteXfProductFamilyByIds(ids); }
        catch (Exception e) { log.error("批量删除产品族失败", e); return AjaxResult.error("批量删除产品族失败: " + e.getMessage()); }
    }
}
