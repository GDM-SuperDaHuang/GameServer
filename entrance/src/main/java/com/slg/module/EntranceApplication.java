package com.slg.module;


import com.slg.module.app.GameServerNodeApp;
import com.slg.module.fightApp.app.FightApp;
import com.slg.module.rpc.client.NettyClient;
import com.slg.module.rpc.server.NodeServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
//@ComponentScan(basePackages = {"com.slg.module","com.slg.module.handle"})
//@Slf4j
public class EntranceApplication {
    private static NodeServer nodeServer;
    private static NettyClient nodeClient;
    public static void main(String[] args) {
        System.out.println("Node服务器开始启动.......");

        // 设置泄漏检测级别（建议在开发环境使用）
//		System.setProperty("io.netty.leakDetection.level", "PARANOID");
        ConfigurableApplicationContext context = SpringApplication.run(EntranceApplication.class, args);
        nodeServer = new NodeServer();
        nodeClient = NettyClient.getInstance();
        nodeServer.start(nodeServer.getPort());
        // 3. 注册优雅关闭钩子
        registerShutdownHook(context);

        //战斗服务器
        //gameApp = new FightApp();
        //gameApp.init();
    }

    private static void registerShutdownHook(ConfigurableApplicationContext context) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("正在关闭Node服务器...");
            if (nodeServer != null) {
                System.out.println("正在关闭nodeServer服务器...");

                nodeServer.shutdown(); // 关闭Netty
            }
            if (nodeClient != null) {
                System.out.println("正在关闭nodeClient服务器...");

                nodeClient.shutdown(); // 关闭Netty
            }
            context.close(); // 关闭Spring
            System.out.println("Node服务器已关闭");
        }));
    }

}
