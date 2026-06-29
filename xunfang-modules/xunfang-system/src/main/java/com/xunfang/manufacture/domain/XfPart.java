package com.xunfang.manufacture.domain;

import com.xunfang.common.core.web.domain.BaseEntity;

/**
 * Part 基础实体 — 通用属性
 * 版本/工作流字段见 XfVersionPart
 */
public class XfPart extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private String id;
    private String partCode;           // Part 编码（可选，可由外部系统生成）
    private String partName;           // 部件名称（必填，≤128）
    private String partNameEn;         // 部件英文名称（可选，≤128）
    private String partType;           // Part种类枚举：Ma/Sfp/Pro/Rhy/Oth
    private String specificationsModel; // 规格型号（可选，≤128）
    private String unit;               // 单位（必填，来自单位管理）
    private String status;             // Part状态枚举：Enable/Disable
    private String purchaseOrManufacture; // 购制属性枚举：Pur/Manu
    private String partDeclaration;    // Part说明（可选，≤512）
    protected String fileId;          // 附件引用ID（子类需访问）
    protected String fileName;        // 附件文件名（子类需访问）

    // 内部辅助字段
    private String unitName;           // 单位名称（查询/展示用）
    private String creator;            // 创建者
    private String modifier;           // 修改者

    // ----- getters / setters -----
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getPartCode() { return partCode; }
    public void setPartCode(String partCode) { this.partCode = partCode; }
    public String getPartName() { return partName; }
    public void setPartName(String partName) { this.partName = partName; }
    public String getPartNameEn() { return partNameEn; }
    public void setPartNameEn(String partNameEn) { this.partNameEn = partNameEn; }
    public String getPartType() { return partType; }
    public void setPartType(String partType) { this.partType = partType; }
    public String getSpecificationsModel() { return specificationsModel; }
    public void setSpecificationsModel(String specificationsModel) { this.specificationsModel = specificationsModel; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPurchaseOrManufacture() { return purchaseOrManufacture; }
    public void setPurchaseOrManufacture(String purchaseOrManufacture) { this.purchaseOrManufacture = purchaseOrManufacture; }
    public String getPartDeclaration() { return partDeclaration; }
    public void setPartDeclaration(String partDeclaration) { this.partDeclaration = partDeclaration; }
    public String getFileId() { return fileId; }
    public void setFileId(String fileId) { this.fileId = fileId; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getUnitName() { return unitName; }
    public void setUnitName(String unitName) { this.unitName = unitName; }
    public String getCreator() { return creator; }
    public void setCreator(String creator) { this.creator = creator; }
    public String getModifier() { return modifier; }
    public void setModifier(String modifier) { this.modifier = modifier; }
}
