package cn.qdm.tob.infrastructure.autoconfigure;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步任务配置
 * 用于菜单/权限缓存的异步更新，不影响主事务接口响应。
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * 菜单/权限缓存更新线程池
     * - 核心线程 2，最大 4，队列 100
     * - 拒绝策略 DiscardPolicy：队列满时丢弃（缓存 miss 兜底，不影响业务）
     */
    @Bean("cacheExecutor")
    public Executor cacheExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("cache-async-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
        executor.initialize();
        return executor;
    }
}
