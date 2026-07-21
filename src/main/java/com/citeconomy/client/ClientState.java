package com.citeconomy.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientState {
    private static int balance = 0;

    public static int getBalance() {
        return balance;
    }

    public static void setBalance(int newBalance) {
        balance = newBalance;
    }
}
