package cn.qdm.tob.framework;

import org.springframework.modulith.ApplicationModule;

/**
 * 框架共享模块 — 所有业务模块均可依赖。
 */
@ApplicationModule(type = ApplicationModule.Type.OPEN, displayName = "Framework")
public class FrameworkModule {
}
