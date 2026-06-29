package com.xunfang.manufacture.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 产品版本实体 — 继承 XfProduct，增加生命周期/工作流字段
 */
public class XfVersionProduct extends XfProduct
{
    private static final long serialVersionUID = 1L;

    private String lifecycleTemplateId;
    private String lifecycleStateId;
    private String workCopyType;
    private String viewNo;

    // 生命周期嵌套对象（前端检出/检入时使用）
    private Map<String, Object> lifecycleTemplate;
    private Map<String, Object> lifecycleState;

    public String getLifecycleTemplateId() { return lifecycleTemplateId; }
    public void setLifecycleTemplateId(String lifecycleTemplateId) { this.lifecycleTemplateId = lifecycleTemplateId; }
    public String getLifecycleStateId() { return lifecycleStateId; }
    public void setLifecycleStateId(String lifecycleStateId) { this.lifecycleStateId = lifecycleStateId; }
    public String getWorkCopyType() { return workCopyType; }
    public void setWorkCopyType(String workCopyType) { this.workCopyType = workCopyType; }
    public String getViewNo() { return viewNo; }
    public void setViewNo(String viewNo) { this.viewNo = viewNo; }
    public Map<String, Object> getLifecycleTemplate() { return lifecycleTemplate; }
    public void setLifecycleTemplate(Map<String, Object> lifecycleTemplate) { this.lifecycleTemplate = lifecycleTemplate; }
    public Map<String, Object> getLifecycleState() { return lifecycleState; }
    public void setLifecycleState(Map<String, Object> lifecycleState) { this.lifecycleState = lifecycleState; }

    /** 从 extAttrs 提取第一个附件信息 */
    public void extractFileName() {
        if (extAttrs == null || extAttrs.isEmpty()) return;
        Map<String, Object> first = extAttrs.get(0);
        if (first == null) return;
        String attrName = String.valueOf(first.getOrDefault("name", ""));
        if (!attrName.contains("File")) return;
        Object valObj = first.get("value");
        if (valObj instanceof List) {
            List<?> list = (List<?>) valObj;
            if (!list.isEmpty() && list.get(0) instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> fileObj = (Map<String, Object>) list.get(0);
                if (fileObj.containsKey("fileName")) this.fileName = String.valueOf(fileObj.get("fileName"));
                if (fileObj.containsKey("id")) this.fileId = String.valueOf(fileObj.get("id"));
                if (fileObj.containsKey("fileDownloadUrl")) this.fileDownloadUrl = String.valueOf(fileObj.get("fileDownloadUrl"));
            }
        }
        if (fileName != null && !fileName.isEmpty()) {
            int dot = fileName.lastIndexOf('.');
            this.fileNameNoExt = dot > 0 ? fileName.substring(0, dot) : fileName;
        }
    }
}
