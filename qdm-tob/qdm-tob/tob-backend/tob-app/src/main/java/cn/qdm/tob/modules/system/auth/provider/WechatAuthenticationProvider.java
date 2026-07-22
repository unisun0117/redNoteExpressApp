package cn.qdm.tob.modules.system.auth.provider;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import cn.binarywang.wx.miniapp.bean.WxMaPhoneNumberInfo;
import cn.qdm.tob.framework.exception.ErrorCode;
import cn.qdm.tob.framework.exception.TobServiceException;
import cn.qdm.tob.framework.util.AssertUtils;
import cn.qdm.tob.modules.system.auth.enums.AuthType;
import cn.qdm.tob.infrastructure.security.domain.UserPrincipal;
import cn.qdm.tob.modules.system.auth.token.WechatAuthenticationToken;
import cn.qdm.tob.modules.system.user.domain.SysUser;
import cn.qdm.tob.modules.system.user.enums.UserStatus;
import cn.qdm.tob.modules.system.user.mapper.SysUserMapper;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.time.LocalDateTime;

/**
 * 小程序微信认证 Provider
 * 调用微信 API（code → openid）→ 查询/创建 User → 返回已认证 Token（User 无权限）
 */
@Slf4j
@Component
public class WechatAuthenticationProvider implements AuthenticationProvider {
    private final SysUserMapper userMapper;
    private final WxMaService wxMaService;

    public WechatAuthenticationProvider(SysUserMapper userMapper, WxMaService wxMaService) {
        this.userMapper = userMapper;
        this.wxMaService = wxMaService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) {
        WechatAuthenticationToken token = (WechatAuthenticationToken) authentication;
        String phoneCode = (String) token.getPrincipal();    // getPhoneNumber 返回的 code
        String wxCode = (String) token.getCredentials();     // wx.login() 返回的 code

        // 1. wx.login() code → openId（code2Session）
        String openId;
        try {
            WxMaJscode2SessionResult session = wxMaService.jsCode2SessionInfo(wxCode);
            openId = session.getOpenid();
        } catch (WxErrorException e) {
            throw new TobServiceException(ErrorCode.UNAUTHORIZED.code(), "微信认证失败: " + e.getMessage());
        }
        AssertUtils.notEmpty(openId, ErrorCode.UNAUTHORIZED.code(), "无法获取微信 openId，请重试");

        // 2. getPhoneNumber code → 手机号
        WxMaPhoneNumberInfo phoneInfo;
        try {
            phoneInfo = wxMaService.getUserService().getPhoneNumber(phoneCode);
        } catch (WxErrorException e) {
            throw new TobServiceException(ErrorCode.UNAUTHORIZED.code(), "获取手机号失败: " + e.getMessage());
        }
        AssertUtils.notEmpty(phoneInfo.getPhoneNumber(), ErrorCode.UNAUTHORIZED.code(), "无法获取手机号，请重试");

        // 3. 按手机号查询用户
        SysUser user = userMapper.findByMobile(phoneInfo.getPhoneNumber()).orElse(null);
        AssertUtils.notNull(user, ErrorCode.NOT_FOUND.code(), "用户不存在，请先注册！");

        // 4. 校验账号状态（禁用则拒绝登录）
        if (UserStatus.INACTIVE.equals(user.getStatus()) || UserStatus.FROZEN.equals(user.getStatus())) {
            throw new TobServiceException(ErrorCode.FORBIDDEN.code(), "账号已被禁用，请联系管理员");
        }

        // 5. 自动绑定微信 openId（预置账号 wechat_openid=NULL，首次微信登录时自动绑定）
        if (user.getWechatOpenid() == null || user.getWechatOpenid().isBlank()) {
            user.setWechatOpenid(openId);
            log.info("Auto-bound wechat openId: mobile={}, openId={}",
                    phoneInfo.getPhoneNumber(), openId);
        }

        // 6. 更新最近登录时间
        user.setLastLoginAt(LocalDateTime.now());
        userMapper.updateById(user);

        // 7. 构造 UserPrincipal
        // 优先 real_name（手机号注册账号有 real_name、wechat_nickname 为空）；
        // 微信注册账号 real_name 可能为空，则回退到 wechat_nickname。
        String displayName = user.getRealName() != null ? user.getRealName() : user.getWechatNickname();
        UserPrincipal principal = new UserPrincipal(
                user.getId(), AuthType.WECHAT.toString(),
                user.getMobile(), displayName);

        return new WechatAuthenticationToken(principal, Collections.emptyList());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return WechatAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
