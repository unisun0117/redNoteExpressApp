package cn.qdm.tob.client.message.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author zhaoxiaoyun
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SmsResponseDTO extends MessageBaseResponseDTO {
    /**
     * 成功时返回消息Id
     */
    @JsonProperty("Object")
    private String msgId;
}
