package de.adesso.anki.roadmap;

import de.adesso.anki.roadmap.roadpieces.Roadpiece;

/**
 * ReverseSection object used to differentiate reversed and regular roadpieces, curves, and intersections from one another
 * Entering the same Section using a Roadpiece might have different entry and exist positions.
 * This is mostly relevant for curves as well as intersections, e.g., left curves will be ReverseSections, and right
 * curves will be regular Sections.
 * This is the original adesso version, but updated with a Serializable marker and SerialVersionUID for saving Roadmaps.
 *
 * @since 2016-12-13
 * @version 2020-05-12
 * @author adesso AG
 * @author Bastian Tenbergen (bastian.tenbergen@oswego.edu)
 */
public class ReverseSection extends Section {

  private Section original;
  private boolean isReversed = true;
  private static final long serialVersionUID = 4217292978578338519L;
  
  public ReverseSection(Section original) {
    this.original = original;
  }
  
  public Section reverse() {
    return original;
  }

  public Section getPrev() {
    return original.getNext();
  }
  
  public void setPrev(Section prev) {
    original.setNext(prev);
  }

  public Section getNext() {
    return original.getPrev();
  }

  public void setNext(Section next) {
    original.setPrev(next);
  }

  public Roadpiece getPiece() {
    return original.getPiece();
  }

  public Position getEntry() {
    return original.getExit().reverse();
  }

  public Position getExit() {
    return original.getEntry().reverse();
  }
  
}
