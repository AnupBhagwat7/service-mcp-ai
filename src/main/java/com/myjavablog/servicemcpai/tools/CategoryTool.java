package com.myjavablog.servicemcpai.tools;

import com.myjavablog.servicemcpai.model.Category;
import com.myjavablog.servicemcpai.repository.CategoryRepository;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CategoryTool {
    private final CategoryRepository categoryRepository;

    public CategoryTool(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Tool(description = "List all the system categories", name = "listCategories")
    public List<Category> listCategories() {
        return categoryRepository.listCategories();
    }

    @Tool(description = "Create a new category based on the category name", name = "createCategory")
    public Category createCategory(
            @ToolParam(description = "The name of the category, should be clean and preferable no more than 3 words")
            String categoryName
    ) {
        Long newId = listCategories().stream()
                .map(Category::id)
                .max(Long::compareTo)
                .orElse(1L);
        Category category = new Category(newId + 1, categoryName);
        return categoryRepository.createCategory(category);
    }
}
