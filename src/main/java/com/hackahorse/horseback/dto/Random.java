package com.hackahorse.horseback.dto;

import java.math.BigInteger;
import java.security.SecureRandom;

public class Random {

    public static BigInteger randomGen(){
        byte r1[] = new byte[24];
        java.util.Random r = new SecureRandom();
        r.nextBytes(r1);
        return new BigInteger(r1).mod(EC.Constants.m);
    }
}