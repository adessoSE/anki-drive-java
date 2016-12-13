package de.adesso.anki;

import de.adesso.anki.messages.LocalizationPositionUpdateMessage;
import de.adesso.anki.messages.LocalizationTransitionUpdateMessage;
import de.adesso.anki.messages.SetSpeedMessage;
import de.adesso.anki.roadmap.Roadmap;

public class RoadmapScanner {

  private Vehicle vehicle;
  private Roadmap roadmap;
  
  private LocalizationPositionUpdateMessage lastPosition;
  
  public RoadmapScanner(Vehicle vehicle) {
    this.vehicle = vehicle;
    this.roadmap = new Roadmap();
  }
  
  public void startScanning() {
    vehicle.addMessageListener(
        LocalizationPositionUpdateMessage.class,
        (message) -> handlePositionUpdate(message)
    );
    
    vehicle.addMessageListener(
        LocalizationTransitionUpdateMessage.class,
        (message) -> handleTransitionUpdate(message)
    );
    
    vehicle.sendMessage(new SetSpeedMessage(500, 12500));
  }
  
  public void stopScanning() {
    vehicle.sendMessage(new SetSpeedMessage(0, 12500));
  }
  
  public boolean isComplete() {
    return roadmap.isComplete();
  }
  
  public Roadmap getRoadmap() {
    return roadmap;
  }
  
  public void reset(){
	  this.roadmap = new Roadmap();
	  this.lastPosition = null;
  }

  private void handlePositionUpdate(LocalizationPositionUpdateMessage message) {
    lastPosition = message;
  }

  protected void handleTransitionUpdate(LocalizationTransitionUpdateMessage message) {
    if (lastPosition != null) {
      roadmap.add(
          lastPosition.getRoadPieceId(),
          lastPosition.getLocationId(),
          lastPosition.isParsedReverse()
      );
      
      if (roadmap.isComplete()) {
        this.stopScanning();
      }
    }
  }
  
}
