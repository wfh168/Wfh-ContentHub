package com.contenthub.user.feign;

import com.contenthub.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件服务Feign客户端
 */
@FeignClient(name = "contenthub-file-service", path = "/file")
public interface FileServiceClient {

    /**
     * 上传头像
     * @param file 文件
     * @param userId 用户ID
     */
    @PostMapping(value = "/avatar/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    Result<String> uploadAvatar(
            @RequestPart("file") MultipartFile file,
            @RequestParam("userId") Long userId
    );
}

