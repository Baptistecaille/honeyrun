/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Interface;

/**
 *
 * @author cpoussie
 */
public class PlayerSQL {
    private int id;
    private String pseudo;
    private Integer characterId; // null tant qu'il n'a pas choisi

    public PlayerSQL(int id, String pseudo, Integer characterId) {
        this.id = id;
        this.pseudo = pseudo;
        this.characterId = characterId;
    }

    public int getId() { return id; }
    public String getPseudo() { return pseudo; }
    public Integer getCharacterId() { return characterId; }
    public void setCharacterId(Integer characterId) { this.characterId = characterId; }

}
