package com.aihuishou.lhs.afl.doc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 业务场景
 *
 * @author eddieYang
 * @date 2023/4/20
 * @since
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Scene {

    /**
     * 业务场景关联主体
     *
     * @return
     */
    Class subject();

    /**
     * 场景前提
     */
    String caseW() default "";

    /**
     * 入口说明
     *
     * @return
     */
    String entryPoint();
}
