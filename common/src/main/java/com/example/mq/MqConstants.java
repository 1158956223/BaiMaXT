package com.example.mq;

public final class MqConstants {

    public static final String ORDER_EXCHANGE = "baimaxt.order.exchange";
    public static final String PAY_EXCHANGE = "baimaxt.pay.exchange";
    public static final String COURSE_EXCHANGE = "baimaxt.course.exchange";

    public static final String ORDER_TIMEOUT_DELAY_QUEUE = "order.timeout.delay.queue";
    public static final String ORDER_TIMEOUT_CLOSE_QUEUE = "order.timeout.close.queue";
    public static final String ORDER_PAYMENT_SUCCESS_QUEUE = "order.payment.success.queue";
    public static final String SEARCH_COURSE_INDEX_QUEUE = "search.course.index.queue";

    public static final String ORDER_TIMEOUT_DELAY_ROUTING_KEY = "order.timeout.delay";
    public static final String ORDER_TIMEOUT_CLOSE_ROUTING_KEY = "order.timeout.close";
    public static final String PAYMENT_SUCCESS_ROUTING_KEY = "payment.success";
    public static final String COURSE_INDEX_CHANGED_ROUTING_KEY = "course.index.changed";

    public static final int ORDER_TIMEOUT_TTL_MILLIS = 30 * 60 * 1000;

    private MqConstants() {
    }
}
