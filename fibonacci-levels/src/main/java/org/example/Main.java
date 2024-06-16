package org.example;

import java.math.BigDecimal;
import java.util.Scanner;

import static org.example.utils.FibaHelper.calculateValueForLevel;

public class Main {
    public static void main(String[] args) {
        System.out.println("fibonacci-levels start");

        BigDecimal low = new BigDecimal("67473.63");
        BigDecimal high = new BigDecimal("68426.14");

        System.out.println(calculateValueForLevel(low, high));
    }
}