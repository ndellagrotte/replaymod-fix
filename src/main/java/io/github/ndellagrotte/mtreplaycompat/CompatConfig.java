package io.github.ndellagrotte.mtreplaycompat;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Config(modid = Tags.MOD_ID)
@Config.LangKey("mtreplaycompat.config.title")
public final class CompatConfig {

    @Config.Name("blockedChannels")
    @Config.Comment({
            "Custom-payload channel names that are dropped while ReplayMod plays back a replay.",
            "Packets on these channels never reach the mod that registered the channel.",
            "Music Triggers 6.x and 7.x both talk over 'theimpossiblelibrary' (The Impossible Library)."
    })
    public static String[] blockedChannels = {"theimpossiblelibrary", "musictriggers"};

    @Config.Name("logDroppedPackets")
    @Config.Comment("Log (once per channel, per replay) when a replayed packet is dropped.")
    public static boolean logDroppedPackets = false;

    private CompatConfig() {
    }

    @Mod.EventBusSubscriber(modid = Tags.MOD_ID)
    public static final class Handler {

        @SubscribeEvent
        public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
            if (Tags.MOD_ID.equals(event.getModID())) {
                ConfigManager.sync(Tags.MOD_ID, Config.Type.INSTANCE);
            }
        }
    }
}
