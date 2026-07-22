package cn.qdm.tob.infrastructure.config;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.api.impl.WxMaServiceImpl;
import cn.binarywang.wx.miniapp.config.impl.WxMaDefaultConfigImpl;
import cn.qdm.tob.infrastructure.autoconfigure.WeChatProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WeChatConfiguration {

    @Bean
    public WxMaService wxMaService(WeChatProperties weChatProperties) {
        var cfg = new WxMaDefaultConfigImpl();
        cfg.setAppid(weChatProperties.getAppId());
        cfg.setSecret(weChatProperties.getSecret());

        var wxMaService = new WxMaServiceImpl();
        wxMaService.setWxMaConfig(cfg);
        return wxMaService;
    }
}
