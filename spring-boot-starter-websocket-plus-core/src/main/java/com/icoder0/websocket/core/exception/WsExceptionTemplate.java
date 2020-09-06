package com.icoder0.websocket.core.exception;

/**
 * @author bofa1ex
 * @since 2020/9/3
 */
public class WsExceptionTemplate {

    public static final String MAPPING_ALREADY_REGISTER;

    public static final String METHOD_PARAMETER_NAME_PRESENT;
    public static final String METHOD_MAPPING_EXPRESSION_CONFLICT;
    public static final String METHOD_MAPPING_NONE_MATCH;

    public static final String REQUEST_PARAMETER_NONE_MATCH;
    public static final String REQUEST_PARAMETER_INBOUND_SPECIFICATION_ERROR;
    public static final String REQUEST_PARAMETER_HEADER_PARAMS_SPECIFICATION_ERROR;
    public static final String REQUEST_PARAMETER_HEADER_SEQUENCE_SPECIFICATION_ERROR;
    public static final String REQUEST_PARAMETER_HEADER_FUNCTION_CODE_SPECIFICATION_ERROR;

    public static final String CONSTRAINT_VIOLATION_VALIDATE_ERROR;

    static {
        MAPPING_ALREADY_REGISTER                                    =       "%s类已注册#WEBSOCKET路由: %s\n" +
                                                                            "检查 %s类下所修饰的@WebsocketMapping#mapping";



        METHOD_PARAMETER_NAME_PRESENT                               =       "获取不到方法参数的真实参数名, 需要指定compiler参数 -parameters\n" +
                                                                            "参见spring-boot-starter-websocket-plus-example模块的pom.xml\n" +
                                                                            "\t<plugin>\n" +
                                                                            "\t\t<groupId>org.apache.maven.plugins</groupId>\n" +
                                                                            "\t\t<artifactId>maven-compiler-plugin</artifactId>\n" +
                                                                            "\t\t\t<configuration>\n" +
                                                                            "\t\t\t\t<compilerArgs>\n" +
                                                                            "\t\t\t\t\t<arg>-parameters</arg>\n" +
                                                                            "\t\t\t\t</compilerArgs>\n" +
                                                                            "\t\t\t</configuration>\n" +
                                                                            "\t</plugin>";

        METHOD_MAPPING_EXPRESSION_CONFLICT                          =       "%s @WebsocketMethodMapping#expression %s冲突\n" +
                                                                            "冲突对象 %s";

        METHOD_MAPPING_NONE_MATCH                                   =       "%s 未匹配到WebsocketMethodMapping路由";

        REQUEST_PARAMETER_NONE_MATCH                                =       "%s #%s is required";

        CONSTRAINT_VIOLATION_VALIDATE_ERROR                         =       "#%1s IS NOT VALID\n" +
                                                                            "#REQUIRED {%2s %1s}";

        REQUEST_PARAMETER_INBOUND_SPECIFICATION_ERROR               =       "检查inbound规范 %s";

        REQUEST_PARAMETER_HEADER_PARAMS_SPECIFICATION_ERROR         =       "检查header#业务参数变量名规范 %s";

        REQUEST_PARAMETER_HEADER_SEQUENCE_SPECIFICATION_ERROR       =       "检查header#消息序号变量名规范 %s";

        REQUEST_PARAMETER_HEADER_FUNCTION_CODE_SPECIFICATION_ERROR  =       "检查header#函数枚举变量名规范 %s";
    }
}
