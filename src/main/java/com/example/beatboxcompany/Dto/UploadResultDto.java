package com.example.beatboxcompany.Dto;

public class UploadResultDto {
    private String publicId; // ID để quản lý (xóa/cập nhật)
    private String secureUrl; // URL công khai của file

    public UploadResultDto(String publicId, String secureUrl) {
        this.publicId = publicId;
        this.secureUrl = secureUrl;
    }

    // Getters and Setters (tạo bằng IDE)
    public String getPublicId() { return publicId; }
    public void setPublicId(String publicId) { this.publicId = publicId; }
    public String getSecureUrl() { return secureUrl; }
    public void setSecureUrl(String secureUrl) { this.secureUrl = secureUrl; }
}