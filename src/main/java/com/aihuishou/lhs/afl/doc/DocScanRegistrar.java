package com.aihuishou.lhs.afl.doc;

import com.aihuishou.lhs.afl.doc.annotation.Biz;
import com.aihuishou.lhs.afl.doc.annotation.EnableAflDocScan;
import com.aihuishou.lhs.afl.doc.annotation.Scene;
import com.aihuishou.lhs.afl.doc.filter.MethodAnnotationTypeFilter;
import com.aihuishou.lhs.afl.doc.modules.BizModule;
import com.aihuishou.lhs.afl.doc.modules.SceneModule;
import com.aihuishou.lhs.afl.doc.util.MdKiller;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.context.annotation.ScannedGenericBeanDefinition;
import org.springframework.core.annotation.AnnotationAttributes;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(DocScanRegistrar.class);

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
            LOGGER.error("afl doc generate occur an error :{}", e.getMessage(), e);
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
                        .subject(((Class<?>) annotationAttrs.get("subject")).getName())
                        .caseWhen((String) annotationAttrs.get("caseWhen"))
                        .entryMethod(ClassUtils.getShortName(methodMeta.getDeclaringClassName()) + "#" + ClassUtils.getShortName(methodMeta.getReturnTypeName()) + " " + methodMeta.getMethodName())
                        .entryPoint((String) annotationAttrs.get("entryPoint"))
                        .links(buildLinks((AnnotationAttributes[]) annotationAttrs.get("links")))
                        .build());
            });
        });

        return sceneModules;
    }

    private List<SceneModule.SceneLink> buildLinks(AnnotationAttributes[] links) {

        if (links == null || links.length == 0) {
            return Collections.emptyList();
        }
        List<SceneModule.SceneLink> sceneLinks = new ArrayList<>();
        for (AnnotationAttributes link : links) {
            SceneModule.SceneLink sceneLink = SceneModule.SceneLink.builder()
                    .name(String.valueOf(link.get("name")))
                    .link(String.valueOf(link.get("url")))
                    .build();

            sceneLinks.add(sceneLink);
        }

        return sceneLinks;
    }

    private void markdown(List<BizModule> bizModules) throws IOException {
        MdKiller.SectionBuilder bd = MdKiller.of();
        bd.bigTitle("文档");

        bd.br();

        bizModules.forEach(biz -> {
            bd.title(biz.getSubjectName());
            bd.subTitle("主体：" + biz.getSubject());
            bd.subTitle("描述：" + biz.getDesc());
            bd.subTitle("备注：" + biz.getNote());
            bd.link("参考文档", biz.getUrl());
            biz.getSceneModules().forEach(sceneModule -> {
                MdKiller.SectionBuilder sectionBuilder = bd.ul();
                sectionBuilder.subTitle("场景：" + sceneModule.getEntryPoint());
                sectionBuilder.ul().text("场景前提：" + sceneModule.getCaseWhen());
                sectionBuilder.ul().text("入口方法：" + sceneModule.getEntryMethod());
                if (CollectionUtils.isNotEmpty(sceneModule.getLinks())) {
                    for (SceneModule.SceneLink link : sceneModule.getLinks()) {
                        sectionBuilder.ul().text("外部链接：");
                        sectionBuilder.ul().ul().link(link.getName(), link.getLink());
                    }
                }

                sectionBuilder.endUl();
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
        if (MapUtils.isEmpty(attributes)) {
            return Collections.emptySet();
        }
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
        for (Class<?> clazz : (Class<?>[]) attributes.get("basePackageClasses")) {
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
