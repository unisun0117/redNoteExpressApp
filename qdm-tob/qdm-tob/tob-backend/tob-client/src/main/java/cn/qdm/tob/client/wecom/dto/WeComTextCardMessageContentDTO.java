package cn.qdm.tob.client.wecom.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.io.Serializable;

/**
 * @author zhaoxiaoyun
 * @date 2022/12/20
 */
@Getter
public class WeComTextCardMessageContentDTO implements Serializable {
    private final String title;

    private final String description;

    private final String url;

    @JsonProperty("btntxt")
    private String btnTxt;

    WeComTextCardMessageContentDTO(String title, String description, String url) {
        this.title = title;
        this.description = description;
        this.url = url;
    }

    WeComTextCardMessageContentDTO(String title, String description, String url, String btnTxt) {
        this(title, description, url);
        this.btnTxt = btnTxt;
    }
}
