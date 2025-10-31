package com.contenthub.common.annotation;

import java.lang.annotation.*;

/**
 * 不需要认证的注解
 * 标注在Controller方法上，表示该方法不需要JWT认证
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NoAuth {
}

