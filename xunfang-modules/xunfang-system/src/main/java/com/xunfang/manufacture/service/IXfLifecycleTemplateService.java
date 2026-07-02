package com.xunfang.manufacture.service;

import com.xunfang.common.core.web.domain.AjaxResult;

/**
 * 生命周期模板Service接口
 *
 * @author xunfang
 */
public interface IXfLifecycleTemplateService
{
    /**
     * 获取生命周期模板列表
     */
    AjaxResult getLifecycleTemplateList() throws Exception;

    /**
     * 获取业务操作列表（根据模板ID）
     */
    AjaxResult getLifeBusiness(String templateId) throws Exception;

    /**
     * 获取生命周期状态
     *
     * @param templateId          模板ID
     * @param businessOperationId 业务操作ID（create操作时可为空）
     * @param operation           操作类型（create / 其他）
     */
    AjaxResult getLifeState(String templateId, String businessOperationId, String operation) throws Exception;
}
