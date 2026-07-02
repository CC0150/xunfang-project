package com.xunfang.manufacture.service.impl;

import com.xunfang.common.core.web.domain.AjaxResult;
import com.xunfang.manufacture.service.IXfLifecycleTemplateService;
import com.xunfang.system.tools.DMEUtil;
import com.xunfang.system.tools.RequestUtil;
import com.xunfang.system.tools.TokenAndProject;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 生命周期模板Service实现 — 从产品 GET 的 lifecycleTemplate 提取数据
 *
 * @author xunfang
 */
@Service
public class XfLifecycleTemplateServiceImpl implements IXfLifecycleTemplateService
{
    @Autowired
    private DMEUtil dmeUtil;

    @Override
    public AjaxResult getLifecycleTemplateList() throws Exception
    {
        JSONObject template = getTemplate();
        if (template == null) return AjaxResult.success(Collections.emptyList());
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", template.optString("id"));
        m.put("name", template.optString("name", ""));
        m.put("nameEn", template.optString("nameEn", ""));
        list.add(m);
        return AjaxResult.success(list);
    }

    @Override
    public AjaxResult getLifeBusiness(String templateId) throws Exception
    {
        return extractBizOps(templateId);
    }

    @Override
    public AjaxResult getLifeState(String templateId, String businessOperationId, String operation) throws Exception
    {
        JSONObject template = getTemplate();
        if (template == null || !templateId.equals(template.optString("id")))
            return AjaxResult.success(Collections.emptyList());

        List<Map<String, Object>> list = new ArrayList<>();

        if ("create".equals(operation) && (businessOperationId == null || businessOperationId.isEmpty()))
        {
            Map<String, Object> st = findStartState(template);
            if (st != null) list.add(st);
            return AjaxResult.success(list);
        }

        if (businessOperationId != null && !businessOperationId.isEmpty())
        {
            Map<String, Object> st = findTargetState(template, businessOperationId);
            if (st != null) list.add(st);
        }

        return AjaxResult.success(list);
    }

    // ==================== 核心提取逻辑 ====================

    private AjaxResult extractBizOps(String templateId) throws Exception
    {
        JSONObject template = getTemplate();
        if (template == null || !templateId.equals(template.optString("id")))
            return AjaxResult.success(Collections.emptyList());

        Set<String> seen = new HashSet<>();
        List<Map<String, Object>> list = new ArrayList<>();
        JSONArray reserved = template.optJSONArray("reserved");
        if (reserved != null)
        {
            for (int i = 0; i < reserved.length(); i++)
            {
                JSONObject item = reserved.getJSONObject(i);
                JSONObject data = item.optJSONObject("data");
                if (data != null)
                {
                    JSONObject bizOp = data.optJSONObject("bizOperation");
                    if (bizOp != null && !seen.contains(bizOp.optString("id")))
                    {
                        seen.add(bizOp.optString("id"));
                        Map<String, Object> m = new LinkedHashMap<>();
                        m.put("id", bizOp.optString("id"));
                        m.put("name", bizOp.optString("name", bizOp.optString("nameEn", "")));
                        m.put("nameEn", bizOp.optString("nameEn", ""));
                        m.put("businessCode", bizOp.optString("businessCode", ""));
                        list.add(m);
                    }
                }
            }
        }
        return AjaxResult.success(list);
    }

    private Map<String, Object> findStartState(JSONObject template) throws Exception
    {
        JSONArray reserved = template.optJSONArray("reserved");
        if (reserved == null) return null;
        for (int i = 0; i < reserved.length(); i++)
        {
            JSONObject item = reserved.getJSONObject(i);
            JSONObject data = item.optJSONObject("data");
            if (data != null && "start".equals(data.optString("phaseType")))
            {
                JSONObject st = data.optJSONObject("status");
                if (st != null) return stateToMap(st);
            }
        }
        return null;
    }

