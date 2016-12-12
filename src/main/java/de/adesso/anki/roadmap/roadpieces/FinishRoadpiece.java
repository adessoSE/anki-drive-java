package de.adesso.anki.roadmap.roadpieces;

import de.adesso.anki.roadmap.Position;
import de.adesso.anki.roadmap.Section;

public class FinishRoadpiece extends Roadpiece {

  public final static int[] ROADPIECE_IDS = { 34 };
  public final static Position ENTRY = Position.at(-170, 0);
  public final static Position EXIT = Position.at(170, 0);

  public FinishRoadpiece() {
    this.section = new Section(this, ENTRY, EXIT);
  }
  
}
