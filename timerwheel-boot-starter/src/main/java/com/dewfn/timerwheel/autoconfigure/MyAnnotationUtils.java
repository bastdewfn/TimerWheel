package com.dewfn.timerwheel.autoconfigure;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.annotation.Annotation;

public class MyAnnotationUtils extends AnnotationUtils {
    public static boolean isCandidateClass(Class<?> clazz, Class<? extends Annotation> annotationType) {
        if (annotationType.getName().startsWith("java.")) {
            return true;
        }
        return !(clazz.getName().startsWith("java.") || clazz == Ordered.class);

    }
}
