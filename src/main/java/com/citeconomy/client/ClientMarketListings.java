package com.citeconomy.client;

import com.citeconomy.network.MarketListingsPayload;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class ClientMarketListings {
    private static List<MarketListingsPayload.Entry> listings = new ArrayList<>();

    public static List<MarketListingsPayload.Entry> getListings() {
        return listings;
    }

    public static void setListings(List<MarketListingsPayload.Entry> data) {
        listings = data;
    }
}
