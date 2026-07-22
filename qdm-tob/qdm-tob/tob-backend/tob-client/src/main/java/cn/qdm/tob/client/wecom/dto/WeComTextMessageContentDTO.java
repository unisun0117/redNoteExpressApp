package cn.qdm.tob.client.wecom.dto;

import lombok.Getter;

import java.io.Serializable;

/**
 * @author zhaoxiaoyun
 */
@Getter
public class WeComTextMessageContentDTO implements Serializable {
    private final String content;

    WeComTextMessageContentDTO(String content) {
        this.content = content;
    }

}
