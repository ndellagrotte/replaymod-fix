package io.github.ndellagrotte.mtreplaycompat;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// client-only compat shim between ReplayMod and Music Triggers.

@Mod(
        modid = Tags.MOD_ID,
        name = Tags.MOD_NAME,
        version = Tags.VERSION,
        clientSideOnly = true,
        acceptableRemoteVersions = "*",
        dependencies = "required-after:mixinbooter;after:replaymod;after:musictriggers"
)
public class MTReplayCompat {

    public static final Logger LOGGER = LogManager.getLogger(Tags.MOD_NAME);

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        LOGGER.info("{} {} loaded; blocking replayed payloads on channels {}",
                Tags.MOD_NAME, Tags.VERSION, String.join(", ", CompatConfig.blockedChannels));
    }
}
