package com.example.mq;

import java.time.LocalDateTime;

public record OrderTimeoutMessage(
        String orderNo,
        Long userId,
        LocalDateTime expireTime
) {
}
