package com.contenthub.user.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户信息VO
 */
@Data
@Schema(description = "用户信息")
public class UserInfoVO {

    @Schema(description = "用户ID", example = "1")
    private Long id;

    @Schema(description = "用户名", example = "admin")
    private String username;

    @Schema(description = "昵称", example = "管理员")
    private String nickname;

    @Schema(description = "邮箱", example = "admin@example.com")
    private String email;

    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    @Schema(description = "头像URL")
    private String avatarUrl;

    @Schema(description = "个人简介", example = "这个人很懒，什么都没写")
    private String bio;

    @Schema(description = "性别（0-未知，1-男，2-女）", example = "1")
    private Integer gender;

    @Schema(description = "生日", example = "1990-01-01")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthday;

    @Schema(description = "所在地", example = "北京市")
    private String location;

    @Schema(description = "用户等级", example = "1")
    private Integer level;

    @Schema(description = "经验值", example = "100")
    private Integer experience;

    @Schema(description = "角色", example = "user")
    private String role;

    @Schema(description = "创建时间", example = "2024-01-01 12:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
