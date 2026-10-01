# Changelog

## [1.0.0] - 2026-10-01

### Added
- Netty filter injected into ReplayMod's replay pipeline that drops custom-payload packets on
  configurable channels (default: `theimpossiblelibrary`, `musictriggers`), so Music Triggers never
  sees replayed network traffic and the replay viewer no longer disconnects.
- Config keys `blockedChannels` and `logDroppedPackets` in `config/mtreplaycompat.cfg`.
