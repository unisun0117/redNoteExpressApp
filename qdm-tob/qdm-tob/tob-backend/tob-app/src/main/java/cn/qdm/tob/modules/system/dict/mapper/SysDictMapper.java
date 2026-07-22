package cn.qdm.tob.modules.system.dict.mapper;

import cn.qdm.tob.infrastructure.base.TobBaseMapper;
import cn.qdm.tob.modules.system.dict.domain.SysDict;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 字典类型 Mapper
 */
@Mapper
public interface SysDictMapper extends TobBaseMapper<SysDict> {

    /** 分页搜索字典（XML 实现，避免动态拼接） */
    Page<SysDict> pageSearch(Page<SysDict> page, @Param("keyword") String keyword);
}
