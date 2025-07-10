package com.slg.module.util;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.ListView;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

/**
 * Nacos客户端工具类，用于服务注册、配置管理和服务信息查询
 */
public class NacosClientUtil {
    private String serverAddr;
    private NamingService namingService;
    private ConfigService configService;

    private CountDownLatch connectLatch = new CountDownLatch(1);
    private volatile boolean connectFailed = false;

    /**
     * 构造函数，初始化Nacos客户端
     *
     * @param serverAddr Nacos服务器地址
     * @param namespace  命名空间ID
     * @throws NacosException 初始化异常
     */
    public NacosClientUtil(String serverAddr, String namespace) {
        this.serverAddr = serverAddr;
        try {
            Properties namingProps = new Properties();
            namingProps.put(PropertyKeyConst.SERVER_ADDR, serverAddr);
            namingProps.put(PropertyKeyConst.NAMESPACE, namespace);
            namingService = NamingFactory.createNamingService(namingProps);

            Properties configProps = new Properties();
            configProps.put(PropertyKeyConst.SERVER_ADDR, serverAddr);
            configProps.put(PropertyKeyConst.NAMESPACE, namespace);
            configService = NacosFactory.createConfigService(configProps);
        } catch (NacosException e) {
            //throws NacosException
            throw new RuntimeException(e);
        }

    }

    // ==================== 服务注册与发现相关方法 ====================

    /**
     * 注册服务实例
     *
     * @param serviceName 服务名
     * @param groupName   组名
     * @param ip          IP地址
     * @param port        端口
     * @param weight      权重
     * @param metadata    元数据
     * @throws NacosException 注册异常
     */
    public void registerInstance(String serviceName, String groupName, String ip, int port, double weight, Map<String, String> metadata) throws NacosException {
        Instance instance = new Instance();
        instance.setServiceName(serviceName);
        instance.setIp(ip);
        instance.setPort(port);
        instance.setWeight(weight);
        instance.setMetadata(metadata);
        namingService.registerInstance(serviceName, groupName, instance);
        // 注册连接状态监听器（使用反射获取内部状态）
//        registerConnectionListener();
    }
    /**
     * 注册连接状态监听器
     */
//    private void registerConnectionListener() {
//        Thread listenerThread = new Thread(() -> {
//            try {
//                // 循环检查连接状态，直到连接成功或失败
//                while (true) {
//                    if (isClientConnected()) {
//                        connectLatch.countDown();
//                        return;
//                    }
//
//                    if (connectFailed) {
//                        throw new NacosException(NacosException.SERVER_ERROR, "连接Nacos服务器失败");
//                    }
//
//                    Thread.sleep(100);
//                }
//            } catch (Exception e) {
//                connectFailed = true;
//                connectLatch.countDown();
//            }
//        });
//
//        listenerThread.setDaemon(true);
//        listenerThread.start();
//    }

//    /**
//     * 等待客户端连接到Nacos服务器
//     * @param timeoutMs 超时时间(毫秒)
//     * @return 是否成功连接
//     * @throws NacosException 连接异常
//     */
//    public boolean waitForConnected(long timeoutMs) throws NacosException {
//        try {
//            // 等待连接完成，或超时
//            boolean connected = connectLatch.await(timeoutMs, TimeUnit.MILLISECONDS);
//
//            if (!connected) {
//                throw new NacosException(NacosException.CLIENT_INVALID_PARAM,
//                        "连接Nacos服务器超时，等待时间：" + timeoutMs + "ms");
//            }
//
//            if (connectFailed) {
//                throw new NacosException(NacosException.SERVER_ERROR, "Nacos客户端连接失败");
//            }
//
//            return true;
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//            throw new NacosException(NacosException.SERVER_ERROR, "等待Nacos连接被中断", e);
//        }
//    }
//    /**
//     * 检查客户端是否已连接
//     * 使用反射获取Nacos 3.0.x内部状态
//     */
//    private boolean isClientConnected() {
//        try {
//            // 针对Nacos 3.0.2版本的反射实现
//            Field clientField = namingService.getClass().getDeclaredField("client");
//            clientField.setAccessible(true);
//            Object client = clientField.get(namingService);
//
//            Field statusField = client.getClass().getDeclaredField("status");
//            statusField.setAccessible(true);
//            String status = (String) statusField.get(client);
//
//            return "CONNECTED".equals(status);
//        } catch (Exception e) {
//            // 反射失败时，尝试通过简单请求检查
//            try {
//                namingService.getServicesOfServer(1, 1, "DEFAULT_GROUP");
//                return true;
//            } catch (Exception ex) {
//                return false;
//            }
//        }
//    }

    /**
     * 获取服务实例列表
     *
     * @param serviceName 服务名
     * @param groupName   组名
     * @param healthyOnly 是否只返回健康实例
     * @return 实例列表
     * @throws NacosException 查询异常
     */
    public List<Instance> getInstances(String serviceName, String groupName, boolean healthyOnly)
            throws NacosException {
        return namingService.selectInstances(serviceName, groupName, healthyOnly);
    }

    /**
     * 获取服务列表
     *
     * @param pageSize  每页大小
     * @param pageNo    页码
     * @param groupName 组名
     * @return 服务列表视图
     * @throws NacosException 查询异常
     */
    public ListView<String> getServiceList(int pageSize, int pageNo, String groupName) throws NacosException {
        return namingService.getServicesOfServer(pageNo, pageSize, groupName);
    }

    // ==================== 配置管理相关方法 ====================

    /**
     * 获取配置
     *
     * @param dataId    配置ID
     * @param group     配置组
     * @param timeoutMs 超时时间(毫秒)
     * @return 配置内容
     * @throws NacosException 获取异常
     */
    public String getConfig(String dataId, String group, long timeoutMs) throws NacosException {
        return configService.getConfig(dataId, group, timeoutMs);
    }

