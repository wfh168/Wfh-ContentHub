package com.contenthub.user.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户关注实体
 */
@Data
@TableName("user_follows")
public class UserFollow {
    
    /**
     * ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 关注者ID
     */
    private Long followerId;
    
    /**
     * 被关注者ID
     */
    private Long followedId;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}

