package mDimension.tool;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.concurrent.ConcurrentHashMap;

public final class ReflectUtils {
    private ReflectUtils() {}

    // ==================== 缓存：类+字段名 → Field 对象 ====================
    // 用 ConcurrentHashMap 保证线程安全，Mindustry 多线程环境下也安全
    private static final ConcurrentHashMap<String, Field> FIELD_CACHE = new ConcurrentHashMap<>();

    /**
     * 获取字段的"路标"（Field 对象），支持递归查找父类、自动 setAccessible。
     * 返回的 Field 可以反复用于不同实例，建议缓存到 static final 中。
     *
     * @param clazz     字段所在的类
     * @param fieldName 字段名
     * @return Field 对象（已 setAccessible）
     * @throws RuntimeException 包装后的 NoSuchFieldException
     */
    public static Field getField(Class<?> clazz, String fieldName) {
        if (clazz == null || fieldName == null || fieldName.isEmpty()) {
            throw new IllegalArgumentException("clazz 和 fieldName 不能为空");
        }

        String cacheKey = clazz.getName() + "#" + fieldName;
        Field cached = FIELD_CACHE.get(cacheKey);
        if (cached != null) return cached;

        Class<?> current = clazz;
        while (current != null) {
            try {
                Field field = current.getDeclaredField(fieldName);
                field.setAccessible(true);

                // 放入缓存，下次直接命中
                FIELD_CACHE.put(cacheKey, field);
                return field;

            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }

        throw new RuntimeException(
                "字段 '" + fieldName + "' 在 " + clazz.getName() + " 及其父类中未找到");
    }

    /**
     * 便捷重载：从对象实例的类开始查找字段。
     */
    public static Field getField(Object target, String fieldName) {
        if (target == null) {
            throw new IllegalArgumentException("target 不能为 null");
        }
        return getField(target.getClass(), fieldName);
    }

    // ==================== 基于缓存 Field 的快速取值 ====================

    public static Object getValue(Object target, String fieldName) {
        try {
            return getField(target, fieldName).get(target);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("反射取值失败: " + fieldName, e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T getValue(Object target, String fieldName, Class<T> type) {
        Object value = getValue(target, fieldName);
        if (value != null && !type.isInstance(value)) {
            throw new ClassCastException(
                    "字段 " + fieldName + " 实际类型 " + value.getClass().getName()
                            + " 无法转为 " + type.getName());
        }
        return (T) value;
    }

    // 静态字段取值（target 传 null）
    public static Object getStaticValue(Class<?> clazz, String fieldName) {
        try {
            Field f = getField(clazz, fieldName);
            if (!Modifier.isStatic(f.getModifiers())) {
                throw new IllegalArgumentException(fieldName + " 不是静态字段");
            }
            return f.get(null);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    // ==================== 基于缓存 Field 的快速设值 ====================

    public static void setValue(Object target, String fieldName, Object value) {
        try {
            getField(target, fieldName).set(target, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("反射设值失败: " + fieldName, e);
        }
    }

    public static void setStaticValue(Class<?> clazz, String fieldName, Object value) {
        try {
            Field f = getField(clazz, fieldName);
            if (!Modifier.isStatic(f.getModifiers())) {
                throw new IllegalArgumentException(fieldName + " 不是静态字段");
            }
            f.set(null, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}