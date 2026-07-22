package cn.qdm.tob.infrastructure.security.annotation;

import java.lang.annotation.*;

/**
 * 当前选中客户注解 — 自动校验地址绑定并注入客户上下文
 * <p>
 * 标注在 Controller 方法参数上（类型为 {@code CustomerContext}），
 * 由 {@code CurrentCustomerArgumentResolver} 自动解析：
 * <ol>
 *   <li>从请求参数中取 addressId</li>
 *   <li>校验当前用户是否绑定了该地址</li>
 *   <li>校验地址审核状态为已通过</li>
 *   <li>注入客户上下文（含销售大区编号、价格组等）</li>
 * </ol>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CurrentCustomer {
    String DEFAULT_PARAM_NAME = "addressId";

    /** 请求参数名称，默认 addressId */
    String value() default DEFAULT_PARAM_NAME;
}
