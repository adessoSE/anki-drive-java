package de.adesso.anki.roadmap.roadpieces;

import de.adesso.anki.roadmap.Position;
import de.adesso.anki.roadmap.Section;

public class CurvedRoadpiece extends Roadpiece {

  public final static int[] ROADPIECE_IDS = { 17, 18, 20, 23, 24, 27 };
  public final static Position ENTRY = Position.at(-280, 0);
  public final static Position EXIT = Position.at(0, -280, 90);

  public CurvedRoadpiece() {
    this.section = new Section(this, ENTRY, EXIT);
  }
  
}
