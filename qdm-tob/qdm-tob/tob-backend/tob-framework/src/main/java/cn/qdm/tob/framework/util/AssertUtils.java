package cn.qdm.tob.framework.util;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

import cn.qdm.tob.framework.exception.ErrorCode;
import cn.qdm.tob.framework.exception.TobServerException;
import cn.qdm.tob.framework.exception.TobServiceException;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * 断言工具类
 */
public final class AssertUtils {

    private AssertUtils() {
    }

    // ================================================================
    // Warn / Error 辅助工厂方法
    // ================================================================

    public static Supplier<TobServiceException> warn(int code, String message, Object... args) {
        return () -> {
            String msg = ArrayUtils.isEmpty(args) ? message : MessageFormat.format(message, args);
            return new TobServiceException(code, msg);
        };
    }

    public static Supplier<TobServiceException> warn(String message, Object... args) {
        return warn(ErrorCode.BAD_REQUEST.code(), message, args);
    }

    public static Supplier<TobServiceException> warn(ErrorCode error, Object... args) {
        return warn(error.code(), error.message(), args);
    }

    public static Supplier<TobServerException> error(int code, String message, Object... args) {
        return () -> {
            String msg = (args == null || args.length == 0) ? message : MessageFormat.format(message, args);
            return new TobServerException(code, msg);
        };
    }

    public static Supplier<TobServerException> error(String message, Object... args) {
        return error(ErrorCode.BAD_REQUEST.code(), message, args);
    }

    public static Supplier<TobServerException> error(ErrorCode error, Object... args) {
        return error(error.code(), error.message(), args);
    }

    // ================================================================
    // 核心断言：IsTrue / IsFalse
    // ================================================================

    public static void isTrue(boolean expression, Supplier<? extends RuntimeException> exceptionSupplier) {
        if (!expression) {
            throw exceptionSupplier.get();
        }
    }

    public static void isTrue(boolean expression, int code, String message, Object... args) {
        if (!expression) {
            String msg = ArrayUtils.isEmpty(args) ? message : MessageFormat.format(message, args);
            throw new TobServiceException(code, msg);
        }
    }

    public static void isTrue(boolean expression, ErrorCode error, Object... args) {
        isTrue(expression, error.code(), error.message(), args);
    }

    public static void isTrue(boolean expression, String message, Object... args) {
        isTrue(expression, ErrorCode.BAD_REQUEST.code(), message, args);
    }

    public static void isFalse(boolean expression, Supplier<? extends RuntimeException> exceptionSupplier) {
        if (expression) {
            throw exceptionSupplier.get();
        }
    }

    public static void isFalse(boolean expression, int code, String message, Object... args) {
        if (expression) {
            String msg = ArrayUtils.isEmpty(args) ? message : MessageFormat.format(message, args);
            throw new TobServiceException(code, msg);
        }
    }

    public static void isFalse(boolean expression, ErrorCode error, Object... args) {
        isFalse(expression, error.code(), error.message(), args);
    }

    public static void isFalse(boolean expression, String message, Object... args) {
        isFalse(expression, ErrorCode.BAD_REQUEST.code(), message, args);
    }

    // ================================================================
    // IsNull / NotNull
    // ================================================================

    public static void isNull(Object obj, Supplier<? extends RuntimeException> exceptionSupplier) {
        isTrue(Objects.isNull(obj), exceptionSupplier);
    }

    public static void isNull(Object obj, int code, String message, Object... args) {
        isTrue(Objects.isNull(obj), code, message, args);
    }

    public static void isNull(Object obj, ErrorCode error, Object... args) {
        isTrue(Objects.isNull(obj), error.code(), error.message(), args);
    }

    public static void isNull(Object obj, String message, Object... args) {
        isTrue(Objects.isNull(obj), ErrorCode.BAD_REQUEST.code(), message, args);
    }

    public static void notNull(Object obj, Supplier<? extends RuntimeException> exceptionSupplier) {
        isTrue(Objects.nonNull(obj), exceptionSupplier);
    }

    public static void notNull(Object obj, int code, String message, Object... args) {
        isTrue(Objects.nonNull(obj), code, message, args);
    }

    public static void notNull(Object obj, ErrorCode error, Object... args) {
        isTrue(Objects.nonNull(obj), error.code(), error.message(), args);
    }

