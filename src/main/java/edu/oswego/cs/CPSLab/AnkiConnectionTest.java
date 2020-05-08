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
 * Simple follow the installation instructions at http://github.com/adessoAG/anki-drive-java, build this project, start the
 * bluetooth gateway using ./gradlew server, and run this class.
 *
 * @author Bastian Tenbergen (bastian.tenbergen@oswego.edu)
 */
public class AnkiConnectionTest implements Runnable {

    public static void main(String[] args) throws IOException, InterruptedException {

        System.out.println("Launching connector...");
        AnkiConnector anki = new AnkiConnector("192.168.1.62", 5000);
        System.out.print("...looking for cars...");
        List<Vehicle> vehicles = anki.findVehicles();

        if (vehicles.isEmpty()) {
            System.out.println(" NO CARS FOUND. I guess that means we're done.");

        } else {
            System.out.println(" FOUND " + vehicles.size() + " CARS!");
            System.out.println("\nNow connecting to and doing stuff to your cars.\n\n");

            Iterator<Vehicle> iter = vehicles.iterator();
            while (iter.hasNext()) {
                Vehicle v = iter.next();
                //Thread ct = new Thread(new AnkiConnectionTest((v)));
                //ct.run();
                AnkiConnectionTest act = new AnkiConnectionTest(v);
                act.run();
            }
        }
        anki.close();
    }

    private Vehicle v;

    public AnkiConnectionTest(Vehicle v) {
        this.v = v;
    }

    @Override
    public void run() {
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
        System.out.println("   SDK Mode set.");

        System.out.println("   Sending asynchronous Battery Level Request. The Response will come in eventually.");
        //we have to set up a response handler first, in order to handle async responses
        BatteryLevelResponseHandler blrh = new BatteryLevelResponseHandler();
        //now we tell the car, who is listening to the replies
        v.addMessageListener(BatteryLevelResponseMessage.class, blrh);
        //now we can actually send it.
        v.sendMessage(new BatteryLevelRequestMessage());

        System.out.println("   Sending Ping Request...");
        //again, some async set-up required...
        PingResponseHandler prh = new PingResponseHandler();
        v.addMessageListener(PingResponseMessage.class, prh);
        prh.pingSentAt = System.currentTimeMillis();
        v.sendMessage(new PingRequestMessage());

        System.out.println("   Flashing lights...");
        //Lights require configurations. So first construct a lights configuration,
        //then add it to the lights pattern message, then send the message. No handler required.
        LightConfig lc = new LightConfig(LightsPatternMessage.LightChannel.TAIL, LightsPatternMessage.LightEffect.STROBE, 0, 0, 0);
        LightsPatternMessage lpm = new LightsPatternMessage();
        lpm.add(lc);
        v.sendMessage(lpm);

        System.out.println("   Setting Speed...");
        //Speed is easy. Just tell the car how fast to go and how quickly to accelerate.
        v.sendMessage(new SetSpeedMessage(500, 100));

        System.out.println("    Looking for finish line...");
        //Use the sensor on the bottom to check the road pieces. This is like a response/request, but will
        //update whenever there's a new value.
        FinishLineDetector fld = new FinishLineDetector();
        v.addMessageListener(LocalizationPositionUpdateMessage.class, fld);
        v.sendMessage(new LocalizationPositionUpdateMessage());
        while (!fld.stop) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //  System.out.print("continue");
            //    System.out.print("    Looking for finish line...");
        }
        //         v.disconnect();
        System.out.println("disconnected from " + v + "\n");
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
   //         System.out.println(m.toString());
            if (m.getRoadPieceId() == finishLineId) stop = true;
        }
    }

    /**
     * Handles the response from the vehicle from the BatteryLevelRequestMessage.
     * We need handler classes because responses from the vehicles are asynchronous.
     */
    private class BatteryLevelResponseHandler implements MessageListener<BatteryLevelResponseMessage> {
        private int batt_level;
        @Override
        public void messageReceived(BatteryLevelResponseMessage m) {
            System.out.println("   Battery Level is: " + m.getBatteryLevel() + " mV");
        }
    }

    /**
     * Handles the response from the vehicle from the PingRequestMessage.
     * We need handler classes because responses from the vehicles are asynchronous.
     */
    private class PingResponseHandler implements MessageListener<PingResponseMessage> {
        private long pingReceivedAt;
        private long pingSentAt;

        @Override
        public void messageReceived(PingResponseMessage m) {
            pingReceivedAt = System.currentTimeMillis();
            System.out.println("   Ping response received. Roundtrip: " + (pingReceivedAt - pingSentAt) + " msec.");
        }
    }
}
