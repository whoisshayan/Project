import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.scene.Node;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Line;
import javafx.scene.paint.Color;
import java.io.*;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;

/**
 * Manages game save and load operations
 */
public class SaveManager {
    private static final String SAVE_DIR = "saves";
    private static final String TEMP_DIR = "saves/tmp";
    private final Gson gson;
    private final Path saveRoot;
    private final Path tempRoot;
    
    public SaveManager() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.saveRoot = Paths.get(SAVE_DIR);
        this.tempRoot = Paths.get(TEMP_DIR);
        
        // Create directories if they don't exist
        try {
            Files.createDirectories(saveRoot);
            Files.createDirectories(tempRoot);
        } catch (IOException e) {
            System.err.println("Failed to create save directories: " + e.getMessage());
        }
    }
    
    /**
     * Save current game state
     */
    public void saveGame(String profileId, String levelId, GameState gameState, 
                        WireBudgetService wireBudget, ScoreService score,
                        ConnectionRepo connections, GameController controller) {
        try {
            SaveData save = buildSnapshot(profileId, levelId, gameState, wireBudget, 
                                         score, connections, controller);
            
            Path profileDir = saveRoot.resolve(profileId);
            Files.createDirectories(profileDir);
            
            Path saveFile = profileDir.resolve(levelId + ".json");
            Path tempFile = profileDir.resolve(levelId + ".json.tmp");
            Path backupFile = profileDir.resolve(levelId + ".json.bak");
            
            // Write to temp file first
            String json = gson.toJson(save);
            Files.writeString(tempFile, json);
            
            // Rotate existing save to backup
            if (Files.exists(saveFile)) {
                Files.move(saveFile, backupFile, 
                          StandardCopyOption.REPLACE_EXISTING,
                          StandardCopyOption.ATOMIC_MOVE);
            }
            
            // Move temp to main save file
            Files.move(tempFile, saveFile,
                      StandardCopyOption.REPLACE_EXISTING,
                      StandardCopyOption.ATOMIC_MOVE);
                      
        } catch (IOException e) {
            System.err.println("Failed to save game: " + e.getMessage());
        }
    }
    
    /**
     * Load saved game state
     */
    public Optional<SaveData> loadGame(String profileId, String levelId) {
        try {
            Path saveFile = saveRoot.resolve(profileId).resolve(levelId + ".json");
            if (Files.exists(saveFile)) {
                String json = Files.readString(saveFile);
                return Optional.of(gson.fromJson(json, SaveData.class));
            }
            
            // Try backup file
            Path backupFile = saveRoot.resolve(profileId).resolve(levelId + ".json.bak");
            if (Files.exists(backupFile)) {
                String json = Files.readString(backupFile);
                return Optional.of(gson.fromJson(json, SaveData.class));
            }
        } catch (IOException e) {
            System.err.println("Failed to load game: " + e.getMessage());
        }
        return Optional.empty();
    }
    
    /**
     * Apply loaded save data to game state (before view creation)
     */
    public void applyBasicState(SaveData save, GameState gameState, 
                               WireBudgetService wireBudget, ScoreService score) {
        if (save == null || save.levelData == null) return;
        
        LevelSaveData data = save.levelData;
        
        // Restore game state
        gameState.setPaused(data.paused);
        gameState.setGameOver(data.gameOver);
        gameState.setGameStarted(data.gameStarted);
        gameState.setCurrentTime(data.currentTime);
        gameState.setCoins(data.coins);
        gameState.circlesConnected = data.circlesConnected;
        gameState.rectsConnected = data.rectsConnected;
        gameState.suppressImpact = data.suppressImpact;
        gameState.suppressPacketLoss = data.suppressPacketLoss;
        
        // Restore services
        wireBudget.setRemaining(data.remainingWireLength);
        score.setPacketLoss(data.packetLoss);
        
        // Restore flow state
        if (data.flowState != null) {
            gameState.validSteam = data.flowState.validSteam;
        }
    }
    
    /**
     * Restore connections and wire views after view creation
     */
    public void restoreConnections(SaveData save, ConnectionRepo connections,
                                  GameController controller, javafx.scene.layout.Pane root,
                                  Map<String, Node> nodeMap) {
        if (save == null || save.levelData == null) return;
        
        LevelSaveData data = save.levelData;

        // Restore body positions if nodes provided indices
        if (data.nodes != null) {
            for (NodeSaveData nd : data.nodes) {
                if ("BODY_RECT".equals(nd.type)) {
                    Node node = nodeMap.get(nd.id);
                    if (node instanceof Rectangle) {
                        Rectangle r = (Rectangle) node;
                        // Place body by absolute coordinates using its local x/y offsets
                        r.setLayoutX(nd.x - r.getX());
                        r.setLayoutY(nd.y - r.getY());
                    }
                }
            }
        }
        
        // Restore connections
        for (ConnectionSaveData conn : data.connections) {
            Node node1 = nodeMap.get(conn.node1Id);
            Node node2 = nodeMap.get(conn.node2Id);
            
            if (node1 != null && node2 != null) {
                Line line = new Line(conn.lineStartX, conn.lineStartY, 
                                   conn.lineEndX, conn.lineEndY);
                line.setStroke(Color.YELLOW);
                line.setStrokeWidth(3);
                line.setMouseTransparent(true);
                
                root.getChildren().add(line);
                connections.putPair(node1, node2, line, root);
                
                // Create wire view if needed
                controller.createWireView(line);
            }
        }

        // Resume packets
        if (data.packets != null) {
            for (PacketSaveData ps : data.packets) {
                Node from = nodeMap.get(ps.fromId);
                Node to = nodeMap.get(ps.toId);
                if (from != null && to != null) {
                    controller.resumeSavedFlow(from, to, ps.tag, ps.progress01, ps.secondsTotal);
                }
            }
        }
    }
    
    /**
     * Delete save file
     */
    public void deleteSave(String profileId, String levelId) {
        try {
            Path saveFile = saveRoot.resolve(profileId).resolve(levelId + ".json");
            Path backupFile = saveRoot.resolve(profileId).resolve(levelId + ".json.bak");
            Files.deleteIfExists(saveFile);
            Files.deleteIfExists(backupFile);
        } catch (IOException e) {
            System.err.println("Failed to delete save: " + e.getMessage());
        }
    }
    
    /**
     * List all saved levels for a profile
     */
    public List<String> listSavedLevels(String profileId) {
        List<String> levels = new ArrayList<>();
        Path profileDir = saveRoot.resolve(profileId);
        
        if (Files.isDirectory(profileDir)) {
            try {
                Files.list(profileDir)
                    .filter(p -> p.toString().endsWith(".json"))
                    .forEach(p -> {
                        String filename = p.getFileName().toString();
                        levels.add(filename.substring(0, filename.length() - 5));
                    });
            } catch (IOException e) {
                System.err.println("Failed to list saves: " + e.getMessage());
            }
        }
        
        return levels;
    }
    
    /**
     * Build a snapshot of the current game state without persisting it.
     * Exposed for reuse by autosave and tests.
     */
    public SaveData buildSnapshot(String profileId, String levelId, GameState gameState,
                                   WireBudgetService wireBudget, ScoreService score,
                                   ConnectionRepo connections, GameController controller) {
        SaveData save = new SaveData();
        save.profileId = profileId;
        save.levelId = levelId;
        save.savedAtEpochMillis = Instant.now().toEpochMilli();
        try { save.currentLevelNumber = gameState != null ? gameState.getCurrentLevelNumber() : 0; } catch (Throwable ignored) {}
        
        LevelSaveData levelData = new LevelSaveData();
        
        // Save game state
        levelData.paused = gameState.isPaused();
        levelData.gameOver = gameState.isGameOver();
        levelData.gameStarted = gameState.isGameStarted();
        levelData.currentTime = gameState.getCurrentTime();
        levelData.coins = gameState.getCoins();
        levelData.circlesConnected = gameState.circlesConnected;
        levelData.rectsConnected = gameState.rectsConnected;
        levelData.suppressImpact = gameState.suppressImpact;
        levelData.suppressPacketLoss = gameState.suppressPacketLoss;
        
        // Save services state
        levelData.remainingWireLength = wireBudget.remaining();
        levelData.packetLoss = score.getPacketLoss();
        
        // Save nodes
        List<Circle> circles = controller.getCircles();
        List<Rectangle> smallRects = controller.getSmallRects();
        Set<Rectangle> bodies = controller.getBodies();
        
        // Assign IDs to nodes and save their data
        Map<Node, String> nodeToId = new HashMap<>();
        int nodeCounter = 0;
        
        // Save circles
        for (int i = 0; i < circles.size(); i++) {
            Circle circle = circles.get(i);
            String id = "circle_" + i;
            nodeToId.put(circle, id);
            
            NodeSaveData nodeData = new NodeSaveData();
            nodeData.id = id;
            nodeData.type = "CIRCLE";
            nodeData.x = circle.getLayoutX() + circle.getCenterX();
            nodeData.y = circle.getLayoutY() + circle.getCenterY();
            nodeData.radius = circle.getRadius();
            nodeData.color = colorToHex(circle.getFill());
            nodeData.index = i;
            
            levelData.nodes.add(nodeData);
        }
        
        // Save small rectangles
        for (int i = 0; i < smallRects.size(); i++) {
            Rectangle rect = smallRects.get(i);
            String id = "smallrect_" + i;
            nodeToId.put(rect, id);
            
            NodeSaveData nodeData = new NodeSaveData();
            nodeData.id = id;
            nodeData.type = "SMALL_RECT";
            nodeData.x = rect.getLayoutX() + rect.getX();
            nodeData.y = rect.getLayoutY() + rect.getY();
            nodeData.width = rect.getWidth();
            nodeData.height = rect.getHeight();
            nodeData.color = colorToHex(rect.getFill());
            nodeData.index = i;
            
            levelData.nodes.add(nodeData);
        }
        
        // Save body rectangles (system positions)
        int bodyIndex = 0;
        for (Rectangle body : bodies) {
            String id = "body_" + bodyIndex++;
            nodeToId.put(body, id);
            
            NodeSaveData nodeData = new NodeSaveData();
            nodeData.id = id;
            nodeData.type = "BODY_RECT";
            // Use absolute scene-based position to be robust to layout
            double absX = body.getX() + body.getLayoutX();
            double absY = body.getY() + body.getLayoutY();
            nodeData.x = absX;
            nodeData.y = absY;
            nodeData.width = body.getWidth();
            nodeData.height = body.getHeight();
            nodeData.color = colorToHex(body.getFill());
            nodeData.index = bodyIndex - 1;
            
            levelData.nodes.add(nodeData);
        }
        
        // Save connections
        for (Map.Entry<Node, Node> entry : connections.getAllConnections()) {
            Node node1 = entry.getKey();
            Node node2 = entry.getValue();
            Line line = connections.get(node1);
            
            if (line != null && nodeToId.containsKey(node1) && nodeToId.containsKey(node2)) {
                ConnectionSaveData connData = new ConnectionSaveData();
                connData.node1Id = nodeToId.get(node1);
                connData.node2Id = nodeToId.get(node2);
                connData.lineStartX = line.getStartX();
                connData.lineStartY = line.getStartY();
                connData.lineEndX = line.getEndX();
                connData.lineEndY = line.getEndY();
                
                levelData.connections.add(connData);
            }
        }
        
        // Capture active packets via controller helper
        try {
            levelData.packets.addAll(controller.exportActivePackets(nodeToId));
        } catch (Throwable ignored) {}
        
        // Capture flow state
        FlowStateSave fs = new FlowStateSave();
        fs.validSteam = gameState.validSteam;
        // Additional flow state would be captured from controller if exposed
        levelData.flowState = fs;

        save.levelData = levelData;
        return save;
    }
    
    private String colorToHex(javafx.scene.paint.Paint paint) {
        if (paint instanceof Color) {
            Color c = (Color) paint;
            return String.format("#%02X%02X%02X",
                (int)(c.getRed() * 255),
                (int)(c.getGreen() * 255),
                (int)(c.getBlue() * 255));
        }
        return "#000000";
    }
}
