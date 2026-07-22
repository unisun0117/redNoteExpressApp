package cn.qdm.tob.modules.system.dict.service;

import cn.qdm.tob.modules.system.dict.dto.DictPageQuery;
import cn.qdm.tob.modules.system.dict.dto.DictSaveDTO;
import cn.qdm.tob.modules.system.dict.vo.DictVO;
import com.baomidou.mybatisplus.core.metadata.IPage;

/**
 * 字典类型服务接口
 */
public interface DictService {

    /** 分页搜索字典 */
    IPage<DictVO> listPage(DictPageQuery dto);

    /** 创建字典（含字典项） */
    void create(DictSaveDTO vo);

    /** 更新字典 */
    void update(DictSaveDTO vo);

    /** 删除字典（含字典项） */
    void delete(String code);
}
