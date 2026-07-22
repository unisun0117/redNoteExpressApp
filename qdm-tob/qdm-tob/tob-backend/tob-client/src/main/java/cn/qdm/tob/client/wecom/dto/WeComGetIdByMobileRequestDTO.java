package cn.qdm.tob.client.wecom.dto;

import lombok.Data;

/**
 * @author zhaoxiaoyun
 */
@Data
public class WeComGetIdByMobileRequestDTO {
    private String mobile;

    public WeComGetIdByMobileRequestDTO(String mobile) {
        this.mobile = mobile;
    }
}
