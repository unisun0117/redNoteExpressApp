package cn.qdm.tob.client.wecom.dto;

import cn.qdm.tob.client.wecom.enums.WeComMsgType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.io.Serializable;
import java.util.List;

/**
 * @author zhaoxiaoyun
 */
@Getter
public class WeComMessageRequestBaseDTO implements Serializable {
    @JsonProperty("touser")
    String toUser;

    @JsonProperty("toparty")
    String toParty;

    @JsonProperty("totag")
    String toTag;

    @JsonProperty("msgtype")
    WeComMsgType msgType;

    @JsonProperty("agentid")
    int agentId;

    @JsonProperty("enable_id_trans")
    Integer enableIdTrans;

    @JsonProperty("enable_duplicate_check")
    Integer enableDuplicateCheck;

    @JsonProperty("duplicate_check_interval")
    Integer duplicateCheckInterval;

    WeComMessageRequestBaseDTO(WeComMsgType msgType) {
        this.msgType = msgType;
    }

    public static WeComTextMessageRequestDTO.TextMessageBuilder text(String content, Boolean safe) {
        return new WeComTextMessageRequestDTO.TextMessageBuilder(content, safe);
    }

    public static WeComTextMessageRequestDTO.TextMessageBuilder text(String content) {
        return new WeComTextMessageRequestDTO.TextMessageBuilder(content);
    }

    public static WeComTextMessageRequestAtUserDTO.TextMessageBuilder text(String content, List<String> sendPhone) {
        return new WeComTextMessageRequestAtUserDTO.TextMessageBuilder(content, sendPhone);
    }

    public static WeComMarkdownMessageRequestAtUserDTO.TextMessageBuilder markdown(String content) {
        return new WeComMarkdownMessageRequestAtUserDTO.TextMessageBuilder(content);
    }

    public static WeComTextCardMessageRequestDTO.TextCardMessageBuilder textCard(String title, String description, String url, String btnTxt) {
        return new WeComTextCardMessageRequestDTO.TextCardMessageBuilder(title, description, url, btnTxt);
    }

    public static WeComTextCardMessageRequestDTO.TextCardMessageBuilder textCard(String title, String description, String url) {
        return new WeComTextCardMessageRequestDTO.TextCardMessageBuilder(title, description, url);
    }

    public abstract static class AbstractMessageBuilder<E extends WeComMessageRequestBaseDTO> {
        String toUser;

        String toParty;

        String toTag;

        int agentId;

        Boolean enableIdTrans;

        Boolean enableDuplicateCheck;

        Integer duplicateCheckInterval;

        public AbstractMessageBuilder<E> setToUser(String toUser) {
            this.toUser = toUser;
            return this;
        }

        public AbstractMessageBuilder<E> setToParty(String toParty) {
            this.toParty = toParty;
            return this;
        }

        public AbstractMessageBuilder<E> setToTag(String toTag) {
            this.toTag = toTag;
            return this;
        }

        public AbstractMessageBuilder<E> setAgentId(int agentId) {
            this.agentId = agentId;
            return this;
        }

        public AbstractMessageBuilder<E> setEnableIdTrans(Boolean enableIdTrans) {
            this.enableIdTrans = enableIdTrans;
            return this;
        }

        public AbstractMessageBuilder<E> setEnableDuplicateCheck(Boolean enableDuplicateCheck) {
            this.enableDuplicateCheck = enableDuplicateCheck;
            return this;
        }

        public AbstractMessageBuilder<E> setDuplicateCheckInterval(Integer duplicateCheckInterval) {
            this.duplicateCheckInterval = duplicateCheckInterval;
            return this;
        }

        protected abstract E create();

        public E build() {
            E obj = create();
            obj.toUser = this.toUser;
            obj.toParty = this.toParty;
            obj.toTag = this.toTag;
            obj.agentId = this.agentId;
            obj.enableIdTrans = Boolean.TRUE.equals(this.enableIdTrans) ? 1 : 0;
            obj.enableDuplicateCheck = Boolean.TRUE.equals(this.enableDuplicateCheck) ? 1 : 0;
            obj.duplicateCheckInterval = this.duplicateCheckInterval;
            return obj;
        }
    }
}
