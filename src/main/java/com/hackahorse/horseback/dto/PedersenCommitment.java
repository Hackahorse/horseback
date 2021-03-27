package com.hackahorse.horseback.dto;

import java.math.BigInteger;
import java.security.spec.ECPoint;

public class PedersenCommitment {

    public static class Commitment {
        private ECPoint commitment;

        public Commitment(ECPoint point){
            this.setCommitment(point);
        }

        public ECPoint getCommitment() {
            return commitment;
        }

        public void setCommitment(ECPoint commitment) {
            this.commitment = commitment;
        }

        public static void printCommitment(Commitment commit){
            System.out.println("Commitment: ");
            EC.Points.printEPoint(commit.getCommitment());
        }

        public static String string(Commitment commit){
            return EC.Points.EPointToString(commit.getCommitment());
        }
    }

    public static class Witness {
        private BigInteger vote;
        private BigInteger nonce;

        public Witness (BigInteger vote, BigInteger nonce) {
            this.vote = vote;
            this.nonce = nonce;
        }

        public BigInteger getNonce() {
            return nonce;
        }

        public BigInteger getVote() {
            return vote;
        }

        public void setNonce(BigInteger nonce) {
            this.nonce = nonce;
        }

        public void setVote(BigInteger vote) {
            this.vote = vote;
        }

        public static void printWitness(Witness witness){
            System.out.println("Witness: ");
            System.out.println("Vote: \t" + witness.getVote());
            System.out.println("Nonce: \t" + witness.getNonce());
        }

        public static String string(Witness witness){
            return witness.getVote() + " " + witness.getNonce();
        }

    }

    public static Commitment generate (Witness witness){

        // v*G + n*H

        return new Commitment(EC.Points.addPoint(EC.Points.scalmult(EC.Constants.G, witness.getVote()),EC.Points.scalmult(EC.Constants.H, witness.getNonce())));

    }

    public static boolean verify (Commitment commitment, Witness witness){

        // Point = v*G + n*H
        // if Point.x == Commitment.x && Point.y == Commitment.y => true

        ECPoint point = EC.Points.addPoint(EC.Points.scalmult(EC.Constants.G, witness.getVote()),EC.Points.scalmult(EC.Constants.H, witness.getNonce()));
        if (point.getAffineX().equals(commitment.getCommitment().getAffineX()) && point.getAffineY().equals(commitment.getCommitment().getAffineY()))
            return true;
        else return false;
    }
}