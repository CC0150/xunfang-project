package com.xunfang.manufacture.controller;

import com.xunfang.common.core.web.controller.BaseController;
import com.xunfang.common.core.web.domain.AjaxResult;
import com.xunfang.common.core.web.page.TableDataInfo;
import com.xunfang.manufacture.domain.XfUnit;
import com.xunfang.manufacture.service.IXfPartUnitService;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/unit")
public class XfPartUnitController extends BaseController
{
    @Resource
    private IXfPartUnitService xfPartUnitService;

    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id) throws Exception {
        return success(xfPartUnitService.selectXfPartUnitById(id));
    }

    @GetMapping("/list")
    public TableDataInfo list(XfUnit xfUnit, HttpServletRequest request) throws Exception {
        return xfPartUnitService.selectXfPartUnitList(xfUnit, request);
    }

    @PostMapping
    public AjaxResult add(@RequestBody XfUnit xfUnit) throws Exception {
        return xfPartUnitService.insertXfPartUnit(xfUnit);
    }

    @PostMapping("/batch")
    public AjaxResult batchAdd(@RequestBody List<XfUnit> list) throws Exception {
        return xfPartUnitService.batchInsertXfPartUnit(list);
    }

    @PutMapping
    public AjaxResult edit(@RequestBody XfUnit xfUnit) throws Exception {
        return xfPartUnitService.updateXfPartUnit(xfUnit);
    }

    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids) throws Exception {
        return xfPartUnitService.deleteXfPartUnitByIds(ids);
    }

    @DeleteMapping("/delete/{id}")
    public AjaxResult removeOne(@PathVariable("id") String id) throws Exception {
        return xfPartUnitService.deleteXfPartUnitById(id);
    }
}
