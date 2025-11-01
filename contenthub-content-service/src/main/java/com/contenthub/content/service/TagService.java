package com.contenthub.content.service;

import com.contenthub.content.dto.CreateTagDTO;
import com.contenthub.content.vo.TagVO;

import java.util.List;

/**
 * 标签服务接口
 */
public interface TagService {
    
    /**
     * 创建标签
     * @param createTagDTO 创建标签DTO
     * @return 标签ID
     */
    Long createTag(CreateTagDTO createTagDTO);
    
    /**
     * 更新标签
     * @param tagId 标签ID
     * @param createTagDTO 更新标签DTO
     */
    void updateTag(Long tagId, CreateTagDTO createTagDTO);
    
    /**
     * 删除标签
     * @param tagId 标签ID
     */
    void deleteTag(Long tagId);
    
    /**
     * 获取标签列表（按文章数量排序）
     * @param limit 限制数量（可选）
     * @return 标签列表
     */
    List<TagVO> getTagList(Integer limit);
    
    /**
     * 获取标签详情
     * @param tagId 标签ID
     * @return 标签详情
     */
    TagVO getTagDetail(Long tagId);
    
    /**
     * 根据文章ID获取标签列表
     * @param articleId 文章ID
     * @return 标签列表
     */
    List<TagVO> getTagsByArticleId(Long articleId);
}

