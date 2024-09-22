package com.slg.common.interfaceT.monitor1;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class EventPublisher {
    private ApplicationEventPublisher publisher;
    @Autowired
    public EventPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }
    public void publishCustomEvent(Object event) {
        publisher.publishEvent(event);
    }

}