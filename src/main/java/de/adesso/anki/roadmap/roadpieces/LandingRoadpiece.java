package de.adesso.anki.roadmap.roadpieces;

import de.adesso.anki.roadmap.Position;
import de.adesso.anki.roadmap.Section;

/**
 * Represents a Landing Roadpiece from the Overdrive Launch Kit.
 * @since 2020-05-18
 * @version 2020-05-18
 * @author Bastian Tenbergen (bastian.tenbergen@oswego.edu)
 */
public class LandingRoadpiece extends Roadpiece {

    public final static int[] ROADPIECE_IDS = { 63 };
    public final static Position ENTRY = Position.at(-280, 0);
    public final static Position EXIT = Position.at(280, 0);

    public LandingRoadpiece() {
        this.section = new Section(this, ENTRY, EXIT);
    }

}
