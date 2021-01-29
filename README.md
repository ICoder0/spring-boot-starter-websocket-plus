# spring-boot-starter-websocket-plus
基于spring-websocket-starter依赖, 提供mvc开发风格

示例参见example模块即可.
```xml
<dependencies>
    <dependency>
        <groupId>com.icoder0</groupId>
        <artifactId>spring-boot-starter-websocket-plus</artifactId>
        <version>${latest.version}</version>
    </dependency>
</dependencies>
```

```java
// 启动类声明 @EnableWebsocketPlus即可.
@EnableWebsocketPlus
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

@WebsocketMapping(value = "/api/trista", prototype = true)
@WebsocketApi("ws联系人接口")
public class WsContactController{

    /**
     * websocket上行报文: {
     *     "sequence": 0,
     *     "version": 0,
     *     "topic": 1000,
     *     "params": {
     *         "account": 123456789
     *     }
     * }
     * @param  account 提取于payload#params#account
     * @return WsOutboundBeanSpecification类型即自动下发下行报文.
     */ 
    @WebsocketOperation("获取指定账号的联系人列表")
    @WebsocketMethodMapping("#inbound.topic == 1000")
    public WsOutboundBean<?> getContacts(@Size(min = 4, max = 11, message = "长度不可小于4, 不可大于11")
                                         @NotBlank String account
    ){
        return WsOutboundBean.topic(T1000.topic).ok(ImmutableList.of(
            Contact.builder().name("张三").build(),
            Contact.builder().name("李四").build()
        ));
    }
    
    /**
     * 入参可选WebSocketSession/TextMessage/BinaryMessage/PingMessage/PongMessage
     * TextMessage对应可选上行报文中(sequence, version, topic, params或params指定内部参数)
     * BinaryMessage对应可选(io.netty.buffer.ByteBuf, java.nio.ByteBuffer, byte[])
     *
     * @param  account 提取于payload#params#account
     * @return Void跳过自动下发下行报文, 可通过WebsocketMessageEmitter#emit下发下行报文.
     */
    @WebsocketMethodMapping("#inbound.topic == 1001")
    public void getContactsV2(WebSocketSession session, 
                              @Size(min = 4, max = 11, message = "长度不可小于4, 不可大于11")
                              @NotBlank String account
    ) {
        WebsocketMessageEmitter.emit(WsOutboundBean.topic(T1001.topic).ok(ImmutableList.of(
            Contact.builder().name("张三").build(),
            Contact.builder().name("李四").build() 
        )), session);
    }
}

/**
 * 考虑到同一个路由映射到不同业务的Controller, 需要对WebsocketMapping#prototype设置为True.
 */
@WebsocketMapping(value = "/api/trista", prototype = true)
public class WsOtherBussinessController{

    /**
     * websocket上行报文: {
     *     "sequence": 0,
     *     "version": 0,
     *     "topic": 1003,
     *     "params": {
     *         "predicates": {
     *             "account": "^123.*$"
     *         },
     *         "orders": {
     *             "createTime":0,
     *             "account":1
     *         },
     *         pageNo: 1,
     *         pageSize: 5
     *     }
     * }
     * @param businessDTO 根据payload#params提取.
     * @param pageNo 根据payload#params#pageNo, 修饰于WebsocketPayload, 因此根据required和defaultValue属性, 即使字段不存在, 也会视具体情况给定值.
     * @param pageSize 根据payload#pararms#pageSize, 修饰于WebsocketPayload, 因此根据required和defaultValue属性, 即使字段不存在, 也会视具体情况给定值.
     */
    @WebsocketOperation("获取业务数据列表(分页)")
    @WebsocketMethodMapping("#inbound.topic == 1003")
    public WsOutboundBean<?> getList(WebSocketSession session,
                                     BusinessDTO businessDTO,
                                     @WebsocketPayload(defaultValue = "0") Integer pageNo,
                                     @WebsocketPayload(defaultValue = "10") Integer pageSize) {
        return WsOutboundBean.topic(T1003.topic).ok(busiService.getList(businessDTO, ImmutablePair.of(pageNo, pageSize)));
    }
    
    public class BusinessDTO {
        private Map<String,String> predicates;
        private Map<String,Byte> orders;
    }
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
  inboundSpecification: {seq:0, topic:xxx, version:0, params:{}}
  # 下行数据规范信息
  outboundSpecification: {seq:0, topic:'xxx', code:xxx, message:{"this is message"}, payload:{}}
  # 上行数据header字段#params业务参数
  payloadParamsDecodeName: params
  # 上行数据header字段#sequence消息序号
  payloadSequenceDecodeName: sequence
  # 上行数据header字段#functionCode函数枚举
  payloadTopicDecodeName: topic
  spelVariableName: inbound
  origins:
    - http://test.domain.com
```

文档参见Wiki https://github.com/ICoder0/spring-boot-starter-websocket-plus/wiki

如有需求提issue.
