package cn.qdm.tob.client.message;

import cn.qdm.tob.client.message.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * 消息推送客户端
 *
 * @author zhaoxiaoyun
 */
@FeignClient(name = "message", url = "${tob.client.message.base-url}", configuration = MessageClientConfiguration.class)
public interface MessageClient {

    /**
     * 发送邮件（无附件，JSON）
     */
    @PostMapping(value = "push/push/email", consumes = MediaType.APPLICATION_JSON_VALUE)
    MessageBaseResponseDTO sendEmail(@RequestBody EmailRequestDTO req);

    /**
     * 发送邮件（带附件，Multipart）
     */
    @PostMapping(value = "push/Push/EmailForFiles", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    MessageBaseResponseDTO sendEmailWithAttachment(@RequestBody Map<String, ?> req);

    /**
     * 发送短信
     */
    @PostMapping(value = "push/push/sms", consumes = MediaType.APPLICATION_JSON_VALUE)
    SmsResponseDTO sendSms(@RequestBody SmsRequestDTO req);

    /**
     * 批量发送短信
     */
    @PostMapping(value = "push/push/SMS_batch", consumes = MediaType.APPLICATION_JSON_VALUE)
    SmsResponseDTO sendSmsBatch(@RequestBody SmsBatchRequestDTO req);
}
