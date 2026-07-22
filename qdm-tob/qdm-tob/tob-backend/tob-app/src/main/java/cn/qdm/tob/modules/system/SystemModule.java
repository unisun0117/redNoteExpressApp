package cn.qdm.tob.modules.system;

import org.springframework.modulith.ApplicationModule;

/**
 * 系统管理模块
 *
 * 职责: 处理用户、角色、权限、菜单等权限管理相关业务
 *
 * 子域:
 * - user: 用户管理
 * - role: 角色管理
 * - permission: 权限管理
 * - menu: 菜单管理
 * - auth: 鉴权与授权
 * - privacy: 隐私政策管理
 */
@ApplicationModule(displayName = "System Module")
public class SystemModule {
}
