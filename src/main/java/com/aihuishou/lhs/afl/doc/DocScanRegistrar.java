package com.aihuishou.lhs.afl.doc;

import com.aihuishou.lhs.afl.doc.annotation.Biz;
import com.aihuishou.lhs.afl.doc.annotation.EnableAflDocScan;
import com.aihuishou.lhs.afl.doc.annotation.Scene;
import com.aihuishou.lhs.afl.doc.filter.MethodAnnotationTypeFilter;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 文档扫描注册器
 *
 * @author eddieYang
 * @date 2023/4/26
 * @since
 */
public class DocScanRegistrar implements ImportBeanDefinitionRegistrar,
        ResourceLoaderAware, EnvironmentAware {

    private Environment environment;
    private ResourceLoader resourceLoader;


    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);

        scanner.setResourceLoader(resourceLoader);
        scanner.setEnvironment(environment);


        Set<String> basePackages = getBasePackages(importingClassMetadata);

        //注解过滤
        Arrays.asList(
                new AnnotationTypeFilter(Biz.class, true, true),
                new MethodAnnotationTypeFilter(Scene.class, true)
        ).forEach(scanner::addIncludeFilter);

        Set<BeanDefinition> beanDefinitions = new HashSet<>();
        for (String basePackage : basePackages) {
            beanDefinitions.addAll(scanner.findCandidateComponents(basePackage));
        }


        System.out.println(beanDefinitions);


    }

    protected Set<String> getBasePackages(AnnotationMetadata importingClassMetadata) {
        Map<String, Object> attributes = importingClassMetadata
                .getAnnotationAttributes(EnableAflDocScan.class.getCanonicalName());

        Set<String> basePackages = new HashSet<>();
        for (String pkg : (String[]) attributes.get("value")) {
            if (StringUtils.hasText(pkg)) {
                basePackages.add(pkg);
            }
        }
        for (String pkg : (String[]) attributes.get("basePackages")) {
            if (StringUtils.hasText(pkg)) {
                basePackages.add(pkg);
            }
        }
        for (Class<?> clazz : (Class[]) attributes.get("basePackageClasses")) {
            basePackages.add(ClassUtils.getPackageName(clazz));
        }

        if (basePackages.isEmpty()) {
            basePackages.add(
                    ClassUtils.getPackageName(importingClassMetadata.getClassName()));
        }
        return basePackages;
    }


    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }


}
