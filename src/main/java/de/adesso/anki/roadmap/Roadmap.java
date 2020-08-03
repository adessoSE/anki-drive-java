package de.adesso.anki.roadmap;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import de.adesso.anki.roadmap.roadpieces.*;

/**
 * Roadmap object created by de.adesso.anki.RoadmapScanner.
 * 2020-05-10 - Updated from original adesso version with ability to compare Roadmaps against each other.
 * Roadmaps against each other.
 *
 * @author adesso AG
 * @author Bastian Tenbergen (bastian.tenbergen@oswego.edu)
 * @version 2020-05-12
 * @since 2016-12-13
 */
public class Roadmap implements Serializable, Cloneable {

    private Section nonNormalizedAnchor = null;
    private Section anchor;
    private Section current;
    private static final long serialVersionUID = -8832115043850834353L;

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
     *
     * @param roadpieceId The ID of the Roadpiece. Must correspond to an Integer in ROADPIECE_IDS field of Roadpiece subclasses.
     * @param locationId  The location on the Roadpiece from which the Vehicle entered.
     * @param reverse     flag to determine if the Roadpiece is reversed.
     * @version 2020-05-12
     * @author adesso AG
     * @author Bastian Tenbergen (bastian.tenbergen@oswego.edu)
     * @since 2016-12-13
     */
    public void add(int roadpieceId, int locationId, boolean reverse) {
        Roadpiece piece = Roadpiece.createFromId(roadpieceId);
        //    System.out.println(piece.getType());
        if (piece == null) {
            System.out.println("Error scanning track: Unknown roadpiece with ID " + roadpieceId + ", location: " + locationId + ", revesed: " + reverse);
            return;
        }
        Section section = piece.getSectionByLocation(locationId, reverse);
        //    System.out.println("   entry: " + section.getEntry());
        //    System.out.println("   exit: " + section.getExit());
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

    /**
     * Normalized this Roadmap. Normalized means that the StartRoadpiece is the first Roadpiece in the Roadmap. This
     * is done by setting the first StartRoadpiece to the anchor.
     * @version 2020-05-14
     * @author Bastian Tenbergen (bastian.tenbergen@oswego.edu)
     * @since 2020-05-13
     */
    public void normalize() {
        this.nonNormalizedAnchor = this.anchor;
        System.out.println("normalizing...");
        if (this.anchor.getPiece().getType().equals(StartRoadpiece.class.getSimpleName())) {
            System.out.println("anchor is StartPiece");
            return;     //if the anchor is a Startpiece, it's already normalized.
        }

        Section iter = this.anchor.getNext();
        while (iter != this.anchor) {
            if (iter.getPiece().getType().equals(StartRoadpiece.class.getSimpleName())) {
                this.anchor = iter;
                return;
            }
            iter = iter.getNext();
        }
        //if there is no Startpiece in the Roadmap, normalized Roadmap is the same as the original one.
    }

    /**
     * De-Normalized this Roadmap by restoring the anchor back to what it was before normalize() was called.
     * @version 2020-05-14
     * @author Bastian Tenbergen (bastian.tenbergen@oswego.edu)
     * @since 2020-05-13
     */
    public void deNormalize() {
        this.anchor = this.nonNormalizedAnchor;
        this.nonNormalizedAnchor = null;
    }

    /**
     * Reverses this Roadmap. Reversed means that the order of Roadpieces is reversed.
     * Known issue: Roadpieces themselves are NOT reversed, meaning that a reversed Roadmap is also mirrer-reflected.
     * (i.e., from Start-left-left-straight-left-left-finish, it would be turned into Finish-left-left-straight-left-left-start).
     * @version 2020-05-14
     * @author Bastian Tenbergen (bastian.tenbergen@oswego.edu)
     * @since 2020-05-13
     */
    public void reverse() {
        this.anchor = this.anchor.getPrev();
        Section curr = this.anchor;
        Section next = curr.getNext();
        Section prev = curr.getPrev();
        curr.setPrev(next);
        curr.setNext(prev);
        curr = curr.getNext();

        while (curr != null && curr != this.anchor) {
            next = curr.getNext();
            prev = curr.getPrev();
            curr.setPrev(next);
            curr.setNext(prev);
            curr = curr.getNext();
        }
    }

    public boolean isComplete() {
        return anchor != null && anchor.getPrev() != null;
    }

    /**
     * Computes the length of the Roadmap in number of Roadpieces.
     *
     * @return The number of Roadpieces in this Roadmap, identical to toList().size()
     * @version 2020-05-11
     * @author Bastian Tenbergen (bastian.tenbergen@oswego.edu)
     * @since 2020-05-10
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
     * @version 2020-05-13
     * @author Bastian Tenbergen (bastian.tenbergen@oswego.edu)
     * @since 2020-05-10
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
                } else if (thisCurrent.getType().equals(CurvedRoadpiece.class.getSimpleName())) {
                    //if the two Roadpieces are both a Curve, it matters if they are "left curves" or "right curves."
                    //Only if not the order of Roadpieces and also their direction are the same, the Roadmaps can
                    //truly be equal.
                    //Here, we check if the entry point is the same in both pieces. If they aren't, they are not the same curve.
                    thisCurrent = (CurvedRoadpiece) thisCurrent;
                    thatCurrent = (CurvedRoadpiece) thatCurrent;
                    if (thisCurrent.getSections().get(0).getEntry() != thatCurrent.getSections().get(0).getEntry()) {
                        return false; //if they are both reversed (or both not reversed), they are both left or both right.
                    }
                } else if (thisCurrent.getType().equals(IntersectionRoadpiece.class.getSimpleName())) {
                    //if the two Roadpieces are both an Intersection, the direction in which the car enters (i.e.,
                    //North, East, West, South) matters. Just like in curves
                    //Only if not the order of Roadpieces and also their direction are the same, the Roadmaps can
                    //truly be equal.
                    thisCurrent = (IntersectionRoadpiece) thisCurrent;
                    thatCurrent = (IntersectionRoadpiece) thatCurrent;
                    if (thisCurrent.getSections().get(0).getEntry() != thatCurrent.getSections().get(0).getEntry()) {
                        return false; //if they are both reversed (or both not reversed), they are both left or both right.
                    }
                } else {
                    //the direction of straights or Powerzones doesn't matter.
                    //Finishpieces and Startpieces matter, but are treated as different Roadpieces, so we don't need to
                    //do anything.
                }
                // PROBLEM 1: what if the curve isn't the same direction? This would be considered equal:
                // start -> (left) curve -> (left) curve -> straight -> (left) curve -> (left) curve -> Finish
                // and
                // start -> (right) curve -> (right) curve -> straight -> (right) curve -> (right) curve -> Finish
                //fix might involve checking Sections and Positions rather than Roadpieces

                // PROBLEM 2: should identify two of the same track but one traveled in reverse as the same track
                //fix might require second .equals method with boolean "ignoreReversed"
                //will need a reverse() function for the Roadmap
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
     *
     * @return String representation of this Roadmap.
     * @version 2020-05-11
     * @author Bastian Tenbergen (bastian.tenbergen@oswego.edu)
     * @since 2020-05-10
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

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    /**
     * Writes a Roadmap to disk.
     * @param toSave The Roadmap to save.
     * @param path The path where to save it to. Filename and extension doesn't matter.
     * @return True, if saving was successful, false else.
     */
    public static boolean saveRoadmap(Roadmap toSave, String path) {
        try {
            FileOutputStream fileWriter = new FileOutputStream(path);
            ObjectOutputStream rmWriter = new ObjectOutputStream(fileWriter);
            rmWriter.writeObject(toSave);
            rmWriter.close();
        } catch (IOException ex) {
            System.err.println(ex.toString());
            return false;
        }
        return true;
    }

    /**
     * Loads a Roadmap from disk.
     * @param path The path where to load from.
     * @return The Roadmap as it was written to disk, or null if there was a failure.
     */
    public static Roadmap loadRoadmap(String path) {
        Roadmap rm = null;
        try {
            FileInputStream fileReader = new FileInputStream(path);
            ObjectInputStream rmReader = new ObjectInputStream(fileReader);
            rm = (Roadmap) rmReader.readObject();
            rmReader.close();
        } catch (IOException | ClassNotFoundException | NullPointerException ex) {
            System.err.println(ex.toString());
        }
        return rm;
    }
}
