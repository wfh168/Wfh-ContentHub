package com.contenthub.comment.feign;

import com.contenthub.common.result.Result;
import com.contenthub.user.vo.UserInfoVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 用户服务Feign客户端
 */
@FeignClient(name = "contenthub-user-service", path = "/user")
public interface UserServiceClient {
    
    /**
     * 根据ID获取用户信息
     * @param userId 用户ID
     * @return 用户信息
     */
    @GetMapping("/{userId}")
    Result<UserInfoVO> getUserById(@PathVariable("userId") Long userId);
    
    /**
     * 批量获取用户信息
     * @param userIds 用户ID列表（逗号分隔）
     * @return 用户信息列表
     */
    @GetMapping("/batch")
    Result<List<UserInfoVO>> getUsersByIds(@RequestParam("userIds") String userIds);
}

