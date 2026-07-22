package cn.qdm.tob.modules.system.auth.service;

import cn.qdm.tob.client.message.MessageSender;
import cn.qdm.tob.framework.exception.ErrorCode;
import cn.qdm.tob.framework.exception.TobServiceException;
import cn.qdm.tob.modules.system.common.CacheKeys;
import cn.qdm.tob.modules.system.user.enums.UserStatus;
import cn.qdm.tob.modules.system.user.service.SysUserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * 短信验证码服务
 * 负责短信码的生成、发送、验证
 */
@Slf4j
@Service
public class SmsCodeService {

    private final RedisTemplate<String, String> redisTemplate;
    private final MessageSender messageSender;
    private final SysUserService sysUserService;

    @Value("${app.auth.sms.max-attempts:3}")
    private int maxAttempts;

    @Value("${app.auth.sms.code-expiration:300}")
    private long codeExpiration;

    @Value("${app.auth.sms.resend-interval:60}")
    private long resendInterval;

    /**
     * 临时跳过短信验证码开关（app.auth.sms.bypass-enabled）。
     * 开启后 sendSmsCode / verifySmsCode 直接放行，不读写 Redis，便于本地联调。
     */
    @Value("${app.auth.sms.bypass-enabled:false}")
    private boolean bypassEnabled;

    /** 短信模板：钱鲜达验证码 */
    private static final String SMS_TEMPLATE = "【钱鲜达】您的验证码是%s，%d分钟内有效，请勿泄露给他人。";

    public SmsCodeService(RedisTemplate<String, String> redisTemplate,
                          MessageSender messageSender,
                          SysUserService sysUserService) {
        this.redisTemplate = redisTemplate;
        this.messageSender = messageSender;
        this.sysUserService = sysUserService;
    }

    /**
     * 发送短信验证码
     */
    public void sendSmsCode(String phone) {
        if (bypassEnabled) {
            log.warn("SMS bypass enabled — skip sending code to phone: {}", phone);
            return;
        }
        // 1. 校验账号状态（禁用/冻结账号不允许发送验证码）
        sysUserService.findByMobile(phone).ifPresent(user -> {
            if (UserStatus.INACTIVE.equals(user.getStatus()) || UserStatus.FROZEN.equals(user.getStatus())) {
                throw new TobServiceException(ErrorCode.FORBIDDEN.code(), "账号已被禁用，请联系管理员");
            }
        });

        // 2. 检查频率限制（resendInterval 秒内不能重复发送）
        String lastSendKey = "auth:sms:last_send:" + phone;
        String lastSendTime = redisTemplate.opsForValue().get(lastSendKey);

        if (lastSendTime != null) {
            long waitTime = resendInterval;
            throw new TobServiceException(ErrorCode.TOO_MANY_REQUESTS.code(), "请在 " + waitTime + " 秒后重试");
        }

        // 3. 生成 6 位验证码
        String code = RandomStringUtils.insecure().nextNumeric(6);

        // 4. 存储到 Redis（设置过期时间）
        String codeKey = CacheKeys.AUTH_SMS_CODE + phone;
        redisTemplate.opsForValue().set(codeKey, code, Duration.ofSeconds(codeExpiration));

        // 5. 标记发送时间
        redisTemplate.opsForValue().set(lastSendKey, String.valueOf(System.currentTimeMillis()),
                Duration.ofSeconds(resendInterval));

        // 6. 调用短信服务商发送验证码
        String content = String.format(SMS_TEMPLATE, code, codeExpiration / 60);
        messageSender.sendSms(content, phone);
        log.info("SMS code sent to phone: {}", phone);
    }

    /**
     * 验证短信码
     */
    public void verifySmsCode(String phone, String code) {
        if (bypassEnabled) {
            log.warn("SMS bypass enabled — skip verifying code for phone: {}", phone);
            return;
        }
        String codeKey = CacheKeys.AUTH_SMS_CODE + phone;
        String attemptKey = "auth:sms:attempt:" + phone;

        // 1. 检查尝试次数
        String attemptCount = redisTemplate.opsForValue().get(attemptKey);
        if (attemptCount != null && Integer.parseInt(attemptCount) >= maxAttempts) {
            throw new TobServiceException(ErrorCode.LOCKED.code(), "验证码尝试次数过多，请稍后重试");
        }

        // 2. 验证码比对
        String storedCode = redisTemplate.opsForValue().get(codeKey);
        if (storedCode == null || !storedCode.equals(code)) {
            // 记录失败次数
            redisTemplate.opsForValue().increment(attemptKey);
            redisTemplate.expire(attemptKey, Duration.ofMinutes(10));

            int remaining = maxAttempts - (attemptCount != null ? Integer.parseInt(attemptCount) + 1 : 1);
            if (remaining > 0) {
                throw new TobServiceException(ErrorCode.BAD_REQUEST.code(), "验证码错误，还有 " + remaining + " 次机会");
            } else {
                throw new TobServiceException(ErrorCode.BAD_REQUEST.code(), "验证码错误次数过多");
            }
        }

        // 3. 验证成功，清除验证码和失败记录
        redisTemplate.delete(codeKey);
        redisTemplate.delete(attemptKey);

        log.info("SMS code verified successfully for phone: {}", phone);
    }
}
