package com.icoder0.websocket.core.utils;

import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.util.TypeUtils;
import lombok.experimental.UtilityClass;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.SimpleEvaluationContext;

/**
 * @author bofa1ex
 * @since 2020/8/3
 */
@UtilityClass
public class SpelUtils {

    private final SpelExpressionParser parse = new SpelExpressionParser();

    public SpelContextHolder builder() {
        return new SpelContextHolder();
    }

    public class SpelContextHolder {

        private EvaluationContext context;

        private Object result;

        public SpelContextHolder context(String name, Object variable) {
            this.context = SimpleEvaluationContext.forReadOnlyDataBinding().build();
            context.setVariable(name, variable);
            return this;
        }

        public SpelContextHolder expr(String expr) {
            this.result = parse.parseExpression(expr).getValue(this.context);
            return this;
        }

        public Object getResult() {
            return this.result;
        }

        public String getStringResult(){
            return TypeUtils.castToString(this.result);
        }

        public Boolean getBooleanResult(){
            return TypeUtils.castToBoolean(this.result);
        }
    }
}
