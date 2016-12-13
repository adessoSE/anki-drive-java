package de.adesso.anki.roadmap.roadpieces;

import de.adesso.anki.roadmap.Position;
import de.adesso.anki.roadmap.Section;

public class StraightRoadpiece extends Roadpiece {
  
  public final static int[] ROADPIECE_IDS = { 36, 39, 40, 48, 51 };
  public final static Position ENTRY = Position.at(-280, 0);
  public final static Position EXIT = Position.at(280, 0);

  public StraightRoadpiece() {
    this.section = new Section(this, ENTRY, EXIT);
  }

}
