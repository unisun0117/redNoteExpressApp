package cn.qdm.tob.infrastructure.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
@MapperScan(basePackages = "cn.qdm.tob.modules.**.mapper")
public class MybatisConfiguration {
    /**
     * 添加分页插件
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        InnerInterceptor paginationInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
        Properties properties = new Properties();
        properties.setProperty("optimizeJoin", "false");
        paginationInterceptor.setProperties(properties);
        interceptor.addInnerInterceptor(paginationInterceptor);
        return interceptor;
    }
}
