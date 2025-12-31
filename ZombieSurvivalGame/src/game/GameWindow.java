package game;

import javax.swing.*;
import map.GamePanel;

public class GameWindow {
    public static void createWindow() {
        JFrame frame = new JFrame("Zombie Survival");
        frame.setSize(600, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);

        GamePanel panel = new GamePanel();
        frame.add(panel);
        frame.setVisible(true);
        frame.add(panel);
        frame.pack();
        frame.setFocusable(true);
        frame.requestFocus();
        frame.setVisible(true);
        panel.requestFocus();


    }
}