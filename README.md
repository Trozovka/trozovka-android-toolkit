# Trozovka Android Toolkit

Two small, independent Android library modules, shared across Trozovka's offline-first Android
apps ([OpenCPN GPS Server](https://github.com/Trozovka/opencpn-gps-server-free), PocketVDR, and
whatever comes next) so the same two patterns aren't hand-rolled or copy-pasted per project.

Developed by [Trozovka](https://github.com/Trozovka).

## `:reliability`

The permission/service pattern needed by any Android app that has to keep doing real work with
the screen off and Doze active:

- `WakeLockController` -- acquire/release a single partial wake lock keyed by a tag, scoped to one
  foreground service's lifecycle.
- `BatteryOptimizationHelper` -- check whether the app is exempt from battery optimization, and
  build the system settings intent to request the exemption.
- `LocationReliabilityPermissionFlow` -- the full fine/coarse location -> background location ->
  `POST_NOTIFICATIONS` -> battery-optimization-exemption permission chain as a single reusable
  class, wired up via `ActivityResultContracts`. Construct it as an Activity property (not inside
  `onCreate`, since Activity Result registration requires that timing), call `begin()`, get an
  `onReady` callback once every step is satisfied.

Ships its own `AndroidManifest.xml` declaring the permissions it uses (`ACCESS_FINE_LOCATION`,
`ACCESS_COARSE_LOCATION`, `ACCESS_BACKGROUND_LOCATION`, `FOREGROUND_SERVICE`,
`FOREGROUND_SERVICE_LOCATION`, `WAKE_LOCK`, `POST_NOTIFICATIONS`,
`REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`) -- these merge automatically into any app that depends on
this module.

## `:licensing`

The Gumroad one-time-purchase license pattern: verify a key against Gumroad's API **at most
once**, then trust it forever after with zero further network calls, so a Pro app built on this
keeps working indefinitely offline once activated.

- `GumroadLicenseVerifier` -- calls `api.gumroad.com/v2/licenses/verify` directly (blocking I/O,
  call it from `Dispatchers.IO`).
- `GumroadVerifyOnceStore` -- SharedPreferences-backed cache of the license key, the
  already-verified key, and a human-readable last-check message. Give each consuming app its own
  `prefsName` to avoid collisions.
- `GumroadEntitlementChecker` -- ties the two together: bypass-code check first (for your own
  on-device testing, never shipped as a hardcoded value), then "was this exact key already
  verified," and only then an actual network call, caching the result permanently on success.

Ships its own `AndroidManifest.xml` declaring `INTERNET` and `ACCESS_NETWORK_STATE`.

## Using this from another repo

Both current consumers (public and private) include this repo as a **git submodule** and resolve
the modules by path in `settings.gradle.kts`, e.g.:

```kotlin
include(":reliability")
project(":reliability").projectDir = file("trozovka-android-toolkit/reliability")
include(":licensing")
project(":licensing").projectDir = file("trozovka-android-toolkit/licensing")
```

Bugfixes and improvements to either module go into this repo and get pulled into consumers via
`git submodule update --remote` -- never patched separately in a consumer's own copy.

## Where this pattern came from

Both modules were extracted from the [OpenCPN GPS Server](https://github.com/Trozovka/opencpn-gps-server-free)
project after the second app (PocketVDR) needed the identical foreground-service and Gumroad
licensing logic -- rather than copy-pasting a second, separately-maintained version of either.

## License

MIT -- see [LICENSE](LICENSE) for the full text.

Copyright (c) 2026 Trozovka. Original Author: Trozovka. All derivative works must retain the
[NOTICE](NOTICE) file's attribution. Not a fork of, or derived from, any other project's source.
