package cn.qdm.tob.modules.system.dict.mapper;

import cn.qdm.tob.framework.model.RecordStatus;
import cn.qdm.tob.infrastructure.base.TobBaseMapper;
import cn.qdm.tob.modules.system.dict.domain.SysDictItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 字典项 Mapper
 */
@Mapper
public interface SysDictItemMapper extends TobBaseMapper<SysDictItem> {

    /** 按字典编码和状态查询，按排序号升序。status 为 null 时不参与过滤 */
    default List<SysDictItem> findByDictCodeAndStatus(String code, RecordStatus status) {
        return lambdaSelect(w -> {
            w.eq(SysDictItem::getDictCode, code);
            w.eq(status != null, SysDictItem::getStatus, status);
            w.orderByAsc(SysDictItem::getSort);
        });
    }

    /** 按字典编码模糊搜索字典项（XML 管理 SQL） */
    List<SysDictItem> search(@Param("code") String code,
                             @Param("keyword") String keyword,
                             @Param("status") String status);

    /** 按字典编码删除全部字典项 */
    default int deleteByDictCode(String code) {
        return lambdaDelete(w -> w.eq(SysDictItem::getDictCode, code));
    }

    /** 按字典编码和数据值删除单条字典项 */
    default int deleteByDictCodeAndValue(String dictCode, String value) {
        return lambdaDelete(w -> w.eq(SysDictItem::getDictCode, dictCode)
                .eq(SysDictItem::getValue, value));
    }
}
