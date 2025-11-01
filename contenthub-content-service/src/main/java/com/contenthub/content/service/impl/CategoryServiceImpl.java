package com.contenthub.content.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.contenthub.common.exception.BusinessException;
import com.contenthub.content.domain.Category;
import com.contenthub.content.dto.CreateCategoryDTO;
import com.contenthub.content.mapper.CategoryMapper;
import com.contenthub.content.service.CategoryService;
import com.contenthub.content.vo.CategoryVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 分类服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    
    private final CategoryMapper categoryMapper;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createCategory(CreateCategoryDTO createCategoryDTO) {
        // 1. 验证分类名称
        if (StrUtil.isBlank(createCategoryDTO.getName())) {
            throw new BusinessException("分类名称不能为空");
        }
        
        // 2. 检查分类名称是否已存在
        LambdaQueryWrapper<Category> nameWrapper = new LambdaQueryWrapper<>();
        nameWrapper.eq(Category::getName, createCategoryDTO.getName());
        Category existingCategory = categoryMapper.selectOne(nameWrapper);
        if (existingCategory != null) {
            throw new BusinessException("分类名称已存在");
        }
        
        // 3. 生成slug（如果没有提供）
        String slug = createCategoryDTO.getSlug();
        if (StrUtil.isBlank(slug)) {
            slug = createCategoryDTO.getName().toLowerCase()
                    .replaceAll("[^a-z0-9\\u4e00-\\u9fa5]", "-")
                    .replaceAll("-+", "-")
                    .replaceAll("^-|-$", "");
        }
        
        // 检查slug是否已存在
        LambdaQueryWrapper<Category> slugWrapper = new LambdaQueryWrapper<>();
        slugWrapper.eq(Category::getSlug, slug);
        Category existingBySlug = categoryMapper.selectOne(slugWrapper);
        if (existingBySlug != null) {
            throw new BusinessException("分类别名已存在");
        }
        
        // 4. 创建分类
        Category category = new Category();
        category.setName(createCategoryDTO.getName());
        category.setSlug(slug);
        category.setDescription(createCategoryDTO.getDescription());
        category.setIcon(createCategoryDTO.getIcon());
        category.setSortOrder(createCategoryDTO.getSortOrder() != null ? createCategoryDTO.getSortOrder() : 0);
        category.setStatus(1); // 默认启用
        
        categoryMapper.insert(category);
        
        log.info("创建分类成功: categoryId={}, name={}", category.getId(), category.getName());
        return category.getId();
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCategory(Long categoryId, CreateCategoryDTO createCategoryDTO) {
        // 1. 查询分类
        Category category = categoryMapper.selectById(categoryId);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }
        
        // 2. 检查分类名称是否已存在（如果更新了名称）
        if (StrUtil.isNotBlank(createCategoryDTO.getName()) && !createCategoryDTO.getName().equals(category.getName())) {
            LambdaQueryWrapper<Category> nameWrapper = new LambdaQueryWrapper<>();
            nameWrapper.eq(Category::getName, createCategoryDTO.getName());
            Category existingCategory = categoryMapper.selectOne(nameWrapper);
            if (existingCategory != null) {
                throw new BusinessException("分类名称已存在");
            }
        }
        
        // 3. 更新分类字段
        if (StrUtil.isNotBlank(createCategoryDTO.getName())) {
            category.setName(createCategoryDTO.getName());
        }
        if (StrUtil.isNotBlank(createCategoryDTO.getSlug())) {
            // 检查slug是否已存在
            LambdaQueryWrapper<Category> slugWrapper = new LambdaQueryWrapper<>();
            slugWrapper.eq(Category::getSlug, createCategoryDTO.getSlug())
                      .ne(Category::getId, categoryId);
            Category existingBySlug = categoryMapper.selectOne(slugWrapper);
            if (existingBySlug != null) {
                throw new BusinessException("分类别名已存在");
            }
            category.setSlug(createCategoryDTO.getSlug());
        }
        if (StrUtil.isNotBlank(createCategoryDTO.getDescription())) {
            category.setDescription(createCategoryDTO.getDescription());
        }
        if (StrUtil.isNotBlank(createCategoryDTO.getIcon())) {
            category.setIcon(createCategoryDTO.getIcon());
        }
        if (createCategoryDTO.getSortOrder() != null) {
            category.setSortOrder(createCategoryDTO.getSortOrder());
        }
        
        categoryMapper.updateById(category);
        
        log.info("更新分类成功: categoryId={}, name={}", categoryId, category.getName());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCategory(Long categoryId) {
        Category category = categoryMapper.selectById(categoryId);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }
        
        // 注意：这里只是删除分类，关联的文章category_id会设置为NULL（外键约束）
        categoryMapper.deleteById(categoryId);
        
        log.info("删除分类成功: categoryId={}", categoryId);
    }
    
    @Override
    public List<CategoryVO> getCategoryList() {
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Category::getStatus, 1) // 只查询启用的分类
                   .orderByAsc(Category::getSortOrder)
                   .orderByAsc(Category::getId);
        
        List<Category> categories = categoryMapper.selectList(queryWrapper);
        
        return categories.stream()
                .map(category -> {
                    CategoryVO vo = new CategoryVO();
                    BeanUtil.copyProperties(category, vo);
                    return vo;
                })
                .collect(Collectors.toList());
    }
    
    @Override
    public CategoryVO getCategoryDetail(Long categoryId) {
        Category category = categoryMapper.selectById(categoryId);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }
        
        CategoryVO vo = new CategoryVO();
        BeanUtil.copyProperties(category, vo);
        return vo;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCategoryStatus(Long categoryId, Integer status) {
        Category category = categoryMapper.selectById(categoryId);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }
        
        category.setStatus(status);
        categoryMapper.updateById(category);
        
        log.info("更新分类状态成功: categoryId={}, status={}", categoryId, status);
    }
}

