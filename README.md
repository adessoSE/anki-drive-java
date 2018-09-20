# Anki Drive SDK for Java

The Anki Drive SDK for Java is an implementation of the message protocols
and data parsing routines necessary for communicating with Anki Drive vehicles.

*See [anki/drive-sdk](https://github.com/anki/drive-sdk) for the official
SDK written in C.*

### Disclaimer
The authors of this software are in no way affiliated to Anki.
All naming rights for Anki, Anki Drive and Anki Overdrive are property of
[Anki](http://anki.com).

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
git clone https://github.com/adessoAG/anki-drive-java
cd anki-drive-java
./gradlew build
```

### On Linux

Optional Dependency node-usb will not be installed. So, run:
```
sudo apt-get install libudev-dev
```

## Usage

Start the Node.js gateway service:
```
./gradlew server
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
    compile 'com.github.adessoAG:anki-drive-java:<Git commit-ish>'
}
```

For the Maven instructions see the [JitPack.io website](https://jitpack.io/#adessoAG/anki-drive-java).

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
To try a connection, start the server and run the class:
```java
edu.oswego.cs.CPSLab.anki.AnkiConnectionTest
```

## Contributing

Contributions are always welcome! Feel free to fork this repository and submit
a pull request.
