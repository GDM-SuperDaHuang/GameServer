//package com.slg.common.observerTest;
//import com.example.demo.interfaceT.monitor.Observer;
//import com.example.demo.interfaceT.monitor.Subject;
//
//public class Test1 implements Observer {
//    public static void main(String[] args) {
//        //Test1，Test需要实现Observer接口
//        Test1 test1 = new Test1();
//        Test test = new Test();
//        Subject subject = new Subject();
//        subject.registerObserver(test1);
//        subject.registerObserver(test);
//        subject.notifyObservers("55555","233231");
//    }
//
//    @Override
//    public void onChange(Object object, String message) {
//        System.out.println("asdasssssssss");
//    }
//}
