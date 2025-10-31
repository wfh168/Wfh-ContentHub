package com.contenthub.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.Size;
import java.time.LocalDate;

/**
 * 更新用户信息DTO
 */
@Data
@Schema(description = "更新用户信息请求")
public class UpdateUserDTO {

    @Schema(description = "昵称", example = "新昵称")
    @Size(max = 50, message = "昵称长度不能超过50")
    private String nickname;

    @Schema(description = "邮箱", example = "newemail@example.com")
    @Email(message = "邮箱格式不正确")
    private String email;

    @Schema(description = "手机号", example = "13900139000")
    private String phone;

    @Schema(description = "头像URL（前端上传后返回的URL）", example = "http://localhost:9000/avatars/xxx.jpg")
    private String avatarUrl;

    @Schema(description = "个人简介", example = "这是我的新简介")
    @Size(max = 500, message = "个人简介不能超过500字")
    private String bio;

    @Schema(description = "性别（0-未知，1-男，2-女）", example = "1")
    private Integer gender;

    @Schema(description = "生日", example = "1990-01-01")
    private LocalDate birthday;

    @Schema(description = "所在地", example = "上海市")
    @Size(max = 100, message = "所在地不能超过100字")
    private String location;
}

