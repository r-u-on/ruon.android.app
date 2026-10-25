# FCM: moving from registration tokens to Firebase Installation IDs

Firebase deprecated FCM registration tokens in firebase-messaging 25.1.0
(`FirebaseMessaging.getToken()`, `FirebaseMessagingService.onNewToken()`).
They are replaced by `FirebaseMessaging.register()` and `onRegistered(installationId)`,
which address a device by its Firebase Installation ID (FID). Servers then send with
the FCM HTTP v1 `message.fid` field instead of `message.token`.

The app supports both. The `ruon.fcmUseInstallationId` property in `gradle.properties`
selects one per build:

| Value | App sends to the server | Server must send with |
|-------|-------------------------|-----------------------|
| `false` (current) | FCM registration token | `message.token` |
| `true` | Firebase Installation ID | `message.fid` |

It cannot be both at once in one build: with `true`, the
`firebase_messaging_installation_id_enabled` manifest flag is set and `getToken()` fails.

## What the app sends

Unchanged in both modes: after a successful login the app calls

```
GET https://www.r-u-on.com/androidNotif?email=…&password=…&name=<device model>&device=<push id>
```

Only the meaning of `device` changes. A FID is a 22-character URL-safe base64
string. An FCM registration token is much longer.

## Server changes needed before switching to `true`

1. Send pushes with `message.fid` for devices registered with a FID
   (Admin SDKs: the `fid` field instead of `token`).
2. Keep sending with `message.token` for devices registered with a token. Users who are
   already logged in do not register again until they log in again, so both kinds will
   exist side by side after the app update. Either store which kind each device sent, or
   tell them apart by length (22 characters is a FID).
3. Optional: plan how to move the remaining token devices before Firebase stops accepting
   token sends. The app only registers at login, because `androidNotif` needs the user's
   email and password.

## Rollout

1. Ship the server changes above.
2. Set `ruon.fcmUseInstallationId=true` in `gradle.properties` and release the app.
3. Check on a device with internet: log in, confirm the server received a 22-character
   `device` value, send a test alarm and confirm it arrives.

To build a FID test build without editing the file:

```bash
./gradlew assembleDebug -Pruon.fcmUseInstallationId=true
```

## Code

- `PushRegistration`: fetches the push id for the current mode and caches tokens and
  FIDs under separate keys, so a token is never sent as a FID.
- `RUOnMessagingService`: `onNewToken()` (token mode) and `onRegistered()` (FID mode)
  store the new id and mark the server registration stale when it changed.
- `LoginActivity`: registers the push id with the server after login.
