package com.basic.log.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.ValueFilter;
import com.basic.log.annotation.Mask;
import com.basic.log.properties.LogProperties;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 敏感数据脱敏工具类
 * <p>
 * 支持两种脱敏方式：
 * 1. @Mask 注解标记字段
 * 2. 配置文件中全局敏感字段名匹配
 * </p>
 *
 * @author actor
 * @date 2024/5/15
 */
public class MaskUtil {

    /** 类字段缓存，key: className, value: 需要脱敏的字段名集合 */
    private static final Map<Class<?>, Set<String>> MASK_FIELD_CACHE = new ConcurrentHashMap<>();

    /** 类字段脱敏策略缓存，key: className.fieldName, value: MaskType */
    private static final Map<String, Mask.MaskType> MASK_TYPE_CACHE = new ConcurrentHashMap<>();

    /**
     * 将对象序列化为 JSON 字符串，并对敏感字段进行脱敏
     *
     * @param obj     待序列化的对象
     * @param properties 日志配置属性
     * @return 脱敏后的 JSON 字符串
     */
    public static String toMaskedJson(Object obj, LogProperties properties) {
        if (obj == null) {
            return "null";
        }
        if (!properties.isMaskEnabled()) {
            return JSON.toJSONString(obj);
        }
        return JSON.toJSONString(obj, createMaskFilter(properties));
    }

    /**
     * 创建 Fastjson ValueFilter，对敏感字段进行脱敏
     */
    private static ValueFilter createMaskFilter(LogProperties properties) {
        Set<String> globalMaskFields = properties.getMaskFields() != null
                ? new HashSet<>(properties.getMaskFields()) : Collections.emptySet();

        return (object, name, value) -> {
            if (value == null || !(value instanceof String) || ((String) value).isEmpty()) {
                return value;
            }
            // 检查是否需要脱敏
            Mask.MaskType maskType = getMaskType(object, name, globalMaskFields);
            if (maskType != null) {
                return applyMask((String) value, maskType);
            }
            return value;
        };
    }

    /**
     * 判断字段是否需要脱敏，并返回脱敏策略
     */
    private static Mask.MaskType getMaskType(Object object, String fieldName, Set<String> globalMaskFields) {
        // 1. 优先检查 @Mask 注解
        Class<?> clazz = object.getClass();
        Set<String> annotatedFields = MASK_FIELD_CACHE.computeIfAbsent(clazz, c -> {
            Set<String> fields = new HashSet<>();
            for (Field field : getAllFields(c)) {
                Mask mask = field.getAnnotation(Mask.class);
                if (mask != null) {
                    fields.add(field.getName());
                    String key = c.getName() + "." + field.getName();
                    MASK_TYPE_CACHE.put(key, mask.type());
                }
            }
            return fields;
        });

        if (annotatedFields.contains(fieldName)) {
            String key = clazz.getName() + "." + fieldName;
            return MASK_TYPE_CACHE.getOrDefault(key, Mask.MaskType.DEFAULT);
        }

        // 2. 检查全局配置的敏感字段名
        if (globalMaskFields.contains(fieldName)) {
            return Mask.MaskType.DEFAULT;
        }

        return null;
    }

    /**
     * 获取类的所有字段（包括父类字段）
     */
    private static List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            fields.addAll(Arrays.asList(current.getDeclaredFields()));
            current = current.getSuperclass();
        }
        return fields;
    }

    /**
     * 根据策略对字符串值进行脱敏
     */
    private static String applyMask(String value, Mask.MaskType type) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        switch (type) {
            case ALL:
                return "****";
            case LAST_4:
                return value.length() <= 4 ? "****" : "****" + value.substring(value.length() - 4);
            case FIRST_3:
                return value.length() <= 3 ? "****" : value.substring(0, 3) + "****";
            case EMAIL:
                return maskEmail(value);
            case DEFAULT:
            default:
                return maskDefault(value);
        }
    }

    /**
     * 默认脱敏：保留首尾各1个字符（长度足够时），中间用 **** 替代
     */
    private static String maskDefault(String value) {
        if (value.length() <= 1) {
            return "*";
        }
        if (value.length() <= 4) {
            return value.charAt(0) + "***";
        }
        return value.charAt(0) + "****" + value.charAt(value.length() - 1);
    }

    /**
     * 邮箱脱敏：隐藏 @ 前用户名部分
     */
    private static String maskEmail(String email) {
        int atIndex = email.indexOf('@');
        if (atIndex <= 0) {
            return maskDefault(email);
        }
        String username = email.substring(0, atIndex);
        String domain = email.substring(atIndex);
        if (username.length() <= 2) {
            return "*" + domain;
        }
        return username.charAt(0) + "****" + domain;
    }
}
