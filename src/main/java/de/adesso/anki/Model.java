package de.adesso.anki;

import java.util.HashMap;
import java.util.Map;

/**
 * Enumerates some currently known Anki vehicle models.
 * Known vehicles currently available, but not considered in this class:
 * Supercars:
 * - NUKE Phantom
 * - Fast & Furious Dom's Charger
 * - Fast & Furious Hobbs' MXT
 * Supertrucks:
 * - X52 (probably ID 0x10).
 * 
 * @author Yannick Eckey <yannick.eckey@adesso.de>
 * @author B. Tenbergen <bastian.tenbergen@oswego.edu>
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
  GUARDIAN(0x0d, "#42b1d7"),
  BIGBANG(0x0e, "#4e674d"),
  FREEHWEEL(0x0f),              //BT update on 9/14/18 to add new supertruck
//  __SOMECAR1(0x10),             //TODO: figure out which car this is (BT)... probably X52
  X52ICE(0x11);                 //BT update on 9/14/18 to add new supertruck
//  __SOMECAR3(0x12);             //TODO: figure out which car this is (BT)
  
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