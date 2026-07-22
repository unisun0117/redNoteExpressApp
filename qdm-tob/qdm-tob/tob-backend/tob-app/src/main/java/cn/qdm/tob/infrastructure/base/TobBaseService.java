package cn.qdm.tob.infrastructure.base;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

public class TobBaseService<M extends BaseMapper<T>, T> extends ServiceImpl<M, T> {
}
