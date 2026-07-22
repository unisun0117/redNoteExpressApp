package cn.qdm.tob.infrastructure.base;

import cn.qdm.tob.infrastructure.security.domain.UserPrincipal;
import cn.qdm.tob.infrastructure.security.util.SecurityUtil;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * MyBatis-Plus 审计字段自动填充处理器。
 * <p>
 * 使用 strict 模式：字段已有值时跳过（保留手动设置），为 null 时自动填充。
 * INSERT 时填充全部审计字段；UPDATE 时仅填充更新端字段。
 * 操作人信息从 {@link SecurityUtil#getCurrentUser()} 获取，未认证时 createdBy="未知"。
 * <p>
 * <b>注意：</b>updateById 传入从 DB 查出的完整实体时，审计字段携带旧值会被跳过，
 * 应使用 UpdateWrapper 按需 set 字段，或手动将审计字段置 null。
 */
@Slf4j
@Component
public class MyBatisMetaObjectHandler implements MetaObjectHandler {

    private static final String UNKNOWN_USER_NAME = "未知";

    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        UserPrincipal currentUser = SecurityUtil.getCurrentUser();

        this.strictInsertFill(metaObject, TobBaseEntity.CREATED_AT, LocalDateTime.class, now);
        this.strictInsertFill(metaObject, TobBaseEntity.UPDATED_AT, LocalDateTime.class, now);

        String userName = currentUser != null && currentUser.getName() != null ? currentUser.getName() : UNKNOWN_USER_NAME;

        this.strictInsertFill(metaObject, TobBaseEntity.CREATED_BY, String.class, userName);
        this.strictInsertFill(metaObject, TobBaseEntity.UPDATED_BY, String.class, userName);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        UserPrincipal currentUser = SecurityUtil.getCurrentUser();

        this.strictUpdateFill(metaObject, TobBaseEntity.UPDATED_AT, LocalDateTime.class, now);

        String userName = currentUser != null && currentUser.getName() != null ? currentUser.getName() : UNKNOWN_USER_NAME;

        this.strictUpdateFill(metaObject, TobBaseEntity.UPDATED_BY, String.class, userName);
    }
}
