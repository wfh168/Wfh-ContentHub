package com.contenthub.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.contenthub.content.domain.Tag;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 标签Mapper
 */
public interface TagMapper extends BaseMapper<Tag> {
    
    /**
     * 增加文章数量
     */
    @Update("UPDATE tags SET article_count = article_count + 1 WHERE id = #{tagId}")
    void incrementArticleCount(@Param("tagId") Long tagId);
    
    /**
     * 减少文章数量
     */
    @Update("UPDATE tags SET article_count = GREATEST(article_count - 1, 0) WHERE id = #{tagId}")
    void decrementArticleCount(@Param("tagId") Long tagId);
}

