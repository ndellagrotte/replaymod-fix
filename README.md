# Music Triggers ReplayMod Compat

Client-side compat mod for Minecraft 1.12.2 that stops
[Music Triggers](https://github.com/TheRoutineGamer/Music-Triggers) from reacting to network
traffic that [ReplayMod](https://www.replaymod.com/) plays back inside the replay viewer.

## The problem

ReplayMod records every server-to-client packet, including the Forge custom-payload packets that
Music Triggers (through The Impossible Library) uses to talk to its server half. When a replay is
opened in the viewer, ReplayMod re-feeds those packets through an embedded netty pipeline. Music
Triggers sees them as a live server talking to it, answers into a connection that does not exist, and
the viewer disconnects a moment after it opens.

## What this mod does

A single late mixin into ReplayMod's `ReplayHandler#setup()` inserts a small netty inbound handler
into the replay pipeline, right before the vanilla `packet_handler`. Forge inserts its own
`fml:packet_handler` at the same position later, so the final order is

```
replay sender -> mtreplaycompat_filter -> fml:packet_handler -> packet_handler
```

Any `SPacketCustomPayload` whose channel is in the blocked list is dropped before it can reach a mod
channel. Outbound packets Music Triggers may still try to send during playback are already discarded by
ReplayMod itself. The hook runs on every pipeline build, so replay restarts are covered too.

The mixin config is registered through MixinBooter's late loader rather than a `MixinConfigs` manifest
entry. The late-loader interface is deprecated since MixinBooter 11 but still supported there and in Cleanroom,
and it is the only way to target a mod class on MixinBooter 10.x: a manifest entry would be processed before
ReplayMod's jar is reachable and the mixin would be dropped as targetless. The config is only queued when
`com.replaymod.replay.ReplayHandler` is on the classpath; without ReplayMod the mod does nothing.

## Requirements

- Minecraft 1.12.2 with Forge 14.23.5.2847+ or Cleanroom
- [MixinBooter](https://github.com/CleanroomMC/MixinBooter) 10.7 or newer (Cleanroom bundles it)
- ReplayMod 2.6.x (tested with 2.6.24)
- Music Triggers 6.3.1 or RotN's fork(?) (both use The Impossible Library's `theimpossiblelibrary` channel)

Client only. Servers do not need it.

## Config

`config/mtreplaycompat.cfg`

| Key                 | Default                                     | Meaning                                                            |
|---------------------|---------------------------------------------|--------------------------------------------------------------------|
| `blockedChannels`   | `theimpossiblelibrary`, `musictriggers`     | Custom-payload channel names dropped during replay playback.       |
| `logDroppedPackets` | `false`                                     | Log once per channel, per replay, when a replayed packet is dropped.|

Channel names are compared case-insensitively.

## Building

```
./gradlew build
```

The jar is written to `build/libs/mtreplaycompat-<version>.jar`. The ReplayMod mixin is compiled
against `replaymod-1.12.2-2.6.24.jar`, so that jar must be present in the project root.
