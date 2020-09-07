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

@WebsocketMapping("/api/mirai")
public class WsBootStrap{
    @WebsocketMethodMapping("#inbound.code == 1001")
    public WsOutboundBean<?> login(WebSocketSession webSocketSession, @Validated WsLoginVO req) {
        log.info("login {}", req);
        webSocketSession.getAttributes().put("account", req.getAccount());
        return WsOutboundBean.ok().body(ImmutableMap.of(
                "hello", "world"
        ));
    }
    // 返参会自动装配成下行TextMessage类型并下发.
}
```

配置清单application.yml

```yml
websocket-plus:
  asyncSendTimeout: 8000
  maxSessionIdleTimeout: 66000
  maxTextMessageBufferSize: 20480
  maxBinaryMessageBufferSize: 20480
  inboundBeanClazz: com.icoder0.websocket.core.model.WsInboundBean
  outboundBeanClazz: com.icoder0.websocket.core.model.WsOutboundBean
  inboundSpecification: {seq:0, code:xxx, version:0, params:{}}
  outboundSpecification: {seq:0, code:xxx, message:{"this is message"}, content:{}}
  payloadParamsDecodeName: params
  payloadSequenceDecodeName: sequence
  payloadFunctionCodeDecodeName: code
  spelVariableName: inbound
  origins:
    - http://test.domain.com
```

如有需求提issue.
