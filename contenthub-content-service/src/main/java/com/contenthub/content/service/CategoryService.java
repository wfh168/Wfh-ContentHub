package com.contenthub.content.service;

import com.contenthub.content.dto.CreateCategoryDTO;
import com.contenthub.content.vo.CategoryVO;

import java.util.List;

/**
 * 分类服务接口
 */
public interface CategoryService {
    
    /**
     * 创建分类
     * @param createCategoryDTO 创建分类DTO
     * @return 分类ID
     */
    Long createCategory(CreateCategoryDTO createCategoryDTO);
    
    /**
     * 更新分类
     * @param categoryId 分类ID
     * @param createCategoryDTO 更新分类DTO
     */
    void updateCategory(Long categoryId, CreateCategoryDTO createCategoryDTO);
    
    /**
     * 删除分类
     * @param categoryId 分类ID
     */
    void deleteCategory(Long categoryId);
    
    /**
     * 获取分类列表（所有启用的分类）
     * @return 分类列表
     */
    List<CategoryVO> getCategoryList();
    
    /**
     * 获取分类详情
     * @param categoryId 分类ID
     * @return 分类详情
     */
    CategoryVO getCategoryDetail(Long categoryId);
    
    /**
     * 启用/禁用分类
     * @param categoryId 分类ID
     * @param status 状态（0-禁用，1-启用）
     */
    void updateCategoryStatus(Long categoryId, Integer status);
}

