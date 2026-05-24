package com.neu.webapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ParcelEntryRequest {

    @NotBlank(message = "包裹名称不能为空")
    private String packageName;

    @NotNull(message = "重量不能为空")
    private Double weight;

    @NotNull(message = "体积不能为空")
    private Double volume;

    @NotNull(message = "申报价值不能为空")
    private Double declaredValue;

    @NotBlank(message = "寄件人不能为空")
    private String senderName;

    @NotBlank(message = "收件人不能为空")
    private String receiverName;

    private String receiverPhone;
    private String receiverAddress;
    private String notes;

    public String getPackageName() { return packageName; }
    public void setPackageName(String packageName) { this.packageName = packageName; }
    public Double getWeight() { return weight; }
    public void setWeight(Double weight) { this.weight = weight; }
    public Double getVolume() { return volume; }
    public void setVolume(Double volume) { this.volume = volume; }
    public Double getDeclaredValue() { return declaredValue; }
    public void setDeclaredValue(Double declaredValue) { this.declaredValue = declaredValue; }
    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }
    public String getReceiverName() { return receiverName; }
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }
    public String getReceiverPhone() { return receiverPhone; }
    public void setReceiverPhone(String receiverPhone) { this.receiverPhone = receiverPhone; }
    public String getReceiverAddress() { return receiverAddress; }
    public void setReceiverAddress(String receiverAddress) { this.receiverAddress = receiverAddress; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
