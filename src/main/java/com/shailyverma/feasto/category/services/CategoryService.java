package com.shailyverma.feasto.category.services;

import com.shailyverma.feasto.category.dtos.CategoryDTO;
import com.shailyverma.feasto.category.entity.Category;
import com.shailyverma.feasto.response.Response;

import java.util.List;

public interface CategoryService {

    Response<CategoryDTO> addCategory(CategoryDTO categoryDTO);

    Response<CategoryDTO> updateCategory(CategoryDTO categoryDTO);

    Response<CategoryDTO> getCategoryById(Long id);

    Response<List<CategoryDTO>> getAllCategories();

    Response<?> deleteCategory(Long id);


}
