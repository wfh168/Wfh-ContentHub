package com.contenthub.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 关注用户VO
 */
@Data
@Schema(description = "关注用户信息")
public class FollowUserVO {
    
    @Schema(description = "用户ID")
    private Long userId;
    
    @Schema(description = "用户名")
    private String username;
    
    @Schema(description = "昵称")
    private String nickname;
    
    @Schema(description = "头像URL")
    private String avatarUrl;
    
    @Schema(description = "个人简介")
    private String bio;
    
    @Schema(description = "是否互相关注")
    private Boolean isMutualFollow;
    
    @Schema(description = "关注时间")
    private LocalDateTime followTime;
}

