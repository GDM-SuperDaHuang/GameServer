package com.slg.module.util;

import java.util.concurrent.atomic.AtomicLong;

public class UniqueCidGenerator {
    // 使用 AtomicLong 保证线程安全的自增操作
    private static final AtomicLong counter = new AtomicLong(0);

    // 私有构造函数，防止实例化
    private UniqueCidGenerator() {}

    // 获取唯一自增的 cid
    public static long getNextCid() {
        return counter.incrementAndGet();
    }

}    