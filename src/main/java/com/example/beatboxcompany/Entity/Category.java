package com.example.beatboxcompany.Entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "categories")
public class Category {
    @Id
    private String id;
    private String name;     // Tên hiển thị (Ví dụ: Nhạc Pop)
    private String slug;     // Dùng cho URL/Filter (Ví dụ: pop)
    private String color;    // Mã màu Hex (Ví dụ: #148a08)
    private String imageUrl; // Ảnh minh họa cho thể loại
}