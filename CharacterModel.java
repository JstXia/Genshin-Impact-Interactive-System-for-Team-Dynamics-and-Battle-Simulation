package model;

public class CharacterModel {
    public int characterID;
    public String name;
    public String element;
    public String region;
    public int level;
    public Integer weaponID;
    public Integer artifact1, artifact2, artifact3, artifact4;
    public String ascensionMaterial;

    public String toString() {
        return characterID + " - " + name;
    }
}