    public static void notNull(Object obj, String message, Object... args) {
        isTrue(Objects.nonNull(obj), ErrorCode.BAD_REQUEST.code(), message, args);
    }

    // ================================================================
    // Equals / NotEquals
    // ================================================================

    public static void equals(Object a, Object b, Supplier<? extends RuntimeException> exceptionSupplier) {
        isTrue(Objects.equals(a, b), exceptionSupplier);
    }

    public static void equals(Object a, Object b, ErrorCode error, Object... args) {
        isTrue(Objects.equals(a, b), error, args);
    }

    public static void equals(Object a, Object b, String message, Object... args) {
        isTrue(Objects.equals(a, b), message, args);
    }

    public static void equals(Object a, Object b, int code, String message, Object... args) {
        isTrue(Objects.equals(a, b), code, message, args);
    }

    public static void notEquals(Object a, Object b, Supplier<? extends RuntimeException> exceptionSupplier) {
        isFalse(Objects.equals(a, b), exceptionSupplier);
    }

    public static void notEquals(Object a, Object b, ErrorCode error, Object... args) {
        isFalse(Objects.equals(a, b), error, args);
    }

    public static void notEquals(Object a, Object b, String message, Object... args) {
        isFalse(Objects.equals(a, b), message, args);
    }

    public static void notEquals(Object a, Object b, int code, String message, Object... args) {
        isFalse(Objects.equals(a, b), code, message, args);
    }

    // ================================================================
    // 字符串断言
    // ================================================================

    // ---------- IsEmpty ----------

    public static void isEmpty(String s, Supplier<? extends RuntimeException> exceptionSupplier) {
        isTrue(StringUtils.isEmpty(s), exceptionSupplier);
    }

    public static void isEmpty(String s, ErrorCode error, Object... args) {
        isTrue(StringUtils.isEmpty(s), error, args);
    }

    public static void isEmpty(String s, String message, Object... args) {
        isTrue(StringUtils.isEmpty(s), message, args);
    }

    public static void isEmpty(String s, int code, String message, Object... args) {
        isTrue(StringUtils.isEmpty(s), code, message, args);
    }

    // ---------- NotEmpty ----------

    public static void notEmpty(String s, Supplier<? extends RuntimeException> exceptionSupplier) {
        isTrue(StringUtils.isNotEmpty(s), exceptionSupplier);
    }

    public static void notEmpty(String s, ErrorCode error, Object... args) {
        isTrue(StringUtils.isNotEmpty(s), error, args);
    }

    public static void notEmpty(String s, String message, Object... args) {
        isTrue(StringUtils.isNotEmpty(s), message, args);
    }

    public static void notEmpty(String s, int code, String message, Object... args) {
        isTrue(StringUtils.isNotEmpty(s), code, message, args);
    }

    // ---------- IsBlank ----------

    public static void isBlank(String s, Supplier<? extends RuntimeException> exceptionSupplier) {
        isTrue(StringUtils.isBlank(s), exceptionSupplier);
    }

    public static void isBlank(String s, ErrorCode error, Object... args) {
        isTrue(StringUtils.isBlank(s), error, args);
    }

    public static void isBlank(String s, String message, Object... args) {
        isTrue(StringUtils.isBlank(s), message, args);
    }

    public static void isBlank(String s, int code, String message, Object... args) {
        isTrue(StringUtils.isBlank(s), code, message, args);
    }

    // ---------- NotBlank ----------

    public static void notBlank(String s, Supplier<? extends RuntimeException> exceptionSupplier) {
        isTrue(StringUtils.isNotBlank(s), exceptionSupplier);
    }

    public static void notBlank(String s, ErrorCode error, Object... args) {
        isTrue(StringUtils.isNotBlank(s), error, args);
    }

    public static void notBlank(String s, String message, Object... args) {
        isTrue(StringUtils.isNotBlank(s), message, args);
    }

    public static void notBlank(String s, int code, String message, Object... args) {
        isTrue(StringUtils.isNotBlank(s), code, message, args);
    }

    // ---------- EqualsIgnoreCase ----------

