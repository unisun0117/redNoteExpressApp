package cn.qdm.tob.client.wecom.dto;

import cn.qdm.tob.client.wecom.enums.WeComMsgType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

/**
 * @author zhaoxiaoyun
 */
@Getter
public class WeComTextCardMessageRequestDTO extends WeComMessageRequestBaseDTO {
    @JsonProperty("textcard")
    private WeComTextCardMessageContentDTO textCard;

    WeComTextCardMessageRequestDTO(WeComTextCardMessageContentDTO textCard) {
        super(WeComMsgType.TEXTCARD);
        this.textCard = textCard;
    }

    public static class TextCardMessageBuilder extends AbstractMessageBuilder<WeComTextCardMessageRequestDTO> {
        String title;

        String description;

        String url;

        String btnTxt;

        TextCardMessageBuilder(String title, String description, String url) {
            this.title = title;
            this.description = description;
            this.url = url;
        }

        TextCardMessageBuilder(String title, String description, String url, String btnTxt) {
            this(title, description, url);
            this.btnTxt = btnTxt;
        }

        @Override
        protected WeComTextCardMessageRequestDTO create() {
            WeComTextCardMessageContentDTO textCard = new WeComTextCardMessageContentDTO(title, description, url, btnTxt);
            return new WeComTextCardMessageRequestDTO(textCard);
        }
    }
}
