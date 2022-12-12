/*
 * State University of New York, College at Oswego
 *
 * A tcp client that acts as a middle man between ANKI Drive vehicles and the ANKI Drive SDK for Java.
 * Forms a tcp/ip connection to the SDK and uses tinygo BLE module for connecting to each ANKI Drive vehicle.
 * ANKI Drive vehicle firmware and message protocol can be found here:
 *		https://github.com/tenbergen/anki-drive-java/blob/master/Anki%20Drive%20Programming%20Guide.pdf
 *
 * Date:    November 13, 2022
 * Author:  Bastian Tenbergen, PhD & Gregory Maldonado {bastian.tenbergen | gmaldona}@oswego.edu
 * Version: 1.0
 *
 */

package main

import (
	"bytes"
	"encoding/hex"
	"fmt"
	cmap "github.com/orcaman/concurrent-map/v2"
	"gopkg.in/yaml.v3"
	"io/ioutil"
	"log"
	"net"
	"regexp"
	"strings"
	"time"
	"tinygo.org/x/bluetooth"
)

const (
	ANSI_RESET = "\u001B[0m"
	ANSI_RED   = "\u001B[31m"
	ANSI_GREEN = "\u001B[32m"
)

var (
	server                  Server
	Adapter                 = bluetooth.DefaultAdapter
	AdapterEnabled          = false
	ANKI_STR_SERVICE_UUID   = bluetooth.NewUUID([16]byte{0xBE, 0x15, 0xBE, 0xEF, 0x61, 0x86, 0x40, 0x7E, 0x83, 0x81, 0x0B, 0xD8, 0x9C, 0x4D, 0x8D, 0xF4})
	ANKI_STR_CHR_READ_UUID  = bluetooth.NewUUID([16]byte{0xBE, 0x15, 0xBE, 0xE0, 0x61, 0x86, 0x40, 0x7E, 0x83, 0x81, 0x0B, 0xD8, 0x9C, 0x4D, 0x8D, 0xF4})
	ANKI_STR_CHR_WRITE_UUID = bluetooth.NewUUID([16]byte{0xBE, 0x15, 0xBE, 0xE1, 0x61, 0x86, 0x40, 0x7E, 0x83, 0x81, 0x0B, 0xD8, 0x9C, 0x4D, 0x8D, 0xF4})
)

type Server struct {
	DiscoveredDevices     cmap.ConcurrentMap[string, AnkiVehicle]
	ConnectedDevices      cmap.ConcurrentMap[string, *bluetooth.Device]
	DeviceCharacteristics cmap.ConcurrentMap[string, []bluetooth.DeviceCharacteristic]
}

type AnkiVehicle struct {
	Address          string
	ManufacturerData string
	LocalName        string
	Addresser        bluetooth.Addresser
}

type ServerConf struct {
	Host string `yaml:"host"`
	Port string `yaml:"port"`
}

func main() {
	server.DiscoveredDevices = cmap.New[AnkiVehicle]()
	server.ConnectedDevices = cmap.New[*bluetooth.Device]()
	server.DeviceCharacteristics = cmap.New[[]bluetooth.DeviceCharacteristic]()

	file, err := ioutil.ReadFile("serverconf.yml")
	if err != nil {
		displayError(err.Error())
	}

	serverConf := ServerConf{}
	err = yaml.Unmarshal(file, &serverConf)
	if err != nil {
		displayError(err.Error())
	}

	// Listen for connections on host and port
	l, err := net.Listen("tcp", serverConf.Host+":"+serverConf.Port)
	if err != nil {
		displayError(err.Error())
	}

	// terminate server on port when disconnected
	defer func(l net.Listener) {
		l.Close()
	}(l)
	displayInfo("Starting Server... Listening on " + serverConf.Host + ":" + serverConf.Port)
	for {
		// Listen for an incoming connection.
		conn, err := l.Accept()
		displayInfo("Connection established.")

		if err != nil {
			displayError(err.Error())
		}
		// Handle connections in a new goroutine.
		go handleRequest(conn)
	}
}

