package com.xunfang.manufacture.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.xunfang.common.core.web.domain.BaseEntity;

import java.util.Date;

/**
 * 供应商实体
 *
 * @author xunfang
 * @date 2025-09-12
 */
public class XfSupplier extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 供应商ID */
    private String id;

    /** 供应商编码 */
    private String supplierCode;

    /** 供应商名称 */
    private String supplierName;

    /** 供应商联系人 */
    private String linkMan;

    /** 联系电话 */
    private String linkPhone;

    /** 联系邮箱 */
    private String linkEmail;

    /** 供应商类型（1=原材料供应商 2=设备供应商 3=零件供应商） */
    private String supplierType;

    /** 联系地址 */
    private String address;

    /** 供货范围 */
    private String scopeOfSupply;

    /** 合作状态（1=合作中 2=已暂停 3=已中止） */
    private String cooperativeStatus;

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getSupplierCode()
    {
        return supplierCode;
    }

    public void setSupplierCode(String supplierCode)
    {
        this.supplierCode = supplierCode;
    }

    public String getSupplierName()
    {
        return supplierName;
    }

    public void setSupplierName(String supplierName)
    {
        this.supplierName = supplierName;
    }

    public String getLinkMan()
    {
        return linkMan;
    }

    public void setLinkMan(String linkMan)
    {
        this.linkMan = linkMan;
    }

    public String getLinkPhone()
    {
        return linkPhone;
    }

    public void setLinkPhone(String linkPhone)
    {
        this.linkPhone = linkPhone;
    }

    public String getLinkEmail()
    {
        return linkEmail;
    }

    public void setLinkEmail(String linkEmail)
    {
        this.linkEmail = linkEmail;
    }

    public String getSupplierType()
    {
        return supplierType;
    }

    public void setSupplierType(String supplierType)
    {
        this.supplierType = supplierType;
    }

    public String getAddress()
    {
        return address;
    }

    public void setAddress(String address)
    {
        this.address = address;
    }

    public String getScopeOfSupply()
    {
        return scopeOfSupply;
    }

    public void setScopeOfSupply(String scopeOfSupply)
    {
        this.scopeOfSupply = scopeOfSupply;
    }

    public String getCooperativeStatus()
    {
        return cooperativeStatus;
    }

    public void setCooperativeStatus(String cooperativeStatus)
    {
        this.cooperativeStatus = cooperativeStatus;
    }

    @Override
    public String toString()
    {
        return "XfSupplier{" +
                "id='" + id + '\'' +
                ", supplierCode='" + supplierCode + '\'' +
                ", supplierName='" + supplierName + '\'' +
                ", linkMan='" + linkMan + '\'' +
                ", linkPhone='" + linkPhone + '\'' +
                ", linkEmail='" + linkEmail + '\'' +
                ", supplierType='" + supplierType + '\'' +
                ", address='" + address + '\'' +
                ", scopeOfSupply='" + scopeOfSupply + '\'' +
                ", cooperativeStatus='" + cooperativeStatus + '\'' +
                "} " + super.toString();
    }
}
