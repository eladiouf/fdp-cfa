package com.citeconomy.network;

import com.citeconomy.CiteconomyMod;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = CiteconomyMod.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ModNetworking {

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");

        registrar.playToServer(
                BankTransactionPayload.TYPE,
                BankTransactionPayload.STREAM_CODEC,
                BankTransactionPayload::handleData
        );
        
        registrar.playToClient(
                SyncBalancePayload.TYPE,
                SyncBalancePayload.STREAM_CODEC,
                SyncBalancePayload::handleData
        );
        
        registrar.playToServer(
                ShopBuyPayload.TYPE,
                ShopBuyPayload.STREAM_CODEC,
                ShopBuyPayload::handleData
        );

        registrar.playToServer(
                SetShopPricePayload.TYPE,
                SetShopPricePayload.STREAM_CODEC,
                SetShopPricePayload::handleData
        );

        registrar.playToServer(
                BankbookPayPayload.TYPE,
                BankbookPayPayload.STREAM_CODEC,
                BankbookPayPayload::handleData
        );

        registrar.playToServer(
                MarketBuyPayload.TYPE,
                MarketBuyPayload.STREAM_CODEC,
                MarketBuyPayload::handleData
        );

        registrar.playToClient(
                MarketListingsPayload.TYPE,
                MarketListingsPayload.STREAM_CODEC,
                MarketListingsPayload::handleData
        );

        registrar.playToClient(
                NeedsDataPayload.TYPE,
                NeedsDataPayload.STREAM_CODEC,
                NeedsDataPayload::handleData
        );

        registrar.playToServer(
                QuestCompletePayload.TYPE,
                QuestCompletePayload.STREAM_CODEC,
                QuestCompletePayload::handleData
        );
    }
}
