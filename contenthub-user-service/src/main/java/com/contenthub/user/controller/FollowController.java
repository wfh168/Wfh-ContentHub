package com.contenthub.user.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.contenthub.common.result.Result;
import com.contenthub.user.service.UserService;
import com.contenthub.user.vo.FollowUserVO;
import com.contenthub.user.vo.UserInfoVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 用户关注和黑名单控制器
 */
@Tag(name = "用户关系管理", description = "用户关注、粉丝、黑名单相关接口")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class FollowController {

    private final UserService userService;

    // ==================== 关注相关接口 ====================

    @Operation(summary = "关注用户", description = "关注指定用户")
    @PostMapping("/follow/{followedId}")
    public Result<String> followUser(
            @Parameter(description = "被关注用户ID", required = true)
            @PathVariable Long followedId) {
        long followerId = StpUtil.getLoginIdAsLong();
        userService.followUser(followerId, followedId);
        return Result.success("关注成功", null);
    }

    @Operation(summary = "取消关注", description = "取消关注指定用户")
    @DeleteMapping("/follow/{followedId}")
    public Result<String> unfollowUser(
            @Parameter(description = "被关注用户ID", required = true)
            @PathVariable Long followedId) {
        long followerId = StpUtil.getLoginIdAsLong();
        userService.unfollowUser(followerId, followedId);
        return Result.success("取消关注成功", null);
    }

    @Operation(summary = "检查是否关注", description = "检查当前用户是否关注了指定用户")
    @GetMapping("/follow/check/{followedId}")
    public Result<Boolean> checkFollowing(
            @Parameter(description = "被关注用户ID", required = true)
            @PathVariable Long followedId) {
        long followerId = StpUtil.getLoginIdAsLong();
        boolean isFollowing = userService.isFollowing(followerId, followedId);
        return Result.success(isFollowing);
    }

    @Operation(summary = "获取关注列表", description = "获取当前用户的关注列表（我关注的人）")
    @GetMapping("/following")
    public Result<List<FollowUserVO>> getFollowingList(
            @Parameter(description = "页码", example = "1")
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @Parameter(description = "每页数量", example = "20")
            @RequestParam(value = "size", defaultValue = "20") Integer size) {
        long userId = StpUtil.getLoginIdAsLong();
        List<FollowUserVO> followingList = userService.getFollowingList(userId, page, size);
        return Result.success(followingList);
    }

    @Operation(summary = "获取指定用户的关注列表", description = "获取指定用户的关注列表")
    @GetMapping("/{userId}/following")
    public Result<List<FollowUserVO>> getUserFollowingList(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long userId,
            @Parameter(description = "页码", example = "1")
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @Parameter(description = "每页数量", example = "20")
            @RequestParam(value = "size", defaultValue = "20") Integer size) {
        List<FollowUserVO> followingList = userService.getFollowingList(userId, page, size);
        return Result.success(followingList);
    }

    @Operation(summary = "获取粉丝列表", description = "获取当前用户的粉丝列表（关注我的人）")
    @GetMapping("/followers")
    public Result<List<FollowUserVO>> getFollowerList(
            @Parameter(description = "页码", example = "1")
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @Parameter(description = "每页数量", example = "20")
            @RequestParam(value = "size", defaultValue = "20") Integer size) {
        long userId = StpUtil.getLoginIdAsLong();
        List<FollowUserVO> followerList = userService.getFollowerList(userId, page, size);
        return Result.success(followerList);
    }

    @Operation(summary = "获取指定用户的粉丝列表", description = "获取指定用户的粉丝列表")
    @GetMapping("/{userId}/followers")
    public Result<List<FollowUserVO>> getUserFollowerList(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long userId,
            @Parameter(description = "页码", example = "1")
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @Parameter(description = "每页数量", example = "20")
            @RequestParam(value = "size", defaultValue = "20") Integer size) {
        List<FollowUserVO> followerList = userService.getFollowerList(userId, page, size);
        return Result.success(followerList);
    }

    @Operation(summary = "获取关注统计", description = "获取当前用户的关注数和粉丝数")
    @GetMapping("/follow/stats")
    public Result<Map<String, Long>> getFollowStats() {
        long userId = StpUtil.getLoginIdAsLong();
        Map<String, Long> stats = userService.getFollowStats(userId);
        return Result.success(stats);
    }

    @Operation(summary = "获取指定用户的关注统计", description = "获取指定用户的关注数和粉丝数")
    @GetMapping("/{userId}/follow/stats")
    public Result<Map<String, Long>> getUserFollowStats(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long userId) {
        Map<String, Long> stats = userService.getFollowStats(userId);
        return Result.success(stats);
    }

    // ==================== 黑名单相关接口 ====================

    @Operation(summary = "拉黑用户", description = "将指定用户加入黑名单，并自动取消关注关系")
    @PostMapping("/block/{blockedUserId}")
    public Result<String> blockUser(
            @Parameter(description = "被拉黑用户ID", required = true)
            @PathVariable Long blockedUserId) {
        long userId = StpUtil.getLoginIdAsLong();
        userService.blockUser(userId, blockedUserId);
        return Result.success("拉黑成功", null);
    }

    @Operation(summary = "取消拉黑", description = "将指定用户从黑名单中移除")
    @DeleteMapping("/block/{blockedUserId}")
    public Result<String> unblockUser(
            @Parameter(description = "被拉黑用户ID", required = true)
            @PathVariable Long blockedUserId) {
        long userId = StpUtil.getLoginIdAsLong();
        userService.unblockUser(userId, blockedUserId);
        return Result.success("取消拉黑成功", null);
    }

    @Operation(summary = "检查是否在黑名单", description = "检查指定用户是否在黑名单中")
    @GetMapping("/block/check/{blockedUserId}")
    public Result<Boolean> checkBlocked(
            @Parameter(description = "被检查用户ID", required = true)
            @PathVariable Long blockedUserId) {
        long userId = StpUtil.getLoginIdAsLong();
        boolean isBlocked = userService.isBlocked(userId, blockedUserId);
        return Result.success(isBlocked);
    }

    @Operation(summary = "获取黑名单列表", description = "获取当前用户的黑名单列表")
    @GetMapping("/blacklist")
    public Result<List<UserInfoVO>> getBlacklist(
            @Parameter(description = "页码", example = "1")
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @Parameter(description = "每页数量", example = "20")
            @RequestParam(value = "size", defaultValue = "20") Integer size) {
        long userId = StpUtil.getLoginIdAsLong();
        List<UserInfoVO> blacklist = userService.getBlacklist(userId, page, size);
        return Result.success(blacklist);
    }
}

