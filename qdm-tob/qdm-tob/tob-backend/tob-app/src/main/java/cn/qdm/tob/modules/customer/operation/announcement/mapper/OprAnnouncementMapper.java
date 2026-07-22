package cn.qdm.tob.modules.customer.operation.announcement.mapper;

import cn.qdm.tob.infrastructure.base.TobBaseMapper;
import cn.qdm.tob.modules.customer.operation.announcement.domain.OprAnnouncement;
import org.apache.ibatis.annotations.Mapper;

/**
 * 公告 Mapper
 * <p>
 * 继承 {@link TobBaseMapper} 直接获得 lambdaSelect / lambdaSelectOne / lambdaCount /
 * lambdaExists / lambdaUpdate / lambdaDelete 等 Lambda 查询默认方法，无需重复声明。
 */
@Mapper
public interface OprAnnouncementMapper extends TobBaseMapper<OprAnnouncement> {
}
