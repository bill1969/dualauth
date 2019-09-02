package com.viewstar.dualauth.config;

import java.lang.annotation.*;

/**
 * @author Administrator
 *
 */
@Target({
        ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TargetDateSource {
    String dataSource() default "";// 数据源
}
