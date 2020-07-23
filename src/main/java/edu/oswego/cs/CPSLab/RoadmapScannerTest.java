package edu.oswego.cs.CPSLab;

import de.adesso.anki.AnkiConnector;
import de.adesso.anki.MessageListener;
import de.adesso.anki.RoadmapScanner;
import de.adesso.anki.Vehicle;
import de.adesso.anki.messages.*;
import de.adesso.anki.roadmap.Roadmap;
import de.adesso.anki.roadmap.roadpieces.FinishRoadpiece;
import de.adesso.anki.roadmap.roadpieces.StartRoadpiece;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * A simple test program to demonstrate how to scan a track with Overdrive vehicles.
 *
 * @since 2020-05-10
 * @version 2020-05-11
 * @author Bastian Tenbergen (bastian.tenbergen@oswego.edu)
 */
public class RoadmapScannerTest {

    public static void main(String[] args) throws InterruptedException {

        System.out.print("Loading Roadmap... ");
        Roadmap rm0 = Roadmap.loadRoadmap(System.getenv("user.home" + "/" + "Roadmap.ovrdrv"));
        if (rm0 != null) {
            System.out.println("loaded track is:");
            System.out.println(rm0.toString());

            System.out.println("Reversed Roadmap:");
            rm0.reverse();
            System.out.println(rm0.toString());

            System.out.println("Normalized Roadmap:");
            rm0.reverse();   // since .reverse() works on the Roadmap itself, gotta reverse again,
            rm0.normalize(); // else we get a reversed AND normalized Roadmap.
            System.out.println(rm0.toString());

        } else {
            System.out.println("Sorry, no Roadmap found on disk.");
        }

        System.out.println("Now, let's look for any car and try to scan the track...");

        AnkiConnector anki = null;
        try {
            anki = new AnkiConnector("localhost", 5000);
        } catch (IOException ioe) {
            System.out.println("Error connecting to server. Is it running?");
            System.out.println("Exiting.");
            System.exit(1);
        }
        List<Vehicle> vehicles = anki.findVehicles();

        if (vehicles.isEmpty()) {
            System.out.println(" NO CARS FOUND. I guess that means we're done.");

        } else {
            System.out.println(" FOUND " + vehicles.size() + " CARS!");
            System.out.println(" Now connecting to cars and scanning track.");

            Iterator<Vehicle> iter = vehicles.iterator();
            Vehicle v = null;
            while (iter.hasNext()) {
                v = iter.next();
                if (v.getAdvertisement().isCharging()) {
                    System.out.println("Skipping " + v + " because it is charging.");
                    continue;
                }
            }

            RoadmapScannerTest rmst = new RoadmapScannerTest(v);
            Roadmap rm2 = rmst.scan();


            System.out.println("Scanned track is:");
            System.out.println(rm2.toString());
            System.out.println("Trying to save Roadmap..." + Roadmap.saveRoadmap(rm2, "user.home" + "/" + "Roadmap.ovrdrv"));

            System.out.println("Are the loaded and the scanned Roadmap equal?");
            System.out.println(rm0.equals(rm2));

        }
        anki.close();
        System.out.println("Scan complete.");
        System.exit(0);
    }

    private Vehicle v;
    private long interval = 10;

    public RoadmapScannerTest(Vehicle v) {
        this.v = v;
    }

    public Roadmap scan() throws InterruptedException {
        System.out.println("Scanning with: " + v);
        v.connect();
        v.sendMessage(new SdkModeMessage());

        Thread.sleep(interval * 10);

        System.out.print("Moving car...");
        v.sendMessage(new SetSpeedMessage(500, 100));

        System.out.print(" scanning...");
        RoadmapScanner rms = new RoadmapScanner(this.v);
        rms.startScanning();
        while (!rms.isComplete()) {
            Thread.sleep(interval);
        }
        rms.stopScanning();
        System.out.println(" complete. Stopping.");
        v.sendMessage(new SetSpeedMessage(0, 100));

        v.disconnect();
        System.out.println("Disconnected from " + v);

        return rms.getRoadmap();
    }
}
