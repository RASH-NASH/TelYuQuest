package arcana.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import arcana.model.character.Character;
import arcana.model.character.Skill;
import arcana.model.item.Fish;
import arcana.model.item.FishingRod;
import arcana.model.item.Grimoire;
import arcana.model.item.Item;

public class SaveManager {
	private static final String DB_URL = "jdbc:sqlite:telyuquest.db";
	
	private Connection connect() throws SQLException {
		return DriverManager.getConnection(DB_URL);
	}
	
	public void initDatabase() {
	    String userSql = "CREATE TABLE IF NOT EXISTS USER("
	                   + "username TEXT PRIMARY KEY, "
	                   + "password TEXT NOT NULL"
	                   + ")";
	    String playerSql = "CREATE TABLE IF NOT EXISTS PLAYER("
	                     + "username TEXT PRIMARY KEY, "
	                     + "charName TEXT, "
	                     + "level INTEGER, "
	                     + "exp INTEGER, "
	                     + "gold INTEGER, "
	                     + "x INTEGER, "
	                     + "y INTEGER, "
	                     + "maxHp INTEGER, "
	                     + "currentHp INTEGER, "
	                     + "maxMp INTEGER, "
	                     + "currentMp INTEGER, "
	                     + "hotbarSlot INTEGER, "
	                     + "areaName TEXT"
	                     + ")";
	    String inventorySql = "CREATE TABLE IF NOT EXISTS INVENTORY_SLOT("
                + "username TEXT, "
                + "slotIndex INTEGER, "
                + "itemType TEXT, "
                + "name TEXT, "
                + "price INTEGER, "
                + "relatedSkill TEXT, "
                + "PRIMARY KEY(username, slotIndex)"
                + ")";
	    String skillSql = "CREATE TABLE IF NOT EXISTS SKILL_STATE("
                + "username TEXT, "
                + "skillName TEXT, "
                + "unlocked INTEGER, "
                + "PRIMARY KEY(username, skillName)"
                + ")";
	    String loadoutSql = "CREATE TABLE IF NOT EXISTS SKILL_LOADOUT("
                  + "username TEXT, "
                  + "slotIndex INTEGER, "
                  + "skillName TEXT, "
                  + "PRIMARY KEY(username, slotIndex)"
                  + ")";
	    try (Connection conn = connect();
	         Statement stmt = conn.createStatement()) {
	        stmt.execute(userSql);
	        stmt.execute(playerSql);
	        stmt.execute(inventorySql);
	        stmt.execute(skillSql);
	        stmt.execute(loadoutSql);
	        System.out.println("Database siap, tabel USER & PLAYER dibuat.");
	    } catch (SQLException e) {
	        System.out.println("Gagal init: " + e.getMessage());
	    }
	}
	
