package de.adesso.anki.roadmap.roadpieces;

import de.adesso.anki.roadmap.Position;
import de.adesso.anki.roadmap.Section;

/**
 * Represents a Jump Roadpiece from the Overdrive Launch Kit.
 * @since 2020-05-18
 * @version 2020-05-18
 * @author Bastian Tenbergen (bastian.tenbergen@oswego.edu)
 */
public class JumpRoadpiece extends Roadpiece {

    public final static int[] ROADPIECE_IDS = { 58 };
    public final static Position ENTRY = Position.at(-280, 0);
    public final static Position EXIT = Position.at(280, 0);

    public JumpRoadpiece() {
        this.section = new Section(this, ENTRY, EXIT);
    }

}
