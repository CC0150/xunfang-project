package com.xunfang.manufacture.domain;

import com.xunfang.common.core.web.domain.BaseEntity;

/**
 * 产品族实体
 */
public class XfProductFamily extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private String id;
    private String productFamilyCode;
    private String productFamilyNameCn;   // 中文名称（必填，≤64）
    private String productFamilyNameEn;   // 英文名称（必填，≤64）
    private String description;           // 描述（选填，≤256）

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getProductFamilyCode() { return productFamilyCode; }
    public void setProductFamilyCode(String productFamilyCode) { this.productFamilyCode = productFamilyCode; }
    public String getProductFamilyNameCn() { return productFamilyNameCn; }
    public void setProductFamilyNameCn(String productFamilyNameCn) { this.productFamilyNameCn = productFamilyNameCn; }
    public String getProductFamilyNameEn() { return productFamilyNameEn; }
    public void setProductFamilyNameEn(String productFamilyNameEn) { this.productFamilyNameEn = productFamilyNameEn; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
