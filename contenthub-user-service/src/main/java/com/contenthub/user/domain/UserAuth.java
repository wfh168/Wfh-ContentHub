package com.contenthub.user.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户认证实体类
 */
@Data
@TableName("user_auths")
public class UserAuth implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 认证类型（phone-手机，email-邮箱，wechat-微信，qq-QQ，github-GitHub）
     */
    private String identityType;

    /**
     * 标识（手机号/邮箱/第三方ID）
     */
    private String identifier;

    /**
     * 凭证（密码/Token）
     */
    private String credential;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
