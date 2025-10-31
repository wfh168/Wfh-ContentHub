package com.contenthub.user.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.contenthub.common.result.Result;
import com.contenthub.user.dto.UpdateUserDTO;
import com.contenthub.user.service.UserService;
import com.contenthub.user.vo.UserInfoVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户控制器
 */
@Tag(name = "用户管理", description = "用户信息管理相关接口")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "根据ID获取用户信息", description = "根据用户ID查询用户详细信息")
    @GetMapping("/{userId}")
    public Result<UserInfoVO> getUserById(
            @Parameter(description = "用户ID", required = true) @PathVariable Long userId) {
        UserInfoVO userInfo = userService.getUserInfo(userId);
        return Result.success(userInfo);
    }

    @Operation(summary = "更新当前用户信息", description = "更新当前登录用户的个人信息")
    @PutMapping("/update")
    public Result<String> updateCurrentUser(
            @Validated @RequestBody UpdateUserDTO updateUserDTO) {
        // 从Sa-Token获取当前登录用户ID
        long userId = StpUtil.getLoginIdAsLong();
        userService.updateUserInfo(userId, updateUserDTO);
        return Result.success("更新成功", null);
    }

    @Operation(summary = "更新指定用户信息", description = "管理员更新指定用户的信息")
    @PutMapping("/{userId}")
    public Result<String> updateUser(
            @Parameter(description = "用户ID", required = true) @PathVariable Long userId,
            @Validated @RequestBody UpdateUserDTO updateUserDTO) {
        userService.updateUserInfo(userId, updateUserDTO);
        return Result.success("更新成功", null);
    }

    @Operation(
            summary = "上传头像", 
            description = "上传并更新当前用户头像\n\n**限制说明：**\n- 文件格式：仅支持图片（jpg, png, gif等）\n- 文件大小：最大2MB\n- 权限：仅可上传自己的头像"
    )
    @PostMapping(value = "/avatar/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<String> uploadAvatar(
            @Parameter(
                    description = "头像文件",
                    required = true,
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
            )
            @RequestPart("file") MultipartFile file) {
        // 从Sa-Token获取当前登录用户ID
        long userId = StpUtil.getLoginIdAsLong();
        String avatarUrl = userService.uploadAvatar(userId, file);
        return Result.success("头像上传成功", avatarUrl);
    }

    @Operation(summary = "批量获取用户信息", description = "根据用户ID列表批量获取用户信息（用于评论等服务）")
    @GetMapping("/batch")
    public Result<java.util.List<UserInfoVO>> getUsersByIds(
            @Parameter(description = "用户ID列表（逗号分隔）", required = true, example = "1,2,3")
            @RequestParam("userIds") String userIds) {
        java.util.List<Long> idList = java.util.Arrays.stream(userIds.split(","))
                .map(String::trim)
                .map(Long::parseLong)
                .collect(java.util.stream.Collectors.toList());
        
        java.util.List<UserInfoVO> users = idList.stream()
                .map(userId -> {
                    try {
                        return userService.getUserInfo(userId);
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(java.util.Objects::nonNull)
                .collect(java.util.stream.Collectors.toList());
        
        return Result.success(users);
    }
}
