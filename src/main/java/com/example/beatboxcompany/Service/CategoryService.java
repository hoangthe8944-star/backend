package com.example.beatboxcompany.Service;
import com.example.beatboxcompany.Entity.Category;
import java.util.List;

public interface CategoryService {
    List<Category> getAllCategories();
    Category createCategory(Category category);
}