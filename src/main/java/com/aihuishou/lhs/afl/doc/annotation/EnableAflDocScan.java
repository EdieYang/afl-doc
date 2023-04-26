package com.aihuishou.lhs.afl.doc.annotation;

import com.aihuishou.lhs.afl.doc.DocScanRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 启用爱分类文档扫描
 *
 * @author eddieYang
 * @date 2023/4/26
 * @since
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(DocScanRegistrar.class)
public @interface EnableAflDocScan {


    String[] value() default {};

    /**
     * 注解扫描包路径
     * @return
     */
    String[] basePackages() default {};

    /**
     * 注解扫描类所在包路径
     * @return
     */
    Class<?>[] basePackageClasses() default {};
}
