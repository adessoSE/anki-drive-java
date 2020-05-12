package de.adesso.anki.roadmap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import de.adesso.anki.roadmap.roadpieces.Roadpiece;

/**
 * Roadmap object created by de.adesso.anki.RoadmapScanner.
 * 2020-05-10 - Updated from original adesso version with ability to compare Roadmaps against each other.
 * Roadmaps against each other.
 *
 * @since 2016-12-13
 * @version 2020-05-12
 * @author adesso AG
 * @author Bastian Tenbergen (bastian.tenbergen@oswego.edu)
 */
public class Roadmap {

    private Section anchor;
    private Section current;

    public void setAnchor(Section anchor) {
        this.anchor = anchor;
    }

    public void addSection(Section section) {
        if (current == null) {
            anchor = current = section;
            anchor.getPiece().setPosition(Position.at(0, 0, 180));
        } else {
            current.connect(section);
            current = section;

            Position currentExit = current.getPiece().getPosition().transform(current.getExit());
            Position anchorEntry = anchor.getPiece().getPosition().transform(anchor.getEntry());
            if (currentExit.distance(anchorEntry) < 1) {
                current.connect(anchor);
            }
        }
    }

    /**
     * Adds a Roadpiece that was encountered to the current Roadmap.
     * 2020-05-12 - Updated to report unknown Roadpieces.
     * @param roadpieceId The ID of the Roadpiece. Must correspond to an Integer in ROADPIECE_IDS field of Roadpiece subclasses.
     * @param locationId The location on the Roadpiece from which the Vehicle entered.
     * @param reverse flag to determine if the Roadpiece is reversed.
     * @since 2016-12-13
     * @version 2020-05-11
     * @author adesso AG
     * @author Bastian Tenbergen (bastian.tenbergen@oswego.edu)
     */
    public void add(int roadpieceId, int locationId, boolean reverse) {
        Roadpiece piece = Roadpiece.createFromId(roadpieceId);
        if (piece == null) {
            System.out.println("Error scanning track: Unknown roadpiece with ID " + roadpieceId + ", location: " + locationId + ", revesed: " + reverse);
            return;
        }
        Section section = piece.getSectionByLocation(locationId, reverse);
        this.addSection(section);
    }

    public List<Roadpiece> toList() {
        if (anchor == null) return Collections.emptyList();

        List<Roadpiece> list = new ArrayList<>();
        list.add(anchor.getPiece());

        Section iterator = anchor.getNext();
        while (iterator != null && iterator != anchor) {
            if (iterator.getPiece() != null) {
                list.add(iterator.getPiece());
            }
            iterator = iterator.getNext();
        }

        return Collections.unmodifiableList(list);
    }

    public boolean isComplete() {
        return anchor != null && anchor.getPrev() != null;
    }

    /**
     * Computes the length of the Roadmap in number of Roadpieces.
     *
     * @return The number of Roadpieces in this Roadmap, identical to toList().size()
     * @since 2020-05-10
     * @version 2020-05-11
     * @author Bastian Tenbergen (bastian.tenbergen@oswego.edu)
     */
    public int getLength() {
        return this.toList().size();
    }

    /**
     * Compares two Roadmaps for equality. General idea:
     * - if both roadmaps aren't of the same length, return false.
     * - advance in both roadmaps
     * - until you find the first piece that differ - so return false.
     * - if you find an end piece,
     * - they must both be the end piece - so return true
     * - else return false.
     *
     * @param o The other Roadmap to be compared to.
     * @return true if the Roadmaps are of same type, length, and contain the same sequence of Roadpieces, false else.
     * @since 2020-05-10
     * @version 2020-05-11
     * @author Bastian Tenbergen (bastian.tenbergen@oswego.edu)
     */
    @Override
    public boolean equals(Object o) {
        //if it's the same object, they are equal
        if (this == o) return true;
        //if they aren't of the same class, they can't be equal
        if (o == null || getClass() != o.getClass()) return false;

        Roadmap roadmap = (Roadmap) o;
        //if they aren't of the same length, they can't be equal
        if (roadmap.getLength() != this.getLength()) return false;

        Iterator<Roadpiece> theseRoadpieces = this.toList().iterator();
        Iterator<Roadpiece> thoseRoadpieces = ((Roadmap) o).toList().iterator();

        //advance in both Roadmaps until you find a reason why they aren't equal.
        while (theseRoadpieces.hasNext()) {
            Roadpiece thisCurrent = theseRoadpieces.next();
            Roadpiece thatCurrent;
            try {
                thatCurrent = thoseRoadpieces.next();

                if (!thisCurrent.getType().equals(thatCurrent.getType())) {
                    System.out.println("NOT EQUAL! pieces don't match.");
                    return false; //if the two Roadpieces are not of the same class, then the Roadmaps are different
                }
                // PROBLEM 1: what if the curve isn't the same direction? This would be considered equal:
                // start -> (left) curve -> (left) curve -> straight -> (left) curve -> (left) curve -> Finish
                // and
                // start -> (right) curve -> (right) curve -> straight -> (right) curve -> (right) curve -> Finish
                //fix might involve checking Sections and Positions rather than Roadpieces

                // PROBLEM 2: should identify two of the same track but one traveled in reverse as the same track
                //fix might require second .equals method with boolean "ignoreReversed"
            } catch (NoSuchElementException nseo) {
                //if there are no more elements in the other roadmap, it's shorter, so they are obviously not equal.
                //Hence, return false.
                return false;
            }
        }
        //if you get all the way to the end without failing, they must be equal.
        return true;
    }

    /**
     * Provides a neatly formatted string representation of all Roadpieces in the Roadmap in their correct order.
     * @return String representation of this Roadmap.
     * @since 2020-05-10
     * @version 2020-05-11
     * @author Bastian Tenbergen (bastian.tenbergen@oswego.edu)
     */
    public String toString() {

        StringBuilder sb = new StringBuilder();
        Iterator<Roadpiece> iter = this.toList().iterator();
        int i = 1;
        while (iter.hasNext()) {
            sb.append(i);
            sb.append(": ");
            sb.append(iter.next().getType());
            sb.append("\n");
            i++;
        }
        return sb.toString();
    }
}
