package de.adesso.anki.roadmap.roadpieces;

import de.adesso.anki.roadmap.Position;
import de.adesso.anki.roadmap.Section;

/**
 * Represents a Powerzone Roadpiece from the Fast & Furious tracks.
 * @since 2020-05-11
 * @version 2020-05-11
 * @author Bastian Tenbergen (bastian.tenbergen@oswego.edu)
 */
public class PowerzoneRoadpiece extends Roadpiece {

    public final static int[] ROADPIECE_IDS = { 57 };
    public final static Position ENTRY = Position.at(-280, 0);
    public final static Position EXIT = Position.at(280, 0);

    public PowerzoneRoadpiece() {
        this.section = new Section(this, ENTRY, EXIT);
    }

}
