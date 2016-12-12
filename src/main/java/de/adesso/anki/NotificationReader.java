package de.adesso.anki;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

/**
 * Reads text from an InputStream using BufferedReader and notifies listeners every time a line is
 * read from the BufferedReader.
 *
 * @author Yannick Eckey <yannick.eckey@adesso.de>
 */
public class NotificationReader {
  private BufferedReader reader;
  private Thread listenerThread;
  private boolean closing;

  private ArrayList<NotificationListener> listeners;

  public NotificationReader(InputStream input) {
    reader = new BufferedReader(new InputStreamReader(input));
    listeners = new ArrayList<>();

    listenerThread = new Thread(() -> {
      while (!closing) {
        try {
          String line = reader.readLine();
          synchronized (listeners) {
            while (listeners.isEmpty()) {
              listeners.wait();
            }
          }
          notifyListeners(line);
        } catch (Exception e) {
          if (!closing)
            e.printStackTrace();
        }
      }
    });

    listenerThread.start();
  }

  public void addListener(NotificationListener listener) {
    synchronized (listeners) {
      listeners.add(listener);
      listeners.notify();
    }
  }

  public void removeListener(NotificationListener listener) {
    listeners.remove(listener);
  }

  private void notifyListeners(String line) {

    for (NotificationListener l : new ArrayList<>(listeners)) {
      l.onReceive(line);
    }
  }

  public String waitFor(String start) {
    CountDownLatch doneSignal = new CountDownLatch(1);
    StringContainer container = new StringContainer();

    NotificationListener listener = (receivedLine) -> {
      if (receivedLine.startsWith(start)) {
        doneSignal.countDown();
        container.line = receivedLine;
      }
    };
    this.addListener(listener);

    try {
      doneSignal.await();
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    this.removeListener(listener);

    return container.line;
  }

  private class StringContainer {
    public String line;
  }

  public void close() {
    this.closing = true;

    // try {
    // reader.close();
    // }
    // catch (IOException ex) {
    // ex.printStackTrace();
    // }
  }
}
