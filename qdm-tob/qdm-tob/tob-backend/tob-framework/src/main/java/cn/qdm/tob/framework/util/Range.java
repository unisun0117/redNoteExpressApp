package cn.qdm.tob.framework.util;

import lombok.Getter;

/**
 * 区间模型，表示一个值范围 [min, max]
 *
 * @param <T> 可比较的类型
 */
@Getter
public class Range<T extends Comparable<T>> {

    private final T min;
    private final T max;
    private final boolean minInclusive;
    private final boolean maxInclusive;

    public Range(T min, T max, boolean minInclusive, boolean maxInclusive) {
        if (min.compareTo(max) > 0) {
            throw new IllegalArgumentException("参数 Min 必须小于或等于 Max");
        }
        AssertUtils.lt(min, max, () -> new IllegalArgumentException("参数 Min 必须小于或等于 Max"));
        this.min = min;
        this.max = max;
        this.minInclusive = minInclusive;
        this.maxInclusive = maxInclusive;
    }

    public Range(T min, T max) {
        this(min, max, true, true);
    }

    /**
     * 检查值是否在区间内
     */
    public boolean contains(T value) {
        if (value == null) {
            throw new IllegalArgumentException("value 不能为 null");
        }
        int minCmp = value.compareTo(min);
        int maxCmp = value.compareTo(max);

        boolean minCheck = minInclusive ? minCmp >= 0 : minCmp > 0;
        boolean maxCheck = maxInclusive ? maxCmp <= 0 : maxCmp < 0;

        return minCheck && maxCheck;
    }

    // ========== 静态工厂方法 ==========

    /** 闭区间 min &lt;= value &lt;= max */
    public static <T extends Comparable<T>> Range<T> closed(T min, T max) {
        return new Range<>(min, max, true, true);
    }

    /** 开区间 min &lt; value &lt; max */
    public static <T extends Comparable<T>> Range<T> open(T min, T max) {
        return new Range<>(min, max, false, false);
    }

    /** 左开右闭区间 min &lt;= value &lt; max */
    public static <T extends Comparable<T>> Range<T> leftOpen(T min, T max) {
        return new Range<>(min, max, false, true);
    }

    /** 左闭右开区间 min &lt; value &lt;= max */
    public static <T extends Comparable<T>> Range<T> rightOpen(T min, T max) {
        return new Range<>(min, max, true, false);
    }

    @Override
    public String toString() {
        String left = minInclusive ? "[" : "(";
        String right = maxInclusive ? "]" : ")";
        return left + min + ", " + max + right;
    }
}
