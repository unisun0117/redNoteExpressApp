package cn.qdm.tob.client.wecom.dto;

import cn.qdm.tob.client.wecom.enums.WeComMsgType;
import lombok.Getter;

import java.util.List;

/**
 * @author zhaoxiaoyun
 */
@Getter
public class WeComTextMessageRequestAtUserDTO extends WeComMessageRequestBaseDTO {
    WeComTextMessageContentAtUserDTO text;
    Integer safe;

    WeComTextMessageRequestAtUserDTO() {
        super(WeComMsgType.TEXT);
    }

    public static class TextMessageBuilder extends AbstractMessageBuilder<WeComTextMessageRequestAtUserDTO> {
        String content;
        List<String> sendPhone;
        Boolean safe;

        TextMessageBuilder(String content, Boolean safe, List<String> sendPhone) {
            this.content = content;
            this.safe = safe;
            this.sendPhone = sendPhone;
        }

        TextMessageBuilder(String content, List<String> sendPhone) {
            this(content, Boolean.FALSE, sendPhone);
        }

        @Override
        protected WeComTextMessageRequestAtUserDTO create() {
            WeComTextMessageRequestAtUserDTO dto = new WeComTextMessageRequestAtUserDTO();
            dto.text = new WeComTextMessageContentAtUserDTO(content,sendPhone);
            dto.safe = Boolean.TRUE.equals(safe) ? 1 : 0;
            return dto;
        }
    }
}
