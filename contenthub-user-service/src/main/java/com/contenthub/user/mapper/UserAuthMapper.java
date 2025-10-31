package com.contenthub.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.contenthub.user.domain.UserAuth;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户认证Mapper接口
 */
@Mapper
public interface UserAuthMapper extends BaseMapper<UserAuth> {
}
