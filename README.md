# Anki Drive SDK for Java

The Anki Drive SDK for Java is an implementation of the message protocols
and data parsing routines necessary for communicating with Anki Drive and Overdrive vehicles. This library is an updated
version of the one found [adessoAG/anki-drive-java](https://github.com/adessoAG/anki-drive-java). 

*See [anki/drive-sdk](https://github.com/anki/drive-sdk) for the official
SDK written in C.*

### Disclaimer
The authors of this software are in no way affiliated to Anki nor adesso AG.
All naming rights for Anki, Anki Drive and Anki Overdrive are property of
[Anki](http://anki.com), initial concept and implementation of the Java version by the folks at adesso.

This is a forked repository from [adessoAG/anki-drive-java](https://github.com/adessoAG/anki-drive-java), which, sadly,
appears to be abandoned. We are maintaining this SDK to serve our [tenbergen/Automotive-CPS](https://github.com/tenbergen/Automotive-CPS) project.

## About

Unfortunately, there is currently no cross-platform Java library to interface
with Bluetooth LE devices.

This project therefore requires a Node.js gateway service to handle low-level
communication with the Anki vehicles. All data processing and message parsing
is carried out in Java code.

## Prerequisites

To build and use the SDK in your own project you will need:

- Java JDK (>= 1.8.0)
- a compatible Bluetooth 4.0 interface with LE support

## Installation

To install the SDK and all required dependencies run the following commands:

```
git clone https://github.com/tenbergen/anki-drive-java
cd anki-drive-java
./gradlew build
```

### On MacOS

Prerequisites for macOS:
- Node.js v6.14.2 or later.
- macOS 10.7 or later

If you get a "node-pre-gyp build fail error" when running npm install run:
```
rm -rf node_modules/
npm install --build-from-resource
```

Once connected, if your cars time out follow these steps:
1. Stop the server
2. From the Mac desktop, hold down the Shift+Option keys and then click on the Bluetooth menu item to reveal the hidden Debug menu
3. Select “Reset the Bluetooth module” from the Debug menu list
4. Once finished reboot your Mac

### On Linux / Raspberry Pi

Optional Dependency node-usb will not be installed. So, run:
```
sudo apt-get install libudev-dev
```

### On Windows

Node.js server is currently not supported on Windows. However, you can run the Node.js server on a Linux device change
`edu.oswego.cs.CPSLab.AnkiConnectionTest` to connect to the IP of the Raspberry Pi instead of `localhost`.

## Usage

Start the Node.js gateway service:
```
sudo ./gradlew server
```

### Add the Java library

To get the Java library into your build:
```gradle
// add JitPack.io as a repository
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    // <Git commit-ish>: commit hash or tag
    compile 'com.github.tenbergen:anki-drive-java:-SNAPSHOT'
}
```

For the Maven instructions see the [JitPack.io website](https://jitpack.io/tenbergen/anki-drive-java).

### API usage

Create a AnkiConnector object:
```java
AnkiConnector anki = new AnkiConnector("localhost", 5000);
```

Start scanning for vehicles:
```java
List<Vehicle> vehicles = anki.findVehicles();
```

### Test File
To try a connection, start the server and run:
```
./gradlew ankiConnectionTest
```
which will execute
```java
edu.oswego.cs.CPSLab.AnkiConnectionTest
```

## Contributing

WINDOWS SERVER WANTED!

Other contributions are welcome as well. Feel free to fork this repository and submit
a pull request.
