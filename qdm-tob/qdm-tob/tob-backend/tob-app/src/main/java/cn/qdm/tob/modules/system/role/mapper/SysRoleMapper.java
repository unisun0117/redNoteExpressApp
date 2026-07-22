package cn.qdm.tob.modules.system.role.mapper;

import cn.qdm.tob.infrastructure.base.TobBaseMapper;
import cn.qdm.tob.modules.system.role.domain.SysRole;
import cn.qdm.tob.modules.system.role.vo.RoleQueryVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;

@Mapper
public interface SysRoleMapper extends TobBaseMapper<SysRole> {

    default Optional<SysRole> findByCode(String code) {
        return lambdaSelectOne(w -> w.eq(SysRole::getCode, code));
    }

    default Optional<SysRole> findByName(String name) {
        return lambdaSelectOne(w -> w.eq(SysRole::getName, name));
    }

    default IPage<SysRole> pageSearch(RoleQueryVO query) {
        Page<SysRole> page = Page.of(query.getPage(), query.getSize());
        LambdaQueryWrapper<SysRole> wrapper = Wrappers.<SysRole>lambdaQuery()
                .like(query.getCode() != null && !query.getCode().isBlank(), SysRole::getCode, query.getCode())
                .like(query.getName() != null && !query.getName().isBlank(), SysRole::getName, query.getName())
                .orderByDesc(SysRole::getUpdatedAt);
        return selectPage(page, wrapper);
    }
}
