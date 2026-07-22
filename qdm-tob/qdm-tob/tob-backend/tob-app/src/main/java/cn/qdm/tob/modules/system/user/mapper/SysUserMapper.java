package cn.qdm.tob.modules.system.user.mapper;

import cn.qdm.tob.modules.system.user.domain.SysUser;
import cn.qdm.tob.infrastructure.base.TobBaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;

@Mapper
public interface SysUserMapper extends TobBaseMapper<SysUser> {

    default Optional<SysUser> findByMobile(String mobile) {
        return lambdaSelectOne(w -> w.eq(SysUser::getMobile, mobile));
    }

    default Optional<SysUser> findByWechatOpenid(String wechatOpenid) {
        return lambdaSelectOne(w -> w.eq(SysUser::getWechatOpenid, wechatOpenid));
    }

    default Optional<SysUser> findByWechatId(String wechatId) {
        return lambdaSelectOne(w -> w.eq(SysUser::getWechatId, wechatId));
    }
}