    public static void equalsIgnoreCase(String a, String b, Supplier<? extends RuntimeException> exceptionSupplier) {
        isTrue(Objects.equals(a, b) || (Objects.nonNull(a) && a.equalsIgnoreCase(b)), exceptionSupplier);
    }

    public static void equalsIgnoreCase(String a, String b, ErrorCode error, Object... args) {
        isTrue(Objects.equals(a, b) || (Objects.nonNull(a) && a.equalsIgnoreCase(b)), error, args);
    }

    public static void equalsIgnoreCase(String a, String b, String message, Object... args) {
        isTrue(Objects.equals(a, b) || (Objects.nonNull(a) && a.equalsIgnoreCase(b)), message, args);
    }

    public static void equalsIgnoreCase(String a, String b, int code, String message, Object... args) {
        isTrue(Objects.equals(a, b) || (Objects.nonNull(a) && a.equalsIgnoreCase(b)), code, message, args);
    }

    // ---------- NotEqualsIgnoreCase ----------

    public static void notEqualsIgnoreCase(String a, String b, Supplier<? extends RuntimeException> exceptionSupplier) {
        isFalse(Objects.equals(a, b) || (Objects.nonNull(a) && a.equalsIgnoreCase(b)), exceptionSupplier);
    }

    public static void notEqualsIgnoreCase(String a, String b, ErrorCode error, Object... args) {
        isFalse(Objects.equals(a, b) || (Objects.nonNull(a) && a.equalsIgnoreCase(b)), error, args);
    }

    public static void notEqualsIgnoreCase(String a, String b, String message, Object... args) {
        isFalse(Objects.equals(a, b) || (Objects.nonNull(a) && a.equalsIgnoreCase(b)), message, args);
    }

    public static void notEqualsIgnoreCase(String a, String b, int code, String message, Object... args) {
        isFalse(Objects.equals(a, b) || (Objects.nonNull(a) && a.equalsIgnoreCase(b)), code, message, args);
    }

    // ---------- Contains / NotContains ----------

    private static boolean contains(String s, String sub, boolean ignoreCase) {
        if (Objects.equals(s, sub)) return true;
        if (Objects.nonNull(s) && Objects.nonNull(sub)) {
            return ignoreCase ? s.toLowerCase().contains(sub.toLowerCase()) : s.contains(sub);
        }
        return false;
    }

    public static void contains(String a, String sub, Supplier<? extends RuntimeException> exceptionSupplier) {
        isTrue(contains(a, sub, false), exceptionSupplier);
    }

    public static void contains(String a, String sub, ErrorCode error, Object... args) {
        isTrue(contains(a, sub, false), error, args);
    }

    public static void contains(String a, String sub, String message, Object... args) {
        isTrue(contains(a, sub, false), message, args);
    }

    public static void contains(String a, String sub, int code, String message, Object... args) {
        isTrue(contains(a, sub, false), code, message, args);
    }

    // --- ContainsIgnoreCase ---

    public static void containsIgnoreCase(String a, String sub, Supplier<? extends RuntimeException> exceptionSupplier) {
        isTrue(contains(a, sub, true), exceptionSupplier);
    }

    public static void containsIgnoreCase(String a, String sub, ErrorCode error, Object... args) {
        isTrue(contains(a, sub, true), error, args);
    }

    public static void containsIgnoreCase(String a, String sub, String message, Object... args) {
        isTrue(contains(a, sub, true), message, args);
    }

    public static void containsIgnoreCase(String a, String sub, int code, String message, Object... args) {
        isTrue(contains(a, sub, true), code, message, args);
    }

    // --- NotContains ---

    public static void notContains(String a, String sub, Supplier<? extends RuntimeException> exceptionSupplier) {
        isFalse(contains(a, sub, false), exceptionSupplier);
    }

    public static void notContains(String a, String sub, ErrorCode error, Object... args) {
        isFalse(contains(a, sub, false), error, args);
    }

    public static void notContains(String a, String sub, String message, Object... args) {
        isFalse(contains(a, sub, false), message, args);
    }

    public static void notContains(String a, String sub, int code, String message, Object... args) {
        isFalse(contains(a, sub, false), code, message, args);
    }

    // --- NotContainsIgnoreCase ---