	public boolean register(String username, String password) {
	    String sql = "INSERT INTO USER(username, password) VALUES(?, ?)";
	    try (Connection conn = connect();
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {
	        pstmt.setString(1, username);
	        pstmt.setString(2, password);
	        pstmt.executeUpdate();
	        System.out.println("Register sukses: " + username);
	        return true;
	    } catch (SQLException e) {
	        System.out.println("Register gagal: " + e.getMessage());
	        return false;
	    }
	}
	
	// cek apakah username sudah terdaftar; dipakai biar password salah != tawaran daftar
		public boolean userExists(String username) {
		    String sql = "SELECT 1 FROM USER WHERE username = ?";
		    try (Connection conn = connect();
		         PreparedStatement pstmt = conn.prepareStatement(sql)) {
		        pstmt.setString(1, username);
		        ResultSet rs = pstmt.executeQuery();
		        return rs.next();
		    } catch (SQLException e) {
		        System.out.println("Cek user error: " + e.getMessage());
		        return false;
		    }
		}
	
	public boolean login(String username, String password) {
	    String sql = "SELECT password FROM USER WHERE username = ?";
	    try (Connection conn = connect();
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {
	        pstmt.setString(1, username);
	        ResultSet rs = pstmt.executeQuery();

	        if (rs.next()) {
	            String storedPassword = rs.getString("password");
	            if (storedPassword.equals(password)) {
	                System.out.println("Login sukses: " + username);
	                return true;
	            } else {
	                System.out.println("Login gagal: password salah");
	                return false;
	            }
	        } else {
	            System.out.println("Login gagal: user tidak ditemukan");
	            return false;
	        }
	    } catch (SQLException e) {
	        System.out.println("Login error: " + e.getMessage());
	        return false;
	    }
	}
	
	public Character loadProgress(String username) {
	    String sql = "SELECT * FROM PLAYER WHERE username = ?";
	    try (Connection conn = connect();
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {
	        pstmt.setString(1, username);
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) {
	            Character character = new Character(
	                rs.getString("charName"), rs.getInt("x"), rs.getInt("y"));
	            character.setLevel(rs.getInt("level"));
	            character.setExp(rs.getInt("exp"));
	            character.setGold(rs.getInt("gold"));
	            character.setMaxHp(rs.getInt("maxHp"));
	            character.setMaxMp(rs.getInt("maxMp"));
	            character.setCurrentHp(rs.getInt("currentHp"));
	            character.setCurrentMp(rs.getInt("currentMp"));
	            character.selectHotbarSlot(rs.getInt("hotbarSlot"));
	            character.setLastAreaName(rs.getString("areaName"));
	            System.out.println("Progress dimuat untuk: " + username);
	            loadInventory(username, character);
	            loadSkills(username, character);
	            return character;
	        } else {
	            System.out.println("Tidak ada save untuk: " + username);
	            return null;
	        }
	    } catch (SQLException e) {
	        System.out.println("Load gagal: " + e.getMessage());
	        return null;
	    }
	}
	
	public void loadInventory(String username, Character character) {
	    String sql = "SELECT slotIndex, itemType, name, price, relatedSkill "
	               + "FROM INVENTORY_SLOT WHERE username = ?";
	    try (Connection conn = connect();
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {
	        pstmt.setString(1, username);
	        ResultSet rs = pstmt.executeQuery();
	        while (rs.next()) {
	            int slotIndex = rs.getInt("slotIndex");
	            String itemType = rs.getString("itemType");
	            String name = rs.getString("name");
	            int price = rs.getInt("price");
	            String relatedSkill = rs.getString("relatedSkill");
	            character.setInventoryItemAt(slotIndex, createItem(itemType, name, price, relatedSkill));
	        }
	        System.out.println("Inventory dimuat untuk: " + username);
	    } catch (SQLException e) {
	        System.out.println("Load inventory gagal: " + e.getMessage());
	    }
	}
	
	public void loadSkills(String username, Character character) {
	    String stateSql = "SELECT skillName, unlocked FROM SKILL_STATE WHERE username = ?";
	    String loadoutSql = "SELECT slotIndex, skillName FROM SKILL_LOADOUT WHERE username = ?";
	    try (Connection conn = connect();
	         PreparedStatement stateStmt = conn.prepareStatement(stateSql);
	         PreparedStatement loadoutStmt = conn.prepareStatement(loadoutSql)) {

	        stateStmt.setString(1, username);
	        ResultSet stateRs = stateStmt.executeQuery();
	        while (stateRs.next()) {
	            if (stateRs.getInt("unlocked") == 1) {
	                character.unlockSkill(stateRs.getString("skillName"));
	            }
	        }

	        loadoutStmt.setString(1, username);
	        ResultSet loadoutRs = loadoutStmt.executeQuery();
	        while (loadoutRs.next()) {
	            character.equipSkillToSlot(loadoutRs.getInt("slotIndex"), loadoutRs.getString("skillName"));
	        }

	        System.out.println("Skill dimuat untuk: " + username);
	    } catch (SQLException e) {
	        System.out.println("Load skill gagal: " + e.getMessage());
	    }
	}

	private Item createItem(String itemType, String name, int price, String relatedSkill) {
	    if ("ROD".equals(itemType)) {
	        return new FishingRod(name, price);
	    }
	    if ("GRIMOIRE".equals(itemType)) {
	        return new Grimoire(name, price, relatedSkill);
	    }
	    if ("FISH".equals(itemType)) {
	        return new Fish(name, price);
	    }
	    return new Item(name, price);
	}
	
	public void saveInventory(String username, Character character) {
	    String deleteSql = "DELETE FROM INVENTORY_SLOT WHERE username = ?";
	    String insertSql = "INSERT INTO INVENTORY_SLOT("
	                     + "username, slotIndex, itemType, name, price, relatedSkill) "
	                     + "VALUES(?,?,?,?,?,?)";
	    try (Connection conn = connect();
	         PreparedStatement deleteStmt = conn.prepareStatement(deleteSql);
	         PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
	        deleteStmt.setString(1, username);
	        deleteStmt.executeUpdate();

	        List<Item> items = character.getInventory().getItems();
	        for (int i = 0; i < items.size(); i++) {
	            Item item = items.get(i);
	            if (item == null) {
	                continue;
	            }
	            insertStmt.setString(1, username);
	            insertStmt.setInt(2, i);
	            insertStmt.setString(3, resolveItemType(item));
	            insertStmt.setString(4, item.getName());
	            insertStmt.setInt(5, item.getPrice());
	            insertStmt.setString(6, item instanceof Grimoire ? ((Grimoire) item).getRelatedSkillName() : null);
	            insertStmt.executeUpdate();
	        }
	        System.out.println("Inventory disimpan untuk: " + username);
	    } catch (SQLException e) {
	        System.out.println("Save inventory gagal: " + e.getMessage());
	    }
	}
	
	public void saveSkills(String username, Character character) {
	    String deleteStateSql = "DELETE FROM SKILL_STATE WHERE username = ?";
	    String deleteLoadoutSql = "DELETE FROM SKILL_LOADOUT WHERE username = ?";
	    String insertStateSql = "INSERT INTO SKILL_STATE(username, skillName, unlocked) VALUES(?,?,?)";
	    String insertLoadoutSql = "INSERT INTO SKILL_LOADOUT(username, slotIndex, skillName) VALUES(?,?,?)";
	    try (Connection conn = connect();
	         PreparedStatement deleteState = conn.prepareStatement(deleteStateSql);
	         PreparedStatement deleteLoadout = conn.prepareStatement(deleteLoadoutSql);
	         PreparedStatement insertState = conn.prepareStatement(insertStateSql);
	         PreparedStatement insertLoadout = conn.prepareStatement(insertLoadoutSql)) {

	        deleteState.setString(1, username);
	        deleteState.executeUpdate();
	        deleteLoadout.setString(1, username);
	        deleteLoadout.executeUpdate();

	        for (Skill skill : character.getSkills()) {
	            insertState.setString(1, username);
	            insertState.setString(2, skill.getSkillName());
	            insertState.setInt(3, skill.isUnlocked() ? 1 : 0);
	            insertState.executeUpdate();
	        }

	        for (int i = 0; i < character.getSkillSlotCount(); i++) {
	            insertLoadout.setString(1, username);
	            insertLoadout.setInt(2, i);
	            insertLoadout.setString(3, character.getEquippedSkillName(i));
	            insertLoadout.executeUpdate();
	        }

	        System.out.println("Skill disimpan untuk: " + username);
	    } catch (SQLException e) {
	        System.out.println("Save skill gagal: " + e.getMessage());
	    }
	}
	
	public boolean saveProgress(String username, Character character, String areaName) {
		saveInventory(username, character);
		saveSkills(username, character);
	    String sql = "INSERT OR REPLACE INTO PLAYER("
	               + "username, charName, level, exp, gold, x, y, "
	               + "maxHp, currentHp, maxMp, currentMp, hotbarSlot, areaName) "
	               + "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)";
	    try (Connection conn = connect();
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {
	        pstmt.setString(1, username);
	        pstmt.setString(2, character.getName());
	        pstmt.setInt(3, character.getLevel());
	        pstmt.setInt(4, character.getExp());
	        pstmt.setInt(5, character.getGold());
	        pstmt.setInt(6, character.getX());
	        pstmt.setInt(7, character.getY());
	        pstmt.setInt(8, character.getMaxHp());
	        pstmt.setInt(9, character.getCurrentHp());
	        pstmt.setInt(10, character.getMaxMp());
	        pstmt.setInt(11, character.getCurrentMp());
	        pstmt.setInt(12, character.getSelectedHotbarSlot());
	        pstmt.setString(13, areaName);
	        pstmt.executeUpdate();
	        System.out.println("Progress disimpan untuk: " + username);
	        return true;
	    } catch (SQLException e) {
	        System.out.println("Save gagal: " + e.getMessage());
	        return false;
	    }
	}

	private String resolveItemType(Item item) {
	    if (item instanceof FishingRod) {
	        return "ROD";
	    }
	    if (item instanceof Grimoire) {
	        return "GRIMOIRE";
	    }
	    if (item instanceof Fish) {
	        return "FISH";
	    }
	    return "ITEM";
	}
	
	public void printSavedProgress(String username) {
	    String sql = "SELECT * FROM PLAYER WHERE username = ?";
	    try (Connection conn = connect();
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {
	        pstmt.setString(1, username);
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) {
	            System.out.println("--- Progress tersimpan ---");
	            System.out.println("Char  : " + rs.getString("charName"));
	            System.out.println("Level : " + rs.getInt("level"));
	            System.out.println("Exp   : " + rs.getInt("exp"));
	            System.out.println("Gold  : " + rs.getInt("gold"));
	            System.out.println("Pos   : (" + rs.getInt("x") + ", " + rs.getInt("y") + ")");
	            System.out.println("HP    : " + rs.getInt("currentHp") + "/" + rs.getInt("maxHp"));
	        } else {
	            System.out.println("Belum ada progress untuk: " + username);
	        }
	    } catch (SQLException e) {
	        System.out.println("Baca progress gagal: " + e.getMessage());
	    }
	}
}