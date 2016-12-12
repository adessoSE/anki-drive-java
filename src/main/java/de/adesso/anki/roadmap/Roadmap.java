package de.adesso.anki.roadmap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.adesso.anki.roadmap.roadpieces.Roadpiece;

public class Roadmap {
  
  private Section anchor;
  private Section current;
  
  public void setAnchor(Section anchor) {
    this.anchor = anchor;
  }
  
  public void addSection(Section section) {
    if (current == null) {
      anchor = current = section;
      anchor.getPiece().setPosition(Position.at(0,0,180));
    }
    else {
      current.connect(section);
      current = section;

      Position currentExit = current.getPiece().getPosition().transform(current.getExit());
      Position anchorEntry = anchor.getPiece().getPosition().transform(anchor.getEntry());
      if (currentExit.distance(anchorEntry) < 1) {
        current.connect(anchor);
      }
    }
  }
  
  public void add(int roadpieceId, int locationId, boolean reverse) {
    Roadpiece piece = Roadpiece.createFromId(roadpieceId);
    Section section = piece.getSectionByLocation(locationId, reverse);
    
    this.addSection(section);
  }
  
  public List<Roadpiece> toList() {
    if (anchor == null) return Collections.emptyList();
    
    List<Roadpiece> list = new ArrayList<>();
    list.add(anchor.getPiece());
    
    Section iterator = anchor.getNext();
    while (iterator != null && iterator != anchor) {
      if (iterator.getPiece() != null) {
        list.add(iterator.getPiece());
      }
      iterator = iterator.getNext();
    }
    
    return Collections.unmodifiableList(list);
  }
  
  public boolean isComplete() {
    return anchor != null && anchor.getPrev() != null;
  }
  
}
