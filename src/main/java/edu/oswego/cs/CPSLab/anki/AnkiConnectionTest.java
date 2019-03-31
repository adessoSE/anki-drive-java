package edu.oswego.cs.CPSLab.anki;

import de.adesso.anki.AnkiConnector;
import de.adesso.anki.MessageListener;
import de.adesso.anki.Vehicle;
import de.adesso.anki.messages.BatteryLevelRequestMessage;
import de.adesso.anki.messages.BatteryLevelResponseMessage;
import de.adesso.anki.messages.LightsPatternMessage;
import de.adesso.anki.messages.LightsPatternMessage.LightConfig;
import de.adesso.anki.messages.PingRequestMessage;
import de.adesso.anki.messages.PingResponseMessage;
import de.adesso.anki.messages.SdkModeMessage;
import de.adesso.anki.messages.SetSpeedMessage;
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
public class AnkiConnectionTest {

    static long pingReceivedAt;
    static long pingSentAt;
    
    public static void main(String[] args) throws IOException, InterruptedException {

        System.out.println("Launching connector...");
        AnkiConnector anki = new AnkiConnector("interplexus.local", 5000);
        System.out.print("...looking for cars...");
        List<Vehicle> vehicles = anki.findVehicles();

        if (vehicles.isEmpty()) {
            System.out.println(" NO CARS FOUND. I guess that means we're done.");
            
        } else {
            System.out.println(" FOUND " + vehicles.size() + " CARS! They are:");
            
            Iterator<Vehicle> iter = vehicles.iterator();
            while (iter.hasNext()) {
                Vehicle v = iter.next();
                System.out.println("   " + v);
                System.out.println("      ID: " + v.getAdvertisement().getIdentifier());
                System.out.println("      Model: " + v.getAdvertisement().getModel());
                System.out.println("      Model ID: " + v.getAdvertisement().getModelId());
                System.out.println("      Product ID: " + v.getAdvertisement().getProductId());
                System.out.println("      Address: " + v.getAddress());
                System.out.println("      Color: " + v.getColor());
                System.out.println("      charging? " + v.getAdvertisement().isCharging());
            }
            
            System.out.println("\nNow connecting to and doing stuff to your cars.\n\n");

            iter = vehicles.iterator();
            while (iter.hasNext()) {
                Vehicle v = iter.next();
                System.out.println("\nConnecting to " + v + " @ " + v.getAddress());
                v.connect();
                System.out.print("   Connected. Setting SDK mode...");   //always set the SDK mode FIRST!                
                v.sendMessage(new SdkModeMessage());
                System.out.println("   SDK Mode set.");
                
                System.out.println("   Sending asynchronous Battery Level Request. The Response will come in eventually.");
                //we have to set up a response handler first, in order to handle async responses
                BatteryLevelResponseHandler blrh = new BatteryLevelResponseHandler();
                //now we tell the car, who is listenening to the replies
                v.addMessageListener(BatteryLevelResponseMessage.class, blrh);
                //now we can actually send it.
                v.sendMessage(new BatteryLevelRequestMessage());
                
                System.out.println("   Sending Ping Request...");
                //again, some async set-up required...
                PingResponseHandler prh = new PingResponseHandler();
                v.addMessageListener(PingResponseMessage.class, prh);
                AnkiConnectionTest.pingSentAt = System.currentTimeMillis();
                v.sendMessage(new PingRequestMessage());
                
                System.out.println("   Flashing lights...");
                LightConfig lc = new LightConfig(LightsPatternMessage.LightChannel.TAIL, LightsPatternMessage.LightEffect.STROBE, 0, 0, 0);
                LightsPatternMessage lpm = new LightsPatternMessage();
                lpm.add(lc);
                v.sendMessage(lpm);
                System.out.println("   Setting Speed...");
                v.sendMessage(new SetSpeedMessage(500, 100));
                //Thread.sleep(1000);
                //gs.sendMessage(new TurnMessage());
                System.out.print("Sleeping for 10secs... ");
                Thread.sleep(10000);
                v.disconnect();
                System.out.println("disconnected from " + v + "\n");
            }
        }
        anki.close();
        System.exit(0);
    }

    /**
     * Handles the response from the vehicle from the BatteryLevelRequestMessage.
     * We need handler classes because responses from the vehicles are asynchronous.
     */
    private static class BatteryLevelResponseHandler implements MessageListener<BatteryLevelResponseMessage> {
        @Override
        public void messageReceived(BatteryLevelResponseMessage m) {
            System.out.println("   Battery Level is: " + m.getBatteryLevel() + " mV");
        }
    }
    
    /**
     * Handles the response from the vehicle from the PingRequestMessage.
     * We need handler classes because responses from the vehicles are asynchronous.
     */
    private static class PingResponseHandler implements MessageListener<PingResponseMessage> {
        @Override
        public void messageReceived(PingResponseMessage m) {
            AnkiConnectionTest.pingReceivedAt = System.currentTimeMillis();
            System.out.println("   Ping response received. Roundtrip: " + (AnkiConnectionTest.pingReceivedAt - AnkiConnectionTest.pingSentAt) + " msec.");
        }
    }
}
