package de.adesso.anki.roadmap.roadpieces;

import java.util.List;

import com.google.common.collect.ImmutableList;

import de.adesso.anki.roadmap.Position;
import de.adesso.anki.roadmap.Section;

public class IntersectionRoadpiece extends Roadpiece {

  public final static int[] ROADPIECE_IDS = { 10 };
  
  public final static Position NORTH = Position.at(0, -280, -90);
  public final static Position SOUTH = Position.at(0, 280, -90);
  public final static Position WEST = Position.at(-280, 0);
  public final static Position EAST = Position.at(280, 0);
  
  private Section sectionH;
  private Section sectionV;

  public IntersectionRoadpiece() {
    this.sectionH = new Section(this, WEST, EAST);
    this.sectionV = new Section(this, NORTH, SOUTH);
  }
  
  @Override
  public List<Section> getSections() {
    return ImmutableList.of(sectionH, sectionV);
  }
  
  @Override
  public Section getSectionByLocation(int locationId, boolean reverse) {
    switch (locationId / 4) {
      case 0: return reverse ? sectionH.reverse() : sectionH;
      case 1: return reverse ? sectionV.reverse() : sectionV;
      case 2: return reverse ? sectionH : sectionH.reverse();
      case 3: return reverse ? sectionV : sectionV.reverse();
      default: return null;
    }
  }
  
}
