package com.contenthub.user.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户黑名单实体
 */
@Data
@TableName("user_blacklist")
public class UserBlacklist {
    
    /**
     * ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户ID（拉黑者）
     */
    private Long userId;
    
    /**
     * 被拉黑用户ID
     */
    private Long blockedUserId;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}