    public static void notContainsIgnoreCase(String a, String sub, Supplier<? extends RuntimeException> exceptionSupplier) {
        isFalse(contains(a, sub, true), exceptionSupplier);
    }

    public static void notContainsIgnoreCase(String a, String sub, ErrorCode error, Object... args) {
        isFalse(contains(a, sub, true), error, args);
    }

    public static void notContainsIgnoreCase(String a, String sub, String message, Object... args) {
        isFalse(contains(a, sub, true), message, args);
    }

    public static void notContainsIgnoreCase(String a, String sub, int code, String message, Object... args) {
        isFalse(contains(a, sub, true), code, message, args);
    }

    // ---------- StartsWith / EndsWith ----------

    private static boolean startsWith(String s, String prefix, boolean ignoreCase) {
        if (Objects.equals(s, prefix)) return true;
        if (Objects.nonNull(s) && Objects.nonNull(prefix)) {
            return ignoreCase ? s.toLowerCase().startsWith(prefix.toLowerCase()) : s.startsWith(prefix);
        }
        return false;
    }

    public static void startsWith(String s, String prefix, Supplier<? extends RuntimeException> exceptionSupplier) {
        isTrue(startsWith(s, prefix, false), exceptionSupplier);
    }

    public static void startsWith(String s, String prefix, ErrorCode error, Object... args) {
        isTrue(startsWith(s, prefix, false), error, args);
    }

    public static void startsWith(String s, String prefix, String message, Object... args) {
        isTrue(startsWith(s, prefix, false), message, args);
    }

    public static void startsWith(String s, String prefix, int code, String message, Object... args) {
        isTrue(startsWith(s, prefix, false), code, message, args);
    }

    // --- StartsWithIgnoreCase ---

    public static void startsWithIgnoreCase(String s, String prefix, Supplier<? extends RuntimeException> exceptionSupplier) {
        isTrue(startsWith(s, prefix, true), exceptionSupplier);
    }

    public static void startsWithIgnoreCase(String s, String prefix, ErrorCode error, Object... args) {
        isTrue(startsWith(s, prefix, true), error, args);
    }

    public static void startsWithIgnoreCase(String s, String prefix, String message, Object... args) {
        isTrue(startsWith(s, prefix, true), message, args);
    }

    public static void startsWithIgnoreCase(String s, String prefix, int code, String message, Object... args) {
        isTrue(startsWith(s, prefix, true), code, message, args);
    }

    // --- EndsWith ---

    private static boolean endsWith(String s, String suffix, boolean ignoreCase) {
        if (Objects.equals(s, suffix)) return true;
        if (Objects.nonNull(s) && Objects.nonNull(suffix)) {
            return ignoreCase ? s.toLowerCase().endsWith(suffix.toLowerCase()) : s.endsWith(suffix);
        }
        return false;
    }

    public static void endsWith(String s, String suffix, Supplier<? extends RuntimeException> exceptionSupplier) {
        isTrue(endsWith(s, suffix, false), exceptionSupplier);
    }

    public static void endsWith(String s, String suffix, ErrorCode error, Object... args) {
        isTrue(endsWith(s, suffix, false), error, args);
    }

    public static void endsWith(String s, String suffix, String message, Object... args) {
        isTrue(endsWith(s, suffix, false), message, args);
    }

    public static void endsWith(String s, String suffix, int code, String message, Object... args) {
        isTrue(endsWith(s, suffix, false), code, message, args);
    }

    // --- EndsWithIgnoreCase ---

    public static void endsWithIgnoreCase(String s, String suffix, Supplier<? extends RuntimeException> exceptionSupplier) {
        isTrue(endsWith(s, suffix, true), exceptionSupplier);
    }

    public static void endsWithIgnoreCase(String s, String suffix, ErrorCode error, Object... args) {
        isTrue(endsWith(s, suffix, true), error, args);
    }

    public static void endsWithIgnoreCase(String s, String suffix, String message, Object... args) {
        isTrue(endsWith(s, suffix, true), message, args);
    }

    public static void endsWithIgnoreCase(String s, String suffix, int code, String message, Object... args) {
        isTrue(endsWith(s, suffix, true), code, message, args);
    }

    // ================================================================
    // 集合/迭代器断言
    // ================================================================

    // ---------- IsEmpty (Iterable) ----------

