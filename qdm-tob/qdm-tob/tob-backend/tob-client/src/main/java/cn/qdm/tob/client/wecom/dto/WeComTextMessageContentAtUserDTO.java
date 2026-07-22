package cn.qdm.tob.client.wecom.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author zhaoxiaoyun
 */
@Data
public class WeComTextMessageContentAtUserDTO implements Serializable {
    private String content;
    private List<String> mentioned_mobile_list;
    WeComTextMessageContentAtUserDTO(String content, List<String> mentioned_mobile_list) {
        this.content = content;
        this.mentioned_mobile_list = mentioned_mobile_list;
    }
}
