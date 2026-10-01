package io.github.ndellagrotte.mtreplaycompat.mixin;

import com.replaymod.replay.ReplayHandler;
import io.github.ndellagrotte.mtreplaycompat.MTReplayCompat;
import io.github.ndellagrotte.mtreplaycompat.ReplayPacketFilter;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.embedded.EmbeddedChannel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// ReplayHandler setup builds replay pipeline when replay is opened and restarted
// after forge injection it looks like sender > mtreplaycompat_filter > fml:packet_handler > packet_handler
// probably stops blocked payloads from reaching impossible lib/music triggers

@Mixin(value = ReplayHandler.class, remap = false)
public abstract class ReplayHandlerMixin {

    @Shadow
    private EmbeddedChannel channel;

    @Inject(method = "setup", at = @At("RETURN"))
    private void mtreplaycompat$installPacketFilter(CallbackInfo ci) {
        if (channel == null) {
            MTReplayCompat.LOGGER.warn("ReplayHandler#setup finished without a channel; packet filter not installed");
            return;
        }
        ChannelPipeline pipeline = channel.pipeline();
        if (pipeline.get(ReplayPacketFilter.HANDLER_NAME) != null) {
            return;
        }
        if (pipeline.get("packet_handler") == null) {
            MTReplayCompat.LOGGER.warn("Replay pipeline has no 'packet_handler'; packet filter not installed");
            return;
        }
        pipeline.addBefore("packet_handler", ReplayPacketFilter.HANDLER_NAME, new ReplayPacketFilter());
        MTReplayCompat.LOGGER.debug("Installed replay packet filter into ReplayMod pipeline");
    }
}
