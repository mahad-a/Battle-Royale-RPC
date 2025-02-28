/**
 * GameState.java
 *
 * This file contains helper classes that must be used for Assignment 02.
 * These three classes will be used to simulate Battle Royale Game
 *
 * @author Dr. Rami Sabouni,
 * Systems and Computer Engineering,
 * Carleton University
 * @version 1.0, January 29th, 2025
 */

import java.util.ArrayList;
import java.util.List;

/**
 * The GameState class maintains the global state of a simple Battle Royale
 * environment, including lists of players and loot boxes. It provides methods
 * for adding players, moving them, processing loot pickups, and serializing
 * the entire game state for transmission.
 */
public class GameState {

    /** A list of all players currently in the game. */
    private List<Player> players;

    /** A list of all loot boxes currently placed in the game world. */
    private List<LootBox> lootBoxes;

    /** Used to assign unique IDs to new players. */
    private int nextPlayerId = 100;

    /** Used to assign unique IDs to new loot boxes. */
    private int nextLootBoxId = 200;

    /**
     * Constructs a new GameState with empty lists of players and loot boxes,
     * plus a couple of default loot boxes for demonstration.
     */
    public GameState() {
        players = new ArrayList<>();
        lootBoxes = new ArrayList<>();

        // Add some default loot boxes as an example.
        lootBoxes.add(new LootBox(nextLootBoxId++, 5, 5, "HealthPack", 1));
        lootBoxes.add(new LootBox(nextLootBoxId++, 10, 2, "Ammo", 5));
    }

    /**
     * Creates and adds a new player to the game state.
     *
     * @param name The name of the new player.
     * @return A Player object representing the newly added player.
     */
    public Player addNewPlayer(String name) {
        Player p = new Player(nextPlayerId++, 0, 0, 100, name);
        players.add(p);
        return p;
    }

    /**
     * Processes a request for a player to pick up a loot box, if they are
     * standing at the same position as the box.
     *
     * @param playerId The unique ID of the player attempting the pickup.
     * @param lootId   The unique ID of the loot box being picked up.
     * @return true if the pickup was successful, false otherwise.
     */
    public boolean processPickup(int playerId, int lootId) {
        Player player = getPlayerById(playerId);
        LootBox box = getLootBoxById(lootId);
        if (player == null || box == null) {
            return false;
        }
        // Check distance (e.g., if same position, allow pickup)
        if (player.getX() == box.getX() && player.getY() == box.getY()) {
            // Example: picking up a HealthPack increases HP
            if (box.getType().equalsIgnoreCase("HealthPack")) {
                player.setHealth(player.getHealth() + 20);
            }
            // Remove the loot from the game
            lootBoxes.remove(box);
            return true;
        }
        return false;
    }

    /**
     * Moves a player in the game state by adjusting their x and y coordinates.
     *
     * @param playerId The unique ID of the player to move.
     * @param dx       The change in the x-direction.
     * @param dy       The change in the y-direction.
     */
    public void movePlayer(int playerId, int dx, int dy) {
        Player p = getPlayerById(playerId);
        if (p != null) {
            p.setX(p.getX() + dx);
            p.setY(p.getY() + dy);
        }
    }

    /**
     * Converts the current game state (players and loot boxes) into a
     * simplified JSON-like string for transmission or debugging.
     *
     * Example format:
     * <pre>
     * PLAYERS=[(id,x,y,health,name),...];LOOT=[(id,x,y,type,quantity),...]
     * </pre>
     *
     * @return A string representing the serialized game state.
     */
    public String serialize() {
        StringBuilder sb = new StringBuilder();
        sb.append("PLAYERS=[");
        for (Player p : players) {
            sb.append("(")
                    .append(p.getId()).append(",")
                    .append(p.getX()).append(",")
                    .append(p.getY()).append(",")
                    .append(p.getHealth()).append(",")
                    .append(p.getName())
                    .append("),");
        }
        sb.append("];");

        sb.append("LOOT=[");
        for (LootBox lb : lootBoxes) {
            sb.append("(")
                    .append(lb.getId()).append(",")
                    .append(lb.getX()).append(",")
                    .append(lb.getY()).append(",")
                    .append(lb.getType()).append(",")
                    .append(lb.getQuantity())
                    .append("),");
        }
        sb.append("]");

        return sb.toString();
    }

