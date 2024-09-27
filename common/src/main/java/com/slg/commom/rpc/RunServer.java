package com.slg.commom.rpc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class RunServer implements CommandLineRunner {
    @Autowired
    private NettyServer server;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("----------------------------启动完成");
        server.init();
    }
}
