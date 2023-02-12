package de.adesso.anki.roadmap;

import de.adesso.anki.roadmap.roadpieces.Roadpiece;

import java.io.Serializable;

/**
 * Section object used to differentiate reversed and regular roadpieces, curves, and intersections from one another
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
public class Section implements Serializable {
  private Roadpiece piece;

  private Position entry;

  private boolean isReversed = false;
  private static final long serialVersionUID = -2053150693350041204L;
  
  protected Section() { }
  
  public Section(Roadpiece piece, Position entry, Position exit) {
    this.piece = piece;
    this.entry = entry;
    this.exit = exit;
  }
  
  public Section getPrev() {
    return prev;
  }

  public void setPrev(Section prev) {
    this.prev = prev;
  }

  public Section getNext() {
    return next;
  }

  public void setNext(Section next) {
    this.next = next;
  }

  public Roadpiece getPiece() {
    return piece;
  }

  public Position getEntry() {
    return entry;
  }

  public Position getExit() {
    return exit;
  }

  private Position exit;
  
  private Section prev;
  private Section next;
  
  public void connect(Section other) {
    this.setNext(other);
    other.setPrev(this);
    
    Position pos = this.getPiece().getPosition();
    Position otherPos = pos.transform(this.getExit()).invTransform(other.getEntry());
    other.getPiece().setPosition(otherPos);
  }
  
  public Section reverse() { return new ReverseSection(this); }
}
