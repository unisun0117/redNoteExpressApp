package cn.qdm.tob.framework.model;

import lombok.Data;

/**
 * 分页查询基类 — 所有分页查询 VO/DTO 继承此类。
 * <p>前后端约定：默认每页 20 条，分页参数统一为 page/size。</p>
 */
@Data
public abstract class PageQuery {

    /** 页码（默认1） */
    private Integer page = 1;

    /** 每页条数（默认20） */
    private Integer size = 20;
}
