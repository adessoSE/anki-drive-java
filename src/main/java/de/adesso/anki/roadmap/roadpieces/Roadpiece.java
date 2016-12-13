package de.adesso.anki.roadmap.roadpieces;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.reflections.Reflections;

import com.google.common.collect.ImmutableList;
import de.adesso.anki.roadmap.Position;
import de.adesso.anki.roadmap.Roadmap;
import de.adesso.anki.roadmap.Section;

public abstract class Roadpiece {
  private final static Reflections reflections = new Reflections("de.adesso.anki.roadmap.roadpieces");
  
  private Position position;
  protected Section section;
  
  public static Roadpiece createFromId(int roadpieceId) {
    Set<Class<? extends Roadpiece>> roadpieces = reflections.getSubTypesOf(Roadpiece.class);

    for (Class<? extends Roadpiece> roadpiece : roadpieces) {
      try {
        int[] ids = (int[]) roadpiece.getField("ROADPIECE_IDS").get(null);

        if (Arrays.binarySearch(ids, roadpieceId) >= 0) {
          return roadpiece.newInstance();
        }
      } catch (NoSuchFieldException | IllegalAccessException | InstantiationException | ClassCastException e) {
        // just skip the Roadpiece subclass if there is no ROADPIECE_IDS constant
        // or it cannot be instantiated
      }
    }

    return null;
  }
  
  public Position getPosition() {
    return position;
  }
  
  public void setPosition(Position position) {
    this.position = position;
  }
  
  public List<Section> getSections() {
    return ImmutableList.of(section);
  }
  
  public String getType() {
    return getClass().getSimpleName();
  };
  
  public Section getSectionByLocation(int locationId, boolean reverse)
  {
    return reverse ? section.reverse() : section;
  }
  
  //TODO: This should be a Unit-Test...
  public static void main(String[] args) {
    
    Roadmap map = new Roadmap();
    
    Roadpiece r0 = Roadpiece.createFromId(17);
    Roadpiece r1 = new FinishRoadpiece();
    Roadpiece r2 = new StartRoadpiece();
    Roadpiece r3 = new CurvedRoadpiece();
    Roadpiece r4 = new CurvedRoadpiece();
    Roadpiece r5 = new StraightRoadpiece();
    Roadpiece r6 = new CurvedRoadpiece();
    
    Section s0 = r0.getSectionByLocation(0, false);
    Section s1 = r1.getSectionByLocation(0, false);
    Section s2 = r2.getSectionByLocation(0, false);
    Section s3 = r3.getSectionByLocation(0, false);
    Section s4 = r4.getSectionByLocation(0, false);
    Section s5 = r5.getSectionByLocation(0, false);
    Section s6 = r6.getSectionByLocation(0, false);
    
    r0.setPosition(Position.at(0, 0));
    map.setAnchor(s0);
    
    s0.connect(s1);
    s1.connect(s2);
    s2.connect(s3);
    s3.connect(s4);
    s4.connect(s5);
    s5.connect(s6);
    s6.connect(s0);
    
    System.out.println(map.toList().toArray());
    
    System.exit(0);
  }
}