    private static boolean isEmpty(Iterable<?> iterable) {
        return !iterable.iterator().hasNext();
    }

    public static void isEmpty(Iterable<?> iterable, Supplier<? extends RuntimeException> exceptionSupplier) {
        isTrue(isEmpty(iterable), exceptionSupplier);
    }

    public static void isEmpty(Iterable<?> iterable, ErrorCode error, Object... args) {
        isTrue(isEmpty(iterable), error, args);
    }

    public static void isEmpty(Iterable<?> iterable, String message, Object... args) {
        isTrue(isEmpty(iterable), message, args);
    }

    public static void isEmpty(Iterable<?> iterable, int code, String message, Object... args) {
        isTrue(isEmpty(iterable), code, message, args);
    }

    // ---------- NotEmpty (Iterable) ----------

    private static boolean isNotEmpty(Iterable<?> iterable) {
        return iterable.iterator().hasNext();
    }

    public static void notEmpty(Iterable<?> iterable, Supplier<? extends RuntimeException> exceptionSupplier) {
        isTrue(isNotEmpty(iterable), exceptionSupplier);
    }

    public static void notEmpty(Iterable<?> iterable, ErrorCode error, Object... args) {
        isTrue(isNotEmpty(iterable), error, args);
    }

    public static void notEmpty(Iterable<?> iterable, String message, Object... args) {
        isTrue(isNotEmpty(iterable), message, args);
    }

    public static void notEmpty(Iterable<?> iterable, int code, String message, Object... args) {
        isTrue(isNotEmpty(iterable), code, message, args);
    }

    // ---------- Contains (Iterable) ----------

    private static <T> boolean contains(Iterable<T> collection, T target) {
        if (Objects.isNull(collection)) return false;
        for (T item : collection) {
            if (Objects.equals(item, target)) return true;
        }
        return false;
    }

    public static <T> void contains(Iterable<T> collection, T target, Supplier<? extends RuntimeException> exceptionSupplier) {
        isTrue(contains(collection, target), exceptionSupplier);
    }

    public static <T> void contains(Iterable<T> collection, T target, ErrorCode error, Object... args) {
        isTrue(contains(collection, target), error, args);
    }

    public static <T> void contains(Iterable<T> collection, T target, String message, Object... args) {
        isTrue(contains(collection, target), message, args);
    }

    public static <T> void contains(Iterable<T> collection, T target, int code, String message, Object... args) {
        isTrue(contains(collection, target), code, message, args);
    }

    // ---------- NotContains (Iterable) ----------

    private static <T> boolean notContains(Iterable<T> collection, T target) {
        return !contains(collection, target);
    }

    public static <T> void notContains(Iterable<T> collection, T target, Supplier<? extends RuntimeException> exceptionSupplier) {
        isTrue(notContains(collection, target), exceptionSupplier);
    }

    public static <T> void notContains(Iterable<T> collection, T target, ErrorCode error, Object... args) {
        isTrue(notContains(collection, target), error, args);
    }

    public static <T> void notContains(Iterable<T> collection, T target, String message, Object... args) {
        isTrue(notContains(collection, target), message, args);
    }

    public static <T> void notContains(Iterable<T> collection, T target, int code, String message, Object... args) {
        isTrue(notContains(collection, target), code, message, args);
    }

    // ---------- ContainsAny ----------

    private static <T> boolean containsAny(Iterable<T> c1, Iterable<T> c2) {
        if (Objects.isNull(c1) || Objects.isNull(c2)) return false;
        Set<T> set = new HashSet<>();
        c1.forEach(set::add);
        for (T e : c2) {
            if (set.contains(e)) return true;
        }
        return false;
    }

    public static <T> void containsAny(Iterable<T> c1, Iterable<T> c2, Supplier<? extends RuntimeException> exceptionSupplier) {
        isTrue(containsAny(c1, c2), exceptionSupplier);
    }

    public static <T> void containsAny(Iterable<T> c1, Iterable<T> c2, ErrorCode error, Object... args) {
        isTrue(containsAny(c1, c2), error, args);
    }

    public static <T> void containsAny(Iterable<T> c1, Iterable<T> c2, String message, Object... args) {
        isTrue(containsAny(c1, c2), message, args);
    }

