package de.adesso.anki.roadmap;

import de.adesso.anki.roadmap.roadpieces.Roadpiece;

public class Section {
  private Roadpiece piece;
  
  private Position entry;
  
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
  
  public Section reverse() {
    return new ReverseSection(this);
  }
}
