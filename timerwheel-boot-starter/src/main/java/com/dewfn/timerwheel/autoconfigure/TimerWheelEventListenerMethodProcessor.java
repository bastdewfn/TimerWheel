package com.dewfn.timerwheel.autoconfigure;

import com.dewfn.timerwheel.TimerTaskConsumerManager;
import com.dewfn.timerwheel.TimerWheel;
import com.dewfn.timerwheel.autoconfigure.annotation.TimerTaskCall;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.autoproxy.AutoProxyUtils;
import org.springframework.aop.scope.ScopedObject;
import org.springframework.aop.scope.ScopedProxyUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
@Configuration
public class TimerWheelEventListenerMethodProcessor implements SmartInitializingSingleton, ApplicationContextAware, BeanFactoryPostProcessor {
    private Logger logger = LoggerFactory.getLogger(TimerWheelEventListenerMethodProcessor.class);
    private final Set<Class<?>> nonAnnotatedClasses = Collections.newSetFromMap(new ConcurrentHashMap<>(64));

    ConfigurableListableBeanFactory beanFactory;
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {
        beanFactory=configurableListableBeanFactory;
    }
    private ApplicationContext applicationContext;
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext=applicationContext;
    }
    @Override
    public void afterSingletonsInstantiated() {
        ConfigurableListableBeanFactory beanFactory = this.beanFactory;
        Assert.state(this.beanFactory != null, "No ConfigurableListableBeanFactory set");
        String[] beanNames = beanFactory.getBeanNamesForType(Object.class);
        for (String beanName : beanNames) {
            if (!ScopedProxyUtils.isScopedTarget(beanName)) {
                Class<?> type = null;
                try {
                    type = AutoProxyUtils.determineTargetClass(beanFactory, beanName);
                }
                catch (Throwable ex) {
                    // An unresolvable bean type, probably from a lazy bean - let's ignore it.
                    if (logger.isDebugEnabled()) {
                        logger.debug("Could not resolve target class for bean with name '" + beanName + "'", ex);
                    }
                }
                if (type != null) {
                    if (ScopedObject.class.isAssignableFrom(type)) {
                        try {
                            Class<?> targetClass = AutoProxyUtils.determineTargetClass(
                                    beanFactory, ScopedProxyUtils.getTargetBeanName(beanName));
                            if (targetClass != null) {
                                type = targetClass;
                            }
                        }
                        catch (Throwable ex) {
                            // An invalid scoped proxy arrangement - let's ignore it.
                            if (logger.isDebugEnabled()) {
                                logger.debug("Could not resolve target bean for scoped proxy '" + beanName + "'", ex);
                            }
                        }
                    }
                    try {
                        processBean(beanName, type);
                    }
                    catch (Throwable ex) {
                        throw new BeanInitializationException("Failed to process @TimerTaskCall " +
                                "annotation on bean with name '" + beanName + "'", ex);
                    }
                }
            }
        }
    }

    private void processBean(final String beanName, final Class<?> targetType) {
        if (!this.nonAnnotatedClasses.contains(targetType) &&
                MyAnnotationUtils.isCandidateClass(targetType, TimerTaskCall.class) &&
                !isSpringContainerClass(targetType)) {

            Map<Method, TimerTaskCall> annotatedMethods = null;
            try {
                annotatedMethods = MethodIntrospector.selectMethods(targetType,
                        (MethodIntrospector.MetadataLookup<TimerTaskCall>) method ->
                                AnnotatedElementUtils.findMergedAnnotation(method, TimerTaskCall.class));
            }
            catch (Throwable ex) {
                // An unresolvable type in a method signature, probably from a lazy bean - let's ignore it.
                if (logger.isDebugEnabled()) {
                    logger.debug("Could not resolve methods for bean with name '" + beanName + "'", ex);
                }
            }

            if (CollectionUtils.isEmpty(annotatedMethods)) {
                this.nonAnnotatedClasses.add(targetType);
                if (logger.isTraceEnabled()) {
                    logger.trace("No @TimerTaskCall annotations found on bean class: " + targetType.getName());
                }
            }
            else {
                // Non-empty set of methods
                for (Method method : annotatedMethods.keySet()) {
                    Method methodToUse = AopUtils.selectInvocableMethod(method, applicationContext.getType(beanName));
                    Class[] timerTaskCallAnnotatedClassArray= annotatedMethods.get(method).classes();
                    if(timerTaskCallAnnotatedClassArray!=null&&timerTaskCallAnnotatedClassArray.length>0){
                        for(Class timerTaskCallAnnotatedClass: timerTaskCallAnnotatedClassArray){
                            TimerTaskConsumerManager.addConsumerTimerTaskList(methodToUse,timerTaskCallAnnotatedClass,getTargetBean(beanName));
                        }
                    }else{
                        if(methodToUse.getParameterTypes().length==0){
                            throw new BeanInitializationException("Failed to addConsumerTimerTaskList method " +
                                    "'" + method.getName() + "'");
                        }
                        TimerTaskConsumerManager.addConsumerTimerTaskList(methodToUse,methodToUse.getParameterTypes()[0],getTargetBean(beanName));
                    }

                }
                if (logger.isDebugEnabled()) {
                    logger.debug(annotatedMethods.size() + " @TimerTaskCall methods processed on bean '" +
                            beanName + "': " + annotatedMethods);
                }
            }
        }
    }
    protected Object getTargetBean(String beanName) {
        Assert.notNull(this.applicationContext, "ApplicationContext must no be null");
        return this.applicationContext.getBean(beanName);
    }

    private static boolean isSpringContainerClass(Class<?> clazz) {
        return (clazz.getName().startsWith("org.springframework.") &&
                !AnnotatedElementUtils.isAnnotated(ClassUtils.getUserClass(clazz), Component.class));
    }

}
