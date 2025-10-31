package com.contenthub.common.constant;

/**
 * 公共常量类
 */
public class Constants {

    /**
     * JWT Token Header
     */
    public static final String TOKEN_HEADER = "Authorization";

    /**
     * JWT Token 前缀
     */
    public static final String TOKEN_PREFIX = "Bearer ";

    /**
     * 用户信息缓存Key前缀
     */
    public static final String USER_INFO_KEY = "user:info:";

    /**
     * 验证码缓存Key前缀
     */
    public static final String CAPTCHA_KEY = "captcha:";

    /**
     * 默认页码
     */
    public static final Integer DEFAULT_PAGE = 1;

    /**
     * 默认每页大小
     */
    public static final Integer DEFAULT_PAGE_SIZE = 10;
}

