package com.aihuishou.lhs.afl.doc;

import com.aihuishou.lhs.afl.doc.annotation.Biz;
import com.aihuishou.lhs.afl.doc.annotation.EnableAflDocScan;
import com.aihuishou.lhs.afl.doc.annotation.Scene;
import com.aihuishou.lhs.afl.doc.filter.MethodAnnotationTypeFilter;
import com.aihuishou.lhs.afl.doc.modules.BizModule;
import com.aihuishou.lhs.afl.doc.modules.SceneModule;
import com.aihuishou.lhs.afl.doc.util.MdKiller;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.context.annotation.ScannedGenericBeanDefinition;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.MethodMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

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

        List<BizModule> bizModules = treefyBizModules(beanDefinitions);

        try {
            markdown(bizModules);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private List<BizModule> treefyBizModules(Set<BeanDefinition> beanDefinitions) {
        List<BizModule> bizModules = beanDefinitions.stream().filter(this::bizAnnotated).map(this::convert2BizModule).collect(Collectors.toList());
        List<SceneModule> sceneModules = convert2SceneModule(beanDefinitions.stream().filter(this::sceneAnnotated).collect(Collectors.toSet()));

        Map<String, List<SceneModule>> sceneMapping = sceneModules.stream().collect(Collectors.groupingBy(SceneModule::getSubject));
        bizModules.forEach(bizModule -> {
            bizModule.setSceneModules(sceneMapping.getOrDefault(bizModule.getSubject(), Collections.emptyList()));
        });
        return bizModules;
    }

    private boolean bizAnnotated(BeanDefinition bdf) {
        ScannedGenericBeanDefinition sbdf = (ScannedGenericBeanDefinition) bdf;
        return sbdf.getMetadata().hasAnnotation(Biz.class.getName());
    }

    private BizModule convert2BizModule(BeanDefinition bdf) {
        ScannedGenericBeanDefinition sbdf = (ScannedGenericBeanDefinition) bdf;
        Map<String, Object> annotationAttrs = sbdf.getMetadata().getAnnotationAttributes(Biz.class.getName());
        return BizModule.builder()
                .subject(bdf.getBeanClassName())
                .subjectName((String) annotationAttrs.get("subject"))
                .desc((String) annotationAttrs.get("desc"))
                .note((String) annotationAttrs.get("note"))
                .url((String) annotationAttrs.get("url"))
                .build();
    }

    private boolean sceneAnnotated(BeanDefinition bdf) {
        ScannedGenericBeanDefinition sbdf = (ScannedGenericBeanDefinition) bdf;
        return sbdf.getMetadata().hasAnnotatedMethods(Scene.class.getName());
    }

    private List<SceneModule> convert2SceneModule(Set<BeanDefinition> beanDefinitions) {

        List<SceneModule> sceneModules = new ArrayList<>();

        beanDefinitions.forEach(bd -> {
            ScannedGenericBeanDefinition sbdf = (ScannedGenericBeanDefinition) bd;
            Set<MethodMetadata> methodMetadata = sbdf.getMetadata().getAnnotatedMethods(Scene.class.getName());

            methodMetadata.forEach(methodMeta -> {
                Map<String, Object> annotationAttrs = methodMeta.getAnnotationAttributes(Scene.class.getName());
                sceneModules.add(SceneModule.builder()
                        .subject(((Class) annotationAttrs.get("subject")).getName())
                        .caseWhen((String) annotationAttrs.get("caseW"))
                        .entryMethod(methodMeta.getDeclaringClassName() + "#" + methodMeta.getMethodName())
                        .entryPoint((String) annotationAttrs.get("entryPoint"))
                        .build());
            });
        });

        return sceneModules;

    }

    private void markdown(List<BizModule> bizModules) throws IOException {
        MdKiller.SectionBuilder bd = MdKiller.of();
        bd.bigTitle("文档");

        bd.br();

        bizModules.forEach(biz -> {

            bd.title(biz.getSubjectName());
            bd.subTitle(biz.getSubject());
            bd.subTitle(biz.getDesc());
            bd.subTitle(biz.getNote());
            bd.link("参考文档", biz.getUrl());
            biz.getSceneModules().forEach(sceneModule -> {
                MdKiller.SectionBuilder sectionBuilder = bd.ul();
                sectionBuilder.subTitle("场景：" + sceneModule.getEntryPoint());
                sectionBuilder.ul().text("场景前提：" + sceneModule.getCaseWhen());
                sectionBuilder.ul().text("入口方法：" + sceneModule.getEntryMethod());
                bd.endUl();
            });
        });

        String markdown = bd.build();

        FileWriter writer = new FileWriter("README.md");
        writer.write(markdown);
        writer.flush();
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
