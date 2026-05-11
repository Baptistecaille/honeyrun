/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Interface;

/**
 *
 * @author cpoussie
 */
public class GameCharacter {
    private int id;
    private String name;
    private boolean taken;

    public GameCharacter(int id, String name, boolean taken) {
        this.id = id;
        this.name = name;
        this.taken = taken;
    }

    public int getId() { 
        return id; 
    }
    public String getName() {
        return name; 
    }
    public boolean isTaken() { 
        return taken; 
    }
}

