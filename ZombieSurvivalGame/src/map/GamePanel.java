package map;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import buildings.Building;

import units.Soldier;
import units.Zombie;
import game.ZombieManager;


public class GamePanel extends JPanel {
    private final int tileSize = 32;
    private final int gridSize = 16;
    int offsetX = 0;



    //time mangement var
    private int turnsLeft = 50;
    private boolean mustReturnToBase = false;
    private boolean isDay = true;
    private int turnCount = 1;


    // player var
    private int playerX = 0, playerY = 0;

    // starting health
    public int baseHp = 20;
    public boolean gameOver = false;

    private String pendingBuildType = null;
    // Base coordinates
    private int baseX = gridSize / 2;
    private int baseY = gridSize / 2;

    // Tiles and buildings
    private Tile[][] tiles = new Tile[gridSize][gridSize];
    private Building[][] buildings = new Building[gridSize][gridSize];

    //solders stuff
    private boolean pendingSoldierSpawn = false;
    private Soldier selectedSoldier;
    private ArrayList<Soldier> soldiers = new ArrayList<>();

    //resources
    private int wood = 0;
    private int rock = 0;
    private int iron = 0;
    private int food = 0;

    private KeyAdapter keyListener;
    //buttons
    private JButton gatherWoodButton, gatherRockButton;
    private JButton mineButton;
    private JButton spawnSoldierButton;
    private JButton gatherFoodButton;
    private JButton upgradeHealthButton;
    private JButton upgradeDamageButton;
    private JButton upgradeRangeButton;

    // buttons how they look
    private JButton buildFarmButton, buildMineButton, buildCampButton, buildWallButton, endDayButton;

