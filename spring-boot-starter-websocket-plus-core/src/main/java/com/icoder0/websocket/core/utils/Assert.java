package com.icoder0.websocket.core.utils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;


/**
 * @author bofa1ex
 * @since 2020/8/19
 */
@Slf4j
@UtilityClass
public class Assert {

    public void checkCondition(Boolean condition, Action action){
        if (!condition){
            log.warn("wrong condition trigger the callback");
            action.act();
        }
    }

    public void checkXorCondition(Boolean condition, Action action){
        if (condition){
            log.warn("wrong condition trigger the callback");
            action.act();
        }
    }

    @FunctionalInterface
    public interface Action{
        void act();
    }
}
