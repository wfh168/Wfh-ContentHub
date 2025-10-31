package com.contenthub.file.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.contenthub.file.domain.FileInfo;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;

/**
 * 文件Mapper
 */
public interface FileMapper extends BaseMapper<FileInfo> {

    /**
     * 物理删除文件记录（绕过逻辑删除）
     * @param id 文件ID
     */
    @Delete("DELETE FROM files WHERE id = #{id}")
    void physicalDeleteById(@Param("id") Long id);
}

