package io.github.ndellagrotte.mtreplaycompat;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import net.minecraft.network.play.server.SPacketCustomPayload;

import java.util.HashSet;
import java.util.Set;

public class ReplayPacketFilter extends ChannelInboundHandlerAdapter {

    public static final String HANDLER_NAME = "mtreplaycompat_filter";

    private final Set<String> loggedChannels = new HashSet<>();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof SPacketCustomPayload) {
            String channel = ((SPacketCustomPayload) msg).getChannelName();
            if (isBlocked(channel)) {
                if (CompatConfig.logDroppedPackets && loggedChannels.add(channel)) {
                    MTReplayCompat.LOGGER.info("Dropping replayed custom payload(s) on channel '{}'", channel);
                }
                ReferenceCountUtil.release(msg);
                return;
            }
        }
        ctx.fireChannelRead(msg);
    }

    // @return whether a custom payload on channel must not reach mod handlers during playback
    public static boolean isBlocked(String channel) {
        return isBlocked(channel, CompatConfig.blockedChannels);
    }

    static boolean isBlocked(String channel, String[] blockedChannels) {
        if (channel == null || blockedChannels == null) {
            return false;
        }
        for (String blocked : blockedChannels) {
            if (blocked != null && blocked.trim().equalsIgnoreCase(channel)) {
                return true;
            }
        }
        return false;
    }
}
