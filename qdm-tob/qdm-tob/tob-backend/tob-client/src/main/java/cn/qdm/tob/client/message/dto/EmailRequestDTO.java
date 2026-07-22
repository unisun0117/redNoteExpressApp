package cn.qdm.tob.client.message.dto;

import cn.qdm.tob.framework.util.MultipartFileResource;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EmailRequestDTO implements Serializable  {
    private String appKey;

    @JsonProperty("securityKey")
    private String appSecret;

    @JsonProperty("title")
    private String subject;

    @JsonProperty("toUser")
    private List<EmailRecipientDTO> recipients;

    @JsonProperty("ccUser")
    private List<EmailRecipientDTO> cc;

    @JsonProperty("Body")
    private String content;

    @JsonProperty("Html")
    private boolean html;

    @JsonProperty("files")
    private List<MultipartFile> attachments;

    public Map<String, ?> toMap() {
        LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("appKey", getAppKey());
        map.add("securityKey", getAppSecret());
        map.add("title", getSubject());
        map.add("Body", getContent());
        map.add("Html", isHtml());

        if (CollectionUtils.isNotEmpty(getRecipients())) {
            int count = getRecipients().size();
            for (int i = 0; i < count; i++) {
                EmailRecipientDTO recipient = getRecipients().get(i);
                map.add("toUser[" + i + "].Email", recipient.getEmail());
                if (StringUtils.isNotBlank(recipient.getDisplayName())) {
                    map.add("toUser[" + i + "].DisplayName", recipient.getDisplayName());
                }
            }
        }

        if (CollectionUtils.isNotEmpty(getCc())) {
            int count = getCc().size();
            for (int i = 0; i < count; i++) {
                EmailRecipientDTO cc = getCc().get(i);
                map.add("ccUser[" + i + "].Email", cc.getEmail());
                if (StringUtils.isNotBlank(cc.getDisplayName())) {
                    map.add("ccUser[" + i + "].DisplayName", cc.getDisplayName());
                }
            }
        }

        if (CollectionUtils.isNotEmpty(getAttachments())) {
            for (MultipartFile file : getAttachments()) {
                map.add("files", new MultipartFileResource(file));
            }
        }

        return map;
    }
}
