package cn.qdm.tob.client.wecom;

import cn.qdm.tob.client.wecom.dto.WeComMessageRequestBaseDTO;
import cn.qdm.tob.client.wecom.dto.WeComMessageResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "wecom-webhook", url = "https://qyapi.weixin.qq.com/webhook")
public interface WeComWebhookClient {
    /**
     * 推送企业机器人消息
     */
    @PostMapping("/send")
    WeComMessageResponseDTO sendWebhook(@RequestParam("key") String key,
                                        @RequestBody WeComMessageRequestBaseDTO message);
}
