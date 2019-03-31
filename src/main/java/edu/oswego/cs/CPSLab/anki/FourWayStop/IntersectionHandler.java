package edu.oswego.cs.CPSLab.anki.FourWayStop;

import de.adesso.anki.AdvertisementData;
import de.adesso.anki.Vehicle;

import java.util.Queue;

/**
 * An interface to handle communication between cyber-physical vehicles.
 * @author Shakhar Dasgupta <sdasgupt@oswego.edu>
 */
public interface IntersectionHandler {
    void awaitClearIntersection();
    void broadcast(AdvertisementData vehicleInfo);
    void clearIntersection();
    void notify(Vehicle target, Queue queue);
    void listenToBroadcast();
    void becomeMaster();
}