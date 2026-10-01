package io.github.ndellagrotte.mtreplaycompat;

import java.util.Collections;
import java.util.List;

@SuppressWarnings({"unused", "deprecation"})
public class MixinLateLoader implements zone.rong.mixinbooter.ILateMixinLoader {

    static final String MIXIN_CONFIG = "mixins.mtreplaycompat.json";
    static final String REPLAY_HANDLER_CLASS = "com.replaymod.replay.ReplayHandler";

    @Override
    public List<String> getMixinConfigs() {
        return Collections.singletonList(MIXIN_CONFIG);
    }

    @Override
    public boolean shouldMixinConfigQueue(String mixinConfig) {
        boolean present = classPresent();
        if (!present) {
            MTReplayCompat.LOGGER.info("ReplayMod not found; {} will stay inactive", MIXIN_CONFIG);
        }
        return present;
    }

    // resource-probes the class without init
    private static boolean classPresent() {
        String resource = MixinLateLoader.REPLAY_HANDLER_CLASS.replace('.', '/') + ".class";
        return MixinLateLoader.class.getClassLoader().getResource(resource) != null;
    }
}
