package com.slg.module;


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

    public static void main(String[] args) {
        System.out.println("Node服务器开始启动.......");
        // 设置泄漏检测级别（建议在开发环境使用）
//		System.setProperty("io.netty.leakDetection.level", "PARANOID");
        ConfigurableApplicationContext context = SpringApplication.run(EntranceApplication.class, args);
        nodeServer = new NodeServer();
        nodeServer.start(nodeServer.getPort());
        // 3. 注册优雅关闭钩子
        registerShutdownHook(context);
    }

    private static void registerShutdownHook(ConfigurableApplicationContext context) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("正在关闭Node服务器...");
            if (nodeServer != null) {
                nodeServer.shutdown(); // 关闭Netty
            }
            context.close(); // 关闭Spring
            System.out.println("Node服务器已关闭");
        }));
    }

}
