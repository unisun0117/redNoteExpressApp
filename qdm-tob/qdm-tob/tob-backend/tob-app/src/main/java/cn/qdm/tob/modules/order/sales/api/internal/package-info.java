/**
 * 跨 Modulith 模块公开接口。
 * <p>
 * 其他模块可通过此包进行方法调用或事件监听，不可直接引用 service / domain / mapper 等内部实现。
 */
@org.springframework.modulith.NamedInterface("order-sales-api")
package cn.qdm.tob.modules.order.sales.api.internal;
