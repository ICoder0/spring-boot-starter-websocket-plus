package com.icoder0.websocket.annotation;


import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * @author bofa1ex
 * @since 2020/8/26
 * {
 *     "seq":0,
 *     "code":1001,
 *     "params":{
 *         "params":{
 *         },
 *         "xxx":xxx
 *     }
 * }
 * 考虑到业务参数内部存在与上行数据格式业务参数字段名冲突的情况.
 * 如果params存在业务参数冲突字段名, 需要在入参显式声明@WebsocketPayload, 否则编译期间会抛出异常.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface WebsocketHeader {

    boolean isSequence() default false;

    boolean isFunctionCode() default false;
}
