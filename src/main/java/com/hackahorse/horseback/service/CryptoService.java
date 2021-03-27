package com.hackahorse.horseback.service;

import com.hackahorse.horseback.dto.PedersenCommitment;
import com.hackahorse.horseback.dto.PedersenCommitment.Witness;
import com.hackahorse.horseback.dto.PedersenCommitment.Commitment;
import com.hackahorse.horseback.dto.EC;
import com.hackahorse.horseback.dto.Random;
import java.math.BigInteger;

public class CryptoService {

    public static BigInteger generateRandomNonce(){   //Nonce generation
        return Random.randomGen();
    }

    public static Witness generateWitness(BigInteger vote, BigInteger nonce){   //Witness generation
        return new Witness(vote, nonce);
    }

    public static Commitment generateCommitment(Witness witness){   //Commitment generation
        return PedersenCommitment.generate(witness);
    }

    public static boolean verifyCommitment(Commitment commitment, Witness witness){   //Commitment verification
        return PedersenCommitment.verify(commitment, witness);
    }

    public  static void genH(){
        EC.regenH();
    }

//    public static void main(String[] args) {
//        BigInteger nonce = generateRandomNonce();
//        BigInteger vote = new BigInteger("12", 10);
//        Witness witness = generateWitness(vote, nonce);
//        Commitment commit = generateCommitment(witness);
//        System.out.println(verifyCommitment(commit, witness));
//    }
}
