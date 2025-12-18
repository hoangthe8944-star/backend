package com.example.beatboxcompany.Service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.beatboxcompany.Dto.UploadResultDto;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    // ======================================================================================
    // PHƯƠNG THỨC 1: DÙNG CHO MULTIPARTFILE (ĐÂY LÀ PHƯƠNG THỨC BẠN ĐANG THIẾU)
    // ======================================================================================
    /**
     * Tải lên một MultipartFile.
     * @param file File được gửi từ client
     * @param folder Thư mục trên Cloudinary (ví dụ: "audio", "covers")
     * @return Kết quả upload chứa publicId và secureUrl
     */
    public UploadResultDto uploadFile(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File không được rỗng.");
        }
        
        try {
            // Lấy dữ liệu file dưới dạng mảng byte và gọi phương thức chung
            return uploadFileBytes(file.getBytes(), folder, file.getContentType());
        } catch (IOException e) {
            throw new RuntimeException("Không thể đọc dữ liệu từ file: " + e.getMessage(), e);
        }
    }

    // ======================================================================================
    // PHƯƠNG THỨC 2: DÙNG CHO BYTE ARRAY (Dùng cho chức năng import từ file ZIP)
    // ======================================================================================
    /**
     * Tải lên một mảng byte.
     * @param fileBytes Dữ liệu file dưới dạng byte[]
     * @param folder Thư mục trên Cloudinary
     * @param originalContentType Loại nội dung gốc của file (để xác định resource_type)
     * @return Kết quả upload
     */
    public UploadResultDto uploadFileBytes(byte[] fileBytes, String folder, String originalContentType) {
        if (fileBytes == null || fileBytes.length == 0) {
            throw new RuntimeException("Dữ liệu file không được rỗng.");
        }

        // Xác định resource_type dựa trên loại nội dung
        String resourceType = "auto";
        if (originalContentType != null) {
            if (originalContentType.startsWith("audio")) {
                resourceType = "video"; // QUAN TRỌNG: file âm thanh phải là "video"
            } else if (originalContentType.startsWith("image")) {
                resourceType = "image";
            }
        }

        Map<String, Object> options = ObjectUtils.asMap(
            "resource_type", resourceType,
            "folder", folder
        );

        try {
            Map uploadResult = cloudinary.uploader().upload(fileBytes, options);
            String publicId = (String) uploadResult.get("public_id");
            String secureUrl = (String) uploadResult.get("secure_url");
            return new UploadResultDto(publicId, secureUrl);
        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi tải file lên Cloudinary: " + e.getMessage(), e);
        }
    }

    /**
     * Xóa file khỏi Cloudinary bằng Public ID.
     * @param publicId ID công khai của file (được lưu trong DB của bạn)
     */
    public void deleteFile(String publicId) {
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi xóa file khỏi Cloudinary: " + e.getMessage(), e);
        }
    }
}