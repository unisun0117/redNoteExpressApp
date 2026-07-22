package cn.qdm.tob.modules.system.operator.service;

import cn.qdm.tob.modules.system.operator.mapper.SysOperatorMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 运营人员停用黑名单服务
 * <p>
 * 使用 Redis Set 缓存停用用户集合，JWT 认证时 O(1) 校验。
 * Set key: operator:inactive:set
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OperatorStatusFilterService {

    private static final String INACTIVE_SET_KEY = "operator:inactive:set";

    private final StringRedisTemplate redisTemplate;
    private final SysOperatorMapper operatorMapper;

    /** 启动时从 DB 加载所有停用/锁定用户，先清空再填充 */
    @PostConstruct
    public void initBlacklist() {
        redisTemplate.delete(INACTIVE_SET_KEY);
        List<Long> ids = operatorMapper.findAllInactiveIds();
        if (!ids.isEmpty()) {
            String[] arr = ids.stream().map(String::valueOf).toArray(String[]::new);
            redisTemplate.opsForSet().add(INACTIVE_SET_KEY, arr);
        }
        log.info("初始化停用黑名单完成: {} 个用户", ids.size());
    }

    /** 判断操作员是否已停用 */
    public boolean isInactive(Long operatorId) {
        return Boolean.TRUE.equals(
                redisTemplate.opsForSet().isMember(INACTIVE_SET_KEY, String.valueOf(operatorId)));
    }

    /** 加入黑名单（停用操作） */
    public void addToBlacklist(Long operatorId) {
        redisTemplate.opsForSet().add(INACTIVE_SET_KEY, String.valueOf(operatorId));
        log.info("停用黑名单 SADD: operatorId={}", operatorId);
    }

    /** 移出黑名单（启用操作） */
    public void removeFromBlacklist(Long operatorId) {
        redisTemplate.opsForSet().remove(INACTIVE_SET_KEY, String.valueOf(operatorId));
        log.info("停用黑名单 SREM: operatorId={}", operatorId);
    }
}
