package com.xunfang.manufacture.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.xunfang.common.core.web.domain.BaseEntity;
import java.util.List;
import java.util.Map;

/**
 * 产品基础实体
 */
public class XfProduct extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    protected String id;
    protected String masterId;
    protected String productName;          // 产品名称（必填，≤64）
    protected String productFamily;        // 产品族名称（必填，来自远程下拉）
    protected String category;             // 类别：CU=定制, ST=标准
    protected String specificationModels;  // 规格（选填，≤128）
    protected String productDescribe;      // 描述（选填，≤512）

    // 附件字段
    protected String fileId;
    protected String fileName;
    protected String fileDownloadUrl;
    protected String fileNameNoExt;
    protected Boolean clearFile;

    // 工作状态
    protected String displayVersion;
    protected String uiWorkingState;

    @JsonIgnore
    protected String workingState;
    @JsonIgnore
    protected String workingStateAlias;

    // 生命周期
    protected String lifecycleStateName;

    @JsonIgnore
    protected List<Map<String, Object>> extAttrs;

    // getters/setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getMasterId() { return masterId; }
    public void setMasterId(String masterId) { this.masterId = masterId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getProductFamily() { return productFamily; }
    public void setProductFamily(String productFamily) { this.productFamily = productFamily; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getSpecificationModels() { return specificationModels; }
    public void setSpecificationModels(String specificationModels) { this.specificationModels = specificationModels; }
    public String getProductDescribe() { return productDescribe; }
    public void setProductDescribe(String productDescribe) { this.productDescribe = productDescribe; }
    public String getFileId() { return fileId; }
    public void setFileId(String fileId) { this.fileId = fileId; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getFileDownloadUrl() { return fileDownloadUrl; }
    public void setFileDownloadUrl(String fileDownloadUrl) { this.fileDownloadUrl = fileDownloadUrl; }
    public String getFileNameNoExt() { return fileNameNoExt; }
    public void setFileNameNoExt(String fileNameNoExt) { this.fileNameNoExt = fileNameNoExt; }
    public Boolean getClearFile() { return clearFile; }
    public void setClearFile(Boolean clearFile) { this.clearFile = clearFile; }
    public String getDisplayVersion() { return displayVersion; }
    public void setDisplayVersion(String displayVersion) { this.displayVersion = displayVersion; }
    public String getUiWorkingState() { return uiWorkingState; }
    public void setUiWorkingState(String uiWorkingState) { this.uiWorkingState = uiWorkingState; }
    public String getWorkingState() { return workingState; }
    public void setWorkingState(String workingState) { this.workingState = workingState; }
    public String getWorkingStateAlias() { return workingStateAlias; }
    public void setWorkingStateAlias(String workingStateAlias) { this.workingStateAlias = workingStateAlias; }
    public String getLifecycleStateName() { return lifecycleStateName; }
    public void setLifecycleStateName(String lifecycleStateName) { this.lifecycleStateName = lifecycleStateName; }
    public List<Map<String, Object>> getExtAttrs() { return extAttrs; }
    public void setExtAttrs(List<Map<String, Object>> extAttrs) { this.extAttrs = extAttrs; }
}
