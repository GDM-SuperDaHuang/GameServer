package com.slg.module.connection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * 目标服务器内部消息管理
 */
public class UserInfoManage {
    private static UserInfoManage instance;

    //目标服务器连接管理
    private final ConcurrentHashMap<Long, ExecutorService> userThreadMap = new ConcurrentHashMap<>();

    // 虚拟线程工厂（命名线程）
    private static final ThreadFactory virtualThreadFactory = Thread.ofVirtual()
            .name("user-processor-", 0)
            .factory();

    private UserInfoManage() {
    }
    public static UserInfoManage getInstance() {
        if (instance == null) {
            synchronized (UserInfoManage.class) {
                if (instance == null) {
                    instance = new UserInfoManage();
                }
            }
        }
        return instance;
    }

    public ExecutorService getVirtualThread(Long userId) {
        return userThreadMap.computeIfAbsent(userId, uid -> Executors.newSingleThreadExecutor(virtualThreadFactory));
    }
}
