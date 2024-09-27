package com.slg.commom.interfaceT.monitor1;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
@Component
public class CustomEventListener implements ApplicationListener<CustomEvent> {
    @Override
    public void onApplicationEvent(CustomEvent event) {
        System.out.println("============CustomEvent - " +event );
    }
}
