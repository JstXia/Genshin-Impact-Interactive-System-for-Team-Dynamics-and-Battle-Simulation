package model;
public class MobModel {
    public int mobID;
    public String name, region, type, element;
    public int level;
    public int hp, atk, def;
    public double critRate, critDmg;
    public String toString(){ return mobID + " - " + name; }
}
