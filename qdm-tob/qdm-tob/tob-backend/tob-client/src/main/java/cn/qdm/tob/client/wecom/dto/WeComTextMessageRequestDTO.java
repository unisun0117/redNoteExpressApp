package cn.qdm.tob.client.wecom.dto;

import cn.qdm.tob.client.wecom.enums.WeComMsgType;
import lombok.Getter;

/**
 * @author zhaoxiaoyun
 */
@Getter
public class WeComTextMessageRequestDTO extends WeComMessageRequestBaseDTO {
    WeComTextMessageContentDTO text;
    Integer safe;

    WeComTextMessageRequestDTO() {
        super(WeComMsgType.TEXT);
    }

    public static class TextMessageBuilder extends AbstractMessageBuilder<WeComTextMessageRequestDTO> {
        String content;

        Boolean safe;

        TextMessageBuilder(String content, Boolean safe) {
            this.content = content;
            this.safe = safe;
        }

        TextMessageBuilder(String content) {
            this(content, Boolean.FALSE);
        }

        @Override
        protected WeComTextMessageRequestDTO create() {
            WeComTextMessageRequestDTO dto = new WeComTextMessageRequestDTO();
            dto.text = new WeComTextMessageContentDTO(content);
            dto.safe = Boolean.TRUE.equals(safe) ? 1 : 0;
            return dto;
        }
    }
}
