# connectwx
Sample ConnectIQ app that demonstrates BTLE Connectivity. 

To run this sample project you will need to get setup with the ConnectIQ SDK and have familiarity
with running Android applications. In order to simulate communication between the garmin device
and the Android application, you'll need to follow the [instructions](http://developer.garmin.com/connect-iq/developer-tools/communication/) to setup port forwarding via ADB.

When running in the simulator you need to explicitly use the Connect menu option after establishing
the port forwarding via ADB. Each time the Android app updates the weather, it will look to see if
any devices are connected, if they are not it tries to pull the list of available devices.

See [OpenWeatherMap](http://openweathermap.org) for details on accessing free weather data.

See the [ConnectIQ SDK](http://developer.garmin.com/connect-iq/overview/) page to grab the SDK.
