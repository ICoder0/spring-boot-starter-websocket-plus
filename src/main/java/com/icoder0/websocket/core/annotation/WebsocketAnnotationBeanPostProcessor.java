package com.icoder0.websocket.core.annotation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.Validator;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.socket.config.annotation.WebSocketConfigurationSupport;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistration;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author bofa1ex
 * @since 2020/8/1
 */
@Slf4j
public class WebsocketAnnotationBeanPostProcessor extends WebSocketConfigurationSupport implements BeanPostProcessor, ApplicationContextAware, WebSocketConfigurer, ApplicationListener<ContextRefreshedEvent> {

    private final Map<String[], WebsocketDelegatorHandler> delegatorHandlerMap = new HashMap<>();

    private final Map<Object, WebsocketAdvice> websocketAdviceMap = new HashMap<>(2 >> 5);

    private final WebsocketPlusProperties websocketPlusProperties;

    private final HandshakeInterceptor[] handlerInterceptors;

    private Validator validator;

    private ApplicationContext context;

    public WebsocketAnnotationBeanPostProcessor(WebsocketPlusProperties websocketPlusProperties, HandshakeInterceptor... handlerInterceptors) {
        this.websocketPlusProperties = websocketPlusProperties;
        this.handlerInterceptors = handlerInterceptors;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        final Class<?> targetClass = AopProxyUtils.ultimateTargetClass(bean);
        final WebsocketMapping websocketMapping = AnnotationUtils.findAnnotation(targetClass, WebsocketMapping.class);
        final WebsocketAdvice websocketAdvice = AnnotationUtils.findAnnotation(targetClass, WebsocketAdvice.class);
        if (Objects.nonNull(websocketMapping)) {
            final List<WebsocketDelegatorHandler.WsMappingHandlerMethodMetadata> wsMappingHandlerMethodMetadataList = MethodIntrospector
                    .selectMethods(targetClass, (ReflectionUtils.MethodFilter) method ->
                            Objects.nonNull(AnnotationUtils.findAnnotation(method, WebsocketMethodMapping.class))
                    ).parallelStream().map(method -> {
                        final WebsocketMethodMapping websocketMethodMapping = _checkWebsocketMethodMapping(method);
                        return WebsocketDelegatorHandler.WsMappingHandlerMethodMetadata.builder()
                                .value(websocketMethodMapping.expr())
                                .bean(bean)
                                .method(method)
                                .build();
                    }).collect(Collectors.toList());
            delegatorHandlerMap.put(websocketMapping.value(), WebsocketDelegatorHandler.builder()
                    .validator(getValidator())
                    .routeMapping(websocketMapping.value())
                    .location(targetClass.getPackage().getName())
                    .websocketPlusProperties(websocketPlusProperties)
                    .mappingMethodMetadataList(wsMappingHandlerMethodMetadataList)
                    .build()
            );
        }
        if (Objects.nonNull(websocketAdvice)) {
            // 滞后执行, @see #onApplicationEvent
            websocketAdviceMap.put(bean, websocketAdvice);
        }
        return bean;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        final List<WebSocketConfigurer> configurers = delegatorHandlerMap.entrySet().parallelStream()
                .map(entry -> (WebSocketConfigurer) _registry -> {
                    final WebSocketHandlerRegistration webSocketHandlerRegistration = _registry.addHandler(entry.getValue(), entry.getKey());
                    if (websocketPlusProperties.isWithSockJS()) {
                        webSocketHandlerRegistration.withSockJS();
                    }
                    Optional.ofNullable(this.handlerInterceptors).ifPresent(webSocketHandlerRegistration::addInterceptors);
                    Optional.ofNullable(this.websocketPlusProperties.getOrigins()).ifPresent(webSocketHandlerRegistration::setAllowedOrigins);
                })
                .collect(Collectors.toList());
        for (WebSocketConfigurer configurer : configurers) {
            configurer.registerWebSocketHandlers(registry);
        }
    }

    WebsocketMethodMapping _checkWebsocketMethodMapping(Method method) {
        final WebsocketMethodMapping websocketMethodMapping = AnnotationUtils.getAnnotation(method, WebsocketMethodMapping.class);
        if (Objects.isNull(websocketMethodMapping)) {
            log.warn("{} not found @WebsocketMapping", method.getName());
            return null;
        }
        return websocketMethodMapping;
    }

    WebsocketExceptionHandler _checkWebsocketExceptionHandler(Method method) {
        final WebsocketExceptionHandler websocketExceptionHandler = AnnotationUtils.getAnnotation(method, WebsocketExceptionHandler.class);
        if (Objects.isNull(websocketExceptionHandler)) {
            log.warn("{} not found @WebsocketExceptionHandler", method.getName());
            return null;
        }
        return websocketExceptionHandler;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        for (Map.Entry<Object, WebsocketAdvice> entry : websocketAdviceMap.entrySet()) {
            final Object bean = entry.getKey();
            final Class<?> targetClass = AopProxyUtils.ultimateTargetClass(bean);
            final WebsocketAdvice websocketAdvice = entry.getValue();
            final String[] locations = websocketAdvice.basePackages();
            final List<WebsocketDelegatorHandler.WsExceptionHandlerMethodMetadata> wsExceptionHandlerMethodMetadataList = MethodIntrospector
                    .selectMethods(targetClass, (ReflectionUtils.MethodFilter) method ->
                            Objects.nonNull(AnnotationUtils.findAnnotation(method, WebsocketExceptionHandler.class))
                    ).parallelStream().flatMap(method -> {
                        final WebsocketExceptionHandler websocketExceptionHandler = _checkWebsocketExceptionHandler(method);
                        return Arrays.stream(websocketExceptionHandler.value())
                                .map(exception -> WebsocketDelegatorHandler.WsExceptionHandlerMethodMetadata.builder()
                                        .value(exception)
                                        .bean(bean)
                                        .method(method)
                                        .build()
                                );
                    }).collect(Collectors.toList());
            delegatorHandlerMap.values().parallelStream()
                    .filter(websocketDelegatorHandler -> Arrays.stream(locations)
                            .anyMatch(location -> StringUtils.startsWithIgnoreCase(websocketDelegatorHandler.getLocation(), location)))
                    .forEach(websocketDelegatorHandler -> {
                                final List<WebsocketDelegatorHandler.WsExceptionHandlerMethodMetadata> original = Optional.ofNullable(websocketDelegatorHandler.getExceptionMethodMetadataList())
                                        .orElseGet(ArrayList::new);
                                original.addAll(wsExceptionHandlerMethodMetadataList);
                                websocketDelegatorHandler.setExceptionMethodMetadataList(original);
                            }
                    );
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        this.context = context;
    }

    public ApplicationContext getContext(){
        return this.context;
    }

    public Validator getValidator(){
        final String MVC_VALIDATOR_NAME = "mvcValidator";
        if (Objects.isNull(validator)){
            if (this.context != null && this.context.containsBean(MVC_VALIDATOR_NAME)) {
                this.validator = this.context.getBean(MVC_VALIDATOR_NAME, org.springframework.validation.Validator.class);
            }
        }
        return this.validator;
    }
}
