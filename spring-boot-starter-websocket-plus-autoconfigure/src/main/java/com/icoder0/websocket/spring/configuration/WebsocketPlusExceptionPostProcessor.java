package com.icoder0.websocket.spring.configuration;

import com.icoder0.websocket.annotation.WebsocketAdvice;
import com.icoder0.websocket.annotation.WebsocketExceptionHandler;
import com.icoder0.websocket.spring.handler.model.WsExceptionHandlerMethodMetadata;
import com.icoder0.websocket.spring.handler.model.WsMappingHandlerMetadata;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author bofa1ex
 * @since 2020/8/19
 */
@Slf4j
public class WebsocketPlusExceptionPostProcessor implements ApplicationContextAware, BeanPostProcessor, ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    private WebsocketProcessorAttributes websocketProcessorAttributes;

    private final Map<String[], List<WsExceptionHandlerMethodMetadata>> exceptionHandlerMethodMetadataMap = new HashMap<>(2 >> 4);

    @Override
    public Object postProcessAfterInitialization(@NotNull Object bean, @NotNull String beanName) throws BeansException {
        final Class<?> beanClazz = AopProxyUtils.ultimateTargetClass(bean);
        final WebsocketAdvice websocketAdvice = AnnotationUtils.findAnnotation(bean.getClass(), WebsocketAdvice.class);
        if (Objects.isNull(websocketAdvice)) {
            return bean;
        }
        final int advicePriority = websocketAdvice.priority();
        final List<WsExceptionHandlerMethodMetadata> wsExceptionHandlerMethodMetadataList = MethodIntrospector.selectMethods(beanClazz,
                (ReflectionUtils.MethodFilter) method -> Objects.nonNull(AnnotationUtils.findAnnotation(method, WebsocketExceptionHandler.class))).parallelStream()
                .flatMap(method -> {
                    final WebsocketExceptionHandler websocketExceptionHandler = AnnotationUtils.getAnnotation(method, WebsocketExceptionHandler.class);
                    final int websocketExceptionHandlerPriority = websocketExceptionHandler.priority();
                    return Arrays.stream(websocketExceptionHandler.value()).parallel()
                            .map(exception -> WsExceptionHandlerMethodMetadata.builder()
                                    // compare and retain the lowest priority.
                                    .priority(Math.min(websocketExceptionHandlerPriority, advicePriority))
                                    .value(exception)
                                    .bean(bean)
                                    .method(method)
                                    .build()
                            );
                }).collect(Collectors.toList());
        exceptionHandlerMethodMetadataMap.put(websocketAdvice.basePackages(), wsExceptionHandlerMethodMetadataList);
        return bean;
    }

    @Override
    public void setApplicationContext(@NotNull ApplicationContext applicationContext) throws BeansException {
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        websocketProcessorAttributes.getMappingHandlerMethodMetadataMap().values().parallelStream()
                .map(WsMappingHandlerMetadata::getArchetypeHandler)
                .forEach(websocketArchetypeHandler -> exceptionHandlerMethodMetadataMap.entrySet().parallelStream()
                        .filter(entry -> Arrays.stream(entry.getKey()).anyMatch(s -> websocketArchetypeHandler.getLocation().startsWith(s)))
                        .map(Map.Entry::getValue)
                        .forEach(websocketArchetypeHandler::addExceptionMethodMetadataList));
    }
}
