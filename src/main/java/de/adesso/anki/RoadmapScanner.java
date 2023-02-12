package de.adesso.anki;

import java.util.ArrayList;
import java.util.Collections;
import de.adesso.anki.messages.LocalizationPositionUpdateMessage;
import de.adesso.anki.messages.LocalizationTransitionUpdateMessage;
//import de.adesso.anki.messages.SetSpeedMessage;
import de.adesso.anki.roadmap.Roadmap;

/**
 * Scans a track from start to finish. Cars may start on any track piece; scan will complete once they arrive again on
 * the same track piece.
 * 2020-05-10 - Updated to no longer move the vehicles. Didn't work reliably due to asynchronous messages interlacing on some architecttures. It's not the caller's responsibility to move the vehicle.
 * 2020-05-11 - Updated to normalize the Roadmap, i.e., the first track piece is a StartRoadpiece, the last one is a FinishRoadpiece
 * 2020-07-27 - Updated to include the creation of lists of pieceIDs and reverses
 * Usage:
 * 1. create new RoadmapScanner with a Vehicle
 * 2. call startScanning()
 * 3. send SpeedMessage to the same Vehicle
 * 4. wait until isComplete() is true
 * 5. call stopScanning()
 * 6. Roadmap object will be available using
 *
 * @since 2016-12-13
 * @version 2020-05-10
 * @author adesso AG
 * @author Bastian Tenbergen (bastian.tenbergen@oswego.edu)
 * @author Ka Ying Chan (kchan2@oswego.edu)
 */
public class RoadmapScanner {

    private Vehicle vehicle;
    private Roadmap roadmap;
    private boolean initReverse;
    private ArrayList<Integer> pieceIDs;
    private ArrayList<Boolean> reverses;

    private LocalizationPositionUpdateMessage lastPosition;

    public RoadmapScanner(Vehicle vehicle) {
        this.vehicle = vehicle;
        this.roadmap = new Roadmap();
        this.pieceIDs = new ArrayList<Integer>();
        this.reverses = new ArrayList<Boolean>();
    }

    /**
     * Starts the scan by adding message listeners to the car.
     * Updated from original version, which would also move the car.
     *
     * @since 2016-12-13
     * @version 2020-05-10
     * @author adesso AG
     * @author Bastian Tenbergen (bastian.tenbergen@oswego.edu)
     */
    public void startScanning() {
        vehicle.addMessageListener(
                LocalizationPositionUpdateMessage.class,
                (message) -> handlePositionUpdate(message)
        );

        vehicle.addMessageListener(
                LocalizationTransitionUpdateMessage.class,
                (message) -> handleTransitionUpdate(message)
        );
        //vehicle.sendMessage(new SetSpeedMessage(500, 12500));
    }

    /**
     * Stops the scan by removing the message listeners from the car.
     * Updated from original version, which would just stop the car.
     *
     * @since 2016-12-13
     * @version 2020-05-10
     * @author adesso AG
     * @author Bastian Tenbergen (bastian.tenbergen@oswego.edu)
     */
    public void stopScanning() {
        vehicle.removeMessageListener(
                LocalizationPositionUpdateMessage.class,
                (message) -> handlePositionUpdate(message)
        );

        vehicle.removeMessageListener(
                LocalizationTransitionUpdateMessage.class,
                (message) -> handleTransitionUpdate(message)
        );
        //vehicle.sendMessage(new SetSpeedMessage(0, 12500));
        if (initReverse) {
            Collections.reverse(pieceIDs);
            Collections.reverse(reverses);
            for (int i = 0; i < pieceIDs.size(); i++) {
                if (pieceIDs.get(i) != 10) {
                    reverses.set(i, !reverses.get(i));
                }
            }
        }
        int distance = pieceIDs.size() - 1 - pieceIDs.indexOf(34);
        Collections.rotate(pieceIDs, distance);
        Collections.rotate(reverses, distance);
    }

    public boolean isComplete() {
        return roadmap.isComplete();
    }

    public Roadmap getRoadmap() {
        return roadmap;
    }

    public boolean getInitReverse() {
        return initReverse;
    }

    public ArrayList<Integer> getPieceIDs() {
        return pieceIDs;
    }

    public ArrayList<Boolean> getReverses() {
        return reverses;
    }

    public void reset() {
        this.roadmap = new Roadmap();
        this.lastPosition = null;
    }

    private void handlePositionUpdate(LocalizationPositionUpdateMessage message) {
        lastPosition = message;
    }

    protected void handleTransitionUpdate(LocalizationTransitionUpdateMessage message) {
        if (lastPosition != null) {
            roadmap.add(
                    lastPosition.getRoadPieceId(),
                    lastPosition.getLocationId(),
                    lastPosition.isParsedReverse()
            );

            pieceIDs.add(lastPosition.getRoadPieceId());
            reverses.add(lastPosition.isParsedReverse());
//            System.out.println("Added a piece... " + pieceIDs.get(pieceIDs.size() - 1));

            if (lastPosition.getRoadPieceId() == 33 || lastPosition.getRoadPieceId() == 34) {
                initReverse = lastPosition.isParsedReverse();
            }

            if (roadmap.isComplete()) {
                this.stopScanning();
            }
        }
    }
}
