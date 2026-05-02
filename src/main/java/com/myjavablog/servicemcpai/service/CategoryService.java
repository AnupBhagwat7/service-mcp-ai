package com.myjavablog.servicemcpai.service;

import com.myjavablog.servicemcpai.model.Category;
import com.myjavablog.servicemcpai.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    final private CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<Category> listCategories() {
        return categoryRepository.listCategories();
    }
}
