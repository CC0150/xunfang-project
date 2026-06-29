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

/**
 * 单位管理 — 控制器
 * 路径前缀：/unit（网关拼装为 /manufacture/unit）
 */
@RestController
@RequestMapping("/unit")
public class XfPartUnitController extends BaseController
{
    @Resource
    private IXfPartUnitService xfPartUnitService;

    /** 查询详情 */
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id) throws Exception {
        return success(xfPartUnitService.selectXfPartUnitById(id));
    }

    /** 分页查询（unitName like） */
    @GetMapping("/list")
    public TableDataInfo list(XfUnit xfUnit, HttpServletRequest request) throws Exception {
        return xfPartUnitService.selectXfPartUnitList(xfUnit, request);
    }

    /** 批量新增（接收 List） */
    @PostMapping
    public AjaxResult add(@RequestBody List<XfUnit> xfUnitList) throws Exception {
        return xfPartUnitService.batchInsertXfPartUnit(xfUnitList);
    }

    /** 修改 */
    @PutMapping
    public AjaxResult edit(@RequestBody XfUnit xfUnit) throws Exception {
        return xfPartUnitService.updateXfPartUnit(xfUnit);
    }

    /** 批量删除（ids 逗号分隔） */
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids) throws Exception {
        return xfPartUnitService.deleteXfPartUnitByIds(ids);
    }
}
