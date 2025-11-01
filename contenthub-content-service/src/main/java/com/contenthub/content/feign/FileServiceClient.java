package com.contenthub.content.feign;

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
     * 上传文件
     * @param file 文件
     * @param directory 目录
     * @return 文件URL
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    Result<String> uploadFile(
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "directory", defaultValue = "articles") String directory
    );
}

