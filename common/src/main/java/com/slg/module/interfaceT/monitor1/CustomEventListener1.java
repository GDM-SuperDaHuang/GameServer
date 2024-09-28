package com.slg.module.interfaceT.monitor1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class CustomEventListener1 implements ApplicationListener<CustomEvent1> {
    private static final Logger logger = LoggerFactory.getLogger(CustomEventListener1.class);

    @Override
    public void onApplicationEvent(CustomEvent1 event) {
        System.out.println("++++++++++++++++CustomEvent1  " +event );
    }
}