// Handles the incoming requests from the tcp connection
func handleRequest(conn net.Conn) {

	// Keep grabbing messages from tcp connection until server termination
	for {
		// Read the incoming connection into the buffer.
		buf := make([]byte, 1024)
		_, err := conn.Read(buf)
		// if err, then probably a client disconnect
		if err != nil {
			displayInfo("Client disconnect? Disconnecting all devices...")
			for _, device := range server.ConnectedDevices.Items() {
				device.Disconnect()
			}
			server.ConnectedDevices = cmap.New[*bluetooth.Device]()
			conn.Close()
			return
		}

		// Create a goroutine for incoming msg and listen for the next msg
		go func(buf []byte) {
			// parsing msg so the payload can go to the vehicle - payload is at index [1]
			re, _ := regexp.Compile(";")
			split := re.Split(string(buf), -1)
			var set []string

			for i := range split {
				set = append(set, strings.Replace(split[i], "\n", "", -1))
			}

			address := set[0]
			var msg string

			if len(set) > 1 {
				msg = set[1]
			}

			// Perform different actions based on the tcp msg received from ANKI SDK
			switch {
			// SCAN request from java
			case strings.Contains(string(buf), "SCAN"):
				displayInfo("Scanning...")
				// call scan function to search for nearby vehicles
				server.DiscoveredDevices = scan()
				for _, device := range server.DiscoveredDevices.Items() {
					// for each found device, send a tcp msg to java saying found
					conn.Write([]byte("SCAN;" + device.Address + ";" + device.ManufacturerData + ";" + device.LocalName + "\n"))

					displayInfo("Found device: " + device.Address)
					time.Sleep(500 * time.Millisecond)
				}
				// Stops scanning on java side
				conn.Write([]byte("SCAN;COMPLETED\n"))
				fmt.Println(ANSI_GREEN + "Scanning Completed." + ANSI_RESET)
				return

			//DISCONNECT request from java
			case strings.Contains(string(buf), "DISCONNECT"):

				// disconnect the vehicle with the address in the buffer
				address := string(bytes.Trim([]byte(set[1]), "\x00"))
				connectedDevice, ok := server.ConnectedDevices.Get(address)
				if !ok {
					displayError("Address: " + address + " could not be found.")
				}
				connectedDevice.Disconnect()
				server.ConnectedDevices.Remove(address)

				conn.Write([]byte("DISCONNECT;SUCCESS\n"))
				displayInfo(address + " Disconnected.")

			// CONNECT request from java
			case strings.Contains(set[0], "CONNECT"):
				// ignore 0x0 fillers
				payload := bytes.Trim([]byte(set[1]), "\x00")

				device, _ := server.DiscoveredDevices.Get(string(payload))

				// connect to device
				connectedDevice, err := Adapter.Connect(device.Addresser, bluetooth.ConnectionParams{})
				if err != nil {
					displayError(err.Error())
				}

				// add device to concurrent map of devices
				server.ConnectedDevices.Set(device.Address, connectedDevice)
				fmt.Println(ANSI_GREEN + "Connected to " + device.Address + ANSI_RESET)

				services, _ := connectedDevice.DiscoverServices([]bluetooth.UUID{ANKI_STR_SERVICE_UUID})
				if err != nil {
					displayInfo(err.Error())
				}

				// Getting the writers and readers services
				service := services[0]
				characteristics, _ := service.DiscoverCharacteristics([]bluetooth.UUID{ANKI_STR_CHR_READ_UUID, ANKI_STR_CHR_WRITE_UUID})
				server.DeviceCharacteristics.Set(device.Address, characteristics)

				readService := characteristics[1]

				// Each time the vehicle sends a msg through bluetooth, the event is triggered
				readService.EnableNotifications(func(value []byte) {
					encodedBytes := hex.EncodeToString(value)
					// Send the vehicle respond back to java
					conn.Write([]byte(device.Address + ";" + encodedBytes + "\n"))
					displayInfo("RECEIVED: [" + device.Address + ";" + encodedBytes + "]")
				})

				// terminate connection request to java
				conn.Write([]byte("CONNECT;SUCCESS\n"))
				fmt.Println(ANSI_GREEN + "CONNECT COMPLETED." + ANSI_RESET)

			/* Any other request is assumed to be a command given to the car. Each byte in the buffer represents an action that is
			outlined in https://github.com/tenbergen/anki-drive-java/blob/master/Anki%20Drive%20Programming%20Guide.pdf
			*/
			default:
				if len(set) == 2 {
					// Get the writer characteristic
					characteristics, _ := server.DeviceCharacteristics.Get(address)
					writeService := characteristics[0]
					payload, _ := hex.DecodeString(msg)

					// write payload to anki vehicle
					_, err := writeService.WriteWithoutResponse(payload)
					if err != nil {
						displayError(err.Error())
					}

					displayInfo("SENDING: [" + strings.Replace(string(buf), "\n", "", -1) + "]")
				}
			}
		}(buf)
	}
}

// function for scanning nearby vehicles returns a map of addresses to vehicles
func scan() cmap.ConcurrentMap[string, AnkiVehicle] {
	devicesFound := cmap.New[AnkiVehicle]()

	channel := make(chan string, 1)
	// func that is wrapped, so it can time out in some number of seconds
	go func() {

		if !AdapterEnabled {
			must("enable BLE stack", Adapter.Enable())
			AdapterEnabled = true
		}

		err := Adapter.Scan(func(adapter *bluetooth.Adapter, device bluetooth.ScanResult) {
			// only scan for devices that contain "Drive" for anki drive
			if strings.Contains(device.LocalName(), "Drive") {
				if !devicesFound.Has(device.Address.String()) {
					var manufacturerData = ""
					for _, data := range device.ManufacturerData() {
						manufacturerData = "beef" + hex.EncodeToString(data)
					}
					var localname = "10603001202020204472697665"
					// ANKI device properties
					devicesFound.Set(strings.Replace(device.Address.String(), "-", "", -1), AnkiVehicle{
						Address:          strings.Replace(device.Address.String(), "-", "", -1),
						ManufacturerData: manufacturerData,
						LocalName:        localname,
						Addresser:        device.Address,
					})
				}
			}
		})
		if err != nil {
			return
		}
		//must("start scan", err)
		//must("enable BLE stack", Adapter.StopScan())

	}()

	// timeout scan
	select {
	case <-channel:
		channel <- "break"
		break
	case <-time.After(5 * time.Second):
		break
	}

	return devicesFound
}

func must(action string, err error) {
	if err != nil {
		panic("failed to " + action + ": " + err.Error())
	}
}

func displayInfo(msg string) {
	fmt.Println(ANSI_GREEN + "[INFO] " + ANSI_RESET + msg)
}

func displayError(msg string) {
	fmt.Print(ANSI_RED + "[ERROR] " + ANSI_RESET)
	log.Fatalln(msg)
}
