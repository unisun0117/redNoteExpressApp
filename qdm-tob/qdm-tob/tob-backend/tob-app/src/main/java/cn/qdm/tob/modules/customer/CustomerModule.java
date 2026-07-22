package cn.qdm.tob.modules.customer;

import org.springframework.modulith.ApplicationModule;

/**
 * 客户模块
 *
 * 职责: 处理客户、客户审核、仓库、运营区等客户运营相关业务
 *
 * 子域:
 * - customer: 客户管理
 * - audit: 客户审核
 * - operation: 运营管理 (仓库、运营区)
 */
@ApplicationModule(displayName = "Customer Module")
public class CustomerModule {
}
