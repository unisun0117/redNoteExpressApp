package cn.qdm.tob.client.wecom;

import cn.qdm.tob.client.wecom.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 企业微信 API 客户端
 */
@FeignClient(name = "wecom-client", url = "https://qyapi.weixin.qq.com/cgi-bin", configuration = WeComClientConfiguration.class)
public interface WeComClient {
    /**
     * 将授权码转换为用户 ID
     */
    @GetMapping("/user/getuserinfo")
    WeComUserInfoResponseDTO getUserInfo(@RequestParam("code") String code);

    /**
     * 推送企业微信消息
     *
     * @see <a href="https://developer.work.weixin.qq.com/document/path/90372">文档</a>
     */
    @PostMapping("message/send")
    WeComMessageResponseDTO sendMessage(@RequestBody WeComMessageRequestBaseDTO message);

    /**
     * 根据手机号获取企业微信ID
     *
     * @see <a href="https://developer.work.weixin.qq.com/document/path/91693">文档</a>
     */
    @PostMapping("user/getuserid")
    WeComGetIdResponseDTO getIdByMobile(@RequestBody WeComGetIdByMobileRequestDTO dto);

    /**
     * 根据邮箱获取企业微信ID
     *
     * @see <a href="https://developer.work.weixin.qq.com/document/path/95892">文档</a>
     */
    @PostMapping("user/get_userid_by_email")
    WeComGetIdResponseDTO getIdByEmail(@RequestBody WeComGetIdByEmailRequestDTO dto);
}
