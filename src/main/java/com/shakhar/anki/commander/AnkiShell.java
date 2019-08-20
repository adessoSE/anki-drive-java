package com.shakhar.anki.commander;

import de.adesso.anki.AnkiConnector;
import de.adesso.anki.Vehicle;
import de.adesso.anki.messages.*;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.command.Command;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnkiShell implements Command, Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnkiShell.class);

    private InputStream in;
    private OutputStream out;
    private OutputStream err;
    private ExitCallback callback;
    private Thread thread;
    private Terminal terminal;
    private AnkiConnector ankiConnector;

    private Map<String, Vehicle> vehicleMap;
    private Vehicle controlVehicle;

    @Override
    public void setInputStream(InputStream in) {
        this.in = in;
    }

    @Override
    public void setOutputStream(OutputStream out) {
        this.out = out;
    }

    @Override
    public void setErrorStream(OutputStream err) {
        this.err = err;
    }

    @Override
    public void setExitCallback(ExitCallback callback) {
        this.callback = callback;
    }

    @Override
    public void start(Environment env) throws IOException {
        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void destroy() throws Exception {
        if (terminal != null)
            terminal.close();
        thread.interrupt();
    }

    @Override
    public void run() {
        try {
            terminal = TerminalBuilder.builder().system(false).streams(in, out).build();
            LineReader reader = LineReaderBuilder.builder().terminal(terminal).build();
            String line;
            while ((line = reader.readLine("anki>")) != null && handleInput(line));
        } catch (IOException e) {
            LOGGER.error("IOException thrown", e);
        } catch (UserInterruptException e) {
            LOGGER.error("UserInterruptException thrown", e);
        } finally {
            callback.onExit(0);
        }
    }

    private boolean handleInput(String command) {
        String[] args = command.split("\\s+");
        switch (args[0]) {
            case "connect":
                handleConnect(args);
                break;
            case "scan":
                handleScan();
                break;
            case "control":
                handleControl(args);
                break;
            case "speed":
                handleSpeed(args);
                break;
            case "turn":
                handleTurn(args);
                break;
            case "lane":
                handleLane(args);
                break;
            case "light":
                handleLight(args);
                break;
            case "help":
                handleHelp();
                break;
            case "exit":
                handleExit();
                return false;
            default:
                write("Unknown command");
        }
        return true;
    }

    private void write(String s) {
        terminal.writer().println(s);
        terminal.writer().flush();
    }

    private void handleConnect(String[] args) {
        try {
            String host = "localhost";
            int port = 5000;
            if (args.length >= 2)
                host = args[1];
            if (args.length >= 3)
                port = Integer.parseInt(args[2]);
            ankiConnector = new AnkiConnector(host, port);
        } catch (IOException e) {
            e.printStackTrace(terminal.writer());
        }
        vehicleMap = new HashMap<>();
    }

    private void handleScan() {
        vehicleMap.clear();
        List<Vehicle> vehicles = ankiConnector.findVehicles();
        if (vehicles.isEmpty())
            write("No Vehicles Found.");
        else {
            write("Found " + vehicles.size() + " vehicle(s):");
            for (Vehicle vehicle : vehicles) {
                vehicleMap.put(vehicle.getAddress(), vehicle);
                write(vehicle.getAddress() + ": " + vehicle.getAdvertisement());
            }
        }
    }

    private void handleControl(String[] args) {
        if (controlVehicle != null)
            controlVehicle.disconnect();
        controlVehicle = vehicleMap.get(args[1]);
        if (controlVehicle != null) {
            controlVehicle.connect();
            controlVehicle.sendMessage(new SdkModeMessage());
        }
    }

    private void handleSpeed(String[] args) {
        int speed = Integer.parseInt(args[1]);
        int acceleration = Integer.parseInt(args[2]);
        controlVehicle.sendMessage(new SetSpeedMessage(speed, acceleration));
    }

    private void handleTurn(String[] args) {
        int turnType = Integer.parseInt(args[1]);
        int trigger = Integer.parseInt(args[2]);
        controlVehicle.sendMessage(new TurnMessage(turnType, trigger));
    }

    private void handleLane(String[] args) {
        int offsetFromCenter = Integer.parseInt(args[1]);
        int horizontalSpeed = Integer.parseInt(args[2]);
        int horizontalAcceleration = Integer.parseInt(args[3]);
        controlVehicle.sendMessage(new ChangeLaneMessage(offsetFromCenter, horizontalSpeed, horizontalAcceleration));
    }

    private void handleLight(String[] args) {
        LightsPatternMessage message = new LightsPatternMessage();
        for (int i = 1; i < args.length; i++) {
            String[] config = args[i].split(",");
            LightsPatternMessage.LightChannel channel = LightsPatternMessage.LightChannel.valueOf(config[0]);
            LightsPatternMessage.LightEffect effect = LightsPatternMessage.LightEffect.valueOf(config[1]);
            int start = Integer.parseInt(config[2]);
            int end = Integer.parseInt(config[3]);
            int cycles = Integer.parseInt(config[4]);
            message.add(new LightsPatternMessage.LightConfig(channel, effect, start, end, cycles));
        }
        controlVehicle.sendMessage(message);
    }

    private void handleHelp() {
        write("connect <host> <port> - Connects to the Anki Server. This should always be the first command. If host and port are not specified, default values of localhost and 5000 are used.\n\n" +
                "scan - Scans for all the available vehicles and prints out information about them. This should always be the second command after connect.\n\n" +
                "control <address> - Connects to the vehicle with the specified address. The address can be found from the output of scan.\n\n" +
                "speed <target_speed> <acceleration> - Changes speed of the connected vehicle.\n\n" +
                "turn <turn_type> <trigger> - Turns the connected vehicle. For a u-turn, use turn_type 3 and trigger 0 or 1.\n\n" +
                "lane <offset_from_center> <horizontal_speed> <horizontal_acceleration> - Makes the connected vehicle do a lane change to the specified offset.\n\n" +
                "light <list of configs> - Sets the lights of the connected vehicle using the specified configs. Each config is a comma-separated list of the format: <channel>,<effect>,<start>,<end>,<cycles>. Possible values of channels are ENGINE_RED, TAIL, ENGINE_BLUE, ENGINE_GREEN, FRONT_RED, FRONT_GREEN. Possible values of effects are STEADY, FADE, THROB, FLASH, STROBE.");
    }

    private void handleExit() {
        if (controlVehicle != null)
            controlVehicle.disconnect();
        if (ankiConnector != null)
            ankiConnector.close();
    }
}
