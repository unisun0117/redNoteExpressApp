package cn.qdm.tob.client.wecom.dto;

import cn.qdm.tob.client.wecom.enums.WeComMsgType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

/**
 * @author zhaoxiaoyun
 */
public class WeComMarkdownMessageRequestAtUserDTO extends WeComMessageRequestBaseDTO {
    @JsonProperty("markdown")
    WeComTextMessageContentAtUserDTO markdown;

    @Getter
    Integer safe;


    WeComMarkdownMessageRequestAtUserDTO() {
        super(WeComMsgType.MARKDOWN);
    }

    public WeComTextMessageContentAtUserDTO getText() {
        return markdown;
    }

    public static class TextMessageBuilder extends AbstractMessageBuilder<WeComMarkdownMessageRequestAtUserDTO> {
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
        protected WeComMarkdownMessageRequestAtUserDTO create() {
            WeComMarkdownMessageRequestAtUserDTO dto = new WeComMarkdownMessageRequestAtUserDTO();
            dto.markdown = new WeComTextMessageContentAtUserDTO(content, null);
            dto.safe = Boolean.TRUE.equals(safe) ? 1 : 0;
            return dto;
        }
    }
}
