package de.adesso.anki;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import de.adesso.anki.messages.Message;

/**
 * Manages a Bluetooth LE connection by communicating with the Node.js socket.
 * 
 * @author Yannick Eckey <yannick.eckey@adesso.de>
 */
@SuppressWarnings("rawtypes")
public class AnkiConnector {
  
  private Socket socket;
  private final String host;
  private final int port;
  
  private PrintWriter writer;
  private NotificationReader reader;
  
  private Multimap<Vehicle, MessageListener> messageListeners;
  private Map<Vehicle, NotificationListener> notificationListeners;
  
  public AnkiConnector(String host, int port) throws IOException {
	this.host = host;
	this.port = port;
    socket = new Socket(host, port);
    writer = new PrintWriter(socket.getOutputStream(), true);
    reader = new NotificationReader(socket.getInputStream());
    
    messageListeners = ArrayListMultimap.create();
    notificationListeners = new HashMap<Vehicle, NotificationListener>();
  }
  
  public AnkiConnector(String host) throws IOException {
    this(host, 5000);
  }
  
  public AnkiConnector(AnkiConnector anki) throws IOException{
	  this(anki.host, anki.port);
  }
  
  public synchronized List<Vehicle> findVehicles() {
    writer.println("SCAN");
    List<Vehicle> foundVehicles = new ArrayList<>();
    boolean expectingResponse = true;
    while (expectingResponse)
    {
      String response = reader.waitFor("SCAN;");
      if (response.equals("SCAN;COMPLETED")) {
        expectingResponse = false;
      }
      else {
        String[] parts = response.split(";");
        
        String address = parts[1];
        String manufacturerData = parts[2];
        String localName = parts[3];
        
        foundVehicles.add(new Vehicle(this, address, manufacturerData, localName));
      }
    }
    return foundVehicles;
  }

  public synchronized void connect(Vehicle vehicle) throws InterruptedException {
    writer.println("CONNECT;"+vehicle.getAddress());
    String response = reader.waitFor("CONNECT;");
    
    if (response.equals("CONNECT;ERROR")) {
      throw new RuntimeException("connect failed");
    }
    
    NotificationListener carsNotificationListener = (line) -> {
        if (line.startsWith(vehicle.getAddress())) {
          String messageString = line.replaceFirst(vehicle.getAddress()+";", "");
          Message message = Message.parse(messageString);
          fireMessageReceived(vehicle, message);
        }
      };
    // check if there is a notification listener -> if yes, remove it! (otherwise it will be a listener we cannot track anymore...)
    if(notificationListeners.containsKey(vehicle)){
    	reader.removeListener(notificationListeners.get(vehicle));
    }
    notificationListeners.put(vehicle, carsNotificationListener);
    reader.addListener(carsNotificationListener);
  }
  
  public synchronized void sendMessage(Vehicle vehicle, Message message) {
    writer.println(vehicle.getAddress() + ";" + message.toHex());
    writer.flush();
  }
  
  public void addMessageListener(Vehicle vehicle, MessageListener<? extends Message> listener) {
    messageListeners.put(vehicle, listener);
  }
  
  public void removeMessageListener(Vehicle vehicle, MessageListener<? extends Message> listener) {
    messageListeners.remove(vehicle, listener);
  }
  
  @SuppressWarnings("unchecked")
  public void fireMessageReceived(Vehicle vehicle, Message message) {
    for (MessageListener l : messageListeners.get(vehicle)) {
      l.messageReceived(message);
    }
  }

  public synchronized void disconnect(Vehicle vehicle) {
    writer.println("DISCONNECT;"+vehicle.getAddress());
    reader.waitFor("DISCONNECT;");
    reader.removeListener(notificationListeners.remove(vehicle));
  }

  public void close() {
    reader.close();
    writer.close();
    
    try {
      socket.close();
    }
    catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
