package arcana.ui;

import java.awt.CardLayout;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import arcana.data.SaveManager;
import arcana.model.character.Character;
import arcana.model.monster.Monster;
import arcana.model.shop.Shop;

public class GameFrame extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;

    private GamePanel gamePanel;
    private BattlePanel battlePanel;

    private Character player;
    private Shop shop;
    private SaveManager saveManager;
    private String currentUsername;
    private boolean loadedFromSave;

    public GameFrame() {
        setTitle("Arcana Incantation");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        saveManager = new SaveManager();
        saveManager.initDatabase();
        
        currentUsername = authenticate();
        if (currentUsername == null) {
            System.exit(0);
        }
        player = loadOrCreatePlayer(currentUsername);

        shop = new Shop();

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        gamePanel = new GamePanel(this, player, shop, loadedFromSave);
        battlePanel = new BattlePanel(this, player);

        mainPanel.add(gamePanel, "GAME");
        mainPanel.add(battlePanel, "BATTLE");

        add(mainPanel);

        showGamePanel();

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
            	saveManager.saveProgress(currentUsername, player, gamePanel.getCurrentAreaName());
                System.out.println("Auto-save sebelum keluar.");
            }
        });

        setVisible(true);
    }

    public void showGamePanel() {
        String defeatedEnemyName = battlePanel.consumeDefeatedEnemyNameForQuest();
        battlePanel.onBattleHidden();
        cardLayout.show(mainPanel, "GAME");
        if (defeatedEnemyName != null) {
            gamePanel.recordMonsterDefeated(defeatedEnemyName);
        }
        gamePanel.prepareAfterBattle();
        gamePanel.requestFocusInWindow();
    }

    public void startBattle(Monster monster) {
        gamePanel.prepareForBattle();
        battlePanel.startBattle(monster);
        cardLayout.show(mainPanel, "BATTLE");
        battlePanel.requestFocusInWindow();
    }
    
    private String authenticate() {
        while (true) {
            String username = JOptionPane.showInputDialog(this, "Username:");
            if (username == null || username.isBlank()) {
                return null;
            }
            String password = JOptionPane.showInputDialog(this, "Password:");
            if (password == null) {
                return null;
            }

            if (saveManager.login(username, password)) {
                return username;
            }

            // user ADA tapi login gagal -> password salah: jangan tawarin daftar, suruh ulang
            if (saveManager.userExists(username)) {
                JOptionPane.showMessageDialog(this, "Password salah. Coba lagi.");
                continue;
            }

            int choice = JOptionPane.showConfirmDialog(this,
                "Akun belum ada. Daftar akun baru dengan data ini?",
                "Daftar?", JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                if (saveManager.register(username, password)) {
                    return username;
                }
                JOptionPane.showMessageDialog(this, "Username sudah dipakai. Coba lagi.");
            }
        }
    }

    private Character loadOrCreatePlayer(String username) {
        Character loaded = saveManager.loadProgress(username);
        if (loaded != null) {
            loadedFromSave = true;
            return loaded;
        }
        loadedFromSave = false;
        Character fresh = new Character(username, 100, 100);
        fresh.addGold(120);
        return fresh;
    }
}
