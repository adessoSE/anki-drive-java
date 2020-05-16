package de.adesso.anki.roadmap.roadpieces;

import de.adesso.anki.roadmap.Position;
import de.adesso.anki.roadmap.Section;

/**
 *
 * Right curves are not reversed.
 * Left curves are reversed.
 */
/**
 * Roadpiece subclass representing instances of type Curve. This is the same version as the orignal adesso version,
 * however we found it prudent to write down somewhere that a curve to the right is reversed == TRUE and references a
 * Section object. A left curve is hence reversed == FALSE and references a ReversedSection object.
 *
 * @author adesso AG
 * @author Bastian Tenbergen (bastian.tenbergen@oswego.edu)
 * @version 2020-05-12
 * @since 2016-12-13
 */
public class CurvedRoadpiece extends Roadpiece {

  public final static int[] ROADPIECE_IDS = { 17, 18, 20, 23, 24, 27 };
  public final static Position ENTRY = Position.at(-280, 0);
  public final static Position EXIT = Position.at(0, -280, 90);

  public CurvedRoadpiece() {
    this.section = new Section(this, ENTRY, EXIT);
  }
  
}
