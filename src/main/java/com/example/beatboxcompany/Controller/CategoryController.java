package com.example.beatboxcompany.Controller;

import com.example.beatboxcompany.Entity.Category;
import com.example.beatboxcompany.Service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public List<Category> getCategories() {
        return categoryService.getAllCategories();
    }

    @PostMapping // Dùng để bạn thêm nhanh dữ liệu mẫu
    public Category addCategory(@RequestBody Category category) {
        return categoryService.createCategory(category);
    }
}