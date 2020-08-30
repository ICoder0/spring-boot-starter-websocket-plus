package com.icoder0.websocket.core.utils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.function.Supplier;


/**
 * @author bofa1ex
 * @since 2020/8/19
 */
@Slf4j
@UtilityClass
public class Assert {

    public void notNull(Object data, String message){
        if (Objects.isNull(data)){
            throw new IllegalArgumentException(message);
        }
    }

    public <X extends Throwable> void checkCondition(Boolean condition, Supplier<? extends X> supplier) throws X {
        if (!condition) {
            throw supplier.get();
        }
    }

    public <X extends Throwable> void checkXorCondition(Boolean condition, Supplier<? extends X> supplier) throws X {
        if (condition) {
            throw supplier.get();
        }
    }

    public void checkCondition(Boolean condition, Action action) {
        if (!condition) {
            action.act();
        }
    }

    public void checkXorCondition(Boolean condition, Action action) {
        if (condition) {
            action.act();
        }
    }

    @FunctionalInterface
    public interface Action {
        void act();
    }
}
