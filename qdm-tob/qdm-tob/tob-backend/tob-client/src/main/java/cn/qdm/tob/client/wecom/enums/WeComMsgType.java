package cn.qdm.tob.client.wecom.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * @author zhaoxiaoyun
 */
public enum WeComMsgType {
    TEXT, // 文本消息
    IMAGE, // 图片消息
    VOICE, // 语音消息
    VIDEO, // 视频消息
    FILE, // 文件消息
    TEXTCARD, // 文本卡片消息
    NEWS, // 普通图文消息
    MPNEWS, // 图文消息，跟普通的图文消息一致，唯一的差异是图文内容存储在企业微信。
    MARKDOWN, // markdown消息
    MINIPROGRAM_NOTICE, // 小程序通知消息
    TASKCARD; // 任务卡片消息

    @JsonValue
    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }
}