    public static <T> void containsAny(Iterable<T> c1, Iterable<T> c2, int code, String message, Object... args) {
        isTrue(containsAny(c1, c2), code, message, args);
    }

    // ---------- ContainsAll ----------

    private static <T> boolean containsAll(Iterable<T> c1, Iterable<T> c2) {
        if (Objects.isNull(c1) || Objects.isNull(c2)) return false;
        Set<T> set = new HashSet<>();
        c1.forEach(set::add);
        for (T e : c2) {
            if (!set.contains(e)) return false;
        }
        return true;
    }

    public static <T> void containsAll(Iterable<T> c1, Iterable<T> c2, Supplier<? extends RuntimeException> exceptionSupplier) {
        isTrue(containsAll(c1, c2), exceptionSupplier);
    }

    public static <T> void containsAll(Iterable<T> c1, Iterable<T> c2, ErrorCode error, Object... args) {
        isTrue(containsAll(c1, c2), error, args);
    }

    public static <T> void containsAll(Iterable<T> c1, Iterable<T> c2, String message, Object... args) {
        isTrue(containsAll(c1, c2), message, args);
    }

    public static <T> void containsAll(Iterable<T> c1, Iterable<T> c2, int code, String message, Object... args) {
        isTrue(containsAll(c1, c2), code, message, args);
    }

    // ================================================================
    // 日期断言
    // ================================================================

    // ---------- IsSameDay (LocalDateTime) ----------

    private static boolean isSameDay(LocalDateTime d1, LocalDateTime d2) {
        return Objects.nonNull(d1) && Objects.nonNull(d2) && d1.toLocalDate().equals(d2.toLocalDate());
    }

    public static void isSameDay(LocalDateTime d1, LocalDateTime d2, Supplier<? extends RuntimeException> exceptionSupplier) {
        isTrue(isSameDay(d1, d2), exceptionSupplier);
    }

    public static void isSameDay(LocalDateTime d1, LocalDateTime d2, ErrorCode error, Object... args) {
        isTrue(isSameDay(d1, d2), error, args);
    }

    public static void isSameDay(LocalDateTime d1, LocalDateTime d2, String message, Object... args) {
        isTrue(isSameDay(d1, d2), message, args);
    }

    public static void isSameDay(LocalDateTime d1, LocalDateTime d2, int code, String message, Object... args) {
        isTrue(isSameDay(d1, d2), code, message, args);
    }

    // ---------- IsSameDay (LocalDate) ----------

    private static boolean isSameDay(LocalDate d1, LocalDate d2) {
        return Objects.nonNull(d1) && d1.equals(d2);
    }

    public static void isSameDay(LocalDate d1, LocalDate d2, Supplier<? extends RuntimeException> exceptionSupplier) {
        isTrue(isSameDay(d1, d2), exceptionSupplier);
    }

    public static void isSameDay(LocalDate d1, LocalDate d2, ErrorCode error, Object... args) {
        isTrue(isSameDay(d1, d2), error, args);
    }

    public static void isSameDay(LocalDate d1, LocalDate d2, String message, Object... args) {
        isTrue(isSameDay(d1, d2), message, args);
    }

    public static void isSameDay(LocalDate d1, LocalDate d2, int code, String message, Object... args) {
        isTrue(isSameDay(d1, d2), code, message, args);
    }

    // ---------- IsSameDay (LocalDate, LocalDateTime) ----------

    private static boolean isSameDay(LocalDate d1, LocalDateTime d2) {
        return Objects.nonNull(d1) && Objects.nonNull(d2) && d1.equals(d2.toLocalDate());
    }

    public static void isSameDay(LocalDate d1, LocalDateTime d2, Supplier<? extends RuntimeException> exceptionSupplier) {
        isTrue(isSameDay(d1, d2), exceptionSupplier);
    }

    public static void isSameDay(LocalDate d1, LocalDateTime d2, ErrorCode error, Object... args) {
        isTrue(isSameDay(d1, d2), error, args);
    }

    public static void isSameDay(LocalDate d1, LocalDateTime d2, String message, Object... args) {
        isTrue(isSameDay(d1, d2), message, args);
    }

