package cn.qdm.tob.framework;

import org.springframework.aop.framework.AopContext;

/**
 * 自注入工具接口。实现此接口后，通过 {@link #self()} 获取当前 Bean 的 AOP 代理，
 * 使内部方法调用能触发 Spring AOP 拦截（{@code @Cacheable}、{@code @Transactional} 等）。
 */
public interface ProxySelf<T> {

    /** 获取当前 Bean 的 AOP 代理。非代理环境（如单测）返回自身。 */
    @SuppressWarnings("unchecked")
    default T self() {
        try {
            return (T) AopContext.currentProxy();
        } catch (IllegalStateException e) {
            return (T) this; // 非代理环境兜底
        }
    }
}
