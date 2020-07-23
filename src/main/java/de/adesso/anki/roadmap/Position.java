package de.adesso.anki.roadmap;

import java.io.Serializable;

/**
 * Position object used to differentiate Sections. Entering the same Section using a Roadpiece might have different
 * entry and exist positions. This is relevant for curves as well as intersections.
 * 2020-05-12 - Updated with toString helper function for debugging. Added Serializable marker for saving Roadmaps.
 *
 * @since 2016-12-13
 * @version 2020-05-12
 * @author adesso AG
 * @author Bastian Tenbergen (bastian.tenbergen@oswego.edu)
 */
public class Position implements Serializable {
  private double x;
  private double y;
  private double angle;
  
  private Position(double x, double y, double angle) {
    this.x = x;
    this.y = y;
    this.angle = (angle % 360 + 360) % 360;
  }
  
  public static Position at(double x, double y) {
    return new Position(x, y, 0);
  }
  
  public static Position at(double x, double y, double angle) {
    return new Position(x, y, angle);
  }
  
  public Position reverse() {
    return new Position(x, y, angle + 180);
  }
  
  public Position translate(double x, double y) {
    double newX = this.x + x;
    double newY = this.y + y;
    double newAngle = this.angle;
    
    return new Position(newX, newY, newAngle);
  }
  
  public Position invTranslate(double x, double y) {
    return this.translate(-x, -y);
  }
  
  public Position rotate(double angle) {
    double newX = Math.cos(Math.toRadians(angle)) * this.x + Math.sin(Math.toRadians(angle)) * this.y;
    double newY = -Math.sin(Math.toRadians(angle)) * this.x + Math.cos(Math.toRadians(angle)) * this.y;
    double newAngle = this.angle + angle;
    
    return new Position(newX, newY, newAngle);
  }
  
  public Position invRotate(double angle) {
    return this.rotate(-angle);
  }
  
  public Position transform(Position other) {    
    return other.rotate(this.angle)
                .translate(this.x, this.y);
  }
  
  public Position invTransform(Position other) {
    double newAngle = this.angle - other.angle;
    Position tmp = other.rotate(newAngle);
    double newX = this.x - tmp.x;
    double newY = this.y - tmp.y;
    
    return new Position(newX, newY, newAngle);
  }
  
  public double distance(Position other) {
    double dx = this.x - other.x;
    double dy = this.y - other.y;
    
    return Math.sqrt(dx*dx + dy*dy);
  }

  public double getX() {
    return x;
  }

  public double getY() {
    return y;
  }

  public double getAngle() {
    return angle;
  }

  /**
   * Provides a neatly formatted string representation of a Position.
   * @return String representation of this Position.
   * @since 2020-05-12
   * @version 2020-05-12
   * @author Bastian Tenbergen (bastian.tenbergen@oswego.edu)
   */
  public String toString() {
    return "X: " + getX() + ", Y: " + getY() + ", Angle: " + getAngle();
  }
}
