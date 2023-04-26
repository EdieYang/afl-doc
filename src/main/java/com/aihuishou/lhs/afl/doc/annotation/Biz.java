package com.aihuishou.lhs.afl.doc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 对应主体名词解释的业务说明
 *
 * @author eddieYang
 * @date 2023/4/20
 * @since
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Biz {

    /**
     * 名词主体
     *
     * @return
     */
    String subject();

    /**
     * 名词描述
     *
     * @return
     */
    String desc();

    /**
     * 外部说明链接
     *
     * @return
     */
    String url() default "";

    /**
     * 备注
     *
     * @return
     */
    String note() default "";

}
