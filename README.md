### ChatApp
[Preview Images](./Preview.md)

ChatApp is a simple local chat for Android. Features included in the chat are:
- Text messages
- Audio messages
- Pictures (can be sent from the gallery or taken the picture at the moment)
- Avatar profiles
- Usernames
- Interactive commands (such as vibrate)
- Link to connect to chats

ChatApp has the following language supports:
- English 🇺🇸 (US) 
- Portuguese 🇧🇷 (BR)

The app implements its own message protocol and uses JSON to send messages across connected members.

ChatApp needs a host to work, anyone with the app installed can run the server (which is an android foreground service) and invite
people who have the app installed (or any compatible implementation of the protocol) and are on the same local network.

It works via the standard IPv4:Port connection.

### Protocol
ChatApp protocol was self made and can be implemented anywhere so different devices can communicate with one another. You
could create a CLI chat, or an iOS app or web application too.
