package com.contenthub.content.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.contenthub.common.exception.BusinessException;
import com.contenthub.content.domain.Tag;
import com.contenthub.content.dto.CreateTagDTO;
import com.contenthub.content.mapper.TagMapper;
import com.contenthub.content.service.TagService;
import com.contenthub.content.vo.TagVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 标签服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {
    
    private final TagMapper tagMapper;
    private final com.contenthub.content.mapper.ArticleTagMapper articleTagMapper;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createTag(CreateTagDTO createTagDTO) {
        // 1. 验证标签名称
        if (StrUtil.isBlank(createTagDTO.getName())) {
            throw new BusinessException("标签名称不能为空");
        }
        
        // 2. 检查标签名称是否已存在
        LambdaQueryWrapper<Tag> nameWrapper = new LambdaQueryWrapper<>();
        nameWrapper.eq(Tag::getName, createTagDTO.getName());
        Tag existingTag = tagMapper.selectOne(nameWrapper);
        if (existingTag != null) {
            throw new BusinessException("标签名称已存在");
        }
        
        // 3. 生成slug（如果没有提供）
        String slug = createTagDTO.getSlug();
        if (StrUtil.isBlank(slug)) {
            slug = createTagDTO.getName().toLowerCase()
                    .replaceAll("[^a-z0-9\\u4e00-\\u9fa5]", "-")
                    .replaceAll("-+", "-")
                    .replaceAll("^-|-$", "");
        }
        
        // 检查slug是否已存在
        LambdaQueryWrapper<Tag> slugWrapper = new LambdaQueryWrapper<>();
        slugWrapper.eq(Tag::getSlug, slug);
        Tag existingBySlug = tagMapper.selectOne(slugWrapper);
        if (existingBySlug != null) {
            throw new BusinessException("标签别名已存在");
        }
        
        // 4. 创建标签
        Tag tag = new Tag();
        tag.setName(createTagDTO.getName());
        tag.setSlug(slug);
        tag.setDescription(createTagDTO.getDescription());
        tag.setColor(createTagDTO.getColor());
        tag.setArticleCount(0);
        
        tagMapper.insert(tag);
        
        log.info("创建标签成功: tagId={}, name={}", tag.getId(), tag.getName());
        return tag.getId();
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTag(Long tagId, CreateTagDTO createTagDTO) {
        // 1. 查询标签
        Tag tag = tagMapper.selectById(tagId);
        if (tag == null) {
            throw new BusinessException("标签不存在");
        }
        
        // 2. 检查标签名称是否已存在（如果更新了名称）
        if (StrUtil.isNotBlank(createTagDTO.getName()) && !createTagDTO.getName().equals(tag.getName())) {
            LambdaQueryWrapper<Tag> nameWrapper = new LambdaQueryWrapper<>();
            nameWrapper.eq(Tag::getName, createTagDTO.getName());
            Tag existingTag = tagMapper.selectOne(nameWrapper);
            if (existingTag != null) {
                throw new BusinessException("标签名称已存在");
            }
        }
        
        // 3. 更新标签字段
        if (StrUtil.isNotBlank(createTagDTO.getName())) {
            tag.setName(createTagDTO.getName());
        }
        if (StrUtil.isNotBlank(createTagDTO.getSlug())) {
            // 检查slug是否已存在
            LambdaQueryWrapper<Tag> slugWrapper = new LambdaQueryWrapper<>();
            slugWrapper.eq(Tag::getSlug, createTagDTO.getSlug())
                      .ne(Tag::getId, tagId);
            Tag existingBySlug = tagMapper.selectOne(slugWrapper);
            if (existingBySlug != null) {
                throw new BusinessException("标签别名已存在");
            }
            tag.setSlug(createTagDTO.getSlug());
        }
        if (StrUtil.isNotBlank(createTagDTO.getDescription())) {
            tag.setDescription(createTagDTO.getDescription());
        }
        if (StrUtil.isNotBlank(createTagDTO.getColor())) {
            tag.setColor(createTagDTO.getColor());
        }
        
        tagMapper.updateById(tag);
        
        log.info("更新标签成功: tagId={}, name={}", tagId, tag.getName());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTag(Long tagId) {
        Tag tag = tagMapper.selectById(tagId);
        if (tag == null) {
            throw new BusinessException("标签不存在");
        }
        
        // 注意：关联的文章标签记录会被自动删除（外键约束 CASCADE）
        tagMapper.deleteById(tagId);
        
        log.info("删除标签成功: tagId={}", tagId);
    }
    
    @Override
    public List<TagVO> getTagList(Integer limit) {
        LambdaQueryWrapper<Tag> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Tag::getArticleCount)
                   .orderByAsc(Tag::getId);
        
        if (limit != null && limit > 0) {
            queryWrapper.last("LIMIT " + limit);
        }
        
        List<Tag> tags = tagMapper.selectList(queryWrapper);
        
        return tags.stream()
                .map(tag -> {
                    TagVO vo = new TagVO();
                    BeanUtil.copyProperties(tag, vo);
                    return vo;
                })
                .collect(Collectors.toList());
    }
    
    @Override
    public TagVO getTagDetail(Long tagId) {
        Tag tag = tagMapper.selectById(tagId);
        if (tag == null) {
            throw new BusinessException("标签不存在");
        }
        
        TagVO vo = new TagVO();
        BeanUtil.copyProperties(tag, vo);
        return vo;
    }
    
    @Override
    public List<TagVO> getTagsByArticleId(Long articleId) {
        LambdaQueryWrapper<com.contenthub.content.domain.ArticleTag> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(com.contenthub.content.domain.ArticleTag::getArticleId, articleId);
        List<com.contenthub.content.domain.ArticleTag> articleTags = articleTagMapper.selectList(queryWrapper);
        
        if (articleTags.isEmpty()) {
            return List.of();
        }
        
        List<Long> tagIds = articleTags.stream()
                .map(com.contenthub.content.domain.ArticleTag::getTagId)
                .collect(Collectors.toList());
        
        List<Tag> tags = tagMapper.selectBatchIds(tagIds);
        
        return tags.stream()
                .map(tag -> {
                    TagVO vo = new TagVO();
                    BeanUtil.copyProperties(tag, vo);
                    return vo;
                })
                .collect(Collectors.toList());
    }
}

