package model;
public class DomainModel {
    public int domainID;
    public String name, region, difficulty;
    public String toString(){ return domainID + " - " + name + " (" + difficulty + ")"; }
}
