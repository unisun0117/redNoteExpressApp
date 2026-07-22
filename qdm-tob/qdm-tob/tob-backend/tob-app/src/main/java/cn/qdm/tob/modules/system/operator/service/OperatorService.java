package cn.qdm.tob.modules.system.operator.service;

import cn.qdm.tob.modules.system.operator.dto.OperatorPageQuery;
import cn.qdm.tob.modules.system.operator.dto.OperatorSaveDTO;
import cn.qdm.tob.modules.system.operator.vo.OperatorVO;
import com.baomidou.mybatisplus.core.metadata.IPage;

/**
 * 运营人员管理服务接口
 */
public interface OperatorService {

    /** 分页搜索运营人员 */
    IPage<OperatorVO> listPage(OperatorPageQuery dto);

    /** 查看详情 */
    OperatorVO get(Long id);

    /** 创建运营人员 */
    void create(OperatorSaveDTO dto);

    /** 更新运营人员 */
    void update(Long id, OperatorSaveDTO dto);

    /** 启用/停用 */
    void updateStatus(Long id, String status);
}
