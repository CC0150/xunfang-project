package com.xunfang.manufacture.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;
import java.util.Map;

/**
 * Part 版本实体 — 继承 XfPart，补齐 masterId、工作状态、附件展示字段
 */
public class XfVersionPart extends XfPart
{
    private static final long serialVersionUID = 1L;

    // ===== 版本/工作流字段 =====
    private String masterId;           // 主对象主键（检入/检出/删除使用）
    private String uiWorkingState;     // UI 工作态：CHECKED_IN / CHECKED_OUT（INWORK → CHECKED_OUT）
    private String displayVersion;     // 显示版本号
    private String workCopyType;       // 检出类型（默认 BOTH）

    // ===== DME 原始工作态字段（用于归一化）=====
    @JsonIgnore
    private String workingState;
    @JsonIgnore
    private String workingStateAlias;
    @JsonIgnore
    private String workingStateCode;

    // ===== 附件展示字段 =====
    private String fileDownloadUrl;    // 文件下载直链
    private String fileNameNoExt;      // 附件无扩展名展示名

    // ===== 修改辅助标志 =====
    private Boolean clearFile;         // true=清空已上传的附件

    // ===== 附件原始数据（内部用）=====
    @JsonIgnore
    private List<Map<String, Object>> extAttrs;

    // ----- getters / setters -----

    public String getMasterId() { return masterId; }
    public void setMasterId(String masterId) { this.masterId = masterId; }

    public String getUiWorkingState() { return uiWorkingState; }
    public void setUiWorkingState(String uiWorkingState) { this.uiWorkingState = uiWorkingState; }

    public String getDisplayVersion() { return displayVersion; }
    public void setDisplayVersion(String displayVersion) { this.displayVersion = displayVersion; }

    public String getWorkCopyType() { return workCopyType; }
    public void setWorkCopyType(String workCopyType) { this.workCopyType = workCopyType; }

    public String getWorkingState() { return workingState; }
    public void setWorkingState(String workingState) { this.workingState = workingState; }

    public String getWorkingStateAlias() { return workingStateAlias; }
    public void setWorkingStateAlias(String workingStateAlias) { this.workingStateAlias = workingStateAlias; }

    public String getWorkingStateCode() { return workingStateCode; }
    public void setWorkingStateCode(String workingStateCode) { this.workingStateCode = workingStateCode; }

    public String getFileDownloadUrl() { return fileDownloadUrl; }
    public void setFileDownloadUrl(String fileDownloadUrl) { this.fileDownloadUrl = fileDownloadUrl; }

    public String getFileNameNoExt() { return fileNameNoExt; }
    public void setFileNameNoExt(String fileNameNoExt) { this.fileNameNoExt = fileNameNoExt; }

    public Boolean getClearFile() { return clearFile; }
    public void setClearFile(Boolean clearFile) { this.clearFile = clearFile; }

    public List<Map<String, Object>> getExtAttrs() { return extAttrs; }
    public void setExtAttrs(List<Map<String, Object>> extAttrs) { this.extAttrs = extAttrs; }

    /**
     * 从 extAttrs 提取第一个附件的文件名和下载地址
     */
    public void extractFileName() {
        if (extAttrs == null || extAttrs.isEmpty()) return;
        Map<String, Object> first = extAttrs.get(0);
        if (first == null) return;
        Object valObj = first.get("value");
        if (valObj instanceof List) {
            List<?> list = (List<?>) valObj;
            if (!list.isEmpty() && list.get(0) instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> fileObj = (Map<String, Object>) list.get(0);
                if (fileObj.containsKey("fileName")) {
                    this.fileName = String.valueOf(fileObj.get("fileName"));
                }
                if (fileObj.containsKey("id")) {
                    this.fileId = String.valueOf(fileObj.get("id"));
                }
                if (fileObj.containsKey("fileDownloadUrl")) {
                    this.fileDownloadUrl = String.valueOf(fileObj.get("fileDownloadUrl"));
                }
            }
        }
        // 提取无扩展名展示名
        if (fileName != null && !fileName.isEmpty()) {
            int dot = fileName.lastIndexOf('.');
            this.fileNameNoExt = dot > 0 ? fileName.substring(0, dot) : fileName;
        }
    }
}
