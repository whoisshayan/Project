import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import java.io.*;
import java.nio.file.*;
import java.util.Optional;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Manages automatic saving for crash recovery
 */
public class AutosaveManager {
    private static final String TEMP_SAVE_DIR = "saves/tmp";
    private final Gson gson;
    private final Path tempRoot;
    private Timeline autosaveTimeline;
    private SaveManager saveManager;
    private String currentProfileId = "default";
    
    public AutosaveManager(SaveManager saveManager) {
        this.saveManager = saveManager;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.tempRoot = Paths.get(TEMP_SAVE_DIR);
        
        try {
            Files.createDirectories(tempRoot);
        } catch (IOException e) {
            System.err.println("Failed to create temp save directory: " + e.getMessage());
        }
    }
    
    /**
     * Start periodic autosaving
     */
    public void startAutosave(String levelId, GameState gameState, 
                             WireBudgetService wireBudget, ScoreService score,
                             ConnectionRepo connections, GameController controller,
                             double intervalSeconds) {
        stopAutosave();
        
        autosaveTimeline = new Timeline(new KeyFrame(Duration.seconds(intervalSeconds), e -> {
            // Save periodically during the level, even if not started yet
            if (!gameState.isGameOver()) {
                performAutosave(levelId, gameState, wireBudget, score, connections, controller);
            }
        }));
        
        autosaveTimeline.setCycleCount(Timeline.INDEFINITE);
        autosaveTimeline.play();
    }
    
    /**
     * Stop autosaving
     */
    public void stopAutosave() {
        if (autosaveTimeline != null) {
            autosaveTimeline.stop();
            autosaveTimeline = null;
        }
    }
    
    /**
     * Perform an autosave to temporary location
     */
    private void performAutosave(String levelId, GameState gameState,
                                WireBudgetService wireBudget, ScoreService score,
                                ConnectionRepo connections, GameController controller) {
        try {
            // Build full snapshot so connections are restored after relaunch
            SaveData save = saveManager.buildSnapshot(currentProfileId, levelId, gameState,
                                                    wireBudget, score, connections, controller);
            
            Path profileDir = tempRoot.resolve(currentProfileId);
            Files.createDirectories(profileDir);
            
            Path autosaveFile = profileDir.resolve(levelId + ".json");
            String json = gson.toJson(save);
            Files.writeString(autosaveFile, json, StandardOpenOption.CREATE, 
                            StandardOpenOption.TRUNCATE_EXISTING);
                            
        } catch (IOException e) {
            System.err.println("Autosave failed: " + e.getMessage());
        }
    }
    
    /**
     * Check if autosave exists for a level
     */
    public Optional<SaveData> checkForAutosave(String levelId) {
        try {
            Path autosaveFile = tempRoot.resolve(currentProfileId).resolve(levelId + ".json");
            if (Files.exists(autosaveFile)) {
                String json = Files.readString(autosaveFile);
                return Optional.of(gson.fromJson(json, SaveData.class));
            }
        } catch (IOException e) {
            System.err.println("Failed to check autosave: " + e.getMessage());
        }
        return Optional.empty();
    }
    
    /**
     * Delete autosave file
     */
    public void deleteAutosave(String levelId) {
        try {
            Path autosaveFile = tempRoot.resolve(currentProfileId).resolve(levelId + ".json");
            Files.deleteIfExists(autosaveFile);
        } catch (IOException e) {
            System.err.println("Failed to delete autosave: " + e.getMessage());
        }
    }
    
    /**
     * Delete all autosave files for the current profile
     */
    public void deleteAllAutosaves() {
        try {
            Path profileDir = tempRoot.resolve(currentProfileId);
            if (Files.exists(profileDir)) {
                Files.list(profileDir)
                    .filter(p -> p.toString().endsWith(".json"))
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            System.err.println("Failed to delete autosave: " + path + " - " + e.getMessage());
                        }
                    });
            }
        } catch (IOException e) {
            System.err.println("Failed to list/delete autosaves: " + e.getMessage());
        }
    }
    
    /**
     * Show resume prompt to user
     */
    public boolean showResumePrompt(SaveData autosave) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Resume Game");
        alert.setHeaderText("Previous game session found");
        
        long savedTime = autosave.savedAtEpochMillis;
        java.time.Instant instant = java.time.Instant.ofEpochMilli(savedTime);
        String timeStr = java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME
            .withZone(java.time.ZoneId.systemDefault())
            .format(instant);
            
        alert.setContentText("An autosave from " + timeStr + " was found.\n" +
                           "Would you like to resume from where you left off?");
        
        ButtonType resumeButton = new ButtonType("Resume");
        ButtonType newGameButton = new ButtonType("Start New");
        alert.getButtonTypes().setAll(resumeButton, newGameButton);
        
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == resumeButton;
    }
    
    /**
     * Show countdown before resuming from autosave
     */
    public void showFrozenPreview(javafx.scene.Scene scene, SaveData autosave, Runnable onComplete) {
        javafx.scene.text.Text countdownText = new javafx.scene.text.Text("Resuming in 3...");
        countdownText.setFont(javafx.scene.text.Font.font("Arial", 48));
        countdownText.setFill(javafx.scene.paint.Color.WHITE);
        countdownText.setStroke(javafx.scene.paint.Color.BLACK);
        countdownText.setStrokeWidth(2);
        
        javafx.scene.layout.StackPane overlay = new javafx.scene.layout.StackPane();
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.7);");
        overlay.getChildren().add(countdownText);
        
        javafx.scene.layout.Pane root = (javafx.scene.layout.Pane) scene.getRoot();
        root.getChildren().add(overlay);
        overlay.prefWidthProperty().bind(scene.widthProperty());
        overlay.prefHeightProperty().bind(scene.heightProperty());
        
        Timeline countdown = new Timeline(
            new KeyFrame(Duration.seconds(1), e -> countdownText.setText("Resuming in 2...")),
            new KeyFrame(Duration.seconds(2), e -> countdownText.setText("Resuming in 1...")),
            new KeyFrame(Duration.seconds(3), e -> {
                root.getChildren().remove(overlay);
                if (onComplete != null) onComplete.run();
            })
        );
        countdown.play();
    }
    
    // No separate minimal autosave; we persist the full snapshot to ensure wiring restoration
}
