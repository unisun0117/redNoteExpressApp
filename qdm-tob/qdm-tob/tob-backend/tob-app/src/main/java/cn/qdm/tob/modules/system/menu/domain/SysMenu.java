package cn.qdm.tob.modules.system.menu.domain;

import cn.qdm.tob.framework.model.RecordStatus;
import cn.qdm.tob.infrastructure.base.TobBaseEntity;
import cn.qdm.tob.modules.system.menu.enums.MenuGroup;
import cn.qdm.tob.modules.system.menu.enums.MenuType;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 菜单实体（MENU 一级/PAGE 二级/BUTTON 三级，三层共用一张表）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@TableName("sys_menu")
public class SysMenu extends TobBaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 编码，全局唯一，创建后不可修改 */
    private String code;

    /** 父菜单ID，0表示根节点 */
    private Long parentId;

    /** 菜单名称 */
    private String name;

    /** 菜单类型 */
    private MenuType type;

    /** 所属端 */
    private MenuGroup menuGroup;

    /** 路由路径 */
    private String path;

    /** 前端组件路径 */
    private String component;

    /** 图标 */
    private String icon;

    /** 排序号 */
    private Integer sort;

    /** 状态 */
    private RecordStatus status;
}
