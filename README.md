# Song Rating Android App

This is the Android client of the Song Rating environment. It communicate with
your [Song Rating API](https://github.com/chikatetsu/song-rating-server) running on
your server to rate your songs. While you listen to your music on Apple Music (and 
on any music streaming services in the future), Song Rating will show a notification
so you can say if the current song playing is better or worse than the previous one.

## Installation
### 1. Install the app
- Install Android Studio on your computer
- Open this project in Android Studio
- Unlock developer mode on your Android (see tutorial for your device)
- Plug your phone to your computer
- In the developer options, unlock USB debugging and authorize your computer
- Select your device in Android Studio
- Run the app

### 2. Create the .env file
Create the `.env` file **under the `app` directory** ([see example]()) :
```dotenv
AUTH_TOKEN=song_rating_token_here
API_URL=song_rating_api_url_here
CIDER_TOKEN=cider_token_here
CIDER_PORT=10767
```
Every variables are optional, but they are most of the time needed to communicate
properly with your Song Rating API or your Cider application. Here are the default
value for every variables :
```dotenv
AUTH_TOKEN="" // If no authentication token is needed to connect to your Song Rating API
API_URL="localhost:8000" // Value by default, if you run the API with the default port locally
CIDER_TOKEN="" // If no authentication token is needed to connect to your Cider application
CIDER_PORT="10767" // The default port used by Cider
```

### 3. Give permissions
- In Android 14, launch the `Settings` application
- Go to `Apps`
- Tap on the button on the up right corner and go to `Special access`
- Go to `Notification access`
- Turn on the notification access for the `Song Rating` app
- Go back to `Apps`
- Tap on the `Song Rating` app
- Go under `Permissions` and give permissions to receive notifications
**Disclaimer:** Those permissions are necessary for the application to run properly.
Otherwise, the app will simply be unable to listen to your music streaming service
notifications, or will be unable to send you notifications so you can vote for your
favorite song. If you are concerned by the permissions given to the `Song Rating`
application (which is something you SHOULD be concerned to), I redirect you to the
`AppleMusicNotificationListener.kt` file, where the filtering on your notifications
are done, so that you can see by yourself that `Song Rating` is only listening to
your music streaming application and nothing else.

### 4. Test the app
If everything works fine, a notification from `Song Rating` should appear while you
listen to at least two songs on your music streaming service.

### 5. Configure for Samsung Watches (optional)
- Connect your watch to your phone using Bluetooth
- On your phone, open the `Galaxy Wearable` app
- Go to `Watch settings`
- Go to `Notifications`
- Go to `App notifications`
- Turn on notification mirroring for `Song Rating` under `Phone apps`

For the notification mirroring to work properly, your watch should always be connected
to your phone using Bluetooth when you listen to your music.

Those steps should also apply to any Wear OS watches using the app of your watch.

## Coming soon
- [ ] Handle more streaming services
- [ ] Add a queue on failure if offline
