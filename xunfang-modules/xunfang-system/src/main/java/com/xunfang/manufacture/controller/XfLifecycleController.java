package com.xunfang.manufacture.controller;

import com.xunfang.common.core.web.controller.BaseController;
import com.xunfang.common.core.web.domain.AjaxResult;
import com.xunfang.manufacture.service.IXfLifecycleTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 生命周期接口 — 数据从产品 GET 的 lifecycleTemplate 提取
 */
@RestController
@RequestMapping("/lifecycle")
public class XfLifecycleController extends BaseController
{
    @Autowired
    private IXfLifecycleTemplateService lifecycleTemplateService;

    @GetMapping("/lifecycleTemplateList")
    public AjaxResult lifecycleTemplateList()
    {
        try
        {
            return lifecycleTemplateService.getLifecycleTemplateList();
        }
        catch (Exception e)
        {
            return error("获取生命周期模板失败: " + e.getMessage());
        }
    }

    @GetMapping("/lifeBusiness")
    public AjaxResult lifeBusiness(@RequestParam String templateId,
                                   @RequestParam(defaultValue = "create") String operation,
                                   @RequestParam(defaultValue = "") String stateId)
    {
        try
        {
            return lifecycleTemplateService.getLifeBusiness(templateId);
        }
        catch (Exception e)
        {
            return error("获取业务操作失败: " + e.getMessage());
        }
    }

    @GetMapping("/lifeBusinessList")
    public AjaxResult lifeBusinessList(@RequestParam String templateId,
                                       @RequestParam(defaultValue = "create") String operation,
                                       @RequestParam(defaultValue = "") String stateId)
    {
        try
        {
            return lifecycleTemplateService.getLifeBusiness(templateId);
        }
        catch (Exception e)
        {
            return error("获取业务操作列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/lifeState")
    public AjaxResult lifeState(@RequestParam String templateId,
                                @RequestParam String businessOperationId,
                                @RequestParam(defaultValue = "") String stateId,
                                @RequestParam String operation)
    {
        try
        {
            return lifecycleTemplateService.getLifeState(templateId, businessOperationId, operation);
        }
        catch (Exception e)
        {
            return error("获取生命周期状态失败: " + e.getMessage());
        }
    }
}
