//package com.slg.module.rpc.server;
//
//import com.alibaba.nacos.api.exception.NacosException;
//import com.alibaba.nacos.api.naming.listener.Event;
//import com.alibaba.nacos.api.naming.listener.EventListener;
//import com.alibaba.nacos.api.naming.pojo.Instance;
//import com.alibaba.nacos.client.naming.event.InstancesChangeEvent;
//import com.slg.module.message.Constants;
//import com.slg.module.rpc.client.ServerConfig;
//import com.slg.module.util.ConfigReader;
//import com.slg.module.util.NacosClientUtil;
//
//import java.util.*;
//
//
//public class ServerConfigManager {
//    private static volatile ServerConfigManager instance;
//    NacosClientUtil client = NacosClientUtil.getAlreadyInstance();
//    private HashMap<String, ServerConfig> serverConfigMap = new HashMap<>();//所有可能的健康的实例
//
//    //有序从小到大排序
//    private List<List<ServerConfig>> serverConfigList = new ArrayList<>();
//
//
//    private ServerConfigManager() {
//        this.init();
//    }
//
//    //初始化
//    private void init() {
//        try {
//            ConfigReader configReader = new ConfigReader("application.properties");
//            String serviceName = configReader.getProperty("server.name");//
//            String groupName = configReader.getProperty("nacos.group");//
//            String config = client.getConfig(
//                    "node.properties",  // 配置ID
//                    "node",         // 配置组
//                    5000                     // 超时时间(ms)
//            );
//            List<Instance> allInstances = client.getAllInstances(serviceName, groupName);
//            for (Instance allInstance : allInstances) {
//                Map<String, String> metadata = allInstance.getMetadata();
//                ServerConfig serverConfig = new ServerConfig();
//                String instanceId = allInstance.getInstanceId();
//                int protoMaxId = Integer.parseInt(metadata.get(Constants.ProtoMaxId));
//                int protoMixId = Integer.parseInt(metadata.get(Constants.ProtoMinId));
//                serverConfig.setServerId(instanceId);
//                serverConfig.setMaxProtoId(protoMaxId);
//                serverConfig.setMinProtoId(protoMixId);
//                serverConfig.setPort(allInstance.getPort());
//                serverConfig.setHost(allInstance.getIp());
//                serverConfigMap.put(instanceId, serverConfig);
//            }
//
//            //排序
//            // 执行排序和分组操作
//            serverConfigList = sortAndGroupByMinProtoId(serverConfigMap);
//
//            // 监听服务实例状态
//            client.listenServiceStatus(serviceName, groupName, new EventListener() {
//                @Override
//                public void onEvent(Event event) {
//                    if (event instanceof InstancesChangeEvent) {
//                        InstancesChangeEvent changeEvent = (InstancesChangeEvent) event;
//                        System.out.println("服务实例变更: " + changeEvent.getServiceName());
//                        // 处理实例状态变化
//                        for (Instance instance : changeEvent.getHosts()) {
//                            if (!instance.isHealthy()) {
//                                serverConfigMap.remove(instance.getInstanceId());
//                                System.out.println("实例 " + instance.getIp() + ":" + instance.getPort() + " 已关闭");
//                            }
//                            serverConfigList = sortAndGroupByMinProtoId(serverConfigMap);
//                        }
//                    }
//                }
//            });
//
//
//            //配置变更
//            client.addListener(
//                    "node.properties",  // 配置ID
//                    "node",         // 配置组
//                    new NacosClientUtil.SimpleListener() {
//                        @Override
//                        public void onConfigChanged(String configInfo) {
//                            System.out.println("配置已更新: " + configInfo);
//                            // TODO: 处理配置变更（如重新加载配置）
//
//                        }
//                    });
//
//        } catch (NacosException var3) {
//            throw new RuntimeException("初始化Nacos客户端失败", var3);
//        }
//    }
//
//    public static ServerConfigManager getInstance() {
//        if (instance == null) {
//            synchronized (NacosClientUtil.class) {
//                if (instance == null) {
//                    instance = new ServerConfigManager();
//                }
//            }
//        }
//        return instance;
//    }
//
//    public static List<List<ServerConfig>> sortAndGroupByMinProtoId(Map<String, ServerConfig> serverConfigMap) {
//        // 首先，从 Map 中提取所有的 ServerConfig 对象
//        List<ServerConfig> allConfigs = new ArrayList<>(serverConfigMap.values());
//
//        // 然后，按照 minProtoId 对这些对象进行排序
//        allConfigs.sort(Comparator.comparingInt(ServerConfig::getMinProtoId));
//
//        // 接着，创建一个列表用于存储分组结果
//        List<List<ServerConfig>> result = new ArrayList<>();
//        if (allConfigs.isEmpty()) {
//            return result;
//        }
//
//        // 对排序后的对象进行遍历，将相同 minProtoId 的对象分组
//        List<ServerConfig> currentGroup = new ArrayList<>();
//        currentGroup.add(allConfigs.get(0));
//        int currentMinProtoId = allConfigs.get(0).getMinProtoId();
//
//        for (int i = 1; i < allConfigs.size(); i++) {
//            ServerConfig config = allConfigs.get(i);
//            if (config.getMinProtoId() == currentMinProtoId) {
//                // 如果当前对象的 minProtoId 与当前组的相同，就将其添加到当前组
//                currentGroup.add(config);
//            } else {
//                // 若不同，就将当前组添加到结果列表，并创建一个新组
//                result.add(currentGroup);
//                currentGroup = new ArrayList<>();
//                currentGroup.add(config);
//                currentMinProtoId = config.getMinProtoId();
//            }
//        }
//
//        // 把最后一个组添加到结果列表
//        result.add(currentGroup);
//
//        return result;
//    }
//
//    // 找到一组相同类型的实例
//    public List<ServerConfig> getChannelKey(int protocolId) {
//        for (List<ServerConfig> serverConfigs : serverConfigList) {
//            for (ServerConfig serverConfig : serverConfigs) {
//                if (protocolId > serverConfig.getMaxProtoId()) {
//                    break;
//                }else {//找到目标
//                    return serverConfigs;
//                }
//            }
//        }
//        return null;
//    }
//}
