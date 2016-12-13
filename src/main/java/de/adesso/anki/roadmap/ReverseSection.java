package de.adesso.anki.roadmap;

import de.adesso.anki.roadmap.roadpieces.Roadpiece;

public class ReverseSection extends Section {

  private Section original;
  
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
