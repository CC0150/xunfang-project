package com.xunfang.manufacture.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.xunfang.common.core.web.domain.BaseEntity;

import java.util.Date;

/**
 * 采购订单实体
 *
 * @author xunfang
 * @date 2025-09-12
 */
public class XfPurchaseOrder extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 采购订单ID */
    private String id;

    /** 采购订单号 */
    private String purchaseOrderCode;

    /** 采购日期 */
    private Date purchaseDate;

    /** 供应商名称 */
    private String supplierName;

    /** 供应商联系人 */
    private String supplierLinkMan;

    /** 物料编号 */
    private String materialCode;

    /** 物料名称 */
    private String materialName;

    /** 规格型号 */
    private String specificationsModels;

    /** 采购数量 */
    private String purchaseQuantity;

    /** 单位 */
    private String unit;

    /** 单价 */
    private String unitPrice;

    /** 总价格 */
    private String totalPrice;

    /** 订单状态（1=待确认 2=已确认 3=已发货 4=已完成） */
    private String status;

    // ---- 以下为查询辅助字段，不持久化 ----

    /** 采购日期开始（查询用） */
    @JsonIgnore
    private String purchaseDateStart;

    /** 采购日期结束（查询用） */
    @JsonIgnore
    private String purchaseDateEnd;

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getPurchaseOrderCode()
    {
        return purchaseOrderCode;
    }

    public void setPurchaseOrderCode(String purchaseOrderCode)
    {
        this.purchaseOrderCode = purchaseOrderCode;
    }

    public Date getPurchaseDate()
    {
        return purchaseDate;
    }

    public void setPurchaseDate(Date purchaseDate)
    {
        this.purchaseDate = purchaseDate;
    }

    public String getSupplierName()
    {
        return supplierName;
    }

    public void setSupplierName(String supplierName)
    {
        this.supplierName = supplierName;
    }

    public String getSupplierLinkMan()
    {
        return supplierLinkMan;
    }

    public void setSupplierLinkMan(String supplierLinkMan)
    {
        this.supplierLinkMan = supplierLinkMan;
    }

    public String getMaterialCode()
    {
        return materialCode;
    }

    public void setMaterialCode(String materialCode)
    {
        this.materialCode = materialCode;
    }

    public String getMaterialName()
    {
        return materialName;
    }

    public void setMaterialName(String materialName)
    {
        this.materialName = materialName;
    }

    public String getSpecificationsModels()
    {
        return specificationsModels;
    }

    public void setSpecificationsModels(String specificationsModels)
    {
        this.specificationsModels = specificationsModels;
    }

    public String getPurchaseQuantity()
    {
        return purchaseQuantity;
    }

    public void setPurchaseQuantity(String purchaseQuantity)
    {
        this.purchaseQuantity = purchaseQuantity;
    }

    public String getUnit()
    {
        return unit;
    }

    public void setUnit(String unit)
    {
        this.unit = unit;
    }

    public String getUnitPrice()
    {
        return unitPrice;
    }

    public void setUnitPrice(String unitPrice)
    {
        this.unitPrice = unitPrice;
    }

    public String getTotalPrice()
    {
        return totalPrice;
    }

    public void setTotalPrice(String totalPrice)
    {
        this.totalPrice = totalPrice;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getPurchaseDateStart()
    {
        return purchaseDateStart;
    }

    public void setPurchaseDateStart(String purchaseDateStart)
    {
        this.purchaseDateStart = purchaseDateStart;
    }

    public String getPurchaseDateEnd()
    {
        return purchaseDateEnd;
    }

    public void setPurchaseDateEnd(String purchaseDateEnd)
    {
        this.purchaseDateEnd = purchaseDateEnd;
    }

    @Override
    public String toString()
    {
        return "XfPurchaseOrder{" +
                "id='" + id + '\'' +
                ", purchaseOrderCode='" + purchaseOrderCode + '\'' +
                ", purchaseDate=" + purchaseDate +
                ", supplierName='" + supplierName + '\'' +
                ", supplierLinkMan='" + supplierLinkMan + '\'' +
                ", materialCode='" + materialCode + '\'' +
                ", materialName='" + materialName + '\'' +
                ", specificationsModels='" + specificationsModels + '\'' +
                ", purchaseQuantity='" + purchaseQuantity + '\'' +
                ", unit='" + unit + '\'' +
                ", unitPrice='" + unitPrice + '\'' +
                ", totalPrice='" + totalPrice + '\'' +
                ", status='" + status + '\'' +
                "} " + super.toString();
    }
}
