package com.prakhar.ecomm.ecommbackend;

import ch.qos.logback.core.net.SyslogOutputStream;

import java.sql.SQLOutput;

class shape {

    public void area(int a) {
        System.out.println("square is");
    }

    public void area(int a, int b) {
        System.out.println("rectangle area");
    }
}
//class rectangle extends shape{
//
//    public void area(int a) {
//        System.out.println("Area is" + " "+ a*a);
//    }
//}
public class dummyTests {

public static void main(String[] args) {

    shape s = new shape() ;
    s.area(10, 20) ;
}
}

/*
* write access modiefier table
* final keyword 3 imp points
*


 */