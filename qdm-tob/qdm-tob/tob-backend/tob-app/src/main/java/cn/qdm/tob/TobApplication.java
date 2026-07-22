package cn.qdm.tob;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.modulith.Modulith;

/**
 * TOB应用主启动类
 */
@SpringBootApplication
@EnableAspectJAutoProxy(exposeProxy = true)
@Modulith(sharedModules = {"infrastructure", "framework"})
@EnableFeignClients(basePackages = "cn.qdm.tob.client")
public class TobApplication {
    static void main(String[] args) {
        SpringApplication.run(TobApplication.class, args);
    }
}
