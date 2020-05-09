package edu.oswego.cs.CPSLab;

import de.adesso.anki.AnkiConnector;
import de.adesso.anki.MessageListener;
import de.adesso.anki.Vehicle;
import de.adesso.anki.messages.*;
import de.adesso.anki.messages.LightsPatternMessage.LightConfig;
import de.adesso.anki.roadmap.roadpieces.FinishRoadpiece;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * A simple test program to test a connection to your Anki 'Supercars' and 'Supertrucks' using the NodeJS Bluetooth gateway.
 * Simple follow the installation instructions at http://github.com/tenbergen/anki-drive-java, build this project, start the
 * bluetooth gateway using ./gradlew server, and run this class.
 *
 * @author Bastian Tenbergen (bastian.tenbergen@oswego.edu)
 */
public class AnkiConnectionTest {

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Launching connector...");
        AnkiConnector anki = new AnkiConnector("localhost", 5000);
        System.out.print(" looking for cars...");
        List<Vehicle> vehicles = anki.findVehicles();

        if (vehicles.isEmpty()) {
            System.out.println(" NO CARS FOUND. I guess that means we're done.");

        } else {
            System.out.println(" FOUND " + vehicles.size() + " CARS!");
            System.out.println(" Now connecting to and doing stuff to your cars.");

            Iterator<Vehicle> iter = vehicles.iterator();
            while (iter.hasNext()) {
                Vehicle v = iter.next();
                AnkiConnectionTest act = new AnkiConnectionTest(v);
                act.run();
            }
        }
        anki.close();
        System.out.println("Test complete.");
        System.exit(0);
    }

    private Vehicle v;

    public AnkiConnectionTest(Vehicle v) {
        this.v = v;
    }

    public void run() throws InterruptedException {
        System.out.println("\nConnecting to " + v + " @ " + v.getAddress());
        v.connect();
        System.out.println("Vehicle Advertisement Data:");
        System.out.println("   " + v);
        System.out.println("      ID: " + v.getAdvertisement().getIdentifier());
        System.out.println("      Model: " + v.getAdvertisement().getModel());
        System.out.println("      Model ID: " + v.getAdvertisement().getModelId());
        System.out.println("      Product ID: " + v.getAdvertisement().getProductId());
        System.out.println("      Address: " + v.getAddress());
        System.out.println("      Color: " + v.getColor());
        System.out.println("      charging? " + v.getAdvertisement().isCharging());


        System.out.print("   Connected. Setting SDK mode...");   //always set the SDK mode FIRST!
        v.sendMessage(new SdkModeMessage());
        System.out.println(" done.");

        System.out.print("   Sending ping...");
        //we have to set up a response handler first, in order to handle async responses
        PingResponseHandler prh = new PingResponseHandler();
        //now we tell the car, who is listening to the replies
        v.addMessageListener(PingResponseMessage.class, prh);
        //now we can actually send it.
        v.sendMessage(new PingRequestMessage());
        prh.pingSentAt = System.currentTimeMillis();
        System.out.print(" sent. Waiting at most 10secs for pong...");
        while (!prh.pingReceived) {
            Thread.sleep(10);
        }
        System.out.println(" Roundtrip: " + prh.roundTrip + " msec.");

        System.out.println("   Sending asynchronous Battery Level Request. Response will come eventually.");
        BatteryLevelResponseHandler blrh = new BatteryLevelResponseHandler();
        //works just like a Ping Request
        v.addMessageListener(BatteryLevelResponseMessage.class, blrh);
        v.sendMessage(new BatteryLevelRequestMessage());

        System.out.println("   Flashing lights...");
        //Lights require configurations. So first construct a lights configuration,
        //then add it to the lights pattern message, then send the message. No handler required.
        LightConfig lc = new LightConfig(LightsPatternMessage.LightChannel.TAIL, LightsPatternMessage.LightEffect.STROBE, 0, 0, 0);
        LightsPatternMessage lpm = new LightsPatternMessage();
        lpm.add(lc);
        v.sendMessage(lpm);
        //we should sleep for at least some factor of the ping roundtrip to give the Vehicle time to set the lights
        Thread.sleep(prh.roundTrip * 10);

        System.out.println("   Setting Speed...");
        //Speed is easy. Just tell the car how fast to go and how quickly to accelerate.
        v.sendMessage(new SetSpeedMessage(500, 100));

        System.out.println("   Driving to finish line...");
        //Use the sensor on the bottom to check the road pieces. This is like a response/request, but will
        //update whenever there's a new value.
        FinishLineDetector fld = new FinishLineDetector();
        v.addMessageListener(LocalizationPositionUpdateMessage.class, fld);
        v.sendMessage(new LocalizationPositionUpdateMessage());
        while (!fld.stop ) {
            Thread.sleep(prh.roundTrip);
        }
        v.disconnect();
        System.out.println("Disconnected from " + v);
    }

    /**
     * Handles the response from the vehicle from the PingRequestMessage.
     * We need handler classes because responses from the vehicles are asynchronous.
     * Sets a received flag to true and computes the roundtrip time.
     */
    private class PingResponseHandler implements MessageListener<PingResponseMessage> {
        private boolean pingReceived = false;
        private long pingSentAt = System.currentTimeMillis();
        private long roundTrip = -1;

        @Override
        public void messageReceived(PingResponseMessage m) {
            this.pingReceived = true;
            this.roundTrip = System.currentTimeMillis() - pingSentAt;
        }
    }

    /**
     * Handles the response from the vehicle from the BatteryLevelRequestMessage.
     * Updates the battery level whenever a BatteryLEvelRequestMessage is sent.
     */
    private class BatteryLevelResponseHandler implements MessageListener<BatteryLevelResponseMessage> {
        private int batt_level;

        @Override
        public void messageReceived(BatteryLevelResponseMessage m) {
            this.batt_level = m.getBatteryLevel();
            System.out.println("   Battery Level is: " + this.batt_level + " mV");
        }
    }

    /**
     * Handles the response from the vehicle on which road piece the vehicle is
     * and sets a stop flag to true if it's the finish line.
     */
    private class FinishLineDetector implements MessageListener<LocalizationPositionUpdateMessage> {
        private int finishLineId = FinishRoadpiece.ROADPIECE_IDS[0];
        private boolean stop = false;

        @Override
        public void messageReceived(LocalizationPositionUpdateMessage m) {
            if (m.getRoadPieceId() == finishLineId) this.stop = true;
        }
    }
}
