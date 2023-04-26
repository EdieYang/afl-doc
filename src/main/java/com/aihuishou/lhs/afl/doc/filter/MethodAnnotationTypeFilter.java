package com.aihuishou.lhs.afl.doc.filter;

import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.filter.AbstractTypeHierarchyTraversingFilter;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;

import java.lang.annotation.Annotation;
import java.lang.annotation.Inherited;

/**
 * 对于指定注解，匹配注解所在方法的类，满足继承层次关系
 *
 * @author eddieYang
 * @date 2023/4/26
 * @since
 */
public class MethodAnnotationTypeFilter extends AbstractTypeHierarchyTraversingFilter {

    private final Class<? extends Annotation> annotationType;


    public MethodAnnotationTypeFilter(Class<? extends Annotation> annotationType) {
        this(annotationType, false);
    }

    public MethodAnnotationTypeFilter(Class<? extends Annotation> annotationType, boolean considerInterfaces) {
        super(annotationType.isAnnotationPresent(Inherited.class), considerInterfaces);
        this.annotationType = annotationType;
    }

    public final Class<? extends Annotation> getAnnotationType() {
        return this.annotationType;
    }

    @Override
    protected boolean matchSelf(MetadataReader metadataReader) {
        AnnotationMetadata metadata = metadataReader.getAnnotationMetadata();
        return metadata.hasAnnotatedMethods(this.annotationType.getName());
    }

    @Override
    @Nullable
    protected Boolean matchSuperClass(String superClassName) {
        return hasAnnotation(superClassName);
    }

    @Override
    @Nullable
    protected Boolean matchInterface(String interfaceName) {
        return hasAnnotation(interfaceName);
    }

    @Nullable
    protected Boolean hasAnnotation(String typeName) {
        if (Object.class.getName().equals(typeName)) {
            return false;
        } else if (typeName.startsWith("java")) {
            if (!this.annotationType.getName().startsWith("java")) {
                // Standard Java types do not have non-standard annotations on them ->
                // skip any load attempt, in particular for Java language interfaces.
                return false;
            }
            try {
                Class<?> clazz = ClassUtils.forName(typeName, getClass().getClassLoader());
                return clazz.getAnnotation(this.annotationType) != null;
            } catch (Throwable ex) {
                // Class not regularly loadable - can't determine a match that way.
            }
        }
        return null;
    }


}
