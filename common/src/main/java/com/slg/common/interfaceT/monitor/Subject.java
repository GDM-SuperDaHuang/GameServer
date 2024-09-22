package com.slg.common.interfaceT.monitor;

//import com.example.demo.interfaceT.monitor.Observer;

import java.util.ArrayList;
import java.util.List;

public class Subject {
    //通知列表
    private List<Observer> observers = new ArrayList<>();

    // 注册观察者
    public void registerObserver(Observer o) {
        observers.add(o);
    }

    // 注销观察者
    public void removeObserver(Observer o) {
        observers.remove(o);
    }

    // 通知所有观察者
    public void notifyObservers(Object object,String message) {
        for (Observer observer : observers) {
            observer.onChange(object,message);
        }
    }
}