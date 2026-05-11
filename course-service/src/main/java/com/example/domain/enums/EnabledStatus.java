package com.example.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum EnabledStatus {
    DISABLED(0),
    ENABLED(1);

    @EnumValue
    @JsonValue
    private final int code;

    EnabledStatus(int code) {
        this.code = code;
    }

}
