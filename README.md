### ChatApp
[Preview Images](./Preview.md)

ChatApp is a simple local chat for Android. Features included in the chat are:
- Text messages
- Audio messages
- Pictures (can be sent from the gallery or taken at the moment)
- Avatar profiles
- Usernames
- Interactive commands (such as /vibrate)

The app implements its own message protocol and uses JSON to send messages across connected members.

ChatApp needs a host to work, anyone with the app installed can run the server (which is an android foreground service) and invite
people who have the app installed (or any compatible implementation of the protocol) and are on the same local network.

It works via the standard IPv4:Port connection.

### License
ChatApp is licensed under the BSD-3 clause [license](./LICENSE).

### Supported languages
- English (US)
- Portuguese (BR)

### Protocol
ChatApp protocol was self made and can be implemented anywhere so different devices can communicate with one another. You
could create a CLI chat, or an iOS app or web application too.
