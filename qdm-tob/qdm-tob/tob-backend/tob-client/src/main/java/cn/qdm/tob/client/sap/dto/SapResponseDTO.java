package cn.qdm.tob.client.sap.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.Objects;

/**
 * @author zhaoxiaoyun
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SapResponseDTO<T> {
    public static final String SUCCESS_CODE = "0";
    public static final String FAILURE_CODE = "1";

    /**
     * 0 成功 1失败
     */
    private String code;

    /**
     * 消息
     */
    private String message;

    /**
     * 时间戳
     */
    private String now;

    /**
     * 返回数据
     */
    private T data;

    public boolean isSuccess() {
        return Objects.equals(code, SUCCESS_CODE);
    }
}
