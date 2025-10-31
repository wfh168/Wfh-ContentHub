package com.contenthub.comment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 评论VO
 */
@Data
@Schema(description = "评论信息")
public class CommentVO {
    
    @Schema(description = "评论ID")
    private Long id;
    
    @Schema(description = "文章ID")
    private Long articleId;
    
    @Schema(description = "评论用户ID")
    private Long userId;
    
    @Schema(description = "评论用户名")
    private String username;
    
    @Schema(description = "评论用户昵称")
    private String nickname;
    
    @Schema(description = "评论用户头像")
    private String avatarUrl;
    
    @Schema(description = "父评论ID（为NULL表示一级评论）")
    private Long parentId;
    
    @Schema(description = "根评论ID（用于二级评论）")
    private Long rootId;
    
    @Schema(description = "评论内容")
    private String content;
    
    @Schema(description = "点赞数")
    private Integer likeCount;
    
    @Schema(description = "是否已点赞（当前用户）")
    private Boolean isLiked;
    
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
    
    @Schema(description = "子评论列表（二级评论）")
    private List<CommentVO> children;
}

