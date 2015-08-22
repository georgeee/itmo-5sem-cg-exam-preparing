package ru.georgeee.itmo.sem5.cg.common;

import java.util.Random;

public class Utils {
    public static Random createRandom(){
        return new Random(System.currentTimeMillis());
    }
}
