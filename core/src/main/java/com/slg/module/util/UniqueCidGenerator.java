package com.slg.module.util;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class UniqueCidGenerator {
    // 使用 AtomicLong 保证线程安全的自增操作
    private static final AtomicInteger counter = new AtomicInteger(0);

    // 私有构造函数，防止实例化
    private UniqueCidGenerator() {}

    // 获取唯一自增的 cid
    public static int getNextCid() {
        return counter.incrementAndGet();
    }

}    