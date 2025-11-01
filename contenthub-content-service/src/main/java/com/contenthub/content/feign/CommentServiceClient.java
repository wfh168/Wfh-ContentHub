package com.contenthub.content.feign;

import com.contenthub.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 评论服务Feign客户端
 */
@FeignClient(name = "contenthub-comment-service", path = "/comment")
public interface CommentServiceClient {
    
    /**
     * 获取文章评论数量
     * @param articleId 文章ID
     * @return 评论数量
     */
    @GetMapping("/count")
    Result<Long> getCommentCount(@RequestParam("articleId") Long articleId);
}

