package com.hackahorse.horseback.dto;

public class BetDTO {
    private String betAmount;
    private String accountId;
    private String teamId;

    public BetDTO(String betAmount, String accountId, String teamId) {
        this.betAmount = betAmount;
        this.accountId = accountId;
        this.teamId = teamId;
    }

    public String getBetAmount() {
        return betAmount;
    }

    public void setBetAmount(String betAmount) {
        this.betAmount = betAmount;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getTeamId() {
        return teamId;
    }

    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }
}
