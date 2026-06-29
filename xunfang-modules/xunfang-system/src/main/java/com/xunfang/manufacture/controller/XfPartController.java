package com.xunfang.manufacture.controller;

import com.xunfang.common.core.web.controller.BaseController;
import com.xunfang.common.core.web.domain.AjaxResult;
import com.xunfang.common.core.web.page.TableDataInfo;
import com.xunfang.manufacture.domain.XfPart;
import com.xunfang.manufacture.service.IXfPartService;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/part")
public class XfPartController extends BaseController
{
    @Resource
    private IXfPartService xfPartService;

    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id) throws Exception {
        return success(xfPartService.selectXfPartById(id));
    }

    @GetMapping("/list")
    public TableDataInfo list(XfPart xfPart, HttpServletRequest request) throws Exception {
        return xfPartService.selectXfPartList(xfPart, request);
    }

    @PostMapping
    public AjaxResult add(@RequestBody XfPart xfPart) throws Exception {
        return xfPartService.insertXfPart(xfPart);
    }

    @PutMapping
    public AjaxResult edit(@RequestBody XfPart xfPart) throws Exception {
        return xfPartService.updateXfPart(xfPart);
    }

    @PutMapping("/checkout")
    public AjaxResult checkout(@RequestBody Map<String, String> params) throws Exception {
        return xfPartService.checkoutXfPart(params.get("masterId"));
    }

    @PutMapping("/checkin")
    public AjaxResult checkin(@RequestBody Map<String, String> params) throws Exception {
        return xfPartService.checkinXfPart(params.get("masterId"));
    }

    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String ids) throws Exception {
        return xfPartService.deleteXfPartByIds(ids.split(","));
    }

    @DeleteMapping("/delete/{id}")
    public AjaxResult removeOne(@PathVariable("id") String id) throws Exception {
        return xfPartService.deleteXfPartById(id);
    }
}
