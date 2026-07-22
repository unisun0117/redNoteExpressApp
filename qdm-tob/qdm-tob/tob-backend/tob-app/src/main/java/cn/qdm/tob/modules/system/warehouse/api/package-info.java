/**
 * 字典模块公开 API。
 * <p>
 * 子包按消费方分类：
 * {@code admin} — Web 管理端、{@code mall} — 小程序商城端、
 * {@code external} — 外部系统回调、{@code internal} — 跨 Modulith 模块接口。
 * <p>
 * 其他模块只能访问此包下的类型，不可直接引用 service / domain / mapper 等内部实现。
 */
package cn.qdm.tob.modules.system.warehouse.api;
