package com.xunfang.manufacture.controller;

import com.xunfang.common.core.web.controller.BaseController;
import com.xunfang.common.core.web.domain.AjaxResult;
import com.xunfang.common.core.web.page.TableDataInfo;
import com.xunfang.manufacture.domain.XfProductFamily;
import com.xunfang.manufacture.service.IXfProductFamilyService;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/family")
public class XfProductFamilyController extends BaseController
{
    @Resource
    private IXfProductFamilyService xfProductFamilyService;

    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id) throws Exception {
        return success(xfProductFamilyService.selectXfProductFamilyById(id));
    }

    @GetMapping("/list")
    public TableDataInfo list(XfProductFamily family, HttpServletRequest request) throws Exception {
        return xfProductFamilyService.selectXfProductFamilyList(family, request);
    }

    @PostMapping
    public AjaxResult add(@RequestBody XfProductFamily family) throws Exception {
        return xfProductFamilyService.insertXfProductFamily(family);
    }

    @PutMapping
    public AjaxResult edit(@RequestBody XfProductFamily family) throws Exception {
        return xfProductFamilyService.updateXfProductFamily(family);
    }

    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids) throws Exception {
        return xfProductFamilyService.deleteXfProductFamilyByIds(ids);
    }
}
