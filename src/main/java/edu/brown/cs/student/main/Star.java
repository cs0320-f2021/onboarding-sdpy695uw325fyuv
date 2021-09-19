package edu.brown.cs.student.main;


//I stole the idea of making a separate Star class from PkR2JE77ps on Github
public class Star {

  private int starnum;
  private String starname;
  private float x;
  private float y;
  private float z;
  private float dist;

  public Star(int starnum, String starname, float x, float y, float z){
    this.starnum = starnum;
    this.starname = starname;
    this.x = x;
    this.y = y;
    this.z = z;
    this.dist = Float.MAX_VALUE;
  }

  public float getX() {
    return x;
  }

  public float getY() {
    return y;
  }

  public float getZ() {
    return z;
  }

  public void setMyDist(float d) {
    dist = d;
  }

  public float getMyDist() {
    return dist;
  }

  public final int getStarNum() {
    return this.starnum;
  }

  public final String getStarName() {
    return this.starname;
  }

}

