package com.xunfang.manufacture.domain;

import com.xunfang.common.core.web.domain.BaseEntity;

/**
 * 单位实体 — DME XfPartUnit_20
 */
public class XfUnit extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private String id;
    private String unitCode;
    private String unitName;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUnitCode() { return unitCode; }
    public void setUnitCode(String unitCode) { this.unitCode = unitCode; }
    public String getUnitName() { return unitName; }
    public void setUnitName(String unitName) { this.unitName = unitName; }
}
