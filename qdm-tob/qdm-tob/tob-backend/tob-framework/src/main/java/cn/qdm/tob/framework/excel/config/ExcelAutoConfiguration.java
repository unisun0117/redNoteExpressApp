package cn.qdm.tob.framework.excel.config;

import cn.qdm.tob.framework.description.DescriptionProvider;
import cn.qdm.tob.framework.excel.handler.ResponseExportHandler;
import cn.qdm.tob.framework.excel.interceptor.ImportInterceptor;
import cn.qdm.tob.framework.excel.interceptor.ExportInterceptor;
import cn.qdm.tob.framework.excel.resolver.ExcelImportArgumentResolver;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Excel 导入/导出自动配置
 * <p>
 * 注册：
 * <ul>
 *   <li>{@link ImportInterceptor} — 导入拦截器</li>
 *   <li>{@link ExportInterceptor} — 导出拦截器</li>
 *   <li>{@link ExcelImportArgumentResolver} — 导入参数解析器</li>
 *   <li>{@link ResponseExportHandler} — 默认 HTTP Response 导出处理器</li>
 * </ul>
 * <p>
 * 字典转换 ({@code DictionaryConverter}) 复用 {@link DescriptionProvider}，
 * 与 Jackson {@code @Description} 序列化共用同一套字典加载机制。
 * </p>
 */
@AutoConfiguration
public class ExcelAutoConfiguration implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(importInterceptor(null))
                .order(0);
        registry.addInterceptor(exportInterceptor(null))
                .order(1);
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(excelImportArgumentResolver());
    }

    @Bean
    public ImportInterceptor importInterceptor(List<DescriptionProvider> providers) {
        ImportInterceptor interceptor = new ImportInterceptor();
        interceptor.setDescriptionProviders(providers);
        return interceptor;
    }

    @Bean
    public ExportInterceptor exportInterceptor(List<DescriptionProvider> providers) {
        ExportInterceptor interceptor = new ExportInterceptor();
        interceptor.setDescriptionProviders(providers);
        return interceptor;
    }

    @Bean
    public ExcelImportArgumentResolver excelImportArgumentResolver() {
        return new ExcelImportArgumentResolver();
    }

    @Bean
    public ResponseExportHandler responseExportHandler() {
        return new ResponseExportHandler();
    }
}