    /**
     * Finds a player by their unique ID.
     *
     * @param id The ID of the player to find.
     * @return The matching Player object, or null if none is found.
     */
    private Player getPlayerById(int id) {
        for (Player p : players) {
            if (p.getId() == id) {
                return p;
            }
        }
        return null;
    }

    /**
     * Finds a loot box by its unique ID.
     *
     * @param id The ID of the loot box to find.
     * @return The matching LootBox object, or null if none is found.
     */
    private LootBox getLootBoxById(int id) {
        for (LootBox lb : lootBoxes) {
            if (lb.getId() == id) {
                return lb;
            }
        }
        return null;
    }
}

/**
 * The Player class represents a single player's state within the game,
 * including their unique ID, position (x,y), health, and a display name.
 */
class Player {

    /** The unique ID of this player. */
    private int id;

    /** The x-coordinate of the player's position in the game world. */
    private int x;

    /** The y-coordinate of the player's position in the game world. */
    private int y;

    /** The current health of this player. */
    private int health;

    /** The display name of this player. */
    private String name;

    /**
     * Constructs a new Player with the given properties.
     *
     * @param id     The unique ID for this player.
     * @param x      The initial x-coordinate of the player.
     * @param y      The initial y-coordinate of the player.
     * @param health The initial health value of the player.
     * @param name   The display name for this player.
     */
    public Player(int id, int x, int y, int health, String name) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.health = health;
        this.name = name;
    }

    /**
     * Gets the unique ID of this player.
     *
     * @return An integer representing the player's ID.
     */
    public int getId() {
        return id;
    }

    /**
     * Gets the player's x-coordinate.
     *
     * @return The x-coordinate of the player's position.
     */
    public int getX() {
        return x;
    }

    /**
     * Gets the player's y-coordinate.
     *
     * @return The y-coordinate of the player's position.
     */
    public int getY() {
        return y;
    }

    /**
     * Gets the current health of this player.
     *
     * @return The player's health as an integer.
     */
    public int getHealth() {
        return health;
    }

    /**
     * Gets the display name of this player.
     *
     * @return A String representing the player's name.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the x-coordinate of the player's position.
     *
     * @param x The new x-coordinate.
     */
    public void setX(int x) {
        this.x = x;
    }

    /**
     * Sets the y-coordinate of the player's position.
     *
     * @param y The new y-coordinate.
     */
    public void setY(int y) {
        this.y = y;
    }

    /**
     * Updates the player's health to the specified value.
     *
     * @param health The new health value.
     */
    public void setHealth(int health) {
        this.health = health;
    }
}

/**
 * The LootBox class represents a single loot box in the game world.
 * Loot boxes contain items or resources (e.g., health packs, ammunition)
 * that players can pick up.
 */
class LootBox {

    /** The unique ID of this loot box. */
    private int id;

    /** The x-coordinate of the loot box's location. */
    private int x;

    /** The y-coordinate of the loot box's location. */
    private int y;

    /** The type of item contained in this loot box (e.g., "HealthPack", "Ammo"). */
    private String type;

    /** The quantity or amount of the resource contained in this loot box. */
    private int quantity;

    /**
     * Constructs a new LootBox with the specified properties.
     *
     * @param id       The unique ID for this loot box.
     * @param x        The x-coordinate of its position.
     * @param y        The y-coordinate of its position.
     * @param type     A string describing the type of item (e.g., "HealthPack").
     * @param quantity The amount or quantity of the item stored.
     */
    public LootBox(int id, int x, int y, String type, int quantity) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.type = type;
        this.quantity = quantity;
    }

    /**
     * Gets the unique ID of this loot box.
     *
     * @return An integer representing the loot box ID.
     */
    public int getId() {
        return id;
    }

    /**
     * Gets the x-coordinate of this loot box.
     *
     * @return The x-coordinate as an integer.
     */
    public int getX() {
        return x;
    }

    /**
     * Gets the y-coordinate of this loot box.
     *
     * @return The y-coordinate as an integer.
     */
    public int getY() {
        return y;
    }

    /**
     * Gets the type of item contained in this loot box.
     *
     * @return A string representing the item type (e.g., "HealthPack").
     */
    public String getType() {
        return type;
    }

    /**
     * Gets the quantity of the item contained in this loot box.
     *
     * @return The quantity of the item as an integer.
     */
    public int getQuantity() {
        return quantity;
    }
}