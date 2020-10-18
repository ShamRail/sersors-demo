package com.example.demoapp;

public class Angles {

    double cosA;
    double cosB;
    double cosG;

    double sinA;
    double sinB;
    double sinG;

    public Angles(double alpha, double beta, double gamma) {

        cosA = Math.cos(alpha);
        cosB = Math.cos(beta);
        cosG = Math.cos(gamma);

        sinA = Math.sin(alpha);
        sinB = Math.sin(beta);
        sinG = Math.sin(gamma);
    }

}
