package cn.qdm.tob.client.message;

import cn.qdm.tob.client.message.dto.*;
import cn.qdm.tob.framework.util.AssertUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class MessageSender {
    private final MessageProperties messageProperties;
    private final MessageClient messageClient;

    public MessageSender(MessageProperties messageProperties, MessageClient messageClient) {
        this.messageProperties = messageProperties;
        this.messageClient = messageClient;
    }

    public MessageBaseResponseDTO sendEmail(String subject,
                                            String content,
                                            List<EmailRecipientDTO> recipients,
                                            List<EmailRecipientDTO> cc,
                                            boolean html,
                                            List<MultipartFile> attachments) {
        AssertUtils.notEmpty(recipients, "无法发送邮件，收件人为空");

        EmailRequestDTO req = new EmailRequestDTO();
        req.setAppKey(messageProperties.getKey());
        req.setAppSecret(messageProperties.getSecret());
        req.setSubject(subject);
        req.setContent(content);
        req.setRecipients(recipients);
        req.setCc(Objects.isNull(cc) ? Collections.emptyList() : cc);
        req.setHtml(html);
        req.setAttachments(attachments);
        if (CollectionUtils.isEmpty(attachments)) {
            return messageClient.sendEmail(req);
        } else {
            return messageClient.sendEmailWithAttachment(req.toMap());
        }
    }

    public MessageBaseResponseDTO sendEmail(String subject, 
                                            String content, 
                                            List<EmailRecipientDTO> recipients, 
                                            List<EmailRecipientDTO> cc, 
                                            boolean html) {
        return sendEmail(subject, content, recipients, cc, html, null);
    }

    public MessageBaseResponseDTO sendEmail(String subject, 
                                            String content, 
                                            List<EmailRecipientDTO> recipients, 
                                            List<EmailRecipientDTO> cc) {
        return sendEmail(subject, content, recipients, cc, true, null);
    }

    public MessageBaseResponseDTO sendEmail(String subject, 
                                            String content, 
                                            List<EmailRecipientDTO> recipients) {
        return sendEmail(subject, content, recipients, null, true, null);
    }

    public MessageBaseResponseDTO sendEmail(String subject, 
                                            String content, 
                                            String recipients, 
                                            String cc, 
                                            boolean html, 
                                            List<MultipartFile> attachments) {
        return sendEmail(subject, content, parseRecipients(recipients), parseRecipients(cc), html, attachments);
    }

    public MessageBaseResponseDTO sendEmail(String subject, String content, String recipients, String cc, boolean html) {
        return sendEmail(subject, content, recipients, cc, html, null);
    }

    public MessageBaseResponseDTO sendEmail(String subject, String content, String recipients, String cc) {
        return sendEmail(subject, content, recipients, cc, true, null);
    }

    public MessageBaseResponseDTO sendEmail(String subject, String content, String recipients) {
        return sendEmail(subject, content, recipients, null, true, null);
    }

    public SmsResponseDTO sendSms(String content, String phone) {
        AssertUtils.notBlank(content, "无法发送短信，短信内容为空！");
        AssertUtils.notBlank(phone, "无法发送短信，接收人为空！");
        var req = new SmsRequestDTO();
        req.setApp(messageProperties.getKey());
        req.setContent(content);
        req.setPhone(phone);
        long timestamp = System.currentTimeMillis() / 1000L;
        req.setTimestamp(timestamp);
        String str = messageProperties.getKey()
                + messageProperties.getSecret()
                + req.getContent()
                + req.getPhone()
                + timestamp;
        String signature = DigestUtils.md5Hex(str);
        req.setSign(signature);
        return messageClient.sendSms(req);
    }

    public SmsResponseDTO sendSms(String content, Collection<String> phone) {
        AssertUtils.notBlank(content, "无法发送短信，短信内容为空！");
        AssertUtils.notEmpty(phone, "无法发送短信，接收人为空！");
        var req = new SmsBatchRequestDTO();
        req.setApp(messageProperties.getKey());
        req.setContent(content);
        req.setPhone(new ArrayList<>(phone));
        req.setApp(messageProperties.getKey());
        long timestamp = System.currentTimeMillis() / 1000L;
        req.setTimestamp(timestamp);
        String str = messageProperties.getKey()
                + messageProperties.getSecret()
                + req.getContent()
                + String.join(",", req.getPhone())
                + timestamp;
        String signature = DigestUtils.md5Hex(str);
        req.setSign(signature);
        return messageClient.sendSmsBatch(req);
    }

    /**
     * 将固定格式的字符串，解析成收件人对象
     * 格式：邮件地址1|显示名称1;邮件地址2|显示名称2
     * 示例：zhangsan@qdama.cn|张三;lisi@qq.com|李四
     */
    private List<EmailRecipientDTO> parseRecipients(String recipients) {
        if (StringUtils.isBlank(recipients)) return null;

        return Arrays.stream(recipients.split("\\s*;\\s*"))
                .filter(StringUtils::isNotBlank)
                .map(i -> {
                    int index = i.indexOf("|");
                    return index < 0 ? new EmailRecipientDTO(i.trim()) : new EmailRecipientDTO(i.substring(0, index).trim(), i.substring(index + 1).trim());
                }).collect(Collectors.toList());
    }
}
