package de.adesso.anki.roadmap.roadpieces;

import de.adesso.anki.roadmap.Position;
import de.adesso.anki.roadmap.Section;

public class StartRoadpiece extends Roadpiece {
  
  public final static int[] ROADPIECE_IDS = { 33 };
  public final static Position ENTRY = Position.at(-110, 0);
  public final static Position EXIT = Position.at(110, 0);

  public StartRoadpiece() {
    this.section = new Section(this, ENTRY, EXIT);
  }
  
}