    public static void isSameDay(LocalDate d1, LocalDateTime d2, int code, String message, Object... args) {
        isTrue(isSameDay(d1, d2), code, message, args);
    }

    // ---------- IsSameDay (LocalDateTime, LocalDate) ----------

    public static void isSameDay(LocalDateTime d1, LocalDate d2, Supplier<? extends RuntimeException> exceptionSupplier) {
        isTrue(isSameDay(d2, d1), exceptionSupplier);
    }

    public static void isSameDay(LocalDateTime d1, LocalDate d2, ErrorCode error, Object... args) {
        isTrue(isSameDay(d2, d1), error, args);
    }

    public static void isSameDay(LocalDateTime d1, LocalDate d2, String message, Object... args) {
        isTrue(isSameDay(d2, d1), message, args);
    }

    public static void isSameDay(LocalDateTime d1, LocalDate d2, int code, String message, Object... args) {
        isTrue(isSameDay(d2, d1), code, message, args);
    }

    // ---------- IsDifferentDay (LocalDateTime) ----------

    public static void isDifferentDay(LocalDateTime d1, LocalDateTime d2, Supplier<? extends RuntimeException> exceptionSupplier) {
        isFalse(isSameDay(d1, d2), exceptionSupplier);
    }

    public static void isDifferentDay(LocalDateTime d1, LocalDateTime d2, ErrorCode error, Object... args) {
        isFalse(isSameDay(d1, d2), error, args);
    }

    public static void isDifferentDay(LocalDateTime d1, LocalDateTime d2, String message, Object... args) {
        isFalse(isSameDay(d1, d2), message, args);
    }

    public static void isDifferentDay(LocalDateTime d1, LocalDateTime d2, int code, String message, Object... args) {
        isFalse(isSameDay(d1, d2), code, message, args);
    }

    // ---------- IsDifferentDay (LocalDate) ----------

    public static void isDifferentDay(LocalDate d1, LocalDate d2, Supplier<? extends RuntimeException> exceptionSupplier) {
        isFalse(isSameDay(d1, d2), exceptionSupplier);
    }

    public static void isDifferentDay(LocalDate d1, LocalDate d2, ErrorCode error, Object... args) {
        isFalse(isSameDay(d1, d2), error, args);
    }

    public static void isDifferentDay(LocalDate d1, LocalDate d2, String message, Object... args) {
        isFalse(isSameDay(d1, d2), message, args);
    }

    public static void isDifferentDay(LocalDate d1, LocalDate d2, int code, String message, Object... args) {
        isFalse(isSameDay(d1, d2), code, message, args);
    }

    // ---------- IsDifferentDay (LocalDate, LocalDateTime) ----------

    public static void isDifferentDay(LocalDate d1, LocalDateTime d2, Supplier<? extends RuntimeException> exceptionSupplier) {
        isFalse(isSameDay(d1, d2), exceptionSupplier);
    }

    public static void isDifferentDay(LocalDate d1, LocalDateTime d2, ErrorCode error, Object... args) {
        isFalse(isSameDay(d1, d2), error, args);
    }

    public static void isDifferentDay(LocalDate d1, LocalDateTime d2, String message, Object... args) {
        isFalse(isSameDay(d1, d2), message, args);
    }

    public static void isDifferentDay(LocalDate d1, LocalDateTime d2, int code, String message, Object... args) {
        isFalse(isSameDay(d1, d2), code, message, args);
    }

    // ---------- IsDifferentDay (LocalDateTime, LocalDate) ----------

    public static void isDifferentDay(LocalDateTime d1, LocalDate d2, Supplier<? extends RuntimeException> exceptionSupplier) {
        isFalse(isSameDay(d2, d1), exceptionSupplier);
    }

    public static void isDifferentDay(LocalDateTime d1, LocalDate d2, ErrorCode error, Object... args) {
        isFalse(!isSameDay(d2, d1), error, args);
    }

    public static void isDifferentDay(LocalDateTime d1, LocalDate d2, String message, Object... args) {
        isFalse(!isSameDay(d2, d1), message, args);
    }

    public static void isDifferentDay(LocalDateTime d1, LocalDate d2, int code, String message, Object... args) {
        isFalse(!isSameDay(d2, d1), code, message, args);
    }

    // ================================================================
    // 可比大小断言
    // ================================================================

