package com.citeconomy.client;

import com.citeconomy.data.WeeklyQuest;

import java.util.Collections;
import java.util.List;

@net.neoforged.api.distmarker.OnlyIn(net.neoforged.api.distmarker.Dist.CLIENT)
public class ClientNeedsData {
    public static List<WeeklyQuest> quests = Collections.emptyList();
}
