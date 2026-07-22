package cn.qdm.tob;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

/**
 * Spring Modulith 模块边界验证测试。
 *
 * <p>一旦检测到跨模块直接引用对方内部类（非公开 API），测试失败 → CI 阻断。
 */
class ModulithVerificationTest {

    @Test
    void verifyModuleBoundaries() {
        ApplicationModules.of(TobApplication.class).verify();
    }
}