    public GamePanel() {
        setPreferredSize(new Dimension(tileSize * gridSize, tileSize * gridSize));
        setFocusable(true);

        setPreferredSize(new Dimension(tileSize * gridSize, tileSize * gridSize + 40));

        // Vertical layout for buttons
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        generateMap();
        setPlayerStartPosition();
        placeCommandCenter();
        initButtons();
        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int clickedX = (e.getX() -  offsetX) / tileSize;
                int clickedY = (e.getY() - 48) / tileSize;

                tiles[clickedX][clickedY].setType("Wall");
                tiles[clickedX][clickedY].setWall(true);
                tiles[clickedX][clickedY].setColor(Color.DARK_GRAY);

                // Soldier placement when click
                if (pendingSoldierSpawn) {
                    if (!tiles[clickedX][clickedY].isAccessible() || buildings[clickedX][clickedY] != null) {
                        System.out.println("Cant place soldier here");
                        return;
                    }

                    if (turnsLeft == 0) {
                        System.out.println("No turns left, Return to base");
                        pendingSoldierSpawn = false;
                        return;
                    }

                    soldiers.add(new Soldier(clickedX, clickedY));
                    turnsLeft--;

                    if (turnsLeft == 0) {
                        mustReturnToBase = true;
                        System.out.println("No turns left, Return to base");
                    }

                    pendingSoldierSpawn = false;
                    System.out.println("Soldier spawned at (" + clickedX + "," + clickedY + ")");
                    updateButtonVisibility();
                    repaint();
                    return;
                }
                if (isDay && selectedSoldier != null) {
                    tryUpgradeSoldier(selectedSoldier, "damage"); // or "range", "health"
                }

                // Building placement when click
                if (pendingBuildType != null) {
                    buildStructureAt(clickedX, clickedY, pendingBuildType);
                    pendingBuildType = null;
                    turnsLeft--;

                    if (turnsLeft == 0) {
                        mustReturnToBase = true;
                        System.out.println("No turns left, Return to base");
                    }

                    updateButtonVisibility();
                    repaint();
                    return;
                }

                // Resource gathering when click
                int dx = Math.abs(clickedX - playerX);
                int dy = Math.abs(clickedY - playerY);
                boolean isAdjacent = (dx + dy == 1); // only 1 tile away

                String tileType = tiles[clickedX][clickedY].getType();

                if (isAdjacent) {
                    if (turnsLeft == 0) {
                        System.out.println("No turns left, Return to base");
                        return;
                    }

                    if (tileType.equals("Forest")) {
                        wood += 5;
                        turnsLeft--;
                        System.out.println("Gathered 5 wood, Total: " + wood);
                    } else if (tileType.equals("Mountain")) {
                        rock += 3;
                        turnsLeft--;
                        System.out.println("Gathered 3 rock, Total: " + rock);
                    } else if (tileType.equals("Farm")) {
                        food += 4;
                        turnsLeft--;
                        System.out.println("Gathered 4 food, Total: " + food);
                    } else if (tileType.equals("Mine")) {
                        iron += 2;
                        turnsLeft--;
                        System.out.println("Mined 2 iron, Total: " + iron);
                    }

                    System.out.println("turns left: " + turnsLeft);

                    if (turnsLeft == 0) {
                        mustReturnToBase = true;
                        System.out.println("no turns left! go back to base to end the day");
                    }
                } else {
                    System.out.println("You must be next to the tile to gather resources");
                }

                updateButtonVisibility();
                repaint();
            }
        });

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                int newX = playerX, newY = playerY;

                switch (e.getKeyCode()) {
                    case KeyEvent.VK_UP -> { if (playerY > 0) newY--; }
                    case KeyEvent.VK_DOWN -> { if (playerY < gridSize - 1) newY++; }
                    case KeyEvent.VK_LEFT -> { if (playerX > 0) newX--; }
                    case KeyEvent.VK_RIGHT -> { if (playerX < gridSize - 1) newX++; }
                }

                // no movement check in night
                if (!isDay) {
                    System.out.println("You cannot move at night!");
                    return;
                }

                if (tiles[newX][newY].isAccessible()) {
                    playerX = newX;
                    playerY = newY;
                }

                // buttons only when on base
                boolean onBase = isOnCommandCenter();
                buildFarmButton.setVisible(onBase);
                buildMineButton.setVisible(onBase);
                buildCampButton.setVisible(onBase);
                buildWallButton.setVisible(onBase);
                endDayButton.setVisible(onBase);

                if (buildings[playerX][playerY] != null &&
                        buildings[playerX][playerY].getType().equals("Mine")) {
                    showMineOption();
                }
                if (buildings[playerX][playerY] == null ||
                        !buildings[playerX][playerY].getType().equals("Mine")) {
                    mineButton.setVisible(false);
                }
                // Show gather buttons only if near forest or mountain
                if (isNextToForest(playerX, playerY)) {
                    gatherWoodButton.setVisible(true);
                } else {
                    gatherWoodButton.setVisible(false);
                }

                if (isNextToMountain(playerX, playerY)) {
                    gatherRockButton.setVisible(true);
                } else {
                    gatherRockButton.setVisible(false);
                }
                updateButtonVisibility();
                repaint();
            }

        });
    }

    private void initButtons() {
        // Build buttons
        buildFarmButton = new JButton("Build Farm");
        buildMineButton = new JButton("Build Mine");
        buildCampButton = new JButton("Build Camp");
        buildWallButton = new JButton("Build Wall");

        // Action buttons
        spawnSoldierButton = new JButton("Spawn Soldier");
        endDayButton = new JButton("End Day");

        // Resource buttons
        mineButton = new JButton("Mine Resources");
        gatherWoodButton = new JButton("Gather Wood");
        gatherRockButton = new JButton("Gather Rock");
        gatherFoodButton = new JButton("Gather Food");

        // Prevent buttons from stealing focus
        JButton[] allButtons = {
                buildFarmButton, buildMineButton, buildCampButton, buildWallButton,
                spawnSoldierButton, endDayButton,
                mineButton, gatherWoodButton, gatherRockButton, gatherFoodButton
        };
        for (JButton b : allButtons) b.setFocusable(false);

        // Build actions
        buildFarmButton.addActionListener(e -> { pendingBuildType = "Farm"; requestFocusInWindow(); });
        buildMineButton.addActionListener(e -> { pendingBuildType = "Mine"; requestFocusInWindow(); });
        buildCampButton.addActionListener(e -> { pendingBuildType = "Camp"; requestFocusInWindow(); });
        buildWallButton.addActionListener(e -> { pendingBuildType = "Wall"; requestFocusInWindow(); });

        // Spawn soldier
        spawnSoldierButton.addActionListener(e -> {
            if (isOnType(playerX, playerY, "Camp")) {
                pendingSoldierSpawn = true;
                System.out.println("Click a tile to place your soldier.");
            } else {
                System.out.println("You must be standing on a Camp to spawn a soldier.");
            }
            requestFocusInWindow();
        });
        // Upgrade Health
        upgradeHealthButton = new JButton("Upgrade Health");
        upgradeHealthButton.setFocusable(false);
        upgradeHealthButton.addActionListener(e -> {
            requestFocusInWindow();
            if (food >= 1 && wood >= 2) {
                for (Soldier s : soldiers) {
                    s.increaseHealth(2);
                }
                food -= 1;
                wood -= 2;
                System.out.println("Soldiers upgraded! +2 HP");
            } else {
                System.out.println("Not enough resources for health upgrade");
            }
        });

// Upgrade Damage
        upgradeDamageButton = new JButton("Upgrade Damage");
        upgradeDamageButton.setFocusable(false);
        upgradeDamageButton.addActionListener(e -> {
            requestFocusInWindow();
            if (iron >= 2) {
                for (Soldier s : soldiers) {
                    s.increaseDamage(1);
                }
                iron -= 2;
                System.out.println("Soldiers upgraded! +1 Damage");
            } else {
                System.out.println("Not enough iron for damage upgrade");
            }
        });

// Upgrade Range
        upgradeRangeButton = new JButton("Upgrade Range");
        upgradeRangeButton.setFocusable(false);
        upgradeRangeButton.addActionListener(e -> {
            requestFocusInWindow();
            if (wood >= 3) {
                for (Soldier s : soldiers) {
                    s.increaseRange(1);
                }
                wood -= 3;
                System.out.println("Soldiers upgraded! +1 Range");
            } else {
                System.out.println("Not enough wood for range upgrade");
            }
        });

// Add buttons to panel
        add(upgradeHealthButton);
        add(upgradeDamageButton);
        add(upgradeRangeButton);

// Hide initially
        upgradeHealthButton.setVisible(false);
        upgradeDamageButton.setVisible(false);
        upgradeRangeButton.setVisible(false);

        // End day
        endDayButton.addActionListener(e -> {
            requestFocusInWindow();
            if (mustReturnToBase && isOnBase()) {
                startNightPhase();
            } else {
                System.out.println("You must be at the base to end the day");
            }
        });

        // Mine iron
        mineButton.addActionListener(e -> {
            requestFocusInWindow();
            if (turnsLeft == 0) {
                System.out.println("No turns left! Return to base");
                return;
            }
            if (!isNextToType(playerX, playerY, "Mine") && !isOnType(playerX, playerY, "Mine")) {
                System.out.println("You must be on or next to a Mine to use it");
                return;
            }
            iron += 2;
            turnsLeft--;
            System.out.println("You mined 2 iron! Total: " + iron);
            if (turnsLeft == 0) {
                mustReturnToBase = true;
                System.out.println("Turns exhausted! Return to base to end the day");
            }
            updateButtonVisibility();
            repaint();
        });

        // Gather wood
        gatherWoodButton.addActionListener(e -> {
            requestFocusInWindow();
            if (turnsLeft == 0) {
                System.out.println("No turns left! Return to base");
                return;
            }
            if (!isNextToType(playerX, playerY, "Forest")) {
                System.out.println("Move next to a Forest to gather");
                return;
            }
            wood += 5;
            turnsLeft--;
            System.out.println("Gathered 5 wood! Total: " + wood);
            if (turnsLeft == 0) {
                mustReturnToBase = true;
                System.out.println("Turns exhausted! Return to base to end the day");
            }
            updateButtonVisibility();
            repaint();
        });

        // Gather rock
        gatherRockButton.addActionListener(e -> {
            requestFocusInWindow();
            if (turnsLeft == 0) {
                System.out.println("No turns left! Return to base");
                return;
            }
            if (!isNextToType(playerX, playerY, "Mountain")) {
                System.out.println("Move next to a Mountain to gather");
                return;
            }
            rock += 3;
            turnsLeft--;
            System.out.println("Gathered 3 rock! Total: " + rock);
            if (turnsLeft == 0) {
                mustReturnToBase = true;
                System.out.println("Turns exhausted! Return to base to end the day");
            }
            updateButtonVisibility();
            repaint();
        });

        // Gather food
        gatherFoodButton.addActionListener(e -> {
            requestFocusInWindow();
            if (turnsLeft == 0) {
                System.out.println("No turns left! Return to base");
                return;
            }
            if (!isNextToType(playerX, playerY, "Farm") && !isOnType(playerX, playerY, "Farm")) {
                System.out.println("Move next to a Farm to gather");
                return;
            }
            food += 2;
            turnsLeft--;
            System.out.println("Gathered 2 food, Total: " + food);
            if (turnsLeft == 0) {
                mustReturnToBase = true;
                System.out.println("Turns exhausted! Return to base to end the day");
            }
            updateButtonVisibility();
            repaint();
        });

        // Add buttons to panel
        for (JButton b : allButtons) add(b);

        // Hide all buttons initially
        for (JButton b : allButtons) b.setVisible(false);
    }

    private void generateMap() {
        for (int x = 0; x < gridSize; x++) {
            for (int y = 0; y < gridSize; y++) {
                if (x == 0 || y == 0 || x == gridSize - 1 || y == gridSize - 1) {
                    tiles[x][y] = new Tile("Water", false, Color.CYAN, x, y);
                } else {
                    tiles[x][y] = new Tile("Grass", true, Color.GREEN, x ,y);
                }
            }
        }
        // Forest in top-right corner
        for (int x = gridSize - 3; x < gridSize; x++) {
            for (int y = 0; y < 3; y++) {
                tiles[x][y] = new Tile("Forest", true, new Color(34, 139, 34), x, y); // dark green
            }
        }

// Mountain in bottom-left corner
        for (int x = 0; x < 3; x++) {
            for (int y = gridSize - 3; y < gridSize; y++) {
                tiles[x][y] = new Tile("Mountain", false, Color.GRAY, x, y);
            }
        }
    }

    private void setPlayerStartPosition() {
        playerX = gridSize / 2;
        playerY = gridSize / 2;
    }

    private void placeCommandCenter() {
        baseX = gridSize / 2;
        baseY = gridSize / 2;

        tiles[baseX][baseY] = new Tile("Base", true, Color.RED, baseX, baseY); // always accessible
        buildings[baseX][baseY] = new Building("Command Center", Color.RED);
    }

    private boolean isOnCommandCenter() {
        return playerX == baseX && playerY == baseY;
    }

    private void buildStructureAt(int targetX, int targetY, String type) {
        if (turnsLeft == 0) {
            System.out.println("No turns left, Return to base");
            return;
        }
        if (targetX < 0 || targetY < 0 || targetX >= gridSize || targetY >= gridSize) return;
        if (!tiles[targetX][targetY].isAccessible() || buildings[targetX][targetY] != null) return;

        // Resource costs
        switch (type) {
            case "Farm" -> {
                if (wood < 10) {
                    System.out.println("Not enough wood!");
                    return;
                }
                wood -= 10;
            }
            case "Mine" -> {
                if (!isNextToMountain(targetX, targetY)) {
                    System.out.println("You can only build a Mine next to a Mountain!");
                    return;
                }
                if (rock < 8) {
                    System.out.println("Not enough rock!");
                    return;
                }
                rock -= 8;
            }
            case "Camp" -> {
                if (wood < 6 || rock < 4) {
                    System.out.println("Not enough resources!");
                    return;
                }
                wood -= 6;
                rock -= 4;
            }
            case "Wall" -> {
                if (rock < 5) {
                    System.out.println("Not enough rock!");
                    return;
                }
                rock -= 5;
            }
            default -> {
                System.out.println("Unknown building type: " + type);
                return;
            }
        }

        // Consume turn
        turnsLeft--;
        if (turnsLeft == 0) {
            mustReturnToBase = true;
            System.out.println("Turns exhausted! Return to base to end the day.");
        }

        // Place building
        Color color = switch (type) {
            case "Farm" -> Color.ORANGE;
            case "Mine" -> Color.DARK_GRAY;
            case "Camp" -> Color.BLUE;
            case "Wall" -> Color.BLACK;
            default -> Color.WHITE;
        };

        buildings[targetX][targetY] = new Building(type, color);
        tiles[targetX][targetY].setType(type); // Ensure tile type is updated

        updateButtonVisibility();
        repaint();
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int hudOffset = 40;

        // ✅ HUD background
        g.setColor(Color.DARK_GRAY);
        g.fillRect(0, 0, tileSize * gridSize, hudOffset);

        // ✅ HUD text

        // ✅ Draw tiles
        for (int x = 0; x < gridSize; x++) {
            for (int y = 0; y < gridSize; y++) {
                Tile tile = tiles[x][y];
                g.setColor(tile.getColor());
                g.fillRect(x * tileSize, y * tileSize + hudOffset, tileSize, tileSize);
                g.setColor(Color.BLACK);
                g.drawRect(x * tileSize, y * tileSize + hudOffset, tileSize, tileSize);
            }
        }

        // ✅ Draw buildings
        for (int x = 0; x < gridSize; x++) {
            for (int y = 0; y < gridSize; y++) {
                Building b = buildings[x][y];
                if (b != null) {
                    g.setColor(b.getColor());
                    g.fillRect(x * tileSize + 4, y * tileSize + hudOffset + 4, tileSize - 8, tileSize - 8);
                }
            }
        }

        // ✅ Draw player
        g.setColor(Color.RED);
        g.fillOval(playerX * tileSize + 8, playerY * tileSize + hudOffset + 8, tileSize - 16, tileSize - 16);

        // ✅ Draw soldiers
        g.setColor(Color.CYAN);
        for (Soldier s : soldiers) {
            g.fillOval(s.getX() * tileSize + 8, s.getY() * tileSize + hudOffset + 8, tileSize - 16, tileSize - 16);
        }

        // Draw zombies
        g.setColor(Color.RED); // or use an image/icon later
        for (Zombie z : ZombieManager.getZombies()) {
            int px = z.getX() * tileSize;
            int py = z.getY() * tileSize;
            g.fillRect(px, py, tileSize, tileSize);
        }
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString("Turns Left: " + turnsLeft, 10, 20);
        g.drawString("Wood: " + wood + " Rock: " + rock + " Iron: " + iron + " Food: " + food, 10, 40);
    }

        private void showMineOption() {
            mineButton.setVisible(true);
        }
    public boolean isNextToMountain(int x, int y) {
        // Check 4 directions around the tile
        int[][] directions = { {1,0}, {-1,0}, {0,1}, {0,-1} };
        for (int[] d : directions) {
            int nx = x + d[0];
            int ny = y + d[1];
            if (nx >= 0 && ny >= 0 && nx < gridSize && ny < gridSize) {
                if (tiles[nx][ny].getType().equals("Mountain")) {
                    return true;
                }
            }
        }
        return false;
    }
    private boolean isNextToForest(int x, int y) {
        int[][] directions = { {1,0}, {-1,0}, {0,1}, {0,-1} };
        for (int[] d : directions) {
            int nx = x + d[0];
            int ny = y + d[1];
            if (nx >= 0 && ny >= 0 && nx < gridSize && ny < gridSize) {
                if (tiles[nx][ny].getType().equals("Forest")) {
                    return true;
                }
            }
        }
        return false;
    }
    private void updateButtonVisibility() {
        boolean canAct = turnsLeft > 0;

        // Contextual visibility
        gatherWoodButton.setVisible(canAct && isNextToType(playerX, playerY, "Forest"));
        gatherRockButton.setVisible(canAct && isNextToType(playerX, playerY, "Mountain"));
        gatherFoodButton.setVisible(isDay && turnsLeft > 0 && (isOnType(playerX, playerY, "Farm") || isNextToType(playerX, playerY, "Farm")));
        boolean onBase = isOnBase();
        buildFarmButton.setVisible(canAct && onBase);
        buildMineButton.setVisible(canAct && onBase);
        buildCampButton.setVisible(canAct && onBase);
        buildWallButton.setVisible(canAct && onBase);



        //camp actions
        spawnSoldierButton.setVisible(isDay && isOnType(playerX, playerY, "Camp"));

        // Mining button visibility
        mineButton.setVisible(canAct && (isOnType(playerX, playerY, "Mine") || isNextToType(playerX, playerY, "Mine")));

        upgradeHealthButton.setVisible(isDay && (isOnSoldier() || isNextToSoldier()));
        upgradeDamageButton.setVisible(isDay && (isOnSoldier() || isNextToSoldier()));
        upgradeRangeButton.setVisible(isDay && (isOnSoldier() || isNextToSoldier()));

        // End day button state
        endDayButton.setEnabled(mustReturnToBase);
    }
    private boolean isOnBase() {
        return playerX == baseX && playerY == baseY;
    }
    private boolean isOnType(int x, int y, String type) {
        return tiles[x][y].getType().equals(type);
    }
    private boolean isNextToType(int px, int py, String type) {
        int[][] dirs = {{1,0},{-1,0},{0,1},{0,-1}};
        for (int[] d : dirs) {
            int nx = px + d[0];
            int ny = py + d[1];
            if (nx >= 0 && ny >= 0 && nx < gridSize && ny < gridSize) {
                if (type.equals(tiles[nx][ny].getType())) return true;
            }
        }
        return false;
    }
    private void startNightPhase() {
        isDay = false;
        mustReturnToBase = false;

        ZombieManager.spawnZombies(turnCount); // Night 1 = 1 zombie, Night 2 = 2, etc
        System.out.println(" Night " + turnCount + " start");

        // Disable player movement
        removeKeyListener(keyListener);

        // zombie AI loop
        Timer nightTimer = new Timer(500, e -> {
            if (gameOver) {
                ((Timer) e.getSource()).stop();
                showGameOver();
                return;
            }

            ZombieManager.updateZombies(soldiers, baseX, baseY, tiles, this);
            repaint();

            if (ZombieManager.allZombiesDead()) {
                ((Timer) e.getSource()).stop();
                startDayPhase();
            }
        });
        nightTimer.start();
    }
    private void startDayPhase() {
        isDay = true;
        turnCount++;
        turnsLeft = 15;
        mustReturnToBase = false;

        addKeyListener(keyListener);
        System.out.println(" Day " + turnCount + " begins!");
        repaint();
    }
    private boolean isOnSoldier() {
        for (Soldier s : soldiers) {
            if (s.getX() == playerX && s.getY() == playerY) {
                return true;
            }
        }
        return false;
    }
    public void tryUpgradeSoldier(Soldier s, String type) {
        if (s.upgrade(type, iron, wood, food)) {
            // Deduct resources if upgrade succeeded
            switch (type) {
                case "range":
                    iron -= 3;
                    break;
                case "damage":
                    iron -= 4;
                    wood -= 2;
                    break;
                case "health":
                    food -= 5;
                    wood -= 2;
                    break;
            }
        }
    }

    private boolean isNextToSoldier() {
        for (Soldier s : soldiers) {
            if (Math.abs(s.getX() - playerX) <= 1 && Math.abs(s.getY() - playerY) <= 1) {
                return true;
            }
        }
        return false;
    }
    private void showGameOver() {
        JOptionPane.showMessageDialog(this, " the base has fallen ");
    }


}
