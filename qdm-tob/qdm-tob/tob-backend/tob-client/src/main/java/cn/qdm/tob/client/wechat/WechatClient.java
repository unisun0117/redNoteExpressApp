package cn.qdm.tob.client.wechat;

import cn.qdm.tob.client.wechat.dto.JscodeSessionResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 微信小程序 API 客户端
 */
@FeignClient(name = "wechat-client", url = "https://api.weixin.qq.com")
public interface WechatClient {

    /**
     * 将授权码转换为 openid
     */
    @GetMapping("/sns/jscode2session")
    JscodeSessionResponseDTO jscode2session(
            @RequestParam("appid") String appId,
            @RequestParam("secret") String secret,
            @RequestParam("js_code") String jsCode,
            @RequestParam("grant_type") String grantType
    );
}
