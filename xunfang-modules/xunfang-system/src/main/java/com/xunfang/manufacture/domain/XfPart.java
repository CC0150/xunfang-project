package com.xunfang.manufacture.domain;

import com.xunfang.common.core.web.domain.BaseEntity;

public class XfPart extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private String id;
    private String partName;           // 部件名称
    private String partNameEn;         // 部件英文名称
    private String partType;           // Part种类(枚举)
    private String specificationsModel; // 规格型号
    private String unit;               // 单位
    private String status;             // Part状态(Enable/Disable)
    private String purchaseOrManufacture; // 购制属性(枚举)
    private String partDeclaration;    // Part说明
    private String fileId;             // 附件 File_20 引用ID
    private String fileName;           // 附件文件名

    // 内部辅助字段
    private String masterId;           // 主版本引用ID
    private String unitName;           // 单位名称(查询用)

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
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
    public String getMasterId() { return masterId; }
    public void setMasterId(String masterId) { this.masterId = masterId; }
    public String getUnitName() { return unitName; }
    public void setUnitName(String unitName) { this.unitName = unitName; }
}
