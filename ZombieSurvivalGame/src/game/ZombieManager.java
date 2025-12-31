package game;

import map.GamePanel;
import map.Tile;
import units.Zombie;
import units.Soldier;
import java.util.*;

public class ZombieManager {
    private static List<Zombie> zombies = new ArrayList<>();

    public static void spawnZombies(int count) {
        Random rnd = new Random();
        for (int i = 0; i < count; i++) {
            int[] pos = randomEdgeSpawn(rnd);
            zombies.add(new Zombie(pos[0], pos[1]));
            System.out.println("Spawned zombie at (" + pos[0] + "," + pos[1] + ")");
        }
    }

    private static int[] randomEdgeSpawn(Random rnd) {
        int side = rnd.nextInt(4);
        return switch (side) {
            case 0 -> new int[]{rnd.nextInt(16), 0};
            case 1 -> new int[]{15, rnd.nextInt(16)};
            case 2 -> new int[]{rnd.nextInt(16), 15};
            default -> new int[]{0, rnd.nextInt(16)};
        };

    }

    private static Soldier findNearestSoldier(Zombie z, List<Soldier> soldiers) {
        Soldier closest = null;
        int bestDistance = Integer.MAX_VALUE;

        for (Soldier s : soldiers) {
            int dx = Math.abs(s.getX() - z.getX());
            int dy = Math.abs(s.getY() - z.getY());
            int distance = dx + dy;

            if (distance < bestDistance) {
                bestDistance = distance;
                closest = s;
            }
        }

        return closest;
    }

    public static void updateZombies(List<Soldier> soldiers, int baseX, int baseY, Tile[][] map, GamePanel game) {
        List<Soldier> deadSoldiers = new ArrayList<>();

        for (Zombie z : zombies) {
            Soldier target = findNearestSoldier(z, soldiers);

            if (target != null) {
                // Always attack if adjacent
                if (z.isAdjacentTo(target.getX(), target.getY())) {
                    target.takeDamage(z.getAttack());
                    System.out.println("Soldier took " + z.getAttack() + " damage! HP: " + target.getHp());

                    if (target.isDead()) {
                        System.out.println("💀 Soldier killed!");
                        deadSoldiers.add(target);
                    }
                } else {
                    // Move toward soldier if not adjacent
                    z.moveToward(target.getX(), target.getY(), map);
                }
            } else {
                // Move toward base only if not adjacent
                if (!z.isAdjacentTo(baseX, baseY)) {
                    z.moveToward(baseX, baseY, map);
                }

                // Always attack if adjacent
                if (z.isAdjacentTo(baseX, baseY)) {
                    game.baseHp -= z.getAttack();
                    System.out.println("Base takes " + z.getAttack() + " damage! HP: " + game.baseHp);

                    if (game.baseHp <= 0) {
                        game.gameOver = true;
                        System.out.println("💀 Game Over! The base has fallen.");
                    }
                }
            }
        }

        // Soldiers attack zombies
        for (Soldier s : soldiers) {
            for (Zombie z : zombies) {
                if (s.inRange(z)) {
                    z.takeDamage(s.getAttack());
                    System.out.println("Soldier attacked zombie! Zombie HP: " + z.getHp());
                }
            }
        }

        // Remove dead zombies
        zombies.removeIf(Zombie::isDead);

        // Remove dead soldiers
        soldiers.removeAll(deadSoldiers);
    }

    public static boolean allZombiesDead() {
        return zombies.isEmpty();
    }
    public static List<Zombie> getZombies() {
        return zombies;
    }

}