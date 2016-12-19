package de.adesso.anki;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

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
  
  private PrintWriter writer;
  private NotificationReader reader;
  
  private Multimap<Vehicle, MessageListener> listeners;
  
  public AnkiConnector(String host, int port) throws IOException {
    socket = new Socket(host, port);
    writer = new PrintWriter(socket.getOutputStream(), true);
    reader = new NotificationReader(socket.getInputStream());
    
    listeners = ArrayListMultimap.create();
  }
  
  public AnkiConnector(String host) throws IOException {
    this(host, 5000);
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

  synchronized void connect(Vehicle vehicle) throws InterruptedException {
    writer.println("CONNECT;"+vehicle.getAddress());
    String response = reader.waitFor("CONNECT;");
    
    if (response.equals("CONNECT;ERROR")) {
      throw new RuntimeException("connect failed");
    }
    
    reader.addListener((line) -> {
      if (line.startsWith(vehicle.getAddress())) {
        String messageString = line.replaceFirst(vehicle.getAddress()+";", "");
        Message message = Message.parse(messageString);
        fireMessageReceived(vehicle, message);
      }
    });
  }
  
  synchronized void sendMessage(Vehicle vehicle, Message message) {
    writer.println(vehicle.getAddress() + ";" + message.toHex());
    writer.flush();
  }
  
  void addMessageListener(Vehicle vehicle, MessageListener<? extends Message> listener) {
    listeners.put(vehicle, listener);
  }
  
  void removeMessageListener(Vehicle vehicle, MessageListener<? extends Message> listener) {
    listeners.remove(vehicle, listener);
  }
  
  @SuppressWarnings("unchecked")
  void fireMessageReceived(Vehicle vehicle, Message message) {
    for (MessageListener l : listeners.get(vehicle)) {
      l.messageReceived(message);
    }
  }

  synchronized void disconnect(Vehicle vehicle) {
    writer.println("DISCONNECT;"+vehicle.getAddress());
    reader.waitFor("DISCONNECT;");
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
