package com.icoder0.websocket.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * @author bofa1ex
 * @since 2020/8/26
 *
 * @apiNote 映射业务参数params字段内部的字段名, 不支持映射多层嵌套字段.
 * example:
 * {
 *     "seq":0,
 *     "params":{
 *         "xxx":{
 *             "yyy":{
 *                 "seq":0
 *             }
 *         }
 *     }
 * }
 *
 * public void methodA(@WebsocketPayload Long seq){...}
 * 这里声明的seq入参是拿不到"params.xxx.yyy.seq"的值的, 仅支持params字段下的当前层级字段.
 *
 * @apiNote 阐述为什么不支持多层嵌套字段的提取, 因为一旦payload过于复杂, 涉及多层级的嵌套搜索, 在Runtime阶段对RT的影响很大.
 *          建议用户自行设计VO对象进行相应处理.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface WebsocketPayload {

    /**
     * Alias for {@link #name}.
     */
    @AliasFor("name")
    String value() default "";

    /**
     * The name of the request parameter to bind to.
     */
    @AliasFor("value")
    String name() default "";

    boolean required() default true;

    String defaultValue() default "";
}
