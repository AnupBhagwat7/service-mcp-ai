package com.myjavablog.servicemcpai.controller;

import com.myjavablog.servicemcpai.model.Category;
import com.myjavablog.servicemcpai.service.CategoryService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api")
public class CategoryController {


    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/categories")
    public List<Category> listCategories() {
        return categoryService.listCategories();
    }

    @GetMapping("/categories/ai-classification")
    public Category classifyCategory(@RequestParam String description) {
        return categoryService.guessCategory(description);
    }

    @GetMapping("/categories/ai-classifications")
    public List<Category> classifyCategories(@RequestParam List<String> descriptions) {
        return categoryService.guessCategories(descriptions);
    }

    @PostMapping("/categories/ai-classifications/pdf")
    public List<Category> classifyPdfCategories(@RequestParam("file") MultipartFile file) throws IOException {
        return categoryService.guessCategoryPdf(file);
    }
}