    /**
     * 发布配置
     *
     * @param dataId  配置ID
     * @param group   配置组
     * @param content 配置内容
     * @return 是否发布成功
     * @throws NacosException 发布异常
     */
    public boolean publishConfig(String dataId, String group, String content) throws NacosException {
        return configService.publishConfig(dataId, group, content);
    }

    /**
     * 删除配置
     *
     * @param dataId 配置ID
     * @param group  配置组
     * @return 是否删除成功
     * @throws NacosException 删除异常
     */
    public boolean deleteConfig(String dataId, String group) throws NacosException {
        return configService.removeConfig(dataId, group);
    }

    /**
     * 添加配置监听器
     *
     * @param dataId   配置ID
     * @param group    配置组
     * @param listener 监听器
     * @throws NacosException 添加异常
     */
    public void addConfigListener(String dataId, String group, Listener listener) throws NacosException {
        configService.addListener(dataId, group, listener);
    }

    /**
     * 移除配置监听器
     *
     * @param dataId   配置ID
     * @param group    配置组
     * @param listener 监听器
     * @throws NacosException 移除异常
     */
    public void removeConfigListener(String dataId, String group, Listener listener) throws NacosException {
        configService.removeListener(dataId, group, listener);
    }

    // ==================== 元数据管理相关方法 ====================

    /**
     * 更新实例元数据
     *
     * @param serviceName 服务名
     * @param groupName   组名
     * @param ip          IP地址
     * @param port        端口
     * @param metadata    元数据
     * @return 是否更新成功
     * @throws NacosException 更新异常
     */
    public boolean updateInstanceMetadata(String serviceName, String groupName, String ip, int port,
                                          Map<String, String> metadata) throws NacosException {
        try {
            // 获取指定服务下的所有实例（包括不健康的）
            List<Instance> instances = namingService.selectInstances(serviceName, groupName, false);

            // 查找匹配IP和端口的实例
            for (Instance instance : instances) {
                if (instance.getIp().equals(ip) && instance.getPort() == port) {
                    // 更新元数据
                    instance.setMetadata(metadata);
                    // 重新注册实例（覆盖原有元数据）
                    namingService.registerInstance(serviceName, groupName, instance);
                    return true;
                }
            }

            // 如果未找到实例，返回失败
            return false;
        } catch (Exception e) {
            // 处理异常
            throw new NacosException(NacosException.SERVER_ERROR, "更新实例元数据失败", e);
        }
    }

    /**
     * 获取实例元数据
     *
     * @param serviceName 服务名
     * @param groupName   组名
     * @param ip          IP地址
     * @param port        端口
     * @return 元数据
     * @throws NacosException 获取异常
     */
    public Map<String, String> getInstanceMetadata(String serviceName, String groupName, String ip, int port)
            throws NacosException {
        // 获取指定服务下的所有健康实例
        List<Instance> instances = namingService.selectInstances(serviceName, groupName, true);
        // 查找匹配IP和端口的实例
        for (Instance instance : instances) {
            if (instance.getIp().equals(ip) && instance.getPort() == port) {
                return instance.getMetadata();
            }
        }
        return Collections.emptyMap();
    }

    // ==================== 工具方法 ====================

    /**
     * 创建一个简单的配置监听器
     *
     * @param listener 配置变更处理逻辑
     * @return 配置监听器
     */
    public static Listener createListener(Consumer<String> listener) {
        return new Listener() {
            @Override
            public Executor getExecutor() {
                return null;
            }

            @Override
            public void receiveConfigInfo(String configInfo) {
                listener.accept(configInfo);
            }
        };
    }

    /**
     * 关闭Nacos客户端连接
     *
     * @throws NacosException 关闭异常
     */
    public void close() throws NacosException {
        if (namingService != null) {
            namingService.shutDown();
        }
    }

    // 简单的函数式接口，用于配置监听器
    @FunctionalInterface
    public interface Consumer<T> {
        void accept(T t);
    }

    // 示例用法
    public static void main(String[] args) {
        try {
            // 初始化Nacos客户端
            NacosClientUtil client = new NacosClientUtil("localhost:8848", "your-namespace-id");

            // 注册服务实例
            Map<String, String> metadata = new HashMap<>();
            metadata.put("version", "1.0.0");
            metadata.put("author", "doubao");
            client.registerInstance("my-service", "DEFAULT_GROUP", "127.0.0.1", 8080, 1.0, metadata);

            // 获取服务实例列表
            List<Instance> instances = client.getInstances("my-service", "DEFAULT_GROUP", true);
            System.out.println("服务实例列表: " + instances);

            // 发布配置
            client.publishConfig("application.properties", "DEFAULT_GROUP", "server.port=8080\nspring.application.name=my-service");

            // 获取配置
            String config = client.getConfig("application.properties", "DEFAULT_GROUP", 5000);
            System.out.println("配置内容: " + config);

            // 添加配置监听器
            client.addConfigListener("application.properties", "DEFAULT_GROUP", createListener(newConfig -> {
                System.out.println("配置已更新: " + newConfig);
            }));

            // 更新实例元数据
            Map<String, String> newMetadata = new HashMap<>();
            newMetadata.put("version", "1.0.1");
            newMetadata.put("env", "test");
            client.updateInstanceMetadata("my-service", "DEFAULT_GROUP", "127.0.0.1", 8080, newMetadata);

            // 获取实例元数据
            Map<String, String> instanceMetadata = client.getInstanceMetadata("my-service", "DEFAULT_GROUP", "127.0.0.1", 8080);
            System.out.println("实例元数据: " + instanceMetadata);

        } catch (NacosException e) {
            e.printStackTrace();
        }
    }
}
