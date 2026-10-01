package io.github.ndellagrotte.mtreplaycompat;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SPacketCustomPayload;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReplayPacketFilterTest {

    // collects whatever makes it past the filter, like forge's fml:packet_handler
    private static final class Sink extends ChannelInboundHandlerAdapter {
        final List<Object> received = new ArrayList<>();

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            received.add(msg);
        }
    }

    private static SPacketCustomPayload payload(String channel) {
        return new SPacketCustomPayload(channel, new PacketBuffer(Unpooled.buffer()));
    }

    @Test
    void isBlockedMatchesConfiguredChannelsCaseInsensitively() {
        String[] blocked = {"theimpossiblelibrary", " musictriggers "};
        assertTrue(ReplayPacketFilter.isBlocked("theimpossiblelibrary", blocked));
        assertTrue(ReplayPacketFilter.isBlocked("TheImpossibleLibrary", blocked));
        assertTrue(ReplayPacketFilter.isBlocked("musictriggers", blocked));
        assertFalse(ReplayPacketFilter.isBlocked("MC|Brand", blocked));
        assertFalse(ReplayPacketFilter.isBlocked("FML|HS", blocked));
        assertFalse(ReplayPacketFilter.isBlocked(null, blocked));
        assertFalse(ReplayPacketFilter.isBlocked("theimpossiblelibrary", null));
    }

    @Test
    void defaultConfigBlocksTheImpossibleLibraryChannel() {
        assertTrue(ReplayPacketFilter.isBlocked("theimpossiblelibrary"));
        assertTrue(ReplayPacketFilter.isBlocked("musictriggers"));
        assertFalse(ReplayPacketFilter.isBlocked("MC|Brand"));
    }

    @Test
    void blockedPayloadNeverReachesNextHandler() {
        Sink sink = new Sink();
        EmbeddedChannel channel = new EmbeddedChannel(new ReplayPacketFilter(), sink);

        channel.writeInbound(payload("theimpossiblelibrary"));

        assertTrue(sink.received.isEmpty(), "blocked payload leaked past the filter");
        channel.finishAndReleaseAll();
    }

    @Test
    void otherPayloadsAndPacketsPassThrough() {
        Sink sink = new Sink();
        EmbeddedChannel channel = new EmbeddedChannel(new ReplayPacketFilter(), sink);

        SPacketCustomPayload brand = payload("MC|Brand");
        Object notAPayload = new Object();
        channel.writeInbound(brand);
        channel.writeInbound(notAPayload);

        assertEquals(2, sink.received.size());
        assertSame(brand, sink.received.get(0));
        assertSame(notAPayload, sink.received.get(1));
        channel.finishAndReleaseAll();
    }
}
