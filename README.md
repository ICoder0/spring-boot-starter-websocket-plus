# spring-boot-starter-websocket-plus
基于spring-websocket-starter依赖, 提供mvc开发风格

示例参见example模块即可.
```xml
#Maven#
<dependencies>
    <dependency>
        <groupId>com.icoder0</groupId>
        <artifactId>spring-boot-starter-websocket-plus</artifactId>
        <version>${latest.version}</version>
    </dependency>
</dependencies>
```

```java
启动类声明 @EnableWebsocketPlus即可.

@WebsocketMapping(value = "/api/xxxx", prototype = true)
public class WsSecurityController{
    // 入参类型@WebsocketHeader可获取header字段(sequence,version,functionCode,etc)
    // 比如 @WebsocketHeader(isSequence = true) Long seq.
    // @WebsocketHeader(isFunctionCode = true) Integer code.
    // @WebsocketHeader("version") Integer version.
    // 入参类型@WebsocketPayload可获取params业务参数内部的参数值.
    // 比如 @WebsocketPayload("account") String account.
    @WebsocketMethodMapping("#inbound.code == 1001")
    public WsOutboundBean<?> login(WebSocketSession session, 
                                   @WebsocketHeader("version") Integer version,
                                   @WebsocketHeader(isSequence = true) Long sequence,
                                   @Validated WsLoginVO req
    ) {
        log.info("login {}", req);
        session.getAttributes().put("account", req.getAccount());
        return WsOutboundBean.ok().body(ImmutableMap.of(
                "hello", "world"
        ));
    }
    // 返参必须是WsOutboundBeanSpecification类型或者Void类型.
    // 如果是WsOutboundBeanSpecification类型, 会自动下发数据.
}

@WebsocketMapping(value = "/api/xxxx", prototype = true)
public class WsBusinessController{
    @WebsocketMethodMapping("#inbound.code == 2001")
    public WsOutboundBean<?> login2(WebSocketSession session, 
                                   @WebsocketHeader("version") Integer version,
                                   @WebsocketHeader(isSequence = true) Long sequence,
                                   @Validated WsLoginVO req
    ) {
        log.info("login2 {}", req);
        session.getAttributes().put("account", req.getAccount());
        return WsOutboundBean.ok().body(ImmutableMap.of(
                "hello", "world"
        ));
    }
    // 返参必须是WsOutboundBeanSpecification类型或者Void类型.
    // 如果是WsOutboundBeanSpecification类型, 会自动下发数据.
}
```

配置清单application.yml

```yml
websocket-plus:
  asyncSendTimeout: 8000
  maxSessionIdleTimeout: 66000
  maxTextMessageBufferSize: 20480
  maxBinaryMessageBufferSize: 20480
  # 上行数据规范WsInboundBeanSpecification接口
  inboundBeanClazz: com.icoder0.websocket.core.model.WsInboundBean
  # 下行数据规范WsOutboundBeanSpecification接口
  outboundBeanClazz: com.icoder0.websocket.core.model.WsOutboundBean
  # 上行数据规范信息
  inboundSpecification: {seq:0, code:xxx, version:0, params:{}}
  # 下行数据规范信息
  outboundSpecification: {seq:0, code:xxx, message:{"this is message"}, content:{}}
  # 上行数据header字段#params业务参数
  payloadParamsDecodeName: params
  # 上行数据header字段#sequence消息序号
  payloadSequenceDecodeName: sequence
  # 上行数据header字段#functionCode函数枚举
  payloadFunctionCodeDecodeName: code
  spelVariableName: inbound
  origins:
    - http://test.domain.com
```

文档参见Wiki https://github.com/ICoder0/spring-boot-starter-websocket-plus.wiki.git

如有需求提issue.
