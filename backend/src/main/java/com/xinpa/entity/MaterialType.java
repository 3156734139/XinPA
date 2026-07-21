package com.xinpa.entity;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 素材类型枚举
 */
public enum MaterialType implements IEnum<Integer> {

    IMAGE(1),
    AUDIO(2),
    VIDEO(3);

    private final int value;

    MaterialType(int value) {
        this.value = value;
    }

    @Override
    public Integer getValue() {
        return value;
    }

    @JsonValue
    public String getType() {
        return name().toLowerCase();
    }

    @JsonCreator
    public static MaterialType fromType(String type) {
        return valueOf(type.toUpperCase());
    }
}
