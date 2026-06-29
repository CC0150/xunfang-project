package com.xunfang.manufacture.controller;

import com.xunfang.common.core.web.controller.BaseController;
import com.xunfang.common.core.web.domain.AjaxResult;
import com.xunfang.system.tools.DMEUtil;
import com.xunfang.system.tools.RequestUtil;
import com.xunfang.system.tools.TokenAndProject;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 生命周期接口 — 前端新增/检出/检入/更新状态依赖
 */
@RestController
@RequestMapping("/lifecycle")
public class XfLifecycleController extends BaseController
{
    @Autowired
    private DMEUtil dmeUtil;

    /** 生命周期模板列表 */
    @GetMapping("/lifecycleTemplateList")
    public AjaxResult lifecycleTemplateList() throws Exception {
        TokenAndProject tap = dmeUtil.getToken();
        String url = DMEUtil.projectUrl + DMEUtil.apiExecute + "/LifecycleTemplate/find/100/1";
        JSONObject params = new JSONObject();
        params.put("characterSet", "UTF8"); params.put("decrypt", false);
        params.put("isNeedTotal", true); params.put("isPresentAll", true);
        params.put("publicData", "INCLUDE_PUBLIC_DATA");
        JSONObject filter = new JSONObject();
        JSONObject masterFilter = new JSONObject();
        masterFilter.put("conditionName", "master.businessCode");
        JSONArray v = new JSONArray(); v.put("ProductLifecycle");
        masterFilter.put("conditionValues", v);
        masterFilter.put("operator", "=");
        JSONArray conditions = new JSONArray(); conditions.put(masterFilter);
        filter.put("conditions", conditions);
        params.put("filter", filter);
        JSONObject pj = new JSONObject(); pj.put("params", params);
        String res = RequestUtil.requestsPost(url, pj.toString(), tap.getToken());
        return success(new JSONObject(res).optJSONArray("data"));
    }

    /** 业务操作列表 */
    @GetMapping("/lifeBusinessList")
    public AjaxResult lifeBusinessList(@RequestParam String templateId,
                                       @RequestParam(defaultValue = "create") String operation,
                                       @RequestParam(defaultValue = "") String stateId) throws Exception {
        TokenAndProject tap = dmeUtil.getToken();
        String url = DMEUtil.projectUrl + DMEUtil.apiExecute + "/LifecycleBusinessOperation/find/100/1";
        JSONObject params = new JSONObject();
        params.put("characterSet", "UTF8"); params.put("decrypt", false);
        params.put("isNeedTotal", true); params.put("isPresentAll", true);
        params.put("publicData", "INCLUDE_PUBLIC_DATA");
        JSONObject filter = new JSONObject();
        JSONArray conditions = new JSONArray();
        JSONObject tCond = new JSONObject(); tCond.put("conditionName", "master.id");
        JSONArray tv = new JSONArray(); tv.put(templateId);
        tCond.put("conditionValues", tv); tCond.put("operator", "=");
        conditions.put(tCond);
        filter.put("conditions", conditions);
        params.put("filter", filter);
        JSONObject pj = new JSONObject(); pj.put("params", params);
        String res = RequestUtil.requestsPost(url, pj.toString(), tap.getToken());
        return success(new JSONObject(res).optJSONArray("data"));
    }

    /** 目标状态列表 */
    @GetMapping("/lifeState")
    public AjaxResult lifeState(@RequestParam String templateId,
                                @RequestParam String businessOperationId,
                                @RequestParam(defaultValue = "") String stateId,
                                @RequestParam String operation) throws Exception {
        TokenAndProject tap = dmeUtil.getToken();
        String url = DMEUtil.projectUrl + DMEUtil.apiExecute + "/LifecycleState/find/100/1";
        JSONObject params = new JSONObject();
        params.put("characterSet", "UTF8"); params.put("decrypt", false);
        params.put("isNeedTotal", true); params.put("isPresentAll", true);
        params.put("publicData", "INCLUDE_PUBLIC_DATA");
        JSONObject filter = new JSONObject();
        JSONArray conditions = new JSONArray();
        JSONObject tCond = new JSONObject(); tCond.put("conditionName", "master.id");
        JSONArray tv = new JSONArray(); tv.put(templateId);
        tCond.put("conditionValues", tv); tCond.put("operator", "=");
        conditions.put(tCond);
        filter.put("conditions", conditions);
        params.put("filter", filter);
        JSONObject pj = new JSONObject(); pj.put("params", params);
        String res = RequestUtil.requestsPost(url, pj.toString(), tap.getToken());
        return success(new JSONObject(res).optJSONArray("data"));
    }
}
