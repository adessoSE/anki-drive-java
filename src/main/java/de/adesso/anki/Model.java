package de.adesso.anki;

import java.util.HashMap;
import java.util.Map;

/**
 * Enumerates some currently known Anki vehicle models.
 * Updated on 9/14/19 and 3/31/19 to include extra models. Model 0x0d, previously "Guardian" reassigned to
 * 0x0c. Model 0x0d is now unknown model.
 * 
 * @author Yannick Eckey <yannick.eckey@adesso.de>
 * @author B. Tenbergen <bastian.tenbergen@oswego.edu>
 * @version 2019-03-31
 */
public enum Model {
  KOURAI(0x01),
  BOSON(0x02),
  RHO(0x03),
  KATAL(0x04),
  HADION(0x05),
  SPEKTRIX(0x06),
  CORAX(0x07),
  GROUNDSHOCK(0x08, "#2994f1"),
  SKULL(0x09, "#df3232"),
  THERMO(0x0a, "#a11c20"),
  NUKE(0x0b, "#bed62f"),
  GUARDIAN(0x0c, "#42b1d7"),   //BT update on 3/31/19 to correct ID of model "Guardian"
  __SOMECAR2(0x0d),                  //BT update on 3/31/19 to add ID of unknown model
  BIGBANG(0x0e, "#4e674d"),
  FREEHWEEL(0x0f, "#25bc00"),  //BT update on 9/14/18 to add new supertruck Freewheel
  X52(0x10, "#990909"),        //BT update on 3/31/19 to add new supertruck X52
  X52ICE(0x11, "#d1e9ff"),     //BT update on 9/14/18 to add new supertruck X52 Ice
  MXT(0x12, "#475666"),        //BT update on 3/31/19 to add Fast & Furious Ed. Intl. MXT
  CHARGER(0x13, "#6d7175"),    //BT update on 3/31/19 to add Fast & Furious Ed. Ice Charger
  PHANTOM(0x14, "#2d2d2d");    //BT update on 3/31/19 to add NUKE Phantom model
  
  private int id;
  private String color = "#f00";
  
  private Model(int id) { this.id = id; }
  private Model(int id, String color) { this.id = id; this.color = color; }
  
  public String getColor() {
    return color;
  }
  
  public static Model fromId(int id) {
    return idToModel.get(id);
  }
  
  private static final Map<Integer, Model> idToModel = new HashMap<Integer, Model>() {{
    for (Model m : Model.values()) {
      put(m.id, m);
    }
  }};
}