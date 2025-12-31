package buildings;

import java.awt.Color;

public class Building {
    private String type;
    private Color color;

    public Building(String type, Color color) {
        this.type = type;
        this.color = color;
    }

    public String getType() { return type; }
    public Color getColor() { return color; }
}