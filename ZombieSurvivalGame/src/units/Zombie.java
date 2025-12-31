package units;

import map.Tile;
import java.util.List;
import java.util.ArrayList;

public class Zombie {
    private int x, y;
    public int hp = 50;
    private int attack = 1;
    public Zombie(int x, int y) {
        this.x = x;
        this.y = y;
    }
    public int getHp() {
        return hp;
    }

    public void moveToward(int tx, int ty, Tile[][] map) {

        List<Tile> options = getAdjacentTiles(x, y, map);
        Tile best = null;
        int bestDist = Integer.MAX_VALUE;

        // try to find a path closer to the target
        for (Tile t : options) {
            if (t == null) continue;
            if (t.isWall()) continue; // skip walls if there is another way

            int dist = Math.abs(t.getX() - tx) + Math.abs(t.getY() - ty);
            if (dist < bestDist) {
                bestDist = dist;
                best = t;
            }
        }

        // Move if there is path exists
        if (best != null) {
            this.x = best.getX();
            this.y = best.getY();
        } else {
            //  No path he will attack wall
            for (Tile t : options) {
                if (t != null && t.isWall()) {
                    t.damageWall(1); // 🧟 Attack wall (reduce HP by 1)
                    break;           // Attack only one wall per tick
                }
            }
        }
    }

    public boolean isAdjacentTo(int tx, int ty) {
        return Math.abs(this.x - tx) + Math.abs(this.y - ty) == 1;
    }

    public void takeDamage(int dmg) {
        hp -= dmg;
    }

    public boolean isDead() {
        return hp <= 0;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getAttack() { return attack; }
    public List<Tile> getAdjacentTiles(int x, int y, Tile[][] map) {
        List<Tile> tiles = new ArrayList<>();

        if (x > 0) tiles.add(map[x - 1][y]);
        if (x < map.length - 1) tiles.add(map[x + 1][y]);
        if (y > 0) tiles.add(map[x][y - 1]);
        if (y < map[0].length - 1) tiles.add(map[x][y + 1]);

        return tiles;
    }

}