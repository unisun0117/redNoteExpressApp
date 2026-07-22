package cn.qdm.tob.infrastructure.security.resolver;

/**
 * 客户上下文提供者 SPI — 由 modules 层实现，供基础设施层的参数解析器调用。
 * <p>
 * 实现类负责校验用户与地址的绑定关系、审核状态，并组装 {@link CustomerContext}。
 * </p>
 */
@FunctionalInterface
public interface CustomerContextProvider {

    /**
     * 根据用户 ID 和地址 ID 解析客户上下文。
     *
     * @param userId    当前登录用户 ID
     * @param addressId 前端传入的收货地址 ID（可为 null，实现类自行校验）
     * @return 客户上下文，包含销售大区、价格组等业务信息
     */
    CustomerContext resolve(Long userId, Long addressId);
}
