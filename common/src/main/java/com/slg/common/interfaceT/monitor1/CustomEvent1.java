package com.slg.common.interfaceT.monitor1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;

public class CustomEvent1 extends ApplicationEvent {
    private String message;

    public CustomEvent1(Object source, String message) {
        super(source);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}