    // ---------- Lt (Less Than) ----------

    private static <T extends Comparable<T>> boolean lt(T a, T b) {
        return Objects.nonNull(a) && Objects.nonNull(b) && a.compareTo(b) < 0;
    }

    public static <T extends Comparable<T>> void lt(T a, T b, Supplier<? extends RuntimeException> exceptionSupplier) {
        isTrue(lt(a, b), exceptionSupplier);
    }

    public static <T extends Comparable<T>> void lt(T a, T b, ErrorCode error, Object... args) {
        isTrue(lt(a, b), error, args);
    }

    public static <T extends Comparable<T>> void lt(T a, T b, int code, String message, Object... args) {
        isTrue(lt(a, b), code, message, args);
    }

    public static <T extends Comparable<T>> void lt(T a, T b, String message, Object... args) {
        isTrue(lt(a, b), message, args);
    }

    // ---------- Le (Less than or Equal) ----------

    private static <T extends Comparable<T>> boolean le(T a, T b) {
        return a != null && b != null && a.compareTo(b) <= 0;
    }

    public static <T extends Comparable<T>> void le(T a, T b, Supplier<? extends RuntimeException> exceptionSupplier) {
        isTrue(le(a, b), exceptionSupplier);
    }

    public static <T extends Comparable<T>> void le(T a, T b, ErrorCode error, Object... args) {
        isTrue(le(a, b), error, args);
    }

    public static <T extends Comparable<T>> void le(T a, T b, int code, String message, Object... args) {
        isTrue(le(a, b), code, message, args);
    }

    public static <T extends Comparable<T>> void le(T a, T b, String message, Object... args) {
        isTrue(le(a, b), message, args);
    }

    // ---------- Gt (Greater Than) ----------

    private static <T extends Comparable<T>> boolean gt(T a, T b) {
        return a != null && b != null && a.compareTo(b) > 0;
    }

    public static <T extends Comparable<T>> void gt(T a, T b, Supplier<? extends RuntimeException> exceptionSupplier) {
        isTrue(gt(a, b), exceptionSupplier);
    }

    public static <T extends Comparable<T>> void gt(T a, T b, ErrorCode error, Object... args) {
        isTrue(gt(a, b), error, args);
    }

    public static <T extends Comparable<T>> void gt(T a, T b, int code, String message, Object... args) {
        isTrue(gt(a, b), code, message, args);
    }

    public static <T extends Comparable<T>> void gt(T a, T b, String message, Object... args) {
        isTrue(gt(a, b), message, args);
    }

    // ---------- Ge (Greater than or Equal) ----------

    private static <T extends Comparable<T>> boolean ge(T a, T b) {
        return a != null && b != null && a.compareTo(b) >= 0;
    }

    public static <T extends Comparable<T>> void ge(T a, T b, Supplier<? extends RuntimeException> exceptionSupplier) {
        isTrue(ge(a, b), exceptionSupplier);
    }

    public static <T extends Comparable<T>> void ge(T a, T b, ErrorCode error, Object... args) {
        isTrue(ge(a, b), error, args);
    }

    public static <T extends Comparable<T>> void ge(T a, T b, int code, String message, Object... args) {
        isTrue(ge(a, b), code, message, args);
    }

    public static <T extends Comparable<T>> void ge(T a, T b, String message, Object... args) {
        isTrue(ge(a, b), message, args);
    }

    // ---------- Contains (Interval) ----------

    private static <T extends Comparable<T>> boolean contains(Range<T> range, T value) {
        return value != null && range.contains(value);
    }

    public static <T extends Comparable<T>> void contains(Range<T> range, T value, Supplier<? extends RuntimeException> exceptionSupplier) {
        isTrue(contains(range, value), exceptionSupplier);
    }

    public static <T extends Comparable<T>> void contains(Range<T> range, T value, ErrorCode error, Object... args) {
        isTrue(contains(range, value), error, args);
    }

    public static <T extends Comparable<T>> void contains(Range<T> range, T value, int code, String message, Object... args) {
        isTrue(contains(range, value), code, message, args);
    }

    public static <T extends Comparable<T>> void contains(Range<T> range, T value, String message, Object... args) {
        isTrue(contains(range, value), message, args);
    }
}
