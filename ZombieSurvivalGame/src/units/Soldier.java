package units;

public class Soldier {
    private int x, y;
    private int hp = 10;
    private int attack = 2;
    private int range = 1; // Level 1 = melee
    private int level = 1;
    //it public because it will be used in other places like building and zombies
    public int damage;
    public Soldier(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public boolean upgrade(String type, int iron, int wood, int food) {
        if (level >= 8) {
            System.out.println("Soldier is max level");
            return false;
        }

        switch (type) {
            case "range":
                if (iron >= 2) {
                    range++;
                    level++;
                    System.out.println("Range upgraded, range: " + range);
                    return true;
                }
                break;
            case "damage":
                if (iron >= 1 && wood >= 1) {
                    attack++;
                    level++;
                    System.out.println("Damage upgraded, attack: " + attack);
                    return true;
                }
                break;
            case "health":
                if (food >= 1 && wood >= 2) {
                    hp += 2;
                    level++;
                    System.out.println("Health upgraded, health: " + hp);
                    return true;
                }
                break;
        }

        System.out.println("Not enough resources to upgrade");
        return false;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getAttack() { return attack; }
    public int getHp() {
        return hp;
    }

    public void increaseHealth(int amount) {
        hp += amount;
    }

    public void increaseDamage(int amount) {
        damage = amount;
    }

    public void increaseRange(int amount) {
        range += amount;
    }

    public boolean inRange(Zombie z) {
        return Math.abs(x - z.getX()) + Math.abs(y - z.getY()) <= range;
    }


    public void takeDamage(int dmg) {
        hp -= dmg;
        System.out.println("Soldier took " + dmg + " damage! HP: " + hp);
    }

    public boolean isDead() {
        return hp <= 0;
    }


}