package com.icoder0.websocket.examples;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author bofa1ex
 * @since 2020/8/4
 */
@Data
public class WsLoginVO {

    private Long account;
    @NotBlank
    private String pwdMD5;
}
