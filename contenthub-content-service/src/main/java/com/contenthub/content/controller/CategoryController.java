package com.contenthub.content.controller;

import com.contenthub.common.result.Result;
import com.contenthub.content.dto.CreateCategoryDTO;
import com.contenthub.content.service.CategoryService;
import com.contenthub.content.vo.CategoryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分类控制器
 */
@Tag(name = "分类管理", description = "分类相关的API接口")
@RestController
@RequestMapping("/content/category")
@RequiredArgsConstructor
public class CategoryController {
    
    private final CategoryService categoryService;
    
    @Operation(summary = "创建分类", description = "创建新的分类", security = {@SecurityRequirement(name = "Authorization")})
    @PostMapping("/create")
    public Result<Long> createCategory(@Validated @RequestBody CreateCategoryDTO createCategoryDTO) {
        Long categoryId = categoryService.createCategory(createCategoryDTO);
        return Result.success("创建成功", categoryId);
    }
    
    @Operation(summary = "更新分类", description = "更新分类信息", security = {@SecurityRequirement(name = "Authorization")})
    @PutMapping("/{categoryId}")
    public Result<String> updateCategory(
            @Parameter(description = "分类ID", required = true) @PathVariable Long categoryId,
            @Validated @RequestBody CreateCategoryDTO createCategoryDTO) {
        categoryService.updateCategory(categoryId, createCategoryDTO);
        return Result.success("更新成功", null);
    }
    
    @Operation(summary = "删除分类", description = "删除分类", security = {@SecurityRequirement(name = "Authorization")})
    @DeleteMapping("/{categoryId}")
    public Result<String> deleteCategory(
            @Parameter(description = "分类ID", required = true) @PathVariable Long categoryId) {
        categoryService.deleteCategory(categoryId);
        return Result.success("删除成功", null);
    }
    
    @Operation(summary = "获取分类列表", description = "获取所有启用的分类列表")
    @GetMapping("/list")
    public Result<List<CategoryVO>> getCategoryList() {
        List<CategoryVO> categories = categoryService.getCategoryList();
        return Result.success(categories);
    }
    
    @Operation(summary = "获取分类详情", description = "根据分类ID获取分类详细信息")
    @GetMapping("/{categoryId}")
    public Result<CategoryVO> getCategoryDetail(
            @Parameter(description = "分类ID", required = true) @PathVariable Long categoryId) {
        CategoryVO category = categoryService.getCategoryDetail(categoryId);
        return Result.success(category);
    }
    
    @Operation(summary = "更新分类状态", description = "启用/禁用分类", security = {@SecurityRequirement(name = "Authorization")})
    @PutMapping("/{categoryId}/status")
    public Result<String> updateCategoryStatus(
            @Parameter(description = "分类ID", required = true) @PathVariable Long categoryId,
            @Parameter(description = "状态（0-禁用，1-启用）", required = true) @RequestParam Integer status) {
        categoryService.updateCategoryStatus(categoryId, status);
        return Result.success("更新成功", null);
    }
}

