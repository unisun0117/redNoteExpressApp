package cn.qdm.tob.client.wecom.dto;

import cn.qdm.tob.client.wecom.enums.EmailType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author zhaoxiaoyun
 */
@Data
public class WeComGetIdByEmailRequestDTO {
    private String email;

    @JsonProperty("email_type")
    private EmailType type;

    public WeComGetIdByEmailRequestDTO(String email, EmailType type) {
        this.email = email;
        this.type = type;
    }

    public WeComGetIdByEmailRequestDTO(String email) {
        this(email, EmailType.ENTERPRISE);
    }
}
