package map;

import java.awt.Color;

public class Tile {
    private String type;
    private boolean accessible;
    private Color color;
    private boolean isWall = false;
    private int wallHp = 5;
    private int x, y;


    public Tile(String type, boolean accessible, Color color, int x, int y) {
        this.type = type;
        this.accessible = accessible;
        this.color = color;
        this.x = x;
        this.y = y;
    }

    public String getType() { return type; }
    public boolean isAccessible() { return accessible; }
    public Color getColor() { return color; }
    public int getX() { return x; }
    public int getY() { return y; }

    public void setType(String type) {
        this.type = type;
    }
    public boolean isWall() {
        return isWall;
    }

    public void setWall(boolean wall) {
        isWall = wall;
        wallHp = 5;
    }

    public void damageWall(int dmg) {
        if (isWall) {
            wallHp -= dmg;
            System.out.println("Wall had" + dmg + " damage health: " + wallHp);

            if (wallHp <= 0) {
                isWall = false;
                wallHp = 0;

                // revert tile back to grass
                type = "Grass";
                accessible = true;
                color = Color.GREEN;

                System.out.println("Wall down");
            }
        }
    }

    public void setColor(Color darkGray) {
    }
}