    private Map<String, Object> findTargetState(JSONObject template, String bizOpId) throws Exception
    {
        JSONArray reserved = template.optJSONArray("reserved");
        if (reserved == null || bizOpId == null || bizOpId.isEmpty()) return null;

        // 1. 在 reserved 中找到 bizOpId 对应的 transition → 获取 target.cell
        String targetCellId = null;
        for (int i = 0; i < reserved.length(); i++)
        {
            JSONObject item = reserved.getJSONObject(i);
            JSONObject d = item.optJSONObject("data");
            if (d != null)
            {
                JSONObject bo = d.optJSONObject("bizOperation");
                if (bo != null && bizOpId.equals(bo.optString("id")))
                {
                    JSONObject tgt = item.optJSONObject("target");
                    if (tgt != null) targetCellId = tgt.optString("cell", "");
                    break;
                }
            }
        }
        if (targetCellId == null || targetCellId.isEmpty()) return null;

        // 2. 在 reserved 中找到 id == targetCellId 的 phase
        for (int i = 0; i < reserved.length(); i++)
        {
            JSONObject item = reserved.getJSONObject(i);
            if (targetCellId.equals(item.optString("id")))
            {
                JSONObject d = item.optJSONObject("data");
                if (d != null)
                {
                    JSONObject st = d.optJSONObject("status");
                    if (st != null && "LifecycleState".equals(st.optString("className")))
                    {
                        return stateToMap(st);
                    }
                }
            }
        }
        return null;
    }

    private Map<String, Object> stateToMap(JSONObject st)
    {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", st.optString("id"));
        m.put("name", st.optString("name", st.optString("nameEn", "")));
        m.put("nameEn", st.optString("nameEn", ""));
        m.put("businessCode", st.optString("businessCode", ""));
        return m;
    }

    // ==================== DME 交互 ====================

    private JSONObject getProduct(String token, String id) throws Exception
    {
        String url = DMEUtil.projectUrl + DMEUtil.apiExecute + "/XfProduct_20/get";
        JSONObject p = new JSONObject();
        p.put("id", id);
        JSONObject pj = new JSONObject();
        pj.put("params", p);
        String res = RequestUtil.requestsPost(url, pj.toString(), token);
        JSONArray arr = new JSONObject(res).optJSONArray("data");
        return (arr != null && arr.length() > 0) ? arr.getJSONObject(0) : new JSONObject();
    }

    private JSONObject getTemplate() throws Exception
    {
        TokenAndProject tap = dmeUtil.getToken();
        String token = tap.getToken();
        String url = DMEUtil.projectUrl + DMEUtil.apiExecute + "/XfProduct_20/find/1/1";
        JSONObject params = new JSONObject();
        params.put("characterSet", "UTF8");
        params.put("isPresentAll", true);
        params.put("isNeedTotal", false);
        params.put("publicData", "INCLUDE_PUBLIC_DATA");
        JSONObject filter = new JSONObject();
        JSONArray conditions = new JSONArray();
        addCond(conditions, "latest", "true", "=");
        filter.put("conditions", conditions);
        filter.put("joiner", "and");
        params.put("filter", filter);
        JSONObject pj = new JSONObject();
        pj.put("params", params);
        String res = RequestUtil.requestsPost(url, pj.toString(), token);
        JSONArray arr = new JSONObject(res).optJSONArray("data");
        if (arr == null || arr.length() == 0) return null;
        JSONObject raw = getProduct(token, arr.getJSONObject(0).optString("id"));
        return raw.optJSONObject("lifecycleTemplate");
    }

    private void addCond(JSONArray conditions, String name, String value, String operator) throws JSONException {
        JSONObject c = new JSONObject();
        c.put("conditionName", name);
        JSONArray v = new JSONArray();
        v.put(value);
        c.put("conditionValues", v);
        c.put("ignoreStr", false);
        c.put("multi", false);
        c.put("operator", operator);
        conditions.put(c);
    }
}
