package cn.qdm.tob.infrastructure.base;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public interface TobBaseMapper<T> extends BaseMapper<T> {
    default List<T> lambdaSelect(IPage<T> page, Consumer<LambdaQueryWrapper<T>> consumer) {
        var wrapper = Wrappers.<T>lambdaQuery();
        consumer.accept(wrapper);
        return selectList(page, wrapper);
    }

    default List<T> lambdaSelect(Consumer<LambdaQueryWrapper<T>> consumer) {
        var wrapper = Wrappers.<T>lambdaQuery();
        consumer.accept(wrapper);
        return selectList(wrapper);
    }

    default Optional<T> lambdaSelectOne(Consumer<LambdaQueryWrapper<T>> consumer) {
        var wrapper = Wrappers.<T>lambdaQuery();
        consumer.accept(wrapper);
        T entity = selectOne(wrapper);
        return Optional.ofNullable(entity);
    }

    default int lambdaDelete(Consumer<LambdaQueryWrapper<T>> consumer) {
        var wrapper = Wrappers.<T>lambdaQuery();
        consumer.accept(wrapper);
        return delete(wrapper);
    }

    default int lambdaUpdate(Consumer<LambdaUpdateWrapper<T>> consumer) {
        var wrapper = Wrappers.<T>lambdaUpdate();
        consumer.accept(wrapper);
        return update(wrapper);
    }

    default Long lambdaCount(Consumer<LambdaQueryWrapper<T>> consumer) {
        var wrapper = Wrappers.<T>lambdaQuery();
        consumer.accept(wrapper);
        return selectCount(wrapper);
    }

    default boolean lambdaExists(Consumer<LambdaQueryWrapper<T>> consumer) {
        var wrapper = Wrappers.<T>lambdaQuery();
        consumer.accept(wrapper);
        return exists(wrapper);
    }
}
