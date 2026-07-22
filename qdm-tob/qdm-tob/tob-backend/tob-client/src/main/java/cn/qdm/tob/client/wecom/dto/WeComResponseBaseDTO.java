package cn.qdm.tob.client.wecom.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author zhaoxiaoyun
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WeComResponseBaseDTO implements Serializable {
    public static final int SUCCESS_CODE = 0;

    // access token已过期
    public static final int ACCESS_TOKEN_EXPIRED_CODE = 42001;

    // 用户不存在
    public static final int USER_NOT_FOUND_CODE = 60111;

    @JsonProperty("errcode")
    private Integer errCode;

    @JsonProperty("errmsg")
    private String errMsg;

    @JsonIgnore
    public boolean isSucceed() {
        return Objects.equals(errCode, SUCCESS_CODE);
    }
}
