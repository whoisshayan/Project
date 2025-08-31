import javafx.animation.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.BoundingBox;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Slider;
import javafx.scene.effect.Blend;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ButtonBar;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import java.io.File;
import java.util.*;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.scene.paint.Color;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;
import javafx.scene.Node;
import java.util.Objects;
import javafx.scene.shape.Polygon;
import javafx.geometry.Bounds;
import javafx.animation.PathTransition;
import javafx.animation.ParallelTransition;
// removed com.sun.javafx imports to avoid build-time dependency
import javafx.beans.binding.Bindings;
import javafx.beans.binding.Bindings;
import javafx.geometry.VPos;
// removed com.sun.javafx imports to avoid build-time dependency
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.animation.*;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.util.Duration;
import javafx.animation.Interpolator;

/* =========================================================
 *                      MAIN (App)
 * ========================================================= */
public class Main extends Application {

    // ---------- UI (View) ----------
    private Pane mainRoot;
    private Group levelLayer;           // لایه‌ی مخصوص نودهای هر مرحله
    private Text remainingWireText;
    private Text packetLossText;
    private Text coinsText;
    private Slider timeSlider;

    // ---------- Model / State ----------
    private final GameState state = new GameState();

    // ---------- Services ----------
    private final WireBudgetService wireBudget = new WireBudgetService(1000);
    private final ScoreService score = new ScoreService(1000, 0);
    private final ConnectionRepo connectionRepo = new ConnectionRepo();

    // ---------- MVC Helpers ----------
    private final HUDModel hudModel = new HUDModel();
    private HUDBinder hudBinder;
    private GameController controller;

    // ---------- Helpers ----------
    private DropShadow sharedShadow;

    @Override
    public void start(Stage primaryStage) {
        // ----------------------- MENU -----------------------
        File bgFile = new File("Menu background.png");
        Image bgImage = new Image(bgFile.exists() ? bgFile.toURI().toString() : new File("Menu background.png").toURI().toString());
        ImageView bgView = new ImageView(bgImage);
        bgView.setFitWidth(700);
        bgView.setFitHeight(600);
        bgView.setPreserveRatio(false);

        File bgFile2 = new File("Setting background.png");
        Image bgImage2 = new Image(bgFile2.exists() ? bgFile2.toURI().toString() : new File("Setting background.png").toURI().toString());
        ImageView bgView2 = new ImageView(bgImage2);
        bgView2.setFitWidth(700);
        bgView2.setFitHeight(600);
        bgView2.setPreserveRatio(false);

        File bgFile3 = new File("level background.png");
        Image bgImage3 = new Image(bgFile3.exists() ? bgFile3.toURI().toString() : new File("level background.png").toURI().toString());
        ImageView bgView3 = new ImageView(bgImage3);
        bgView3.setFitWidth(700);
        bgView3.setFitHeight(600);
        bgView3.setPreserveRatio(false);

        Text line1 = new Text(155, 100, "BLUEPRINT");
        Text line2 = new Text(275, 180, "HELL");
        line1.setFont(Font.font("Impact", 90));
        line2.setFont(Font.font("Impact", 90));
        line1.setFill(Color.WHITE);
        line2.setFill(Color.WHITE);

        // Music (optional) — skip if file not found
        final MediaPlayer[] mediaPlayer = new MediaPlayer[1];
        File file = new File("C:/Users/shaya/Desktop/song/202. Bagatelle in A minor, WoO 59, Fur Elise.mp3");
        if (file.exists()) {
        String url = file.toURI().toString();
        Media media = new Media(url);
            mediaPlayer[0] = new MediaPlayer(media);
            mediaPlayer[0].play();
        } else {
            System.out.println("Music File Path Error (skipping background music)");
        }

        sharedShadow = new DropShadow();
        sharedShadow.setRadius(20);
        sharedShadow.setOffsetX(0);
        sharedShadow.setOffsetY(0);
        sharedShadow.setColor(Color.WHITE);

        Button startButton   = styledButton("Continue", 290, 180, 120, 45, "#4CAF50");
        Button newGameButton = styledButton("Start New", 290, 235, 120, 45, "#2196F3");
        Button levelButton   = styledButton("Levels",  290, 290, 120, 45, "purple");
        Button settingButton = styledButton("Settings",290, 345, 120, 45, "red");
        Button exitButton    = styledButton("Exit",    290, 400, 120, 45, "orange");
        exitButton.setOnAction(e -> Platform.exit());

        Pane menuRoot = new Pane();
        menuRoot.getChildren().add(bgView);
        menuRoot.getChildren().addAll(startButton, newGameButton, levelButton, settingButton, exitButton, line1, line2);
        Scene menuScene = new Scene(menuRoot, 700, 600);

        // Level scene (selector)
        Button firstLevelButton   = styledButton("1",   82.5, 193, 64, 55, "red");
        Button secondLevelButton  = styledButton("2",  316.5, 193, 64, 55, "red");
        Button backButtonFromLevel= styledButton("Back", 520, 530, 120, 50, "red");
        Button clearSavesButton   = styledButton("Clear Saves", 50, 530, 120, 50, "#FF5722");
        Button thirdLevelButton   = styledButton("3",  316.5, 193, 64, 55, "red");
        Button fourthLevelButton  = styledButton("4",  416.5, 193, 64, 55, "red");
        Button fifthLevelButton   = styledButton("5",  516.5, 193, 64, 55, "red"); // ✅ دکمه مرحله 5

        Pane levelRootScene = new Pane();
        levelRootScene.getChildren().add(bgView3);
        levelRootScene.getChildren().addAll(
                firstLevelButton, secondLevelButton, thirdLevelButton,
                fourthLevelButton, fifthLevelButton,                  // ✅ اضافه شد
                backButtonFromLevel, clearSavesButton
        );
        Scene levelScene = new Scene(levelRootScene, 700, 600);
        backButtonFromLevel.setOnAction(e -> primaryStage.setScene(menuScene));
        levelButton.setOnAction(e -> primaryStage.setScene(levelScene));
        
        // Clear saves button functionality
        clearSavesButton.setOnAction(e -> {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Clear All Saves");
            confirmAlert.setHeaderText("Clear All Save Data?");
            confirmAlert.setContentText("This will permanently delete all your saved progress.\nThis action cannot be undone!\n\nAre you sure you want to continue?");
            
            ButtonType yesButton = new ButtonType("Yes, Delete All");
            ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            confirmAlert.getButtonTypes().setAll(yesButton, cancelButton);
            
            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == yesButton) {
                // Clear all saves and autosaves
                String profileId = new PlayerManager().getActiveProfileId();
                SaveManager tempSaveManager = new SaveManager();
                AutosaveManager tempAutosaveManager = new AutosaveManager(tempSaveManager);
                
                List<String> savedLevels = tempSaveManager.listSavedLevels(profileId);
                for (String levelId : savedLevels) {
                    tempSaveManager.deleteSave(profileId, levelId);
                }
                tempAutosaveManager.deleteAllAutosaves();
                
                // Show success message
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Saves Cleared");
                successAlert.setHeaderText("Success!");
                successAlert.setContentText("All save data has been cleared successfully.");
                successAlert.showAndWait();
            }
        });


        // Settings scene
        Button backButton = styledButton("Back", 290, 465, 120, 50, "red");
        backButton.setOnAction(e -> primaryStage.setScene(menuScene));
        Button muteButton = styledButton("Mute", 290, 365, 120, 50, "red");
        muteButton.setOnAction(e -> {
            if (mediaPlayer[0] != null) {
                if (mediaPlayer[0].getStatus() == MediaPlayer.Status.PLAYING) {
                    mediaPlayer[0].pause();
                muteButton.setText("Unmute");
            } else {
                    mediaPlayer[0].play();
                muteButton.setText("Mute");
                }
            }
        });
        Slider volumeSlider = new Slider(0, 1, 0.5);
        volumeSlider.setLayoutX(123);
        volumeSlider.setLayoutY(290);
        volumeSlider.setPrefWidth(470);
        volumeSlider.valueProperty().addListener((obs, ov, nv) -> { if (mediaPlayer[0] != null) mediaPlayer[0].setVolume(nv.doubleValue()); });
        if (mediaPlayer[0] != null) mediaPlayer[0].setVolume(volumeSlider.getValue());
        if (mediaPlayer[0] == null) { muteButton.setDisable(true); volumeSlider.setDisable(true); }
        volumeSlider.setShowTickLabels(true);
        volumeSlider.setMajorTickUnit(0.25);
        volumeSlider.setBlockIncrement(0.25);
        volumeSlider.setStyle("-fx-accent: #4286f4; -fx-control-inner-background: green;");

        Pane settingRoot = new Pane();
        settingRoot.getChildren().add(bgView2);
        settingRoot.getChildren().addAll(backButton, muteButton, volumeSlider);
        Scene settingScene = new Scene(settingRoot, 700, 600);
        settingButton.setOnAction(e -> primaryStage.setScene(settingScene));

        // ----------------------- MAIN GAME SCENE (HUD + LevelLayer) -----------------------
        HUDRefs hud = buildHUDLayer();     // HUD یک‌بار ساخته می‌شود
        this.mainRoot           = hud.root;
        this.levelLayer         = hud.levelLayer;
        this.remainingWireText  = hud.remainingWireText;
        this.packetLossText     = hud.packetLossText;
        this.coinsText          = hud.coinsText;
        this.timeSlider         = hud.timeSlider;

        // HUD Binder
        hudBinder = new HUDBinder(remainingWireText, packetLossText, coinsText, timeSlider);
        hudModel.setRemainingWire(wireBudget.remainingInt());
        hudModel.setPacketLoss(score.getPacketLoss());
        hudModel.setCoins(score.getCoins());
        hudModel.setTimeDisabled(true);
        hudBinder.syncFrom(hudModel);

        // Controller
        controller = new GameController(
                state, wireBudget, score, connectionRepo,
                hudModel, hudBinder,
                mainRoot,
                () -> buildExplosionPlayer(mainRoot),
                () -> sharedShadow
        );

        // رخدادهای عمومی برای سیم‌کشی (برای همه‌ی مراحل ثابت)
        mainRoot.setOnMouseDragged(controller::onDragWire);
        mainRoot.setOnMouseReleased(controller::onReleaseWire);

        // دکمه‌های پایین (Shop/Back)
        Button backToMenuButton = styledBigButton("Back", 650, 610, 160, 80, "red");
        backToMenuButton.setOnAction(actionEvent -> primaryStage.setScene(menuScene));
        mainRoot.getChildren().add(backToMenuButton);

        Button shopButton = styledBigButton("Shop", 80, 610, 160, 80, "red");
        shopButton.setOnAction(e -> controller.openShop());
        mainRoot.getChildren().add(shopButton);

        Scene mainScene = new Scene(mainRoot, 900, 700);

        // ----------------------- Level Manager -----------------------
        LevelManager lm = new LevelManager(
                primaryStage, mainRoot, levelLayer,
                controller, timeSlider,
                wireBudget, score, hudModel, hudBinder,
                connectionRepo, state,
                menuScene
        );

        lm.register(1, new Level1());
        lm.register(2, new Level2());
        lm.register(3, new Level3()); // ← اضافه کن
        lm.register(4, new Level4()); // ← همین را اضافه کن
        lm.register(5, new Level5()); // ✅ ثبت مرحله 5




        // Determine last played level for active player
        PlayerManager players = new PlayerManager();
        String bootProfile = players.getActiveProfileId();
        SaveManager bootSave = new SaveManager();
        AutosaveManager bootAutosave = new AutosaveManager(bootSave);
        final int lastLevel;
        int last = 1;
        
        // Check both normal saves and autosaves to find the highest level
        for (int i = 9; i >= 1; i--) {
            Optional<SaveData> normalSave = bootSave.loadGame(bootProfile, "level-" + i);
            Optional<SaveData> autoSave = bootAutosave.checkForAutosave("level-" + i);
            
            // If we have either type of save for this level, this is our last level
            if ((normalSave.isPresent() && normalSave.get().currentLevelNumber == i) ||
                (autoSave.isPresent() && autoSave.get().currentLevelNumber == i)) {
                last = i; 
                break; 
            }
        }
        lastLevel = last;

        // منو -> شروع (Continue from last save)
        startButton.setOnAction(e -> { 
            // Check if we have any saves (normal or auto) to continue from
            boolean hasSaves = false;
            SaveManager checkSave = new SaveManager();
            String profileId = players.getActiveProfileId();
            
            // Check for normal saves
            List<String> savedLevels = checkSave.listSavedLevels(profileId);
            if (!savedLevels.isEmpty()) {
                hasSaves = true;
            } else {
                // Check for autosaves
                AutosaveManager checkAutosave = new AutosaveManager(checkSave);
                for (int i = 1; i <= 9; i++) {
                    if (checkAutosave.checkForAutosave("level-" + i).isPresent()) {
                        hasSaves = true;
                        break;
                    }
                }
            }
            
            if (hasSaves) {
                primaryStage.setScene(mainScene); 
                lm.goTo(lastLevel); 
            } else {
                // No saves found, show info and ask to start new
                Alert infoAlert = new Alert(Alert.AlertType.INFORMATION);
                infoAlert.setTitle("No Saved Game");
                infoAlert.setHeaderText("No previous progress found");
                infoAlert.setContentText("You don't have any saved progress yet.\nWould you like to start a new game?");
                
                ButtonType startNewButton = new ButtonType("Start New Game");
                ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
                infoAlert.getButtonTypes().setAll(startNewButton, cancelButton);
                
                Optional<ButtonType> result = infoAlert.showAndWait();
                if (result.isPresent() && result.get() == startNewButton) {
                    primaryStage.setScene(mainScene); 
                    lm.startNewGame(); 
                }
            }
        });
        
        // منو -> شروع جدید (Start New Game - clear saves)
        newGameButton.setOnAction(e -> { 
            // Show confirmation dialog for starting new game
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Start New Game");
            confirmAlert.setHeaderText("Start New Game?");
            confirmAlert.setContentText("This will delete all your current progress and start from Level 1.\nAre you sure you want to continue?");
            
            ButtonType yesButton = new ButtonType("Yes, Start New");
            ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            confirmAlert.getButtonTypes().setAll(yesButton, cancelButton);
            
            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == yesButton) {
                primaryStage.setScene(mainScene); 
                lm.startNewGame(); 
            }
        });
        firstLevelButton.setOnAction(e -> { primaryStage.setScene(mainScene); lm.goTo(1); });
        secondLevelButton.setOnAction(e -> { primaryStage.setScene(mainScene); lm.goTo(2); });
        thirdLevelButton.setOnAction(e  -> { primaryStage.setScene(mainScene); lm.goTo(3); });
        fourthLevelButton.setOnAction(e -> { primaryStage.setScene(mainScene); lm.goTo(4); });
        fifthLevelButton.setOnAction(e  -> { primaryStage.setScene(mainScene); lm.goTo(5); }); // ✅ رفتن به مرحله 5




        primaryStage.setTitle("BluePrint Hell");
        primaryStage.setScene(menuScene);
        primaryStage.setResizable(false);

        // Persist state on window close
        primaryStage.setOnCloseRequest(e -> {
            try {
                lm.forceSaveIfPlaying();
            } catch (Throwable ignored) {}
        });

        primaryStage.show();
    }

    /* ---------------- HUD (یک‌بار) + لایه مرحله ---------------- */
    private HUDRefs buildHUDLayer() {
        Pane root = new Pane();
        root.setStyle("-fx-background-color: purple;");

        // لایه مخصوص نودهای مرحله
        Group levelLayer = new Group();
        root.getChildren().add(levelLayer);

        Rectangle upRectangle = hudBar(0, 0, 900, 115);
        root.getChildren().add(upRectangle);

        Rectangle remainingWireBox = hudBox(100, 8, 120, 100, Color.PURPLE);
        Text remainingWireText = hudText("Remaining\n     Wire:\n      1000", 106, 40, 16);
        root.getChildren().addAll(remainingWireBox, remainingWireText);

        Rectangle timeProgressBox = hudBox(300, 8, 120, 100, Color.PURPLE);
        Text timeProgressText = hudText("    Time\nProgress:", 316, 40, 16);
        Slider timeSlider = new Slider(0, 2.0, 0);
        timeSlider.setLayoutX(timeProgressText.getLayoutX() - 7);
        timeSlider.setLayoutY(timeProgressText.getLayoutY() + 30);
        timeSlider.setPrefWidth(timeProgressBox.getWidth() - 10);
        timeSlider.setDisable(true);
        root.getChildren().addAll(timeProgressBox, timeProgressText, timeSlider);

        Rectangle packetLossBox = hudBox(500, 8, 120, 100, Color.PURPLE);
        Text packetLossText = hudText("Packet Loss:\n\n        0", 514, 40, 14);
        root.getChildren().addAll(packetLossBox, packetLossText);

        Rectangle coinsBox = hudBox(700, 8, 120, 100, Color.PURPLE);
        Text coinsText = hudText("     Coins:\n\n     1000", 714, 40, 14);
        root.getChildren().addAll(coinsBox, coinsText);

        Rectangle downRectangle = hudBar(0, 600, 900, 115);
        root.getChildren().add(downRectangle);

        return new HUDRefs(root, levelLayer, remainingWireText, packetLossText, coinsText, timeSlider);
    }

    /* ---------------- Helpers (UI) ---------------- */
    private Button styledButton(String text, double x, double y, double w, double h, String color) {
        Button b = new Button(text);
        b.setLayoutX(x);
        b.setLayoutY(y);
        b.setPrefSize(w, h);
        b.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-size: 16px;");
        addHoverShadow(b);
        return b;
    }

    private Button styledBigButton(String text, double x, double y, double w, double h, String color) {
        Button b = styledButton(text, x, y, w, h, color);
        b.setPrefWidth(w); b.setPrefHeight(h);
        return b;
    }

    private void addHoverShadow(Button b) {
        b.setOnMouseEntered(e -> b.setEffect(sharedShadow));
        b.setOnMouseExited(e -> b.setEffect(null));
    }

    private Rectangle hudBar(double x, double y, double w, double h) {
        Rectangle r = new Rectangle(x, y, w, h);
        r.setArcWidth(30);
        r.setArcHeight(30);
        r.setFill(Color.web("#663399"));
        r.setStroke(Color.BLACK);
        r.setStrokeWidth(2);
        return r;
    }

    private Rectangle hudBox(double x, double y, double w, double h, Color fill) {
        Rectangle r = new Rectangle(x, y, w, h);
        r.setFill(fill);
        r.setArcWidth(10);
        r.setArcHeight(10);
        return r;
    }

    private Text hudText(String content, double x, double y, int size) {
        Text t = new Text(content);
        t.setLayoutX(x); t.setLayoutY(y);
        t.setFont(Font.font("Verdana", FontWeight.BOLD, FontPosture.ITALIC, size));
        t.setFill(Color.web("#FFE4E1"));
        DropShadow ds = new DropShadow();
        ds.setOffsetY(3.0);
        ds.setOffsetX(3.0);
        ds.setColor(Color.color(0, 0, 0, 0.4));
        t.setEffect(ds);
        return t;
    }

    private ExplosionPlayer buildExplosionPlayer(Pane root) {
        return (x, y) -> {
            Circle wave = new Circle(x, y, 10);
            wave.setFill(null);
            wave.setStroke(Color.ORANGE);
            wave.setStrokeWidth(3);
            root.getChildren().add(wave);

            ScaleTransition scale = new ScaleTransition(Duration.seconds(0.5), wave);
            scale.setFromX(1);
            scale.setFromY(1);
            scale.setToX(4);
            scale.setToY(4);

            FadeTransition fade = new FadeTransition(Duration.seconds(0.5), wave);
            fade.setFromValue(1.0);
            fade.setToValue(0.0);

            ParallelTransition explosion = new ParallelTransition(scale, fade);
            explosion.setOnFinished(e -> root.getChildren().remove(wave));
            explosion.play();
        };
    }

    public static void main(String[] args) {
        launch(args);
    }
}

/* =========================================================
 *                   DATA / HUD container
 * ========================================================= */
final class HUDRefs {
    final Pane root;
    final Group levelLayer;
    final Text remainingWireText, packetLossText, coinsText;
    final Slider timeSlider;
    HUDRefs(Pane root, Group levelLayer, Text r, Text p, Text c, Slider s) {
        this.root=root; this.levelLayer=levelLayer;
        this.remainingWireText=r; this.packetLossText=p; this.coinsText=c; this.timeSlider=s;
    }
}

/* =========================================================
 *                      GAME STATE
 * ========================================================= */
final class GameState {
    Circle startCircle = null;
    Rectangle startSmallRect = null;
    Line currentLine = null;

    boolean circlesConnected = false;
    boolean rectsConnected = false;

    boolean suppressImpact = false;
    boolean suppressPacketLoss = false;
    
    // Additional state for save/load
    private boolean paused = false;
    private boolean gameOver = false;
    private boolean gameStarted = false;
    private double currentTime = 0.0;
    private int coins = 0;
    // Track last visited level number
    private int currentLevelNumber = 1;
    
    // Getters and setters for save/load
    public boolean isPaused() { return paused; }
    public void setPaused(boolean paused) { this.paused = paused; }
    
    public boolean isGameOver() { return gameOver; }
    public void setGameOver(boolean gameOver) { this.gameOver = gameOver; }
    
    public boolean isGameStarted() { return gameStarted; }
    public void setGameStarted(boolean gameStarted) { this.gameStarted = gameStarted; }
    
    public double getCurrentTime() { return currentTime; }
    public void setCurrentTime(double time) { this.currentTime = time; }
    
    public int getCoins() { return coins; }
    public void setCoins(int coins) { this.coins = coins; }
    public int getCurrentLevelNumber() { return currentLevelNumber; }
    public void setCurrentLevelNumber(int n) { this.currentLevelNumber = n; }

    int validSteam = 0;

    boolean circle12Connected = false;
    boolean circle34Connected = false;
    boolean rect12Connected = false;
    boolean rect34Connected = false;

    final List<Node> previewNodes = new ArrayList<>();
}

/* =========================================================
 *                      SERVICES
 * ========================================================= */
final class WireBudgetService {
    private double remaining;
    WireBudgetService(double initialWire) { this.remaining = Math.max(0, initialWire); }
    boolean consume(double len) {
        if (len <= 0) return true;
        if (remaining < len) return false;
        remaining -= len; return true;
    }
    void refund(double len) { if (len > 0) remaining += len; }
    int remainingInt() { return (int)Math.round(remaining); }
    double remaining() { return remaining; }
    void setRemaining(double remaining) { this.remaining = Math.max(0, remaining); }
    void resetTo(double v){ remaining = Math.max(0,v); }
}

final class ScoreService {
    private int coins;
    private int packetLoss;
    ScoreService(int coins, int packetLoss) { this.coins = coins; this.packetLoss = packetLoss; }
    void addCoins(int c) { coins += c; }
    void addPacketLoss(int p) { packetLoss += p; }
    void resetPacketLoss() { packetLoss = 0; }
    int getCoins() { return coins; }
    int getPacketLoss() { return packetLoss; }
    void setPacketLoss(int p) { packetLoss = p; }
}

final class ConnectionRepo {
    private final Map<Node, Line> connectionMap = new HashMap<>();

    void putPair(Node a, Node b, Line line, Pane root) {
        removeFor(a, root);
        removeFor(b, root);
        connectionMap.put(a, line);
        connectionMap.put(b, line);
    }
    Line get(Node n) { return connectionMap.get(n); }

    Node otherNode(Node n) {
        Line line = connectionMap.get(n);
        if (line == null) return null;
        for (Map.Entry<Node, Line> e : connectionMap.entrySet()) {
            if (e.getValue() == line && e.getKey() != n) return e.getKey();
        }
        return null;
    }

    double removeFor(Node n, Pane root) {
        Line old = connectionMap.get(n);
        if (old != null) {
            double dx = old.getEndX() - old.getStartX();
            double dy = old.getEndY() - old.getStartY();
            double len = Math.hypot(dx, dy);

            root.getChildren().remove(old);
            connectionMap.entrySet().removeIf(e -> e.getValue() == old);

            return len; // ⬅️ ریفاند لازم به عهده‌ی کالر
        }
        return 0.0;
    }


    void clear(Pane root){
        for (Line l : new HashSet<>(connectionMap.values())) root.getChildren().remove(l);
        connectionMap.clear();
    }

    java.util.Set<Line> allLines() { return new java.util.HashSet<>(connectionMap.values()); }
    
    // Get all connections for saving
    java.util.Set<Map.Entry<Node, Node>> getAllConnections() {
        Map<Node, Node> connections = new HashMap<>();
        Map<Line, List<Node>> lineToNodes = new HashMap<>();
        
        // Group nodes by their line
        for (Map.Entry<Node, Line> entry : connectionMap.entrySet()) {
            lineToNodes.computeIfAbsent(entry.getValue(), k -> new ArrayList<>()).add(entry.getKey());
        }
        
        // Create node pairs from lines
        for (List<Node> nodes : lineToNodes.values()) {
            if (nodes.size() == 2) {
                connections.put(nodes.get(0), nodes.get(1));
            }
        }
        
        return connections.entrySet();
    }
}

/* =========================================================
 *                        HUD (MVC)
 * ========================================================= */
final class HUDModel {
    private int remainingWire;
    private int packetLoss;
    private int coins;
    private double timeValue;
    private boolean timeDisabled;

    int getRemainingWire() { return remainingWire; }
    int getPacketLoss() { return packetLoss; }
    int getCoins() { return coins; }
    double getTimeValue() { return timeValue; }
    boolean isTimeDisabled() { return timeDisabled; }

    void setRemainingWire(int v) { remainingWire = v; }
    void setPacketLoss(int v) { packetLoss = v; }
    void setCoins(int v) { coins = v; }
    void setTimeValue(double v) { timeValue = v; }
    void setTimeDisabled(boolean b) { timeDisabled = b; }
}

final class HUDBinder {
    private final Text remainingWireText, packetLossText, coinsText;
    private final Slider timeSlider;

    HUDBinder(Text remainingWireText, Text packetLossText, Text coinsText, Slider timeSlider) {
        this.remainingWireText = remainingWireText;
        this.packetLossText = packetLossText;
        this.coinsText = coinsText;
        this.timeSlider = timeSlider;
    }

    void syncFrom(HUDModel m) {
        remainingWireText.setText("Remaining\n     Wire:\n      " + m.getRemainingWire());
        packetLossText.setText("Packet Loss:\n\n        " + m.getPacketLoss());
        coinsText.setText("     Coins:\n\n     " + m.getCoins());
        timeSlider.setDisable(m.isTimeDisabled());
        timeSlider.setValue(m.getTimeValue());
    }
}

/* =========================================================
 *                 Explosion player factory
 * ========================================================= */
interface ExplosionPlayer { void explode(double x, double y); }
interface ExplosionPlayerFactory { ExplosionPlayer get(); }
interface ShadowFactory { DropShadow get(); }

/* =========================================================
 *                      CONTROLLER
 * ========================================================= */
    final class GameController {

    private final GameState st;
    private final WireBudgetService wireBudget;
    private final ScoreService score;
    private final ConnectionRepo repo;
    private final HUDModel hud;
    private final HUDBinder binder;
    private final Pane root;
    private final ExplosionPlayerFactory explosionFactory;
    private final ShadowFactory shadowFactory;

    // --- Curved wire overlays (Line -> overlay with up to 3 bends)
    private final java.util.Map<Line, WireView> wireViews = new java.util.HashMap<>();
    // Bodies for collision validation and start buttons to guard run
    private final java.util.Set<Rectangle> bodies = new java.util.HashSet<>();
    private final java.util.List<Button> startButtons = new java.util.ArrayList<>();
    // Track active flows for save/resume
    private final java.util.List<FlowHandle> activeFlows = new java.util.ArrayList<>();
    // refs برای مرحله‌ی فعال
    private List<Circle> circles;
    private List<Rectangle> smallRects;
    private Slider timeSlider;
    
    // Getters for save/load
    public List<Circle> getCircles() { return circles; }
    public List<Rectangle> getSmallRects() { return smallRects; }
    public Set<Rectangle> getBodies() { return bodies; }
    public java.util.List<FlowHandle> getActiveFlows() { return new java.util.ArrayList<>(activeFlows); }



    // GameController
    private boolean level5Tuning = false;
    void setLevel5Tuning(boolean on) { this.level5Tuning = on; }




    // داخل GameController
    Node connectedOf(Node n) {
        return repo.otherNode(n); // همون اتصال واقعی که کاربر کشیده
    }


    GameController(GameState st,
                   WireBudgetService wireBudget,
                   ScoreService score,
                   ConnectionRepo repo,
                   HUDModel hud,
                   HUDBinder binder,
                   Pane root,
                   ExplosionPlayerFactory explosionFactory,
                   ShadowFactory shadowFactory) {
        this.st = st;
        this.wireBudget = wireBudget;
        this.score = score;
        this.repo = repo;
        this.hud = hud;
        this.binder = binder;
        this.root = root;
        this.explosionFactory = explosionFactory;
        this.shadowFactory = shadowFactory;
    }


    void addPacketLoss(int amount) {
        score.addPacketLoss(amount);
        hud.setPacketLoss(score.getPacketLoss());
        binder.syncFrom(hud);
    }
    
    // Create wire view for loaded connections
    public void createWireView(Line line) {
        if (line != null && !wireViews.containsKey(line)) {
            WireView wv = new WireView(line);
            wireViews.put(line, wv);
            wv.attach();
        }
    }



    static final class FlowHandle {
        final Node sprite;           // شکلی که حرکت می‌کند (مثلث/مربع/شش‌ضلعی)
        final Animation anim;        // انیمیشن حرکت
        final Node from, to;         // مبدا/مقصد منطقی
        FlowHandle(Node sprite, Animation anim, Node from, Node to) {
            this.sprite = sprite; this.anim = anim; this.from = from; this.to = to;
        }
        boolean isFinished() { return anim.getStatus() == Animation.Status.STOPPED; }
    }

    // برای whenAllPortsHaveArrival
    private Set<Node> awaitPorts = Collections.emptySet();
    private final Set<Node> reachedPorts = new HashSet<>();
    private Runnable awaitPortsCallback;


    void cleanupFlowSprites() {
        root.getChildren().removeIf(n -> "FLOW_SPRITE".equals(n.getUserData()));
    }

    // =================== GameController ===================

    /** اسپریت مناسب بر اساس تَگ جریان می‌سازد و دقیقا در مختصات start (مختصات ریشه‌ی root) قرار می‌دهد. */
    private Node makeSpriteForTag(String tag, Point2D start) {
        final Node s;
        switch (String.valueOf(tag)) {
            case "M_SQ" -> {                 // مربع زردِ مرکز-محور
                double a = 8;
                Rectangle r = new Rectangle(-a / 2, -a / 2, a, a);
                r.setFill(Color.YELLOW);
                r.setStroke(Color.BLACK);
                r.setStrokeWidth(1);
                s = r;
            }
            case "M_HEX" -> {                // شش‌ضلعی حول مبدأ (0,0)؛ بعدا با Layout جابه‌جا می‌شود
                double R = 6;
                Polygon h = new Polygon(
                        R, 0,
                        R / 2,  Math.sqrt(3) * R / 2,
                        -R / 2, Math.sqrt(3) * R / 2,
                        -R, 0,
                        -R / 2, -Math.sqrt(3) * R / 2,
                        R / 2,  -Math.sqrt(3) * R / 2
                );
                h.setFill(Color.YELLOW);
                h.setStroke(Color.BLACK);
                h.setStrokeWidth(1);
                s = h;
            }
            default -> {                     // M_TRI : مثلث زردِ مرکز-محور
                double size = 10;
                double h = size * Math.sqrt(3) / 2.0;
                Polygon p = new Polygon(
                        0.0,      -h,       // رأس بالا
                        -size/2.,  h/2.,    // چپ پایین
                        size/2.,   h/2.     // راست پایین
                );
                p.setFill(Color.YELLOW);
                p.setStroke(Color.BLACK);
                p.setStrokeWidth(1);
                s = p;
            }
        }

        // برای PathTransition با مختصات مطلق مسیر، نود را در مبدأ (0,0) می‌گذاریم
        // و مسیر، ترجمه را به مختصات مطلق اعمال می‌کند
        s.setLayoutX(0);
        s.setLayoutY(0);

        // برای کارکرد صحیح روی صحنه و پاکسازی مطمئن
        s.setManaged(false);
        s.setMouseTransparent(true);
        s.setUserData("FLOW_SPRITE");  // برای پاکسازی گروهی
        s.toFront();

        return s;
    }

    /** از پورت from به پورت to (یا اتصال واقعیِ repo) یک جریان ارسال می‌کند و هندلش را برمی‌گرداند. */
    // ✅ پیاده‌سازی مشترک با پارامتر مدت و اینترپولیتور
    private FlowHandle doSend(final Node from,
                              final Node to,
                              double seconds,
                              Interpolator interp) {
        Node realTo = connectedOf(from);
        final Node dst = (realTo != null) ? realTo : to;

        Point2D p1 = nodeCenterInScene(from);
        Point2D p2 = nodeCenterInScene(dst);

        String tag = String.valueOf(from.getUserData()); // "M_TRI" / "M_SQ" / "M_HEX"
        Node sprite = makeSpriteForTag(tag, p1);
        root.getChildren().add(sprite);

        Animation anim = createFlowAnimation(from, dst, sprite, seconds, interp);

        final Node spriteRef = sprite;
        final FlowHandle handle = new FlowHandle(sprite, anim, from, dst);
        activeFlows.add(handle);
        anim.setOnFinished(ev -> {
            root.getChildren().remove(spriteRef);
            activeFlows.remove(handle);
            if (!awaitPorts.isEmpty() && awaitPorts.contains(dst)) {
                reachedPorts.add(dst);
                if (reachedPorts.containsAll(awaitPorts) && awaitPortsCallback != null) {
                    Runnable cb = awaitPortsCallback; awaitPortsCallback = null;
                    Platform.runLater(cb);
                }
            }
        });

        anim.play();
        return handle;
    }

    // ✅ متد public که بقیه‌ی کدها صدا می‌زنند (سازگار با مراحل قبل)
    // ✅ GameController.send
    FlowHandle send(final Node from, final Node to) {
        Node realTo = connectedOf(from);
        final Node dst = (realTo != null) ? realTo : to;

        Point2D p1 = nodeCenterInScene(from);
        Point2D p2 = nodeCenterInScene(dst);

        String tag   = String.valueOf(from.getUserData()); // "M_TRI" / "M_SQ" / "M_HEX"
        Node   sprite= makeSpriteForTag(tag, p1);
        root.getChildren().add(sprite);

        Interpolator interp = (level5Tuning && "M_HEX".equals(tag)) ? Interpolator.EASE_IN : null;
        Animation anim = createFlowAnimation(
                from,
                dst,
                sprite,
                (level5Tuning ? // ← اگر تیون مرحله ۵ داری
                switch (tag) {
                    case "M_SQ"  -> 1.0;    // مربع سریع‌تر
                    case "M_HEX" -> 2.0;    // شتاب‌دار
                    default      -> 2.0;    // مثلث
                        }
                        : 2.0),
                interp);

        // ——— پاکسازی تضمینی
        final Node spriteRef = sprite;
        final Runnable cleanup = () -> {
            if (spriteRef.getParent() != null) root.getChildren().remove(spriteRef);
        };

        // 1) پایان طبیعی انیمیشن
        EventHandler<ActionEvent> baseFinish = e -> {
            cleanup.run();
            // notify برای awaitPorts (اگر استفاده می‌کنی)
            if (!awaitPorts.isEmpty() && awaitPorts.contains(dst)) {
                reachedPorts.add(dst);
                if (reachedPorts.containsAll(awaitPorts) && awaitPortsCallback != null) {
                    Runnable cb = awaitPortsCallback; awaitPortsCallback = null;
                    Platform.runLater(cb);
                }
            }
        };
        anim.setOnFinished(baseFinish);

        // 2) اگر جایی stop شد (یا rate منفی/بازگشتی تمام شد و به STOP رفت) هم پاکسازی کن
        anim.statusProperty().addListener((obs, oldSt, newSt) -> {
            if (newSt == Animation.Status.STOPPED) cleanup.run();
        });

        final FlowHandle handle = new FlowHandle(sprite, anim, from, dst); // (sprite, anim, from, to)
        activeFlows.add(handle);
        anim.statusProperty().addListener((obs2, o2, n2) -> { if (n2 == Animation.Status.STOPPED) { activeFlows.remove(handle); }});
        anim.play();
        return handle;
    }

    private Animation createFlowAnimation(Node from, Node dst, Node sprite, double seconds, Interpolator interp) {
        // If there is a curved overlay with bends, follow its path; otherwise straight translate
        Line line = repo.get(from);
        WireView wv = (line != null) ? wireViews.get(line) : null;
        if (wv != null) {
            // Build path in absolute (scene/root) coordinates; sprite sits at (0,0)
            Path path = wv.buildPathForAnimation();
            // ensure path is in scene via overlay, so coordinates match sprite's parent (root)
            if (!root.getChildren().contains(path.getParent())) {
                // already managed by WireView.attach()
            }
            PathTransition pt = new PathTransition(Duration.seconds(seconds), path, sprite);
            if (interp != null) pt.setInterpolator(interp);
            pt.setOrientation(PathTransition.OrientationType.NONE);
            return pt;
        }
        TranslateTransition tt = new TranslateTransition(Duration.seconds(seconds), sprite);
        Point2D p1 = nodeCenterInScene(from);
        Point2D p2 = nodeCenterInScene(dst);
        tt.setToX(p2.getX() - p1.getX());
        tt.setToY(p2.getY() - p1.getY());
        if (interp != null) tt.setInterpolator(interp);
        return tt;
    }

    // =================== Curved Wire Overlay (per Line) ===================
    private final class WireView {
        private final Line baseLine;
        private final Group overlay = new Group();
        private final java.util.List<Bend> bends = new java.util.ArrayList<>();
        private final java.util.List<QuadCurve> curves = new java.util.ArrayList<>();
        private double accountedLength;
        private double bendRadius = 150.0; // default, will be scaled per-wire length

        WireView(Line baseLine) {
            this.baseLine = baseLine;
            this.accountedLength = lengthOfLine(baseLine);
            overlay.setMouseTransparent(false);
            overlay.setUserData("WIRE_OVERLAY");
            // hide the underlying straight wire for better visuals
            baseLine.setStroke(Color.TRANSPARENT);
            baseLine.setMouseTransparent(true);
            overlay.setOnMouseClicked(ev -> {
                if (ev.isShiftDown()) {
                    tryAddBendAt(new Point2D(ev.getSceneX(), ev.getSceneY()));
                    ev.consume();
                }
            });
            // scale bend radius based on wire length for better UX
            double straightLen = accountedLength;
            bendRadius = Math.max(80.0, Math.min(300.0, straightLen * 0.5));
            rebuild();
        }

        void attach() { root.getChildren().add(overlay); overlay.toFront(); }
        void detach() { root.getChildren().remove(overlay); }

        double getCurrentLength() { return computeCurvedLength(); }

        boolean hasBends() { return !bends.isEmpty(); }

        Path buildPathForAnimation() {
            Path path = new Path();
            java.util.List<QSeg> segs = buildSegments();
            if (segs.isEmpty()) return path;
            QSeg first = segs.get(0);
            path.getElements().add(new MoveTo(first.s.getX(), first.s.getY()));
            for (QSeg sg : segs) {
                path.getElements().add(new QuadCurveTo(sg.c.getX(), sg.c.getY(), sg.e.getX(), sg.e.getY()));
            }
            // Put path directly on root so its coordinates are absolute
            path.setManaged(false);
            path.setMouseTransparent(true);
            path.setVisible(false);
            root.getChildren().add(path);
            return path;
        }

        Path buildPathForAnimationNormalized(Point2D layoutStart) {
            Path path = new Path();
            java.util.List<QSeg> segs = buildSegments();
            if (segs.isEmpty()) return path;
            QSeg first = segs.get(0);
            double ox = layoutStart.getX() - first.s.getX();
            double oy = layoutStart.getY() - first.s.getY();
            path.getElements().add(new MoveTo(first.s.getX() + ox, first.s.getY() + oy));
            for (QSeg sg : segs) {
                path.getElements().add(new QuadCurveTo(sg.c.getX() + ox, sg.c.getY() + oy, sg.e.getX() + ox, sg.e.getY() + oy));
            }
            path.setManaged(false);
            path.setMouseTransparent(true);
            path.setVisible(false);
            root.getChildren().add(path);
            return path;
        }

        void rebuild() {
            overlay.getChildren().clear();
            curves.clear();

            java.util.List<QSeg> segs = buildSegments();
            for (QSeg sg : segs) {
                QuadCurve q = new QuadCurve();
                q.setStartX(sg.s.getX()); q.setStartY(sg.s.getY());
                q.setEndX(sg.e.getX());   q.setEndY(sg.e.getY());
                q.setControlX(sg.c.getX()); q.setControlY(sg.c.getY());
                q.setStrokeWidth(3); q.setFill(null);
                q.setStroke(isCurveIntersectingBodies() ? Color.RED : Color.LIME);
                overlay.getChildren().add(q);
                curves.add(q);
            }

            for (int i = 0; i < bends.size(); i++) {
                Bend bp = bends.get(i);
                Circle hitArea = new Circle(bp.position.getX(), bp.position.getY(), 18, Color.color(1,1,0,0.15));
                hitArea.setStroke(Color.YELLOW);
                hitArea.setStrokeWidth(1.0);
                hitArea.setMouseTransparent(false);
                Circle indicator = new Circle(bp.position.getX(), bp.position.getY(), 6, Color.YELLOW);
                indicator.setStroke(Color.BLACK); indicator.setStrokeWidth(1.5);
                setupBendPointDragging(indicator, hitArea, i);
                overlay.getChildren().addAll(hitArea, indicator);
                bp.hitArea = hitArea;
                bp.indicator = indicator;
            }

            reconcileBudgetAfterChange();
            updateRunEligibility();
        }

        private void updateCurvesOnly() {
            java.util.List<QSeg> segs = buildSegments();
            while (curves.size() < segs.size()) curves.add(new QuadCurve());
            for (int i = 0; i < segs.size(); i++) {
                QSeg sg = segs.get(i);
                QuadCurve q = curves.get(i);
                q.setStartX(sg.s.getX()); q.setStartY(sg.s.getY());
                q.setEndX(sg.e.getX());   q.setEndY(sg.e.getY());
                q.setControlX(sg.c.getX()); q.setControlY(sg.c.getY());
                if (!overlay.getChildren().contains(q)) overlay.getChildren().add(i, q);
            }
            // recolor based on intersection
            boolean bad = isCurveIntersectingBodies();
            for (QuadCurve q : curves) q.setStroke(bad ? Color.RED : Color.LIME);
        }

        boolean isCurveIntersectingBodies() {
            // sample quadratic segments; ignore regions near endpoints to allow connectors inside bodies
            java.util.List<QSeg> segs = buildSegments();
            final int perSeg = 28;
            final double eps = 0.2; // relax to 20% at both ends to avoid false positives
            for (int i = 0; i < segs.size(); i++) {
                QSeg sg = segs.get(i);
                for (int s = 0; s <= perSeg; s++) {
                    double t = (double) s / perSeg;
                    if ((i == 0 && t < eps) || (i == segs.size() - 1 && t > 1.0 - eps)) continue;
                    Point2D p = quadPoint(sg.s, sg.c, sg.e, t);
                    for (Rectangle r : bodies) {
                        Bounds rb = r.localToScene(r.getBoundsInLocal());
                        if (p.getX() >= rb.getMinX() && p.getX() <= rb.getMaxX() && p.getY() >= rb.getMinY() && p.getY() <= rb.getMaxY()) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        private java.util.List<Point2D> samplePoints(int perSeg) {
            java.util.List<QSeg> segs = buildSegments();
            java.util.List<Point2D> out = new java.util.ArrayList<>();
            for (QSeg sg : segs) {
                for (int s = 0; s <= perSeg; s++) {
                    double t = (double)s / perSeg;
                    out.add(quadPoint(sg.s, sg.c, sg.e, t));
                }
            }
            return out;
        }

        private void reconcileBudgetAfterChange() {
            double curved = computeCurvedLength();
            double delta = curved - accountedLength;
            if (Math.abs(delta) < 1e-6) return;
            if (delta > 0) {
                if (wireBudget.consume(delta)) {
                    accountedLength = curved;
                    hud.setRemainingWire(wireBudget.remainingInt());
                    binder.syncFrom(hud);
                } else {
                    // if cannot afford more wire, slightly revert current bends towards original
                    for (Bend b : bends) {
                        b.position = b.position.interpolate(b.original, 0.5);
                    }
                    rebuild();
                }
            } else {
                wireBudget.refund(-delta);
                accountedLength = curved;
                hud.setRemainingWire(wireBudget.remainingInt());
                binder.syncFrom(hud);
            }
        }

        private java.util.List<QSeg> buildSegments() {
            java.util.List<QSeg> segs = new java.util.ArrayList<>();
            Point2D start = new Point2D(baseLine.getStartX(), baseLine.getStartY());
            Point2D end   = new Point2D(baseLine.getEndX(), baseLine.getEndY());
            if (bends.isEmpty()) {
                // single straight fallback as quadratic with midpoint control
                Point2D mid = new Point2D((start.getX()+end.getX())*0.5,(start.getY()+end.getY())*0.5);
                segs.add(new QSeg(start, mid, end));
                return segs;
            }
            if (bends.size() == 1) {
                segs.add(new QSeg(start, bends.get(0).position, end));
                return segs;
            }
            // multiple bends: piecewise quadratics with control at the bend and endpoints at midpoints between adjacent bends
            for (int i = 0; i < bends.size(); i++) {
                Point2D ctrl = bends.get(i).position;
                Point2D segStart = (i == 0) ? start : midpoint(bends.get(i-1).position, bends.get(i).position);
                Point2D segEnd   = (i == bends.size()-1) ? end : midpoint(bends.get(i).position, bends.get(i+1).position);
                segs.add(new QSeg(segStart, ctrl, segEnd));
            }
            return segs;
        }

        private Point2D midpoint(Point2D a, Point2D b) { return new Point2D((a.getX()+b.getX())*0.5,(a.getY()+b.getY())*0.5); }

        private final class QSeg { final Point2D s,c,e; QSeg(Point2D s, Point2D c, Point2D e){ this.s=s; this.c=c; this.e=e; } }

        private double computeCurvedLength() {
            java.util.List<QSeg> segs = buildSegments();
            double total = 0.0;
            for (QSeg sg : segs) total += approximateQuadLength(sg.s, sg.c, sg.e, 80);
            return total;
        }

        private double approximateQuadLength(Point2D s, Point2D c, Point2D e, int samples) {
            if (samples < 2) samples = 2;
            double len = 0.0;
            Point2D prev = s;
            for (int i = 1; i <= samples; i++) {
                double t = (double)i / samples;
                Point2D p = quadPoint(s, c, e, t);
                len += prev.distance(p);
                prev = p;
            }
            return len;
        }

        private Point2D quadPoint(Point2D s, Point2D c, Point2D e, double t) {
            double u = 1.0 - t;
            double x = u*u*s.getX() + 2*u*t*c.getX() + t*t*e.getX();
            double y = u*u*s.getY() + 2*u*t*c.getY() + t*t*e.getY();
            return new Point2D(x, y);
        }

        private void setupBendPointDragging(Circle indicator, Circle hitArea, int bendIndex) {
            final boolean[] dragging = {false};
            Runnable onPressed = () -> dragging[0] = true;
            indicator.setOnMousePressed(e -> { e.consume(); onPressed.run(); });
            hitArea.setOnMousePressed(e -> { e.consume(); onPressed.run(); });

            javafx.event.EventHandler<javafx.scene.input.MouseEvent> onDragged = e -> {
                e.consume(); if (!dragging[0]) return;
                Point2D scenePos = new Point2D(e.getSceneX(), e.getSceneY());
                // overlay is attached to root; scene coords align; convert if needed
                Point2D localPos = scenePos; // root.sceneToLocal(scenePos) would be same base
                Bend bendPoint = bends.get(bendIndex);

                Point2D wireStart = new Point2D(baseLine.getStartX(), baseLine.getStartY());
                Point2D wireEnd   = new Point2D(baseLine.getEndX(), baseLine.getEndY());
                double wireLen = wireStart.distance(wireEnd);
                double maxOffset = Math.min(300.0, Math.max(bendRadius, wireLen * 0.6));

                double vx = wireEnd.getX() - wireStart.getX();
                double vy = wireEnd.getY() - wireStart.getY();
                double denom = vx*vx + vy*vy;
                double t = denom == 0 ? 0.0 : ((localPos.getX() - wireStart.getX()) * vx + (localPos.getY() - wireStart.getY()) * vy) / denom;
                t = Math.max(0.0, Math.min(1.0, t));
                Point2D closest = new Point2D(wireStart.getX() + t * vx, wireStart.getY() + t * vy);
                double dist = closest.distance(localPos);
                Point2D finalPos = dist <= maxOffset ? localPos : new Point2D(
                        closest.getX() + (localPos.getX() - closest.getX()) * (maxOffset / dist),
                        closest.getY() + (localPos.getY() - closest.getY()) * (maxOffset / dist)
                );

                bendPoint.position = finalPos;
                indicator.setCenterX(finalPos.getX()); indicator.setCenterY(finalPos.getY());
                hitArea.setCenterX(finalPos.getX());   hitArea.setCenterY(finalPos.getY());
                updateCurvesOnly();
                reconcileBudgetAfterChange();
                updateRunEligibility();
            };

            javafx.event.EventHandler<javafx.scene.input.MouseEvent> onReleased = e -> { e.consume(); dragging[0] = false; };
            indicator.setOnMouseDragged(onDragged);
            hitArea.setOnMouseDragged(onDragged);
            indicator.setOnMouseReleased(onReleased);
            hitArea.setOnMouseReleased(onReleased);
        }

        private boolean tryAddBendAt(Point2D scenePos) {
            if (bends.size() >= 3) return false;
            if (score.getCoins() <= 0) return false;
            // spend 1 coin for new bend
            score.addCoins(-1);
            hud.setCoins(score.getCoins());
            binder.syncFrom(hud);

            Point2D mid = new Point2D(
                    (baseLine.getStartX() + baseLine.getEndX()) * 0.5,
                    (baseLine.getStartY() + baseLine.getEndY()) * 0.5
            );
            Point2D original = mid;
            // place new bend along perpendicular to the line at mid for a visible default curve
            Point2D start = new Point2D(baseLine.getStartX(), baseLine.getStartY());
            Point2D end   = new Point2D(baseLine.getEndX(), baseLine.getEndY());
            Point2D wireVec = end.subtract(start);
            Point2D n = new Point2D(-wireVec.getY(), wireVec.getX());
            if (n.magnitude() < 1e-6) n = new Point2D(0,1);
            double desired = Math.min(bendRadius * 0.8, Math.max(60.0, accountedLength * 0.25));
            Point2D dir = n.normalize().multiply(desired * (scenePos.getY() >= mid.getY() ? 1 : -1));
            Point2D pos = original.add(dir);
            // avoid overlapping handle positions by nudging if too close to existing
            for (Bend b : bends) {
                if (b.position.distance(pos) < 20) {
                    pos = pos.add(0, Math.signum(pos.getY() - b.position.getY()) * 25);
                    break;
                }
            }
            bends.add(new Bend(original, pos));
            rebuild();
            return true;
        }

        private final class Bend {
            final Point2D original;
            Point2D position;
            Circle indicator;
            Circle hitArea;
            Bend(Point2D original, Point2D position) { this.original = original; this.position = position; }
        }
    }




    void onArrive(FlowHandle f, Runnable r) {
        EventHandler<ActionEvent> prev = f.anim.getOnFinished(); // پاکسازی + awaitPorts
        f.anim.setOnFinished(e -> {
            if (prev != null) prev.handle(e); // اول پاکسازیِ send اجرا شود
            r.run();                          // بعداً کار خودت
        });
    }

    void whenAllArrived(List<FlowHandle> flows, Runnable r) {
        AtomicInteger remain = new AtomicInteger(flows.size());
        for (FlowHandle f : flows) {
            onArrive(f, () -> {
                if (remain.decrementAndGet() == 0) Platform.runLater(r);
            });
        }
    }

    // قبل از شروع ارسال‌ها صدا بزن: هر زمان همه‌ی این پورت‌ها حداقل یک فلو دریافت کنند، r اجرا می‌شود
    void whenAllPortsHaveArrival(List<Node> ports, Runnable r) {
        awaitPorts = new HashSet<>(ports);
        reachedPorts.clear();
        awaitPortsCallback = r;
    }













    // بازگرداندن جریان شش‌ضلعی به مبدأ در صورت برخورد با جریان دیگر
    void enableHexBounceBack(Node hexNode,
                             Node otherNode,
                             PathTransition hexTransition,
                             Runnable onAbort) {

        final double COLLISION_DIST = 12.0;
        final ExplosionPlayer explosion = explosionFactory.get();

        new AnimationTimer() {
            private boolean handled = false;

            @Override public void handle(long now) {
                // اگر یکی حذف شد یا هگزا دیگر در حال حرکت نیست، تمام
                if (hexNode.getScene() == null || otherNode.getScene() == null
                        || hexTransition.getStatus() != Animation.Status.RUNNING) {
                    stop(); return;
                }

                Point2D ca = nodeCenter(hexNode);
                Point2D cb = nodeCenter(otherNode);

                if (!handled && ca.distance(cb) < COLLISION_DIST) {
                    handled = true;

                    // PacketLoss + Impact (در صورت عدم Suppress)
                    score.addPacketLoss(2);
                    Platform.runLater(() -> { hud.setPacketLoss(score.getPacketLoss()); binder.syncFrom(hud); });
                    if (!st.suppressPacketLoss && !st.suppressImpact) {
                        double cx = (ca.getX() + cb.getX()) * 0.5, cy = (ca.getY() + cb.getY()) * 0.5;
                        explosion.explode(cx, cy);
                    }

                    // به‌جای stop روی فرزندِ داخل ParallelTransition، همان انیمیشن را برعکس کن
                    // از همین لحظه با نرخ منفی ادامه می‌دهد و به مبدا برمی‌گردد.
                    double r = hexTransition.getRate();
                    hexTransition.setRate(-Math.abs(r) == 0 ? -1 : -Math.abs(r));
                    // تضمین اجرا (اگر لحظه‌ای Pause شده باشد)
                    if (hexTransition.getStatus() != Animation.Status.RUNNING) hexTransition.play();

                    // اعلام ابورت (تا مرحله‌ی راست هگزا را نپذیرد)
                    if (onAbort != null) onAbort.run();

                    stop();
                }
            }
        }.start();
    }






    private boolean isConnectedSetToSet(java.util.List<? extends Node> from, java.util.List<? extends Node> to) {
        java.util.Set<Node> toSet   = new java.util.HashSet<>(to);
        java.util.Set<Node> seenTo  = new java.util.HashSet<>();
        for (Node f : from) {
            Node other = repo.otherNode(f);
            if (other == null || !toSet.contains(other)) return false;
            seenTo.add(other);
        }
        return seenTo.size() == from.size();
    }

    // --- Stage-ready callback for Level3 ---
    private Runnable stageReadyCallback;
    void setStageReadyCallback(Runnable r) { this.stageReadyCallback = r; }
    // آیا a به b وصل است؟
    boolean isConnected(Node a, Node b) {
        Line l = repo.get(a);
        if (l == null) return false;
        Node other = repo.otherNode(a);
        return other == b;
    }



    void awardCoins(int amount) {
        score.addCoins(amount);
        hud.setCoins(score.getCoins());
        binder.syncFrom(hud);
    }



    void clearPreviews() {
        root.getChildren().removeIf(n -> n.getUserData() != null && "preview".equals(n.getUserData()));
        st.previewNodes.clear();
    }



    // داخل کلاس GameController اضافه کن:

    void openShop() {
        Rectangle shopBox = new Rectangle(250,150,400,400);
        shopBox.setArcWidth(30);
        shopBox.setArcHeight(30);
        shopBox.setFill(Color.web("#663399"));
        shopBox.setStroke(Color.BLACK);
        shopBox.setStrokeWidth(2);

        File crossFile = new File("D:/university/AP/project/Cross.png");
        Image img = new Image(crossFile.toURI().toString());
        ImageView iv = new ImageView(img);
        iv.setFitWidth(20);
        iv.setFitHeight(10);
        iv.setPreserveRatio(true);

        Button closeButton = new Button();
        closeButton.setLayoutX(620);
        closeButton.setLayoutY(160);
        closeButton.setPrefHeight(10);
        closeButton.setPrefWidth(20);
        closeButton.setGraphic(iv);
        closeButton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

        Text shopText = new Text("Shop");
        shopText.setLayoutX(370);
        shopText.setLayoutY(250);
        shopText.setFont(Font.font("Arial", FontWeight.BOLD, 70));
        shopText.setFill(Color.RED);
        DropShadow ds = new DropShadow();
        ds.setOffsetY(3.0);
        ds.setColor(Color.color(0.7, 0, 0));
        Glow glow = new Glow();
        glow.setLevel(0.5);
        Blend blend = new Blend();
        blend.setMode(BlendMode.ADD);
        blend.setBottomInput(ds);
        blend.setTopInput(glow);
        shopText.setEffect(blend);

        Button disableImpact = bigShopButton(" O' Atar\n(3 Coins)", 270, 315, 355, 80, "orange");
        disableImpact.setOnAction(e -> {
            score.addCoins(-3);
            hud.setCoins(score.getCoins());
            binder.syncFrom(hud);
            st.suppressImpact = true;
            PauseTransition pause = new PauseTransition(Duration.seconds(10));
            pause.setOnFinished(ev -> st.suppressImpact = false);
            pause.play();
        });

        Button disablePacketLoss = bigShopButton("O' Airyaman\n  (4 Coins)", 270, 420, 160, 80, "purple");
        disablePacketLoss.setOnAction(e -> {
            score.addCoins(-4);
            hud.setCoins(score.getCoins());
            binder.syncFrom(hud);
            st.suppressPacketLoss = true;
            PauseTransition pausePL = new PauseTransition(Duration.seconds(5));
            pausePL.setOnFinished(ev -> st.suppressPacketLoss = false);
            pausePL.play();
        });

        Button packetLossMakeTo0 = bigShopButton("O' Anahita\n  (5 Coins)", 470, 420, 160, 80, "red");
        packetLossMakeTo0.setOnAction(e -> {
            score.addCoins(-5);
            score.resetPacketLoss();
            hud.setCoins(score.getCoins());
            hud.setPacketLoss(score.getPacketLoss());
            binder.syncFrom(hud);
        });

        Group shopUI = new Group(shopBox, shopText, disableImpact, disablePacketLoss, packetLossMakeTo0, closeButton);
        root.getChildren().add(shopUI);

        closeButton.setOnAction(e -> {
            FadeTransition fade = new FadeTransition(Duration.seconds(0.5), shopUI);
            fade.setFromValue(1.0);
            fade.setToValue(0.0);
            fade.setOnFinished(ev -> root.getChildren().remove(shopUI));
            fade.play();
        });
    }

    private Button bigShopButton(String title, double x, double y, double w, double h, String color) {
        Button b = new Button(title);
        b.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-size: 16px;");
        b.setLayoutX(x);
        b.setLayoutY(y);
        b.setPrefWidth(w);
        b.setPrefHeight(h);
        DropShadow shadow = shadowFactory.get();
        b.setOnMouseEntered(e -> b.setEffect(shadow));
        b.setOnMouseExited(e -> b.setEffect(null));
        return b;
    }






    /* ------- اتصال refs مرحله ------- */
    void bindStageRefs(List<Circle> circles, List<Rectangle> smallRects, Slider timeSlider) {
        this.circles   = Objects.requireNonNull(circles, "circles");
        this.smallRects= Objects.requireNonNull(smallRects, "smallRects");
        this.timeSlider= Objects.requireNonNull(timeSlider, "timeSlider");
    }

    /* ------- درگ سیستم‌ها ------- */
    void enableDragSystem(Rectangle body, List<Node> connectors) {
        bodies.add(body);
        final double[] anchor = new double[2];
        body.setOnMousePressed(e -> {
            anchor[0] = e.getSceneX();
            anchor[1] = e.getSceneY();
            e.consume();
        });
        body.setOnMouseDragged(e -> {
            double dx = e.getSceneX() - anchor[0];
            double dy = e.getSceneY() - anchor[1];
            body.setX(body.getX() + dx);
            body.setY(body.getY() + dy);
            for (Node n : connectors) {
                moveNodeBy(n, dx, dy);
                updateLineEndpointsFor(n);
            }
            anchor[0] = e.getSceneX();
            anchor[1] = e.getSceneY();
            e.consume();
            updateRunEligibility();
        });
    }

    void registerStartButton(Button b) {
        if (b != null && !startButtons.contains(b)) {
            startButtons.add(b);
            updateRunEligibility();
        }
    }
    private void moveNodeBy(Node n, double dx, double dy) {
        if (n instanceof Circle) {
            Circle c = (Circle) n;
            c.setCenterX(c.getCenterX() + dx);
            c.setCenterY(c.getCenterY() + dy);
        } else if (n instanceof Rectangle) {
            Rectangle r = (Rectangle) n;
            r.setX(r.getX() + dx);
            r.setY(r.getY() + dy);
        } else {
            n.setLayoutX(n.getLayoutX() + dx);
            n.setLayoutY(n.getLayoutY() + dy);
        }
    }
    private void updateLineEndpointsFor(Node n) {
        Line line = repo.get(n);
        if (line == null) return;
        Node other = repo.otherNode(n);
        Point2D p1 = nodeCenterInScene(n);
        if (other != null) {
            Point2D p2 = nodeCenterInScene(other);
            line.setStartX(p1.getX()); line.setStartY(p1.getY());
            line.setEndX(p2.getX());   line.setEndY(p2.getY());
        } else {
            line.setStartX(p1.getX()); line.setStartY(p1.getY());
        }
        WireView wv = wireViews.get(line);
        if (wv != null) wv.rebuild();
    }
    public Point2D nodeCenterInScene(Node n) {
        if (n instanceof Circle) {
            Circle c = (Circle) n;
            return c.localToScene(c.getCenterX(), c.getCenterY());
        } else if (n instanceof Rectangle) {
            Rectangle r = (Rectangle) n;
            double cx = r.getX() + r.getWidth()/2;
            double cy = r.getY() + r.getHeight()/2;
            return r.localToScene(cx, cy);
        }
        return n.localToScene(n.getLayoutBounds().getCenterX(), n.getLayoutBounds().getCenterY());
    }

    private double lengthOfLine(Line l) {
        double dx = l.getEndX() - l.getStartX();
        double dy = l.getEndY() - l.getStartY();
        return Math.hypot(dx, dy);
    }

    // Cleanup all connections and curved overlays (called on level change)
    void cleanupConnectionsForLevelChange() {
        for (Line line : repo.allLines()) {
            WireView wv = wireViews.remove(line);
            if (wv != null) {
                double extra = Math.max(0.0, wv.getCurrentLength() - lengthOfLine(line));
                wv.detach();
                if (extra > 0) wireBudget.refund(extra);
            }
        }
        repo.clear(root);
        hud.setRemainingWire(wireBudget.remainingInt());
        binder.syncFrom(hud);
    }

    void resetGuardsContext() {
        bodies.clear();
        startButtons.clear();
    }

    private boolean segmentIntersectsRect(Point2D a, Point2D b, Rectangle r) {
        Bounds rb = r.localToScene(r.getBoundsInLocal());
        double minX = rb.getMinX(), minY = rb.getMinY(), maxX = rb.getMaxX(), maxY = rb.getMaxY();
        // quick reject: both points on one side
        if (a.getX() < minX && b.getX() < minX) return false;
        if (a.getX() > maxX && b.getX() > maxX) return false;
        if (a.getY() < minY && b.getY() < minY) return false;
        if (a.getY() > maxY && b.getY() > maxY) return false;
        // if either point inside
        if (a.getX() >= minX && a.getX() <= maxX && a.getY() >= minY && a.getY() <= maxY) return true;
        if (b.getX() >= minX && b.getX() <= maxX && b.getY() >= minY && b.getY() <= maxY) return true;
        // check segment against rectangle edges
        Point2D tl = new Point2D(minX, minY), tr = new Point2D(maxX, minY);
        Point2D bl = new Point2D(minX, maxY), br = new Point2D(maxX, maxY);
        return segmentsIntersect(a, b, tl, tr) || segmentsIntersect(a, b, tr, br)
                || segmentsIntersect(a, b, br, bl) || segmentsIntersect(a, b, bl, tl);
    }

    private boolean segmentsIntersect(Point2D a1, Point2D a2, Point2D b1, Point2D b2) {
        double d1 = direction(b1, b2, a1);
        double d2 = direction(b1, b2, a2);
        double d3 = direction(a1, a2, b1);
        double d4 = direction(a1, a2, b2);
        if (((d1 > 0 && d2 < 0) || (d1 < 0 && d2 > 0)) && ((d3 > 0 && d4 < 0) || (d3 < 0 && d4 > 0))) return true;
        if (d1 == 0 && onSegment(b1, b2, a1)) return true;
        if (d2 == 0 && onSegment(b1, b2, a2)) return true;
        if (d3 == 0 && onSegment(a1, a2, b1)) return true;
        if (d4 == 0 && onSegment(a1, a2, b2)) return true;
        return false;
    }

    private double direction(Point2D a, Point2D b, Point2D c) {
        return (b.getX() - a.getX()) * (c.getY() - a.getY()) - (b.getY() - a.getY()) * (c.getX() - a.getX());
    }

    private boolean onSegment(Point2D a, Point2D b, Point2D p) {
        return Math.min(a.getX(), b.getX()) <= p.getX() && p.getX() <= Math.max(a.getX(), b.getX())
                && Math.min(a.getY(), b.getY()) <= p.getY() && p.getY() <= Math.max(a.getY(), b.getY());
    }

    private void updateRunEligibility() {
        // disable any visible Stage start buttons if any wire intersects any body
        boolean anyInvalid = false;
        for (Line l : repo.allLines()) {
            WireView wv = wireViews.get(l);
            if (wv == null) continue;
            if (wv.isCurveIntersectingBodies()) { anyInvalid = true; break; }
        }
        for (Button b : startButtons) b.setDisable(anyInvalid);
    }


    // مرکز نود بر اساس BoundsInParent
    private Point2D nodeCenter(Node n) {
        Bounds b = n.getBoundsInParent();
        return new Point2D((b.getMinX() + b.getMaxX()) * 0.5, (b.getMinY() + b.getMaxY()) * 0.5);
    }

    // مثلث متساوی‌الاضلاع حول (0,0) ساخته می‌شود؛ بعداً با setLayoutX/Y روی مرکز می‌نشانیم
    private Polygon triangleMarkerAt(double cx, double cy, double size) {
        double h = size * Math.sqrt(3) / 2.0; // ارتفاع مثلث
        Polygon tri = new Polygon(
                0.0, -h/1.0,        // رأس بالا
                -size/2.0, h/2.0,   // چپ پایین
                size/2.0, h/2.0     // راست پایین
        );
        tri.setFill(Color.YELLOW);
        tri.setStroke(Color.BLACK);
        tri.setStrokeWidth(1);
        tri.setLayoutX(cx);
        tri.setLayoutY(cy);
        return tri;
    }

    // مربع زرد با مرکز r (برای پورت‌های مستطیلی کوچک)
    private Rectangle squareMarkerAtRect(Rectangle r, double size) {
        double cx = r.getX() + r.getWidth()/2.0;
        double cy = r.getY() + r.getHeight()/2.0;
        Rectangle sq = new Rectangle(cx - size/2.0, cy - size/2.0, size, size);
        sq.setFill(Color.YELLOW);
        sq.setStroke(Color.BLACK);
        sq.setStrokeWidth(1);
        return sq;
    }

    // برای ساخت مثلث بر اساس دایره (پورت دایره‌ای)
    private Polygon triangleMarkerAtCircle(Circle c, double size) {
        return triangleMarkerAt(c.getCenterX(), c.getCenterY(), size);
    }




    /* ------- Wiring hooks ------- */
    void onStartWireFromCircle(MouseEvent e) {
        st.startCircle = (Circle) e.getSource();
        st.startSmallRect = null;
        double refunded = 0.0;
        Line existing = repo.get(st.startCircle);
        if (existing != null) {
            WireView wv = wireViews.remove(existing);
            if (wv != null) {
                double extra = Math.max(0.0, wv.getCurrentLength() - lengthOfLine(existing));
                wv.detach();
                if (extra > 0) wireBudget.refund(extra);
            }
            refunded = repo.removeFor(st.startCircle, root);
        }
        if (refunded > 0) {
            wireBudget.refund(refunded);
            hud.setRemainingWire(wireBudget.remainingInt());
            binder.syncFrom(hud);
        }
        updateRunEligibility();
        Point2D p = st.startCircle.localToScene(st.startCircle.getCenterX(), st.startCircle.getCenterY());
        st.currentLine = new Line(p.getX(), p.getY(), e.getSceneX(), e.getSceneY());
        st.currentLine.setStrokeWidth(2);
        root.getChildren().add(st.currentLine);
        e.consume();
    }

    void onStartWireFromSmallRect(MouseEvent e) {
        st.startSmallRect = (Rectangle) e.getSource();
        st.startCircle = null;
        // If an existing connection exists, remove and refund including extra curved length
        double refunded = 0.0;
        Line existing = repo.get(st.startSmallRect);
        if (existing != null) {
            WireView wv = wireViews.remove(existing);
            if (wv != null) {
                double extra = Math.max(0.0, wv.getCurrentLength() - lengthOfLine(existing));
                wv.detach();
                if (extra > 0) wireBudget.refund(extra);
            }
            refunded = repo.removeFor(st.startSmallRect, root);
        }
        if (refunded > 0) {
            wireBudget.refund(refunded);
            hud.setRemainingWire(wireBudget.remainingInt());
            binder.syncFrom(hud);
        }
        updateRunEligibility();
        double cx = st.startSmallRect.getX() + st.startSmallRect.getWidth()/2;
        double cy = st.startSmallRect.getY() + st.startSmallRect.getHeight()/2;
        Point2D p = st.startSmallRect.localToScene(cx, cy);
        st.currentLine = new Line(p.getX(), p.getY(), e.getSceneX(), e.getSceneY());
        st.currentLine.setStrokeWidth(2);
        root.getChildren().add(st.currentLine);
        e.consume();
    }

    void onDragWire(MouseEvent e) {
        if (st.currentLine != null) {
            st.currentLine.setEndX(e.getSceneX());
            st.currentLine.setEndY(e.getSceneY());
        }
    }

    void onReleaseWire(MouseEvent e) {
        if (st.currentLine == null) return;

        Circle    hitCircleTarget = null;
        Rectangle hitRectTarget   = null;

        if (st.startCircle != null) {
            for (Circle target : circles) {
                if (target == st.startCircle) continue;
                Point2D center = target.localToScene(target.getCenterX(), target.getCenterY());
                if (center.distance(e.getSceneX(), e.getSceneY()) <= target.getRadius()) {
                    st.currentLine.setEndX(center.getX());
                    st.currentLine.setEndY(center.getY());
                    hitCircleTarget = target;
                    break;
                }
            }
        } else if (st.startSmallRect != null) {
            Point2D scenePt = new Point2D(e.getSceneX(), e.getSceneY());
            for (Rectangle target : smallRects) {
                if (target == st.startSmallRect) continue;
                double cx = target.getX() + target.getWidth() / 2;
                double cy = target.getY() + target.getHeight() / 2;
                Point2D center = target.localToScene(cx, cy);
                if (target.contains(target.sceneToLocal(scenePt))) {
                    st.currentLine.setEndX(center.getX());
                    st.currentLine.setEndY(center.getY());
                    hitRectTarget = target;
                    break;
                }
            }
        }

        if (hitCircleTarget == null && hitRectTarget == null) {
            root.getChildren().remove(st.currentLine);
        } else {
            // ⬅️ قبل از هر چیز: اگر مقصد سیم قبلی داشت، حذفش کن و ریفاند بده
            double refundedTarget = 0.0;
            if (hitCircleTarget != null) {
                Line old = repo.get(hitCircleTarget);
                if (old != null) {
                    WireView wv = wireViews.remove(old);
                    if (wv != null) {
                        double extra = Math.max(0.0, wv.getCurrentLength() - lengthOfLine(old));
                        wv.detach();
                        if (extra > 0) wireBudget.refund(extra);
                    }
                }
                refundedTarget = repo.removeFor(hitCircleTarget, root);
            } else {
                Line old = repo.get(hitRectTarget);
                if (old != null) {
                    WireView wv = wireViews.remove(old);
                    if (wv != null) {
                        double extra = Math.max(0.0, wv.getCurrentLength() - lengthOfLine(old));
                        wv.detach();
                        if (extra > 0) wireBudget.refund(extra);
                    }
                }
                refundedTarget = repo.removeFor(hitRectTarget, root);
            }
            if (refundedTarget > 0) {
                wireBudget.refund(refundedTarget);
                hud.setRemainingWire(wireBudget.remainingInt());
                binder.syncFrom(hud);
            }

            // حالا مصرف طول سیم جدید
            double dx = st.currentLine.getEndX() - st.currentLine.getStartX();
            double dy = st.currentLine.getEndY() - st.currentLine.getStartY();
            double length = Math.hypot(dx, dy);

            if (!wireBudget.consume(length)) {
                root.getChildren().remove(st.currentLine);
                st.currentLine = null;
                hud.setRemainingWire(wireBudget.remainingInt());
                binder.syncFrom(hud);
                e.consume();
                return;
            }

            // ثبت اتصال جدید
            if (hitCircleTarget != null) {
                repo.putPair(st.startCircle, hitCircleTarget, st.currentLine, root);
            } else {
                repo.putPair(st.startSmallRect, hitRectTarget, st.currentLine, root);
            }

            // attach curved overlay for new line
            WireView wv = new WireView(st.currentLine);
            wireViews.put(st.currentLine, wv);
            wv.attach();
            updateRunEligibility();

            // آپدیت HUD
            hud.setRemainingWire(wireBudget.remainingInt());
            binder.syncFrom(hud);

            // Stage 1 (2 circle + 2 small rect)
            // Stage 1 (2 circle + 2 small rect)
            if (circles.size() == 2) {
                if (hitCircleTarget != null) st.circlesConnected = true;
                if (hitRectTarget   != null) st.rectsConnected   = true;

                if (st.circlesConnected && st.rectsConnected) {
                    timeSlider.setDisable(false);
                    timeSlider.setValue(0);

                    // --- پیش‌نمایش‌ها (به‌جای دایره و مستطیل) ---
                    Polygon previewT1 = triangleMarkerAtCircle(circles.get(0), 12); // مثلث اول
                    Polygon previewT2 = triangleMarkerAtCircle(circles.get(1), 12); // مثلث دوم
                    Rectangle previewSq = squareMarkerAtRect(smallRects.get(0), 12); // مربع

                    previewT1.setVisible(false);
                    previewT2.setVisible(false);
                    previewSq.setVisible(false);



                    root.getChildren().addAll(previewT1, previewT2, previewSq);
                    markAsPreview(previewT1, previewT2, previewSq);

                    // مسیرهای شروع و پایان برای حرکت پیش‌نمایش
                    double c1sx = circles.get(0).getCenterX(), c1sy = circles.get(0).getCenterY();
                    double c1ex = circles.get(1).getCenterX(), c1ey = circles.get(1).getCenterY();

                    double rw  = smallRects.get(0).getWidth(), rh = smallRects.get(0).getHeight();
                    double r0sx = smallRects.get(0).getX() + rw/2,  r0sy = smallRects.get(0).getY() + rh/2;
                    double r0ex = smallRects.get(1).getX() + rw/2,  r0ey = smallRects.get(1).getY() + rh/2;

                    // لیسنر نوار زمان
                    timeSlider.valueProperty().addListener((obs, oldV, newV) -> {
                        double t = newV.doubleValue() / 2.0;

                        if (t <= 0.0001) {
                            previewT1.setVisible(false);
                            previewT2.setVisible(false);
                            previewSq.setVisible(false);
                            return;
                        } else {
                            previewT1.setVisible(true);
                            previewT2.setVisible(true);
                            previewSq.setVisible(true);
                        }


                        // جابه‌جایی مثلث‌ها با LayoutX/Y
                        double tx = c1sx + (c1ex - c1sx) * t;
                        double ty = c1sy + (c1ey - c1sy) * t;
                        previewT1.setLayoutX(tx);
                        previewT1.setLayoutY(ty);
                        previewT2.setLayoutX(tx);
                        previewT2.setLayoutY(ty);

                        // جابه‌جایی مربع حول مرکز
                        double sqcx = r0sx + (r0ex - r0sx) * t;
                        double sqcy = r0sy + (r0ey - r0sy) * t;
                        previewSq.setX(sqcx - previewSq.getWidth()/2.0);
                        previewSq.setY(sqcy - previewSq.getHeight()/2.0);
                    });
                }
            }
            else if (circles.size() == 4) { // Stage 2
                checkStageTwoConnections(hitCircleTarget, hitRectTarget);

                if (st.circle12Connected && st.circle34Connected && st.rect12Connected && st.rect34Connected) {
                    timeSlider.setDisable(false);

                    // --- Preview مرحله ۲: مثلث‌ها (به‌جای دایره) و مربع‌ها (به‌جای مستطیل) ---
                    Polygon previewTri12 = triangleMarkerAtCircle(circles.get(0), 12); // جریان دایره‌ای مسیر 1→2
                    Polygon previewTri34 = triangleMarkerAtCircle(circles.get(2), 12); // جریان دایره‌ای مسیر 3→4
                    Rectangle previewSq12 = squareMarkerAtRect(smallRects.get(0), 12); // جریان مستطیلی مسیر 1→2
                    Rectangle previewSq34 = squareMarkerAtRect(smallRects.get(2), 12); // جریان مستطیلی مسیر 3→4

                    // در ابتدا پنهان باشند (مثل مرحله ۱)
                    previewTri12.setVisible(false);
                    previewTri34.setVisible(false);
                    previewSq12.setVisible(false);
                    previewSq34.setVisible(false);

                    root.getChildren().addAll(previewTri12, previewTri34, previewSq12, previewSq34);
                    markAsPreview(previewTri12, previewTri34, previewSq12, previewSq34);

                    // نقاط شروع/پایان برای دو مسیر دایره‌ای
                    double c12sx = circles.get(0).getCenterX(), c12sy = circles.get(0).getCenterY();
                    double c12ex = circles.get(1).getCenterX(), c12ey = circles.get(1).getCenterY();
                    double c34sx = circles.get(2).getCenterX(), c34sy = circles.get(2).getCenterY();
                    double c34ex = circles.get(3).getCenterX(), c34ey = circles.get(3).getCenterY();

                    // مراکز مستطیل‌های کوچک برای دو مسیر مستطیلی
                    double r12sx = smallRects.get(0).getX() + smallRects.get(0).getWidth()/2,
                            r12sy = smallRects.get(0).getY() + smallRects.get(0).getHeight()/2;
                    double r12ex = smallRects.get(1).getX() + smallRects.get(1).getWidth()/2,
                            r12ey = smallRects.get(1).getY() + smallRects.get(1).getHeight()/2;
                    double r34sx = smallRects.get(2).getX() + smallRects.get(2).getWidth()/2,
                            r34sy = smallRects.get(2).getY() + smallRects.get(2).getHeight()/2;
                    double r34ex = smallRects.get(3).getX() + smallRects.get(3).getWidth()/2,
                            r34ey = smallRects.get(3).getY() + smallRects.get(3).getHeight()/2;

                    // لیسنر نوار زمان برای حرکت Preview + کنترل نمایش/عدم‌نمایش
                    timeSlider.valueProperty().addListener((obs, oldV, newV) -> {
                        double t = newV.doubleValue() / 2.0;

                        // اگر t≈0 باشد اصلاً نمایش نده
                        if (t <= 0.0001) {
                            previewTri12.setVisible(false);
                            previewTri34.setVisible(false);
                            previewSq12.setVisible(false);
                            previewSq34.setVisible(false);
                            return;
                        } else {
                            previewTri12.setVisible(true);
                            previewTri34.setVisible(true);
                            previewSq12.setVisible(true);
                            previewSq34.setVisible(true);
                        }

                        // حرکت مثلث‌ها با LayoutX/Y
                        double tx12 = c12sx + (c12ex - c12sx) * t;
                        double ty12 = c12sy + (c12ey - c12sy) * t;
                        previewTri12.setLayoutX(tx12);
                        previewTri12.setLayoutY(ty12);

                        double tx34 = c34sx + (c34ex - c34sx) * t;
                        double ty34 = c34sy + (c34ey - c34sy) * t;
                        previewTri34.setLayoutX(tx34);
                        previewTri34.setLayoutY(ty34);

                        // حرکت مربع‌ها حول مرکز مسیر
                        double sq12cx = r12sx + (r12ex - r12sx) * t;
                        double sq12cy = r12sy + (r12ey - r12sy) * t;
                        previewSq12.setX(sq12cx - previewSq12.getWidth()/2.0);
                        previewSq12.setY(sq12cy - previewSq12.getHeight()/2.0);

                        double sq34cx = r34sx + (r34ex - r34sx) * t;
                        double sq34cy = r34sy + (r34ey - r34sy) * t;
                        previewSq34.setX(sq34cx - previewSq34.getWidth()/2.0);
                        previewSq34.setY(sq34cy - previewSq34.getHeight()/2.0);
                    });

                }
            }
            if (circles.size() == 6 && smallRects.size() == 6) {
                // چپ: باید c1_sqR ↔ c2_sqL و c1_trR ↔ c2_trL وصل باشند (اینجا فقط یک جفت داریم)
                boolean leftSquaresOK = (repo.otherNode(smallRects.get(0)) == smallRects.get(1)) ||
                        (repo.otherNode(smallRects.get(1)) == smallRects.get(0));
                boolean leftTrisOK    = (repo.otherNode(circles.get(0))    == circles.get(1))    ||
                        (repo.otherNode(circles.get(1))    == circles.get(0));

                // راست: دو خروجی SPYهای وسط (sq:2,3) باید به دو ورودی مربع مقصد (sq:4,5) وصل باشند — ترتیب مهم نیست
                boolean rightSquaresOK = isConnectedSetToSet(
                        java.util.List.of(smallRects.get(2), smallRects.get(3)),
                        java.util.List.of(smallRects.get(4), smallRects.get(5))
                );

                // راست: دو خروجی مثلث SPYهای وسط (tri:2,3) باید به دو ورودی مثلث مقصد (tri:4,5) وصل باشند — ترتیب مهم نیست
                boolean rightTrisOK = isConnectedSetToSet(
                        java.util.List.of(circles.get(2), circles.get(3)),
                        java.util.List.of(circles.get(4), circles.get(5))
                );

                if (leftSquaresOK && leftTrisOK && rightSquaresOK && rightTrisOK) {
                    timeSlider.setDisable(false);
                    timeSlider.setValue(0);
                    if (stageReadyCallback != null) stageReadyCallback.run(); // فعال‌کردن Start از بیرون
                }
            }

        }

        st.currentLine    = null;
        st.startCircle    = null;
        st.startSmallRect = null;
        e.consume();
    }

    private void markAsPreview(Node... nodes) {
        for (Node n : nodes) {
            n.setUserData("preview");
            st.previewNodes.add(n);
        }
    }

    private Circle yellowDot(Circle at) {
        Circle c = new Circle(at.getCenterX(), at.getCenterY(), 5, Color.YELLOW);
        c.setStroke(Color.BLACK);
        return c;
    }
    private Rectangle yellowRect(Rectangle r) {
        double rw = r.getWidth(), rh = r.getHeight();
        Rectangle previewR = new Rectangle(
                r.getX() + rw/2 - rw/4,
                r.getY() + rh/2 - rh/2,
                rw/2, rh
        );
        previewR.setFill(Color.YELLOW);
        previewR.setStroke(Color.BLACK);
        return previewR;
    }
    private Rectangle yellowRectCenter(Rectangle r) {
        Rectangle preview = new Rectangle(
                r.getX() + r.getWidth()/2 - 2,
                r.getY() + r.getHeight()/2 - 5,
                5, 10
        );
        preview.setFill(Color.YELLOW);
        preview.setStroke(Color.BLACK);
        return preview;
    }

    private void checkStageTwoConnections(Circle hitCircle, Rectangle hitRect) {
        if (hitCircle != null) {
            int i1 = circles.indexOf(st.startCircle);
            int i2 = circles.indexOf(hitCircle);
            if ((i1 == 0 && i2 == 1) || (i1 == 1 && i2 == 0)) st.circle12Connected = true;
            if ((i1 == 2 && i2 == 3) || (i1 == 3 && i2 == 2)) st.circle34Connected = true;
        }
        if (hitRect != null) {
            int j1 = smallRects.indexOf(st.startSmallRect);
            int j2 = smallRects.indexOf(hitRect);
            if ((j1 == 0 && j2 == 1) || (j1 == 1 && j2 == 0)) st.rect12Connected = true;
            if ((j1 == 2 && j2 == 3) || (j1 == 3 && j2 == 2)) st.rect34Connected = true;
        }
    }

    /* ---------------- Stage1 Flow (بدون وابستگی به UI) ---------------- */
    void runStage1Flow(Runnable onBothFinished) {
        if (timeSlider.getValue() != 0) return;
        if (st.circlesConnected && st.rectsConnected) {
            Circle c1 = circles.get(0), c2 = circles.get(1);
            Rectangle r1 = smallRects.get(0), r2 = smallRects.get(1);

            // --- Triangle flow along the actual connected wire (twice) ---
            FlowHandle tri1 = send(c1, c2);
            onArrive(tri1, () -> {
                awardCoins(3);
                FlowHandle tri2 = send(c1, c2);
                onArrive(tri2, () -> {
                awardCoins(3);
                st.validSteam++;
                if (st.validSteam == 2) onBothFinished.run();
            });
            });

            // --- Square flow along the actual connected wire (twice) ---
            FlowHandle sq1 = send(r1, r2);
            onArrive(sq1, () -> {
                awardCoins(2);
                FlowHandle sq2 = send(r1, r2);
                onArrive(sq2, () -> {
                    awardCoins(2);
                st.validSteam++;
                if (st.validSteam == 2) onBothFinished.run();
            });
            });
        }
    }


    /* ---------------- Collision / Explosion ---------------- */
    // برخورد با معیار فاصله‌ی مرکز به مرکز (ظاهر نرم مثل قبل)
    void enableSteamCollision(Node a, Node b) {
        final double COLLISION_DIST = 12.0; // آستانه‌ی فاصله؛ در صورت نیاز 10..16 تنظیم کن
        final double BUMP           = 6.0;  // میزان پرش ملایم بعد از برخورد

        ExplosionPlayer explosion = explosionFactory.get();

        AnimationTimer timer = new AnimationTimer() {
            private boolean handled = false;

            @Override public void handle(long now) {
                // اگر یکی از نودها دیگر در صحنه نیست، تایمر را ببند
                if (a.getScene() == null || b.getScene() == null) { stop(); return; }

                // مرکز هندسی (همان متدی که داری)
                Point2D ca = nodeCenter(a);
                Point2D cb = nodeCenter(b);

                if (!handled && ca.distance(cb) < COLLISION_DIST) {
                    handled = true;

                    // +2 packet loss
                    score.addPacketLoss(2);
                    Platform.runLater(() -> {
                        hud.setPacketLoss(score.getPacketLoss());
                        binder.syncFrom(hud);
                    });

                    if (!st.suppressPacketLoss) {
                        // بامپ خیلی ملایم در راستای عمود بر خط اتصال (همان استایل قدیمی)
                        double dx = ca.getX() - cb.getX();
                        double dy = ca.getY() - cb.getY();
                        double len = Math.hypot(dx, dy);
                        double nx = (len == 0) ? 0 :  (dy / len) * BUMP;
                        double ny = (len == 0) ? 0 : -(dx / len) * BUMP;

                        a.getTransforms().add(new Translate(nx, ny));
                        b.getTransforms().add(new Translate(-nx, -ny));

                        // موج impact مثل قبل
                        if (!st.suppressImpact) {
                            double cx = (ca.getX() + cb.getX()) * 0.5;
                            double cy = (ca.getY() + cb.getY()) * 0.5;
                            explosion.explode(cx, cy);
                        }
                    }

                    stop(); // همین یک بار برای این جفت کافی است
                }
            }
        };

        timer.start();
    }




    /* ---------------- Final Win UI (مثل قبل) ---------------- */
    void finalWin(Stage stage, Pane mainRoot, Scene menuscene) {
//        score.addCoins(4);
        hud.setCoins(score.getCoins());
        binder.syncFrom(hud);

        Rectangle winShowRectangle = new Rectangle(270,260,300,180);
        winShowRectangle.setFill(Color.web("#663399"));
        winShowRectangle.setArcWidth(30);
        winShowRectangle.setArcHeight(30);
        mainRoot.getChildren().add(winShowRectangle);

        Text winText = new Text("You Win!");
        winText.setLayoutX(340);
        winText.setLayoutY(320);
        winText.setFont(Font.font("Verdana", FontWeight.BOLD, FontPosture.ITALIC, 32));
        winText.setFill(Color.web("#FFE4E1"));
        DropShadow ds = new DropShadow();
        ds.setOffsetY(3.0);
        ds.setOffsetX(3.0);
        ds.setColor(Color.color(0, 0, 0, 0.4));
        winText.setEffect(ds);
        mainRoot.getChildren().add(winText);

        DropShadow shadow = shadowFactory.get();
        Button backToMenuFromWinBoxButton = new Button("Back");
        backToMenuFromWinBoxButton.setLayoutX(290);
        backToMenuFromWinBoxButton.setLayoutY(365);
        backToMenuFromWinBoxButton.setPrefHeight(35);
        backToMenuFromWinBoxButton.setPrefSize(120, 50);
        backToMenuFromWinBoxButton.setStyle("-fx-background-color: purple; -fx-text-fill: white; -fx-font-size: 16px;");
        backToMenuFromWinBoxButton.setOnMouseEntered(e -> backToMenuFromWinBoxButton.setEffect(shadow));
        backToMenuFromWinBoxButton.setOnMouseExited(e -> backToMenuFromWinBoxButton.setEffect(null));
        backToMenuFromWinBoxButton.setOnAction(actionEvent -> stage.setScene(menuscene));
        mainRoot.getChildren().add(backToMenuFromWinBoxButton);

        Button nextLevelButton = new Button("Next Level");
        nextLevelButton.setLayoutX(420);
        nextLevelButton.setLayoutY(365);
        nextLevelButton.setPrefHeight(35);
        nextLevelButton.setPrefSize(120, 50);
        nextLevelButton.setStyle("-fx-background-color: purple; -fx-text-fill: white; -fx-font-size: 16px;");
        nextLevelButton.setOnMouseEntered(e -> nextLevelButton.setEffect(shadow));
        nextLevelButton.setOnMouseExited(e -> nextLevelButton.setEffect(null));
        mainRoot.getChildren().add(nextLevelButton);
    }

    // Resume a saved flow from progress in [0,1]
    FlowHandle resumeSavedFlow(Node from, Node to, String tag, double progress01, double secondsTotal) {
        Node realTo = connectedOf(from);
        final Node dst = (realTo != null) ? realTo : to;

        Point2D p1 = nodeCenterInScene(from);
        Node sprite = makeSpriteForTag(tag, p1);
        root.getChildren().add(sprite);

        Animation anim = createFlowAnimation(from, dst, sprite, Math.max(0.1, secondsTotal), null);
        try { anim.jumpTo(Duration.seconds(Math.max(0.0, Math.min(1.0, progress01)) * Math.max(0.1, secondsTotal))); } catch (Throwable ignored) {}

        final Node spriteRef = sprite;
        final FlowHandle handle = new FlowHandle(sprite, anim, from, dst);
        activeFlows.add(handle);
        anim.setOnFinished(e -> { if (spriteRef.getParent()!=null) root.getChildren().remove(spriteRef); activeFlows.remove(handle); });
        anim.statusProperty().addListener((obs, o, n) -> { if (n == Animation.Status.STOPPED) { if (spriteRef.getParent()!=null) root.getChildren().remove(spriteRef); activeFlows.remove(handle); }});
        anim.play();
        return handle;
    }

    // Export active flows as packet save data without exposing internal types
    public java.util.List<PacketSaveData> exportActivePackets(java.util.Map<Node, String> nodeToId) {
        java.util.List<PacketSaveData> out = new java.util.ArrayList<>();
        for (FlowHandle fh : activeFlows) {
            PacketSaveData ps = new PacketSaveData();
            ps.tag = String.valueOf(fh.sprite.getUserData());
            ps.fromId = nodeToId.getOrDefault(fh.from, null);
            ps.toId = nodeToId.getOrDefault(fh.to, null);
            if (fh.anim instanceof javafx.animation.Transition) {
                javafx.animation.Transition t = (javafx.animation.Transition) fh.anim;
                double total = Math.max(0.0001, t.getCycleDuration().toSeconds());
                ps.secondsTotal = total;
                ps.progress01 = Math.min(1.0, Math.max(0.0, t.getCurrentTime().toSeconds() / total));
            } else {
                ps.secondsTotal = 1.0;
                ps.progress01 = 0.0;
            }
            out.add(ps);
        }
        return out;
    }
}

/* =========================================================
 *                   LEVEL API + MANAGER
 * ========================================================= */
interface Level {
    LevelView build();                                    // ساخت گروه نودهای مرحله
    void bind(GameController c, Slider timeSlider, Runnable onWin);   // بایند رویدادها و Start
    LevelView getView();                                  // برای دسترسی به نودها
    default void dispose() {}                             // پاکسازی اختصاصی در صورت نیاز
}

final class LevelView {
    final Group group;
    final List<Rectangle> bodies;
    final List<Circle> circles;
    final List<Rectangle> smallRects;
    final Button startButton;

    LevelView(Group group, List<Rectangle> bodies, List<Circle> circles, List<Rectangle> smallRects, Button startButton) {
        this.group = group;
        this.bodies = bodies;
        this.circles = circles;
        this.smallRects = smallRects;
        this.startButton = startButton;
    }
}

final class LevelManager {
    private final Stage stage;
    private final Pane root;
    private final Group levelLayer;

    private final GameController controller;
    private final Slider timeSlider;
    private final WireBudgetService wireBudget;
    private final ScoreService score;
    private final HUDModel hud;
    private final HUDBinder binder;
    private final ConnectionRepo repo;
    private final GameState st;
    private final Scene menuScene;

    private final Map<Integer, Level> levels = new HashMap<>();
    private Level current;
    private int currentId = -1;
    
    // Save/Load managers
    private final SaveManager saveManager;
    private final AutosaveManager autosaveManager;

    LevelManager(Stage stage, Pane root, Group levelLayer,
                 GameController controller, Slider timeSlider,
                 WireBudgetService wireBudget, ScoreService score, HUDModel hud, HUDBinder binder,
                 ConnectionRepo repo, GameState st, Scene menuScene) {
        this.stage=stage; this.root=root; this.levelLayer=levelLayer;
        this.controller=controller; this.timeSlider=timeSlider;
        this.wireBudget=wireBudget; this.score=score; this.hud=hud; this.binder=binder;
        this.repo=repo; this.st=st; this.menuScene=menuScene;
        
        // Initialize save managers
        this.saveManager = new SaveManager();
        this.autosaveManager = new AutosaveManager(saveManager);
    }

    void register(int id, Level level) { levels.put(id, level); }

    void goTo(int id) {
        unmountCurrent();

        Level lvl = levels.get(id);
        if (lvl == null) return;
        current = lvl; currentId = id;
        // Persist current level id in HUD/state for snapshot
        try { st.setCurrentLevelNumber(id); } catch (Throwable ignored) {}
        
        String levelId = "level-" + id;
        String profileId = new PlayerManager().getActiveProfileId();
        
        // Load normal save if present
        Optional<SaveData> normalSave = saveManager.loadGame(profileId, levelId);

        // Check for crash autosave and prompt ONLY if it's newer than normal save
        Optional<SaveData> autosave = autosaveManager.checkForAutosave(levelId);
        boolean shouldOfferResume = false;
        if (autosave.isPresent()) {
            long autoTs = autosave.get().savedAtEpochMillis;
            long normalTs = normalSave.map(s -> s.savedAtEpochMillis).orElse(0L);
            if (autoTs > normalTs + 250) { // newer by >250ms => likely crash
                shouldOfferResume = true;
            } else {
                // Autosave stale or redundant -> remove silently
                autosaveManager.deleteAutosave(levelId);
            }
        }

        if (shouldOfferResume) {
            boolean resume = autosaveManager.showResumePrompt(autosave.get());
            if (resume) {
                loadLevelWithSave(lvl, autosave.get(), true);
                autosaveManager.deleteAutosave(levelId);
                return;
            } else {
                autosaveManager.deleteAutosave(levelId);
            }
        }

        if (normalSave.isPresent()) {
            loadLevelWithSave(lvl, normalSave.get(), false);
        } else {
            startFreshLevel(lvl);
        }
    }
    
    private void loadLevelWithSave(Level lvl, SaveData save, boolean isAutosave) {
        // Apply basic state before creating view
        saveManager.applyBasicState(save, st, wireBudget, score);
        
        LevelView v = lvl.build();
        levelLayer.getChildren().add(v.group);
        
        controller.bindStageRefs(v.circles, v.smallRects, timeSlider);
        controller.registerStartButton(v.startButton);
        
        // Create node ID map for restoration
        Map<String, Node> nodeMap = createNodeMap(v, save.levelData);
        
        // Restore connections after view exists
        saveManager.restoreConnections(save, repo, controller, root, nodeMap);
        
        // Bind level logic
        lvl.bind(controller, timeSlider, () -> showBetweenWin(currentId + 1));

        // Refresh HUD to match restored state
        try {
            hud.setRemainingWire(wireBudget.remainingInt());
            hud.setPacketLoss(score.getPacketLoss());
            hud.setCoins(score.getCoins());
            hud.setTimeValue(st.getCurrentTime());
            binder.syncFrom(hud);
        } catch (Throwable ignored) {}
        
        // If autosave, show frozen preview
        if (isAutosave) {
            autosaveManager.showFrozenPreview(stage.getScene(), save, () -> {
                // Start autosave after preview
                startAutosave();
            });
        } else {
            startAutosave();
        }

        // If game was already started, resume flow generation
        try {
            if (save.levelData != null && save.levelData.gameStarted) {
                boolean hasPackets = (save.levelData.packets != null && !save.levelData.packets.isEmpty());
                
                // For level 1: check if we need to continue sending flows
                if (currentId == 1 && save.levelData.flowState != null) {
                    FlowStateSave fs = save.levelData.flowState;
                    // If we haven't completed both flows and no packets are mid-flight, continue
                    if (st.validSteam < 2 && !hasPackets && v.startButton != null) {
                        // Resume flow generation by firing start button
                        v.startButton.fire();
                    }
                } else if (!hasPackets && v.startButton != null) {
                    // For other levels, auto-fire start if no packets mid-flight
                    v.startButton.fire();
                }
            }
        } catch (Throwable ignored) {}
    }
    
    private void startFreshLevel(Level lvl) {
        LevelView v = lvl.build();
        levelLayer.getChildren().add(v.group);

        controller.bindStageRefs(v.circles, v.smallRects, timeSlider);
        controller.registerStartButton(v.startButton);

        // فعال‌سازی درگ هر سیستم
        for (int i=0;i<v.bodies.size();i++) {
            Rectangle body = v.bodies.get(i);
            List<Node> connectors = new ArrayList<>();
            // پورت‌ها و دکمه‌ی Start هم جزء کانکتورها اضافه شوند اگر کنار آن بدنه هستند
            // (این را Level داخل bind خودش مشخص می‌کند؛ اینجا فقط پشتیبانی عمومی داریم)
        }

        lvl.bind(controller, timeSlider, () -> showBetweenWin(currentId + 1));
        
        // Start autosave
        startAutosave();
    }
    
    private Map<String, Node> createNodeMap(LevelView v, LevelSaveData saveData) {
        Map<String, Node> nodeMap = new HashMap<>();
        
        // Map circles by index
        for (NodeSaveData nodeData : saveData.nodes) {
            if ("CIRCLE".equals(nodeData.type) && nodeData.index < v.circles.size()) {
                nodeMap.put(nodeData.id, v.circles.get(nodeData.index));
            } else if ("SMALL_RECT".equals(nodeData.type) && nodeData.index < v.smallRects.size()) {
                nodeMap.put(nodeData.id, v.smallRects.get(nodeData.index));
            }
        }
        
        return nodeMap;
    }
    
    private void startAutosave() {
        if (currentId > 0) {
            String levelId = "level-" + currentId;
            // More precise live-save: every 1.0 second
            autosaveManager.startAutosave(levelId, st, wireBudget, score, repo, controller, 1.0);
        }
    }

    // Save current level if running (used on app close)
    void forceSaveIfPlaying() {
        if (currentId > 0 && current != null) {
            String levelId = "level-" + currentId;
            String profileId = new PlayerManager().getActiveProfileId();
            saveManager.saveGame(profileId, levelId, st, wireBudget, score, repo, controller);
            autosaveManager.stopAutosave();
            // Graceful shutdown: remove autosave so no crash prompt next launch
            autosaveManager.deleteAutosave(levelId);
        }
    }

    private void unmountCurrent() {
        // Save current level before unmounting
        if (currentId > 0 && current != null) {
            String levelId = "level-" + currentId;
            String profileId = new PlayerManager().getActiveProfileId();
            saveManager.saveGame(profileId, levelId, st, wireBudget, score, repo, controller);
            autosaveManager.stopAutosave();
            autosaveManager.deleteAutosave(levelId); // Clean up autosave when switching levels
        }
        
        // پاکسازی UI مرحله‌ی قبل
        controller.cleanupConnectionsForLevelChange();
        root.getChildren().removeIf(n -> n.getUserData() != null && "preview".equals(n.getUserData()));
        st.previewNodes.clear();

        // reset guards context
        controller.resetGuardsContext();

        if (current != null && current.getView() != null) {
            levelLayer.getChildren().remove(current.getView().group);
            current.dispose();
        }

        // ریست وضعیت مرحله
        st.circlesConnected = false;
        st.rectsConnected = false;
        st.validSteam = 0;
        st.circle12Connected = st.circle34Connected = false;
        st.rect12Connected   = st.rect34Connected   = false;

        // ریست تایم و وایر
        timeSlider.setDisable(true);
        timeSlider.setValue(0);
        wireBudget.resetTo(1000);
        hud.setRemainingWire(wireBudget.remainingInt());
        binder.syncFrom(hud);
    }

    /**
     * Start a new game by clearing all saves and starting from level 1
     */
    void startNewGame() {
        // Clear all saves for the current profile
        String profileId = new PlayerManager().getActiveProfileId();
        List<String> savedLevels = saveManager.listSavedLevels(profileId);
        
        for (String levelId : savedLevels) {
            saveManager.deleteSave(profileId, levelId);
        }
        
        // Clear all autosaves as well
        autosaveManager.deleteAllAutosaves();
        
        // Show starting message
        Platform.runLater(() -> {
            Alert startAlert = new Alert(Alert.AlertType.INFORMATION);
            startAlert.setTitle("New Game Started");
            startAlert.setHeaderText("Starting Fresh!");
            startAlert.setContentText("All previous saves have been cleared.\nStarting new game from Level 1.");
            startAlert.show();
            
            // Auto-close after 2 seconds
            Timeline autoClose = new Timeline(new KeyFrame(Duration.seconds(2), e -> startAlert.close()));
            autoClose.play();
        });
        
        // Start from level 1 fresh
        goToFresh(1);
    }

    /**
     * Go to a level without loading any saves (force fresh start)
     */
    void goToFresh(int id) {
        unmountCurrent();

        Level lvl = levels.get(id);
        if (lvl == null) return;
        current = lvl; currentId = id;
        // Persist current level id in HUD/state for snapshot
        try { st.setCurrentLevelNumber(id); } catch (Throwable ignored) {}
        
        // Start fresh regardless of existing saves
        startFreshLevel(lvl);
    }

    /* بین مرحله 1 و 2 همان پنجره‌ی Win قبلی با Next Level */
    private void showBetweenWin(int nextId){
        // Save immediately when completing a level so next level is remembered
        if (currentId > 0 && current != null) {
            String levelId = "level-" + currentId;
            String profileId = new PlayerManager().getActiveProfileId();
            saveManager.saveGame(profileId, levelId, st, wireBudget, score, repo, controller);
        }
        
//        score.addCoins(4);
        hud.setCoins(score.getCoins());
        binder.syncFrom(hud);

        Rectangle box = new Rectangle(270,260,300,180);
        box.setFill(Color.web("#663399"));
        box.setArcWidth(30); box.setArcHeight(30);

        Text t = new Text("You Win!");
        t.setLayoutX(340); t.setLayoutY(320);
        t.setFont(Font.font("Verdana", FontWeight.BOLD, FontPosture.ITALIC, 32));
        t.setFill(Color.web("#FFE4E1"));
        DropShadow ds = new DropShadow();
        ds.setOffsetY(3.0); ds.setOffsetX(3.0);
        ds.setColor(Color.color(0,0,0,0.4));
        t.setEffect(ds);

        Button back = new Button("Back");
        back.setLayoutX(290); back.setLayoutY(365);
        back.setPrefSize(120,50);
        back.setStyle("-fx-background-color: purple; -fx-text-fill: white; -fx-font-size: 16px;");
        Button next = new Button("Next Level");
        next.setLayoutX(420); next.setLayoutY(365);
        next.setPrefSize(120,50);
        next.setStyle("-fx-background-color: purple; -fx-text-fill: white; -fx-font-size: 16px;");

        Group g = new Group(box,t,back,next);
        root.getChildren().add(g);

        back.setOnAction(e -> stage.setScene(menuScene));
        next.setOnAction(e -> {
            root.getChildren().remove(g);
            goTo(nextId);
        });
    }

    /* مرحله‌ی آخر: از خود Controller برای پنجره‌ی نهایی استفاده می‌کنیم */
    void showFinalWin(){
        controller.finalWin(stage, root, menuScene);
    }
}

/* =========================================================
 *                      LEVELS
 * ========================================================= */
final class Level1 implements Level {
    private LevelView view;

    @Override public LevelView build() {
        // سیستم‌ها
        Rectangle mainrect1  = Shapes.rect(200, 250, 100, 200, Color.CORAL);
        Circle    circle1    = Shapes.circle(300, 290, 10, Color.LIGHTGREEN, Color.DARKGREEN, 3);
        Rectangle littlerec1 = Shapes.smallRect(295, 370);

        Rectangle mainrect2  = Shapes.rect(500, 250, 100, 200, Color.CORAL);
        Circle    circle2    = Shapes.circle(500, 290, 10, Color.LIGHTGREEN, Color.DARKGREEN, 3);
        Rectangle littlerec2 = Shapes.smallRect(495, 370);

        Button start = new Button("Start");
        start.setLayoutX(230);
        start.setLayoutY(240);
        start.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #FF6F61, #D7263D);" +
                        "-fx-text-fill: white; -fx-background-radius: 10;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.75), 4, 0, 0, 2);"
        );

        Group g = new Group(mainrect1, circle1, littlerec1,
                mainrect2, circle2, littlerec2,
                start);
        view = new LevelView(
                g,
                List.of(mainrect1, mainrect2),
                List.of(circle1, circle2),
                List.of(littlerec1, littlerec2),
                start
        );
        return view;
    }

    @Override public void bind(GameController c, Slider timeSlider, Runnable onWin) {
        // اتصال refs
        c.bindStageRefs(view.circles, view.smallRects, timeSlider);

        // درگ سیستم‌ها (Start به سیستم چپ چسبیده باشد)
        c.enableDragSystem(view.bodies.get(0), List.of(view.circles.get(0), view.smallRects.get(0), view.startButton));
        c.enableDragSystem(view.bodies.get(1), List.of(view.circles.get(1), view.smallRects.get(1)));

        // سیم‌کشی پورت‌ها
        for (Circle cc : view.circles)       cc.setOnMousePressed(c::onStartWireFromCircle);
        for (Rectangle r : view.smallRects)  r.setOnMousePressed(c::onStartWireFromSmallRect);

        // Start → همان انیمیشن قبلی؛ فقط بجای رفتن مستقیم به مرحله ۲، onWin صدا زده می‌شود
        view.startButton.setOnAction(e -> c.runStage1Flow(onWin));
    }

    @Override public LevelView getView() { return view; }
}

final class Level2 implements Level {
    private LevelView view;

    @Override public LevelView build() {
        Button start = new Button("Start");
        start.setLayoutX(230);
        start.setLayoutY(240);
        start.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #FF6F61, #D7263D);" +
                        "-fx-text-fill: white; -fx-background-radius: 10;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.75), 4, 0, 0, 2);"
        );

        Rectangle mainrect1 = Shapes.mkRect(200, 250); // بدنه‌ی چپ
        Circle    circle1   = Shapes.mkCircle(300, 290);
        Rectangle rect1     = Shapes.mkSmallRect(295, 370);

        Rectangle mainrect2 = Shapes.mkRect(380, 380); // بدنه‌ی وسط
        Circle    circle2   = Shapes.mkCircle(380, 420);
        Rectangle rect2     = Shapes.mkSmallRect(375, 500);
        Rectangle rect3     = Shapes.mkSmallRect(475, 410);
        Circle    circle3   = Shapes.mkCircle(480, 510);

        Rectangle mainrect3 = Shapes.mkRect(610, 250); // بدنه‌ی راست
        Circle    circle4   = Shapes.mkCircle(610, 290);
        Rectangle rect4     = Shapes.mkSmallRect(605, 370);

        Group g = new Group(
                mainrect1, mainrect2, mainrect3,
                circle1, rect1,
                circle2, rect2, circle3, rect3,
                circle4, rect4,
                start
        );
        view = new LevelView(
                g,
                List.of(mainrect1, mainrect2, mainrect3),
                List.of(circle1, circle2, circle3, circle4),
                List.of(rect1, rect2, rect3, rect4),
                start
        );
        return view;
    }

    @Override public void bind(GameController c, Slider timeSlider, Runnable onWin) {
        // refs
        c.bindStageRefs(view.circles, view.smallRects, timeSlider);

        // درگ سیستم‌ها (Start همراه بدنه‌ی چپ جابه‌جا شود)
        c.enableDragSystem(view.bodies.get(0), List.of(view.circles.get(0), view.smallRects.get(0), view.startButton));
        c.enableDragSystem(view.bodies.get(1), List.of(view.circles.get(1), view.smallRects.get(1), view.circles.get(2), view.smallRects.get(2)));
        c.enableDragSystem(view.bodies.get(2), List.of(view.circles.get(3), view.smallRects.get(3)));

        // سیم‌کشی
        for (Circle cc : view.circles)       cc.setOnMousePressed(c::onStartWireFromCircle);
        for (Rectangle r : view.smallRects)  r.setOnMousePressed(c::onStartWireFromSmallRect);

        // Start → همان منطق قبلی مرحله ۲ (دو مسیر دایره‌ها و دو مسیر مستطیل‌ها)
        view.startButton.setOnAction(evt -> {
            // باید هر چهار اتصال برقرار باشد و تایم‌اسلایدر روی 0 باشد
            if (timeSlider.getValue() != 0) return;

            // مسیرها (دایره‌ای‌ها)
            Point2D p1 = view.circles.get(0).localToScene(view.circles.get(0).getCenterX(), view.circles.get(0).getCenterY());
            Point2D p2 = view.circles.get(1).localToScene(view.circles.get(1).getCenterX(), view.circles.get(1).getCenterY());
            Point2D p3 = view.circles.get(2).localToScene(view.circles.get(2).getCenterX(), view.circles.get(2).getCenterY());
            Point2D p4 = view.circles.get(3).localToScene(view.circles.get(3).getCenterX(), view.circles.get(3).getCenterY());

            Line pathCircle12 = new Line(p1.getX(), p1.getY(), p2.getX(), p2.getY());
            Line pathCircle34 = new Line(p3.getX(), p3.getY(), p4.getX(), p4.getY());

            // مسیرها (مستطیلی‌ها)
            Point2D r1 = Shapes.centerOf(view.smallRects.get(0));
            Point2D r2 = Shapes.centerOf(view.smallRects.get(1));
            Point2D r3 = Shapes.centerOf(view.smallRects.get(2));
            Point2D r4 = Shapes.centerOf(view.smallRects.get(3));

            Line pathRect12 = new Line(r1.getX(), r1.getY(), r2.getX(), r2.getY());
            Line pathRect34 = new Line(r3.getX(), r3.getY(), r4.getX(), r4.getY());

            // --- جریان‌ها: جایگزینی Circle→Polygon(مثلث) و Rect باریک→مربع ---
            Polygon steamTri = new Polygon();
            {
                double size = 12;
                double h = size * Math.sqrt(3) / 2.0;
                steamTri.getPoints().addAll(
                        0.0, -h,
                        -size/2.0, h/2.0,
                        size/2.0,  h/2.0
                );
                steamTri.setFill(Color.YELLOW);
                steamTri.setStroke(Color.BLACK);
                steamTri.setStrokeWidth(1);
            }

            Rectangle steamSquare = new Rectangle(12, 12, Color.YELLOW);
            steamSquare.setStroke(Color.BLACK);
            steamSquare.setStrokeWidth(1);

            view.group.getChildren().addAll(steamTri, steamSquare);

            // دایره‌ای‌ها: دو مسیر پشت‌سرهم برای مثلث
            PathTransition pt1 = new PathTransition(Duration.seconds(2), pathCircle12, steamTri);
            PathTransition pt2 = new PathTransition(Duration.seconds(2), pathCircle34, steamTri);
            SequentialTransition seqCircle = new SequentialTransition(pt1, pt2);

            seqCircle.setOnFinished(e -> c.awardCoins(3));


            // مستطیلی‌ها: دو مسیر پشت‌سرهم برای مربع
            PathTransition pr1 = new PathTransition(Duration.seconds(2), pathRect12, steamSquare);
            PathTransition pr2 = new PathTransition(Duration.seconds(2), pathRect34, steamSquare);
            SequentialTransition seqRect = new SequentialTransition(pr1, pr2);

            seqRect.setOnFinished(e -> c.awardCoins(2));


            // اجرای همزمان و تکرار دوباره
            ParallelTransition all = new ParallelTransition(seqCircle, seqRect);
            all.setCycleCount(2);
            all.setOnFinished(e -> {
                view.group.getChildren().removeAll(steamTri, steamSquare);
                onWin.run(); // LevelManager.showFinalWin()
            });
            all.play();

            // برخورد
            c.enableSteamCollision(steamTri, steamSquare);
        });
    }

    @Override public LevelView getView() { return view; }
}


final class Level3 implements Level {
    private LevelView view;

    // مختصات و ابعاد کارت‌ها بر اساس test
    private static final double W = 140, H = 180;

    @Override
    public LevelView build() {
        Group g = new Group();

        // بدنه‌ها (۵ کارت)
        Rectangle c1 = Shapes.rect( 80, 230, W, H, Color.web("#1F4733"));  // چپ‌ترین
        c1.setStroke(Color.web("#3BF07B"));  c1.setStrokeWidth(3);

        Rectangle c2 = Shapes.rect(300, 230, W, H, Color.web("#2C2A2A"));  // دوم از چپ
        c2.setStroke(Color.web("#FF3B3B"));  c2.setStrokeWidth(3);

        Rectangle c3 = Shapes.rect(560, 120, W, H, Color.web("#2C2A2A"));  // بالایی وسط
        c3.setStroke(Color.web("#FF3B3B"));  c3.setStrokeWidth(3);

        Rectangle c4 = Shapes.rect(560, 340, W, H, Color.web("#2C2A2A"));  // پایینی وسط
        c4.setStroke(Color.web("#FF3B3B"));  c4.setStrokeWidth(3);

        Rectangle c5 = Shapes.rect(760, 230, W, H, Color.web("#3A2F2F"));  // راست‌ترین
        c5.setStroke(Color.web("#FF7FB2"));  c5.setStrokeWidth(3);

        // helper برای ساخت پورت مربعی سمت راست/چپ کارت
        java.util.function.BiFunction<Rectangle, Double, Rectangle> rightSquare =
                (card, oy) -> {
                    double x = card.getX() + card.getWidth() - 10; // بیرون لبه راست
                    double y = card.getY() + oy - 9;
                    Rectangle r = new Rectangle(x, y, 18, 18);
                    r.setArcWidth(4); r.setArcHeight(4);
                    r.setFill(Color.WHITE);
                    r.setStroke(Color.web("#FF93AA")); r.setStrokeWidth(2);
                    return r;
                };
        java.util.function.BiFunction<Rectangle, Double, Rectangle> leftSquare =
                (card, oy) -> {
                    double x = card.getX() - 18 - 10; // بیرون لبه چپ
                    double y = card.getY() + oy - 9;
                    Rectangle r = new Rectangle(x, y, 18, 18);
                    r.setArcWidth(4); r.setArcHeight(4);
                    r.setFill(Color.WHITE);
                    r.setStroke(Color.web("#FF93AA")); r.setStrokeWidth(2);
                    return r;
                };

        // helper برای پورت مثلثی → hit-target باید Circle بماند (برای سیم‌کشی)
        java.util.function.Function<javafx.geometry.Point2D, Circle> triHit =
                p -> {
                    Circle cc = new Circle(p.getX(), p.getY(), 10);
                    cc.setFill(Color.LIGHTGREEN);
                    cc.setStroke(Color.DARKGREEN);
                    cc.setStrokeWidth(3);
                    return cc;
                };

        // مختصات مرکز پورت مثلث سمت راست/چپ کارت
        java.util.function.BiFunction<Rectangle, Double, javafx.geometry.Point2D> rightTriCenter =
                (card, oy) -> new javafx.geometry.Point2D(card.getX() + card.getWidth() + 8, card.getY() + oy);
        java.util.function.BiFunction<Rectangle, Double, javafx.geometry.Point2D> leftTriCenter =
                (card, oy) -> new javafx.geometry.Point2D(card.getX() - 8, card.getY() + oy);

        // --- پورت‌های کارت‌ها بر اساس test ---
        // c1: RIGHT → square @ (90-10), triangle @ (120-10)
        Rectangle c1_sqR = rightSquare.apply(c1, 80.0);
        Circle    c1_trR = triHit.apply(rightTriCenter.apply(c1, 110.0));

        // c2: LEFT → square @ (90-10), triangle @ (120-10)
        Rectangle c2_sqL = leftSquare.apply(c2, 80.0);
        Circle    c2_trL = triHit.apply(leftTriCenter.apply(c2, 110.0));

        // c3: RIGHT → square, triangle
        Rectangle c3_sqR = rightSquare.apply(c3, 80.0);
        Circle    c3_trR = triHit.apply(rightTriCenter.apply(c3, 110.0));

        // c4: RIGHT → square, triangle
        Rectangle c4_sqR = rightSquare.apply(c4, 80.0);
        Circle    c4_trR = triHit.apply(rightTriCenter.apply(c4, 110.0));

        // c5: LEFT ×4 → square, triangle, square, triangle
        Rectangle c5_sqL1 = leftSquare.apply(c5, 60.0);
        Circle    c5_trL1 = triHit.apply(leftTriCenter.apply(c5, 85.0));
        Rectangle c5_sqL2 = leftSquare.apply(c5, 110.0);
        Circle    c5_trL2 = triHit.apply(leftTriCenter.apply(c5, 135.0));

        // دکمه Start مرحله (وسطِ بالای c1)
        Button start = new Button("Start");
        start.setPrefSize(70, 30);
        start.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #FF6F61, #D7263D);" +
                        "-fx-text-fill: white; -fx-background-radius: 10;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.75), 4, 0, 0, 2);"
        );
        double startX = c1.getX() + (c1.getWidth() - start.getPrefWidth()) / 2.0;
        double startY = c1.getY() - start.getPrefHeight() - 8.0;
        start.setLayoutX(startX);
        start.setLayoutY(startY + 20); // همون جابجایی که دادی

        // --- برچسب‌های SPY (چسبیده به مرکز بدنه‌ها) ---
        Text spy2 = new Text("SPY");
        spy2.setFill(Color.WHITE);
        spy2.setStyle("-fx-font-size: 26px; -fx-font-weight: bold;");
        spy2.xProperty().bind(c2.xProperty().add(c2.getWidth()/2.0).subtract(18));
        spy2.yProperty().bind(c2.yProperty().add(c2.getHeight()/2.0).add(9));

        Text spy3 = new Text("SPY");
        spy3.setFill(Color.WHITE);
        spy3.setStyle("-fx-font-size: 26px; -fx-font-weight: bold;");
        spy3.xProperty().bind(c3.xProperty().add(c3.getWidth()/2.0).subtract(18));
        spy3.yProperty().bind(c3.yProperty().add(c3.getHeight()/2.0).add(9));

        Text spy4 = new Text("SPY");
        spy4.setFill(Color.WHITE);
        spy4.setStyle("-fx-font-size: 26px; -fx-font-weight: bold;");
        spy4.xProperty().bind(c4.xProperty().add(c4.getWidth()/2.0).subtract(18));
        spy4.yProperty().bind(c4.yProperty().add(c4.getHeight()/2.0).add(9));

        // نامرئی ولی قابل پیک (رویداد ماوس می‌گیرن)
        c1_trR.setOpacity(0.0);
        c2_trL.setOpacity(0.0);
        c3_trR.setOpacity(0.0);
        c4_trR.setOpacity(0.0);
        c5_trL1.setOpacity(0.0);
        c5_trL2.setOpacity(0.0);

        // محض اطمینان
        c1_trR.setMouseTransparent(false);
        c2_trL.setMouseTransparent(false);
        c3_trR.setMouseTransparent(false);
        c4_trR.setMouseTransparent(false);
        c5_trL1.setMouseTransparent(false);
        c5_trL2.setMouseTransparent(false);

        // --- مثلث‌های نمایشی چسبیده به مرکز Circleها ---
        java.util.function.Function<Boolean, Polygon> triShape = pointRight -> {
            Polygon p = pointRight
                    ? new Polygon(-8.0, -8.0, 8.0, 0.0, -8.0, 8.0)   // ►
                    : new Polygon( 8.0, -8.0,-8.0, 0.0,  8.0, 8.0);  // ◄
            p.setFill(Color.web("#FFF59D"));
            p.setStroke(Color.BLACK);
            p.setStrokeWidth(1.5);
            return p;
        };

        Group tri_c1  = new Group(triShape.apply(true));   // راست‌نگر
        tri_c1.layoutXProperty().bind(c1_trR.centerXProperty());
        tri_c1.layoutYProperty().bind(c1_trR.centerYProperty());
        tri_c1.setMouseTransparent(true);


        Group tri_c2  = new Group(triShape.apply(false));  // چپ‌نگر
        tri_c2.layoutXProperty().bind(c2_trL.centerXProperty());
        tri_c2.layoutYProperty().bind(c2_trL.centerYProperty());
        tri_c2.setMouseTransparent(true);


        Group tri_c3  = new Group(triShape.apply(true));
        tri_c3.layoutXProperty().bind(c3_trR.centerXProperty());
        tri_c3.layoutYProperty().bind(c3_trR.centerYProperty());
        tri_c3.setMouseTransparent(true);


        Group tri_c4  = new Group(triShape.apply(true));
        tri_c4.layoutXProperty().bind(c4_trR.centerXProperty());
        tri_c4.layoutYProperty().bind(c4_trR.centerYProperty());
        tri_c4.setMouseTransparent(true);


        Group tri_c5a = new Group(triShape.apply(false));
        tri_c5a.layoutXProperty().bind(c5_trL1.centerXProperty());
        tri_c5a.layoutYProperty().bind(c5_trL1.centerYProperty());
        tri_c5a.setMouseTransparent(true);


        Group tri_c5b = new Group(triShape.apply(false));
        tri_c5b.layoutXProperty().bind(c5_trL2.centerXProperty());
        tri_c5b.layoutYProperty().bind(c5_trL2.centerYProperty());
        tri_c5b.setMouseTransparent(true);


        // گروه‌بندی برای نمایش
        g.getChildren().addAll(
                c1, c2, c3, c4, c5,
                // پورت‌های مربعی
                c1_sqR, c2_sqL, c3_sqR, c4_sqR, c5_sqL1, c5_sqL2,
                // hit-targetهای دایره‌ای (نامرئی)
                c1_trR, c2_trL, c3_trR, c4_trR, c5_trL1, c5_trL2,
                // مثلث‌های نمایشی
                tri_c1, tri_c2, tri_c3, tri_c4, tri_c5a, tri_c5b,
                // برچسب‌های SPY
                spy2, spy3, spy4,
                // Start
                start
        );

        // ساخت LevelView: bodies (5 بدنه)، circles (6 پورت مثلثی به‌صورت Circle)، smallRects (6 مربع)
        view = new LevelView(
                g,
                java.util.List.of(c1, c2, c3, c4, c5),
                java.util.List.of(c1_trR, c2_trL, c3_trR, c4_trR, c5_trL1, c5_trL2),
                java.util.List.of(c1_sqR, c2_sqL, c3_sqR, c4_sqR, c5_sqL1, c5_sqL2),
                start
        );
        return view;
    }


    @Override
    public void bind(GameController c, Slider timeSlider, Runnable onWin) {
        // refs
        c.bindStageRefs(view.circles, view.smallRects, timeSlider);

        // درگ بدنه‌ها (Start همراه بدنه‌ی چپ جابه‌جا شود)
        if (view.bodies.size() >= 5) {
            c.enableDragSystem(view.bodies.get(0), java.util.List.of(view.startButton, view.circles.get(0), view.smallRects.get(0)));
            c.enableDragSystem(view.bodies.get(1), java.util.List.of(view.circles.get(1), view.smallRects.get(1)));
            c.enableDragSystem(view.bodies.get(2), java.util.List.of(view.circles.get(2), view.smallRects.get(2)));
            c.enableDragSystem(view.bodies.get(3), java.util.List.of(view.circles.get(3), view.smallRects.get(3)));
            c.enableDragSystem(view.bodies.get(4), java.util.List.of(view.circles.get(4), view.smallRects.get(4), view.circles.get(5), view.smallRects.get(5)));
        }

        // سیم‌کشی پورت‌ها
        for (Circle cc : view.circles)      cc.setOnMousePressed(c::onStartWireFromCircle);
        for (Rectangle r : view.smallRects) r.setOnMousePressed(c::onStartWireFromSmallRect);

        // Start: اول غیرفعال، با کامل‌شدن سیم‌کشی صحیح (که در onReleaseWire مرحله 3 چک می‌شود) فعال شود
        view.startButton.setDisable(true);
        c.setStageReadyCallback(() -> view.startButton.setDisable(false));

        // ---------- هندلر Start ----------
        view.startButton.setOnAction(evt -> {
            if (timeSlider.getValue() != 0) return;
            view.startButton.setDisable(true); // دوباره استارت نخوره

            // مسیر واقعی بر اساس خودِ سیم کاربر (از src به نودِ متصل‌شده)
            // ⚠️ نیاز به c.connectedOf(...) داریم؛ اگر نداری پایین نوشتم چی اضافه کنی
            java.util.function.Function<Node, Line> pathOf = (Node src) -> {
                Node dst = c.connectedOf(src);
                if (dst == null) return null;

                // مراکز در مختصات صحنه
                Point2D p1Scene = c.nodeCenterInScene(src);
                Point2D p2Scene = c.nodeCenterInScene(dst);
                // تبدیل به مختصات گروه مرحله (parent مارکرها)
                Point2D p1 = view.group.sceneToLocal(p1Scene);
                Point2D p2 = view.group.sceneToLocal(p2Scene);

                return new Line(p1.getX(), p1.getY(), p2.getX(), p2.getY());
            };

            // سازنده‌های مارکر جریان
            java.util.function.Supplier<Rectangle> mkSq = () -> {
                Rectangle r = new Rectangle(12, 12, Color.YELLOW);
                r.setStroke(Color.BLACK); r.setStrokeWidth(1);
                return r;
            };
            java.util.function.Supplier<Polygon> mkTri = () -> {
                double size = 12, h = size * Math.sqrt(3) / 2.0;
                Polygon p = new Polygon(0.0, -h, -size/2.0, h/2.0, size/2.0, h/2.0);
                p.setFill(Color.YELLOW); p.setStroke(Color.BLACK); p.setStrokeWidth(1);
                p.setUserData("M_HEX");
                return p;
            };

            // برای برخورد با فلوهای موازی B
            final java.util.List<Node> activeBMarkers = new java.util.ArrayList<>();

            // شمارنده‌ها برای Win بعد از ۴ دور کامل
            final int MAX_REPEATS = 4;
            final int[] counter  = {0}; // چند A تمام شده
            final int[] pendingB = {0}; // چند B در حال اجراست

            Runnable maybeTryWin = () -> {
                if (counter[0] >= MAX_REPEATS && pendingB[0] == 0) {
                    onWin.run();
                }
            };

            // ---------- فاز B: یک شاخهٔ رندم از SPY → هدف ----------
            Runnable spawnPhaseB = () -> {
                boolean pickTop = java.util.concurrent.ThreadLocalRandom.current().nextBoolean();
                // rSrc/tSrc همان پورتِ مبدا؛ مقصد را از repo می‌گیریم
                Rectangle rSrc = pickTop ? view.smallRects.get(2) : view.smallRects.get(3);
                Circle    tSrc = pickTop ? view.circles.get(2)    : view.circles.get(3);

                Line pathRB = pathOf.apply(rSrc);
                Line pathTB = pathOf.apply(tSrc);
                if (pathRB == null || pathTB == null) return; // اگر سیمی نیست، این B را رد کن

                Rectangle flowSqB = mkSq.get();
                Polygon   flowTriB = mkTri.get();
                view.group.getChildren().addAll(flowSqB, flowTriB);

                activeBMarkers.add(flowSqB);
                activeBMarkers.add(flowTriB);
                pendingB[0]++;

                PathTransition rB = new PathTransition(Duration.seconds(1.6), pathRB, flowSqB);
                PathTransition tB = new PathTransition(Duration.seconds(1.6), pathTB, flowTriB);
                rB.setOnFinished(e -> c.awardCoins(2));
                tB.setOnFinished(e -> c.awardCoins(3));

                // برخورد داخل همین B
                c.enableSteamCollision(flowTriB, flowSqB);

                ParallelTransition phaseB = new ParallelTransition(rB, tB);
                phaseB.setOnFinished(e -> {
                    view.group.getChildren().removeAll(flowSqB, flowTriB);
                    activeBMarkers.remove(flowSqB);
                    activeBMarkers.remove(flowTriB);
                    pendingB[0]--;
                    maybeTryWin.run();
                });
                phaseB.play();
            };

            // ---------- لوپ فاز A: ۴ بار چپ→وسط + هر بار یک B موازی ----------
            final Runnable[] loopA = new Runnable[1];
            loopA[0] = () -> {
                if (counter[0] >= MAX_REPEATS) { maybeTryWin.run(); return; }

                Line pathRA = pathOf.apply(view.smallRects.get(0)); // مربع چپ → مقصد واقعی
                Line pathTA = pathOf.apply(view.circles.get(0));    // مثلث چپ → مقصد واقعی
                if (pathRA == null || pathTA == null) { maybeTryWin.run(); return; }

                Rectangle flowSqA = mkSq.get();
                Polygon   flowTriA = mkTri.get();
                view.group.getChildren().addAll(flowSqA, flowTriA);

                PathTransition rA = new PathTransition(Duration.seconds(1.6), pathRA, flowSqA);
                PathTransition tA = new PathTransition(Duration.seconds(1.6), pathTA, flowTriA);
                rA.setOnFinished(e -> c.awardCoins(2));
                tA.setOnFinished(e -> c.awardCoins(3));

                // برخورد داخل A و با همهٔ Bهای فعال
                c.enableSteamCollision(flowTriA, flowSqA);
                for (Node n : activeBMarkers) {
                    c.enableSteamCollision(flowTriA, n);
                    c.enableSteamCollision(flowSqA, n);
                }

                ParallelTransition phaseA = new ParallelTransition(rA, tA);
                phaseA.setOnFinished(e -> {
                    view.group.getChildren().removeAll(flowSqA, flowTriA);
                    spawnPhaseB.run();         // بعد از هر A یک B موازی
                    counter[0]++;              // این دور تمام شد
                    if (counter[0] < MAX_REPEATS) {
                        Platform.runLater(loopA[0]);
                    } else {
                        maybeTryWin.run();     // همهٔ A ها تمام؛ منتظر Bها
                    }
                });
                phaseA.play();
            };

            // شروع
            loopA[0].run();
        });

    }


    @Override
    public LevelView getView() { return view; }
}










/* =========================================================
 *                     SHAPE HELPERS
 * ========================================================= */
final class Shapes {
    static Rectangle rect(double x, double y, double w, double h, Color fill) {
        Rectangle r = new Rectangle(x, y, w, h);
        r.setFill(fill);
        r.setStroke(Color.BLACK);
        r.setStrokeWidth(2);
        r.setArcWidth(20);
        r.setArcHeight(20);
        return r;
    }
    static Rectangle smallRect(double x, double y) {
        Rectangle r = new Rectangle(x, y, 10, 20);
        r.setFill(Color.GREEN);
        r.setStroke(Color.BLACK);
        r.setStrokeWidth(2);
        r.setArcWidth(2);
        r.setArcHeight(2);
        return r;
    }
    static Circle circle(double x, double y, double radius, Color fill, Color stroke, double sw) {
        Circle c = new Circle(x, y, radius);
        c.setFill(fill);
        c.setStroke(stroke);
        c.setStrokeWidth(sw);
        return c;
    }

    // نسخه‌های مختصر برای مرحله ۲ (با سایزهای ثابت)
    static Rectangle mkRect(double x, double y){ return rect(x, y, 100, 200, Color.CORAL); }
    static Rectangle mkSmallRect(double x, double y){ return smallRect(x, y); }
    static Circle mkCircle(double x, double y){ return circle(x, y, 10, Color.LIGHTGREEN, Color.DARKGREEN, 3); }

    static Point2D centerOf(Rectangle r) {
        return r.localToScene(r.getX() + r.getWidth()/2, r.getY() + r.getHeight()/2);
    }
}

// ============== LEVEL 4 =================
final class Level4 implements Level {
    private LevelView view;

    // پورت‌ها (برای خوانایی در bind)
    private Circle    stTop_tri;      // START بالا: مثلث
    private Rectangle stTop_sq;       // START بالا: مربع
    private Circle    stBot_hex;      // START پایین: شش‌ضلعی

    // DDOS: چپ = H,S,T | راست = T,S,H
    private Circle    ddL_hex;
    private Rectangle ddL_sq;
    private Circle    ddL_tri;

    private Circle    ddR_tri;
    private Rectangle ddR_sq;
    private Circle    ddR_hex;

    // END (چپ) = H,S,T
    private Circle    end_hex;
    private Rectangle end_sq;
    private Circle    end_tri;

    // تایمر اعتبارسنجی فعال‌/غیرفعال کردن Start
    private Timeline validateTimer;

    // ابعاد کارت‌ها
    private static final double W = 150, H = 200;
    // کاهش سرعت حرکت‌ها در این مرحله: 20% کندتر ⇒ مدت‌زمان × 1.25
    private static final double TIME_SCALE_SLOWDOWN = 2;

    @Override
    public LevelView build() {
        Group g = new Group();

        /* -------- بدنه‌ها -------- */
        Rectangle cStartTop = Shapes.rect( 70,  80, W, H, Color.web("#1F4733"));
        cStartTop.setStroke(Color.web("#3BF07B")); cStartTop.setStrokeWidth(4);

        Rectangle cStartBot = Shapes.rect( 70, 340, W, H, Color.web("#1F4733"));
        cStartBot.setStroke(Color.web("#3BF07B")); cStartBot.setStrokeWidth(4);

        Rectangle cDDOS = Shapes.rect(360, 210, W, H, Color.web("#4D2E0B"));
        cDDOS.setStroke(Color.web("#FF8A00")); cDDOS.setStrokeWidth(4);

        Rectangle cEND  = Shapes.rect(655, 210, W, H, Color.web("#471F1F"));
        cEND.setStroke(Color.web("#F03B3B"));  cEND.setStrokeWidth(4);

        // عناوین
        Text tStart1 = centeredLabel("START", cStartTop);
        Text tStart2 = centeredLabel("START", cStartBot);
        Text tDDOS   = centeredLabel("DDOS",  cDDOS);
        Text tEND    = centeredLabel("END",   cEND);

        // دکمه Start مرحله (روی لبه بالای کارت START بالا)
        Button start = new Button("Start");
        start.setPrefSize(70, 30);
        start.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #FF6F61, #D7263D);" +
                        "-fx-text-fill: white; -fx-background-radius: 10;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.75), 4, 0, 0, 2);"
        );
        double sX = cStartTop.getX() + (cStartTop.getWidth() - start.getPrefWidth())/2.0;
        double sY = cStartTop.getY() - start.getPrefHeight() - 8;
        start.setLayoutX(sX);
        start.setLayoutY(sY + 20); // مشابه سایر مراحل

        /* -------- factory‌های پورت‌ها (مثل مرحله 3) -------- */
        java.util.function.BiFunction<Rectangle, Double, Rectangle> rightSquare =
                (card, oy) -> smallSquareAt(card.getX()+card.getWidth()-10, card.getY()+oy-9);
        java.util.function.BiFunction<Rectangle, Double, Rectangle> leftSquare =
                (card, oy) -> smallSquareAt(card.getX()-28, card.getY()+oy-9);

        java.util.function.BiFunction<Rectangle, Double, Point2D> rightCenter =
                (card, oy) -> new Point2D(card.getX()+card.getWidth()+8, card.getY()+oy);
        java.util.function.BiFunction<Rectangle, Double, Point2D> leftCenter =
                (card, oy) -> new Point2D(card.getX()-8, card.getY()+oy);

        // hit-target برای مثلث/شش‌ضلعی
        java.util.function.Function<Point2D, Circle> hit =
                p -> { Circle c = new Circle(p.getX(), p.getY(), 10);
                    c.setOpacity(0.0); c.setMouseTransparent(false);
                    c.setStroke(Color.DARKGREEN); c.setStrokeWidth(0); // نامرئی اما پیک‌پذیر
                    return c; };

        /* -------- START بالا (راست: T@80, S@120) -------- */
        stTop_tri = hit.apply(rightCenter.apply(cStartTop, 80.0));   stTop_tri.setUserData("T");
        stTop_sq  = rightSquare.apply(cStartTop, 120.0);             stTop_sq.setUserData("S");
        attachTriGlyph(g, stTop_tri, true);

        /* -------- START پایین (راست: H@100) -------- */
        stBot_hex = hit.apply(rightCenter.apply(cStartBot, 100.0));  stBot_hex.setUserData("H");
        attachHexGlyph(g, stBot_hex, true);

        /* -------- DDOS چپ (H@60, S@100, T@140) -------- */
        ddL_hex = hit.apply(leftCenter.apply(cDDOS, 60.0));   ddL_hex.setUserData("H");
        ddL_sq  = leftSquare.apply(cDDOS, 100.0);             ddL_sq.setUserData("S");
        ddL_tri = hit.apply(leftCenter.apply(cDDOS, 140.0));  ddL_tri.setUserData("T");
        attachHexGlyph(g, ddL_hex, false);
        attachTriGlyph(g, ddL_tri, false);

        /* -------- DDOS راست (T@60, S@100, H@140) -------- */
        ddR_tri = hit.apply(rightCenter.apply(cDDOS, 60.0));  ddR_tri.setUserData("T");
        ddR_sq  = rightSquare.apply(cDDOS, 100.0);            ddR_sq.setUserData("S");
        ddR_hex = hit.apply(rightCenter.apply(cDDOS, 140.0)); ddR_hex.setUserData("H");
        attachTriGlyph(g, ddR_tri, true);
        attachHexGlyph(g, ddR_hex, true);

        /* -------- END چپ (H@60, S@100, T@140) -------- */
        end_hex = hit.apply(leftCenter.apply(cEND, 60.0));    end_hex.setUserData("H");
        end_sq  = leftSquare.apply(cEND, 100.0);              end_sq.setUserData("S");
        end_tri = hit.apply(leftCenter.apply(cEND, 140.0));   end_tri.setUserData("T");
        attachHexGlyph(g, end_hex, false);
        attachTriGlyph(g, end_tri, false);

        // گروه‌بندی
        g.getChildren().addAll(
                cStartTop, cStartBot, cDDOS, cEND,
                tStart1, tStart2, tDDOS, tEND,
                // پورت‌ها
                stTop_sq, stBot_hex, ddL_sq, ddR_sq, end_sq,
                stTop_tri, ddL_hex, ddL_tri, ddR_tri, ddR_hex, end_hex, end_tri,
                start
        );

        // LevelView: لیست‌ها برای کنترل سیم‌کشی (Circleها = T/H ، Rectها = S)
        view = new LevelView(
                g,
                java.util.List.of(cStartTop, cStartBot, cDDOS, cEND),
                java.util.List.of(stTop_tri, stBot_hex, ddL_hex, ddL_tri, ddR_tri, ddR_hex, end_hex, end_tri),
                java.util.List.of(stTop_sq, ddL_sq, ddR_sq, end_sq),
                start
        );
        return view;
    }

    @Override
    public void bind(GameController c, Slider timeSlider, Runnable onWin) {
        // refs عمومی
        c.bindStageRefs(view.circles, view.smallRects, timeSlider);

        // درگ سیستم‌ها (Start همراه کارت START بالا)
        c.enableDragSystem(view.bodies.get(0), java.util.List.of(view.startButton, stTop_tri, stTop_sq));
        c.enableDragSystem(view.bodies.get(1), java.util.List.of(stBot_hex));
        c.enableDragSystem(view.bodies.get(2), java.util.List.of(ddL_hex, ddL_sq, ddL_tri, ddR_tri, ddR_sq, ddR_hex));
        c.enableDragSystem(view.bodies.get(3), java.util.List.of(end_hex, end_sq, end_tri));

        // سیم‌کشی
        for (Circle cc : view.circles)      cc.setOnMousePressed(c::onStartWireFromCircle);
        for (Rectangle r : view.smallRects) r.setOnMousePressed(c::onStartWireFromSmallRect);

        // Start اول غیرفعال
        view.startButton.setDisable(true);

        // اعتبارسنجیِ سیم‌کشی: چپِ DDOS باید هم‌نوع↔هم‌نوع باشد، راستِ DDOS به END باید پوشش ۱-به-۱ بدهد (نوع مهم نیست)
        Runnable revalidate = () -> {
            boolean leftOK =
                    (c.connectedOf(stTop_tri) == ddL_tri) &&
                            (c.connectedOf(stTop_sq)  == ddL_sq)  &&
                            (c.connectedOf(stBot_hex) == ddL_hex);

            boolean rightOK = isBijectionConnected(
                    c,
                    java.util.List.of(ddR_tri, ddR_sq, ddR_hex),
                    java.util.List.of(end_hex, end_sq, end_tri)
            );

            view.startButton.setDisable(!(leftOK && rightOK));
        };

        // تایمر سبک برای آپدیت وضعیت (هر 200ms) — تا وقتی این مرحله فعاله
        validateTimer = new Timeline(new KeyFrame(Duration.millis(200), e -> revalidate.run()));
        validateTimer.setCycleCount(Animation.INDEFINITE);
        validateTimer.play();

        // سازنده‌های مارکر جریان (با userData برای تشخیص نوع مارکر)
        java.util.function.Supplier<Rectangle> mkSq = () -> {
            Rectangle r = new Rectangle(12, 12, Color.YELLOW);
            r.setStroke(Color.BLACK); r.setStrokeWidth(1);
            r.setUserData("M_SQ");
            return r;
        };
        java.util.function.Supplier<Polygon> mkTri = () -> {
            double size=12, h=size*Math.sqrt(3)/2.0;
            Polygon p=new Polygon(0.0,-h,-size/2.0,h/2.0,size/2.0,h/2.0);
            p.setFill(Color.YELLOW); p.setStroke(Color.BLACK); p.setStrokeWidth(1);
            p.setUserData("M_TRI");
            return p;
        };
        java.util.function.Supplier<Polygon> mkHex = () -> {
            double R=8;
            Polygon p = new Polygon(
                    R,0, R/2, Math.sqrt(3)*R/2, -R/2, Math.sqrt(3)*R/2,
                    -R,0, -R/2,-Math.sqrt(3)*R/2, R/2,-Math.sqrt(3)*R/2
            );
            p.setFill(Color.YELLOW); p.setStroke(Color.BLACK); p.setStrokeWidth(1);
            p.setUserData("M_HEX");
            return p;
        };

        // مسیر واقعی سیم از یک نود (بین from و نود متصل‌شده‌اش) در مختصات گروه مرحله
        java.util.function.Function<Node, Line> pathOf = (Node src) -> {
            Node dst = c.connectedOf(src);
            if (dst == null) return null;
            Point2D p1Scene = c.nodeCenterInScene(src);
            Point2D p2Scene = c.nodeCenterInScene(dst);
            Point2D p1 = view.group.sceneToLocal(p1Scene);
            Point2D p2 = view.group.sceneToLocal(p2Scene);
            return new Line(p1.getX(), p1.getY(), p2.getX(), p2.getY());
        };

        // اجرای جریان‌ها
        view.startButton.setOnAction(evt -> {
            if (timeSlider.getValue() != 0) return;
            if (view.startButton.isDisable()) return;
            view.startButton.setDisable(true);

            // --- کانفیگ تکرار ---
            final int REPEATS = 4; // چهار بار تکرارِ فرایندِ سمتِ چپ

            // --- زمان‌ها (با کند شدن 20%) ---
            final Duration BASE_INCOMPAT = Duration.seconds(1.6).multiply(TIME_SCALE_SLOWDOWN); // ناسازگار
            final Duration BASE_COMPAT   = BASE_INCOMPAT.multiply(0.5);                          // سازگار = نصفِ ناسازگار

            // --- سازگاری پورت مبدا با مقصد (UserData: "T","S","H") ---
            java.util.function.Function<Node, Boolean> isCompatible = (Node src) -> {
                Node dst = c.connectedOf(src);
                if (dst == null) return false;
                Object su = src.getUserData(), du = dst.getUserData();
                return su != null && su.equals(du);
            };
            java.util.function.Function<Node, Duration> durationFor =
                    (Node src) -> isCompatible.apply(src) ? BASE_COMPAT : BASE_INCOMPAT;

            // --- اینترپولاتورهای شتاب دوبرابر برای شش‌ضلعی ---
            final Interpolator ACCEL_POS_2X = new Interpolator() { @Override protected double curve(double t){ return t*t*t; } };
            final Interpolator ACCEL_NEG_2X = new Interpolator() { @Override protected double curve(double t){ double u=1-t; return 1-u*u*u; } };

            // شمارندهٔ چرخه‌های «شروع‌شده» سمت چپ و تعداد فازهای راستِ درحال اجرا
            final int[] cyclesStarted = {0};
            final int[] rightsPending = {0};

            // اگر همهٔ چرخه‌ها آغاز شده‌اند و دیگر فازِ راستِ فعالی باقی نمانده، پایان مرحله
            Runnable maybeFinish = () -> {
                if (cyclesStarted[0] >= REPEATS && rightsPending[0] == 0) {
                    onWin.run();
                }
            };

            // --- شروع فاز راست برای یک چرخه (می‌تواند با چرخه‌های بعد همپوشانی داشته باشد) ---
            java.util.function.Consumer<Boolean> startRightPhase = (Boolean hexCanGoRight) -> {
                // مسیرهای DDOS(Right) → END
                Line pR_tri = pathOf.apply(ddR_tri);
                Line pR_sq  = pathOf.apply(ddR_sq );
                Line pR_hex = pathOf.apply(ddR_hex);

                if (pR_tri == null || pR_sq == null) { // حداقل دو مسیر لازم است
                    maybeFinish.run();
                    return;
                }

                java.util.List<Node> rightPorts = new java.util.ArrayList<>();
                java.util.List<Line> rightPaths = new java.util.ArrayList<>();
                rightPorts.add(ddR_tri); rightPaths.add(pR_tri);
                rightPorts.add(ddR_sq ); rightPaths.add(pR_sq);
                if (hexCanGoRight && pR_hex != null) {
                    rightPorts.add(ddR_hex); rightPaths.add(pR_hex);
                }

                // انتخاب مارکرها: اگر هگزا از چپ برنگشته، مجاز است؛ وگرنه فقط T,S
                java.util.List<java.util.function.Supplier<Node>> choices = (hexCanGoRight && pR_hex != null)
                        ? new java.util.ArrayList<>(java.util.List.of(
                        () -> mkTri.get(), () -> (Node) mkSq.get(), () -> mkHex.get()
                ))
                        : new java.util.ArrayList<>(java.util.List.of(
                        () -> mkTri.get(), () -> (Node) mkSq.get()
                ));
                java.util.Collections.shuffle(choices, java.util.concurrent.ThreadLocalRandom.current());

                java.util.List<Node> markers = new java.util.ArrayList<>();
                for (int i = 0; i < rightPorts.size(); i++) markers.add(choices.get(i).get());
                view.group.getChildren().addAll(markers);

                java.util.List<PathTransition> transitions = new java.util.ArrayList<>();
                for (int i = 0; i < rightPorts.size(); i++) {
                    Node port = rightPorts.get(i);
                    Line path = rightPaths.get(i);
                    Node marker = markers.get(i);

                    PathTransition pr = new PathTransition(durationFor.apply(port), path, marker);
                    if ("M_HEX".equals(marker.getUserData())) {
                        pr.setInterpolator(ACCEL_NEG_2X); // شش‌ضلعی سمت راست همیشه کندشونده
                    }

                    // ⬅️ امتیازدهی هنگام رسیدن به «سیستم نهایی» (END)
                    final Node m = marker; // برای استفاده در لامبدا
                    pr.setOnFinished(ev -> {
                        Object tag = m.getUserData();
                        if ("M_TRI".equals(tag)) {
                            c.awardCoins(3); // مثلث
                        } else if ("M_SQ".equals(tag)) {
                            c.awardCoins(2); // مربع
                        } else if ("M_HEX".equals(tag)) {
                            c.awardCoins(1); // شش‌ضلعی
                        }
                    });

                    transitions.add(pr);
                }


                // برخوردهای جفت‌به‌جفت در راست
                for (int i = 0; i < markers.size(); i++)
                    for (int j = i + 1; j < markers.size(); j++)
                        c.enableSteamCollision(markers.get(i), markers.get(j));

                rightsPending[0]++; // یک فاز راست در حال اجراست
                ParallelTransition rightPhase = new ParallelTransition(transitions.toArray(new Animation[0]));
                rightPhase.setOnFinished(e2 -> {
                    view.group.getChildren().removeAll(markers);
                    rightsPending[0]--;
                    maybeFinish.run();
                });
                rightPhase.play();
            };

            // --- تابع شروع چرخهٔ سمت چپ (بلافاصله پس از اتمام چپ، چرخهٔ بعد آغاز می‌شود) ---
            final Runnable[] startLeftCycle = new Runnable[1];
            startLeftCycle[0] = () -> {
                if (cyclesStarted[0] >= REPEATS) { maybeFinish.run(); return; }
                cyclesStarted[0]++;

                // مسیرهای START → DDOS(Left)
                Line pTriL = pathOf.apply(stTop_tri);
                Line pSqL  = pathOf.apply(stTop_sq );
                Line pHexL = pathOf.apply(stBot_hex);
                if (pTriL == null || pSqL == null || pHexL == null) { maybeFinish.run(); return; }

                // مارکرها
                Polygon   triL = mkTri.get();
                Rectangle sqL  = mkSq.get();
                Polygon   hexL = mkHex.get();
                view.group.getChildren().addAll(triL, sqL, hexL);

                // ترنزیشن‌ها
                PathTransition ptTriL = new PathTransition(durationFor.apply(stTop_tri), pTriL, triL);
                PathTransition ptSqL  = new PathTransition(durationFor.apply(stTop_sq ), pSqL , sqL );
                PathTransition ptHexL = new PathTransition(durationFor.apply(stBot_hex), pHexL, hexL);
                ptHexL.setInterpolator(ACCEL_POS_2X); // سمت چپ سازگار ⇒ شتاب مثبت

                // پرچم‌ها برای همگام‌سازی داخلیِ این چرخه
                final java.util.concurrent.atomic.AtomicBoolean hexLeftSurvived = new java.util.concurrent.atomic.AtomicBoolean(true);
                final java.util.concurrent.atomic.AtomicBoolean leftPairDone     = new java.util.concurrent.atomic.AtomicBoolean(false);
                final java.util.concurrent.atomic.AtomicBoolean hexDone          = new java.util.concurrent.atomic.AtomicBoolean(false);

                // برخوردها: مثلث↔مربع عادی؛ شش‌ضلعی با مثلث/مربع برگشتی
                c.enableSteamCollision(triL, sqL);
                c.enableHexBounceBack(hexL, triL, ptHexL, () -> hexLeftSurvived.set(false));
                c.enableHexBounceBack(hexL, sqL , ptHexL, () -> hexLeftSurvived.set(false));

                // پایان هگزا (چه سالم برسد، چه با rate منفی برگردد)
                ptTriL.setOnFinished(ev -> c.addPacketLoss(1));

                // ✅ وقتی مربع «به DDOS(Left) رسید» → +1 Packet Loss
                ptSqL.setOnFinished(ev -> c.addPacketLoss(1));

                // ✅ برای شش‌ضلعی: فقط اگر «واقعاً به DDOS(Left) رسیده» باشد (برخورد نکرده باشد) +1
                ptHexL.setOnFinished(ev -> {
                    // اگر در طول مسیر برخورد کرده باشد، enableHexBounceBack پرچم را false کرده است
                    if (hexLeftSurvived.get()) {
                        c.addPacketLoss(1);
                    }
                    if (hexL.getParent() instanceof Group) {
                        Group g = (Group) hexL.getParent();
                        g.getChildren().remove(hexL);
                    }
                    hexDone.set(true);
                    if (leftPairDone.get()) {
                        startRightPhase.accept(hexLeftSurvived.get());
                        Platform.runLater(startLeftCycle[0]);
                    }
                });
                ptHexL.play();

                // زوجِ مثلث+مربع
                ParallelTransition leftPair = new ParallelTransition(ptTriL, ptSqL);
                leftPair.setOnFinished(e -> {
                    view.group.getChildren().removeAll(triL, sqL);
                    leftPairDone.set(true);

                    if (hexDone.get()) {
                        startRightPhase.accept(hexLeftSurvived.get());
                        Platform.runLater(startLeftCycle[0]);
                    }
                });
                leftPair.play();
            };
            // شروع چرخهٔ اول
            startLeftCycle[0].run();
        });
    }


    @Override
    public LevelView getView() { return view; }

    @Override
    public void dispose() {
        if (validateTimer != null) {
            validateTimer.stop();
            validateTimer = null;
        }
    }

    /* --------- helpers محلی --------- */

    private static Rectangle smallSquareAt(double x, double y){
        Rectangle r = new Rectangle(x, y, 18, 18);
        r.setArcWidth(4); r.setArcHeight(4);
        r.setFill(Color.WHITE);
        r.setStroke(Color.web("#FF93AA")); r.setStrokeWidth(2.5);
        return r;
    }

    private static Text centeredLabel(String s, Rectangle body) {
        Text t = new Text(s);
        t.setFill(Color.WHITE);
        t.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        // فقط یک‌بار اندازه را محاسبه می‌کنیم؛ بایندینگ به layoutBounds حذف شد
        t.applyCss();
        double w = t.getLayoutBounds().getWidth();

        t.setX(body.getX() + body.getWidth() / 2.0 - w / 2.0);
        t.setY(body.getY() + body.getHeight() / 2.0 + 5);

        return t;
    }

    private static void attachTriGlyph(Group g, Circle c, boolean pointRight){
        Polygon p = pointRight
                ? new Polygon(-8.0,-8.0, 8.0,0.0, -8.0,8.0)   // ►
                : new Polygon( 8.0,-8.0,-8.0,0.0,  8.0,8.0);   // ◄
        p.setFill(Color.web("#FFF59D")); p.setStroke(Color.BLACK); p.setStrokeWidth(1.5);
        Group wrp = new Group(p);
        wrp.setMouseTransparent(true);
        wrp.layoutXProperty().bind(c.centerXProperty());
        wrp.layoutYProperty().bind(c.centerYProperty());
        g.getChildren().add(wrp);
    }

    private static void attachHexGlyph(Group g, Circle c, boolean pointRight){
        // صرفاً جهت ظاهری؛ جهت‌گیری مهم نیست
        double R=11;
        Polygon p = new Polygon(
                R,0, R/2, Math.sqrt(3)*R/2, -R/2, Math.sqrt(3)*R/2,
                -R,0, -R/2,-Math.sqrt(3)*R/2, R/2,-Math.sqrt(3)*R/2
        );
        p.setFill(Color.web("#FFF59D")); p.setStroke(Color.BLACK); p.setStrokeWidth(1.5);
        Group wrp = new Group(p);
        wrp.setMouseTransparent(true);
        wrp.layoutXProperty().bind(c.centerXProperty());
        wrp.layoutYProperty().bind(c.centerYProperty());
        g.getChildren().add(wrp);
    }

    /** بررسی پوشش ۱-به-۱ بین سه پورت DDOS-Right و سه پورت END-Left (نوع مهم نیست). */
    private static boolean isBijectionConnected(GameController c, java.util.List<Node> from, java.util.List<Node> to){
        java.util.Set<Node> toSet = new java.util.HashSet<>(to);
        java.util.Set<Node> seen  = new java.util.HashSet<>();
        for (Node f : from) {
            Node other = c.connectedOf(f);
            if (other == null || !toSet.contains(other)) return false;
            seen.add(other);
        }
        return seen.size() == from.size();
    }
}

// ============== LEVEL 5 =================
final class Level5 implements Level {
    private LevelView view;

    // تایمر اعتبارسنجی برای فعال/غیرفعال کردن Start
    private Timeline validateTimer;

    // ابعاد کارت‌های کوچک مرحله 5
    private static final double W = 60, H = 80;

    // ---- پورت‌ها (برای bind) ----
    // START (راست: مثلث، مربع)
    private Circle    stTri;
    private Rectangle stSq;

    // VPN بالا: چپ=مثلث | راست=مربع + مثلث
    private Circle    vpnTopTriL;
    private Rectangle vpnTopSqR;
    private Circle    vpnTopTriR;

    // VPN پایین: چپ=مربع | راست=شش‌ضلعی
    private Rectangle vpnBotSqL;
    private Circle    vpnBotHexR;

    // SPY چپ: چپ=(مربع،مثلث) | راست=(مربع،مثلث)
    private Rectangle spyL_sqL;
    private Circle    spyL_triL;
    private Rectangle spyL_sqR;
    private Circle    spyL_triR;

    // SPY راست: چپ=(مربع،مثلث) | راست=(مربع،مثلث)
    private Rectangle spyR_sqL;
    private Circle    spyR_triL;
    private Rectangle spyR_sqR;
    private Circle    spyR_triR;

    // DDOS: چپ=(مربع،مثلث،شش‌ضلعی) | راست=(مثلث،مربع،شش‌ضلعی)
    private Rectangle ddL_sq;
    private Circle    ddL_tri;
    private Circle    ddL_hex;
    private Circle    ddR_tri;
    private Rectangle ddR_sq;
    private Circle    ddR_hex;

    // END (چپ: شش‌ضلعی، مربع، مثلث)
    private Circle    end_hex;
    private Rectangle end_sq;
    private Circle    end_tri;


    private static void tag(Node n, String type) { n.setUserData(type); }


    /* ---------- Helpers: ساخت کارت/پورت/لیبل ---------- */

    private static Rectangle card(double x, double y, double w, double h, Color fill, Color stroke) {
        Rectangle r = new Rectangle(x, y, w, h);
        r.setArcWidth(7); r.setArcHeight(7);
        r.setFill(fill);
        r.setStroke(stroke);
        r.setStrokeWidth(1.5);
        return r;
    }

    /** متن وسط کارت؛ با listener جایگذاری می‌شود تا حلقهٔ بایند نشود. */
    private static Text centeredLabel(String s, Rectangle body) {
        Text t = new Text(s);
        t.setFill(Color.WHITE);
        t.setStyle("-fx-font-size: 7px; -fx-font-weight: bold;");

        Runnable place = () -> {
            double cx = body.getX() + body.getWidth()  / 2.0;
            double cy = body.getY() + body.getHeight() / 2.0;
            t.setX(cx - t.getLayoutBounds().getWidth() / 2.0);
            t.setY(cy + 2.5);
        };
        body.xProperty().addListener((o,ov,nv)->place.run());
        body.yProperty().addListener((o,ov,nv)->place.run());
        body.widthProperty().addListener((o,ov,nv)->place.run());
        body.heightProperty().addListener((o,ov,nv)->place.run());
        t.layoutBoundsProperty().addListener((o,ov,nv)->place.run());
        place.run();
        return t;
    }

    private static Rectangle smallSquareAt(double cx, double cy){
        double s = 4.5;
        Rectangle r = new Rectangle(cx - s/2, cy - s/2, s, s);
        r.setArcWidth(1); r.setArcHeight(1);
        r.setFill(Color.TRANSPARENT);
        r.setStroke(Color.web("#FF93AA"));
        r.setStrokeWidth(0.8);
        return r;
    }

    private static Circle hitAt(double cx, double cy){
        Circle c = new Circle(cx, cy, 3);
        c.setOpacity(0.0);               // نامرئی ولی پیک‌پذیر
        c.setMouseTransparent(false);
        return c;
    }

    private static void attachTriGlyph(Group g, Circle c, boolean pointRight){
        Polygon p = pointRight
                ? new Polygon(-4.0,-4.0, 4.0,0.0, -4.0,4.0)
                : new Polygon( 4.0,-4.0,-4.0,0.0,  4.0,4.0);  // ◄
        p.setFill(Color.web("#FFF59D")); p.setStroke(Color.BLACK); p.setStrokeWidth(0.6);
        Group wrap = new Group(p);
        wrap.setMouseTransparent(true);
        wrap.layoutXProperty().bind(c.centerXProperty());
        wrap.layoutYProperty().bind(c.centerYProperty());
        g.getChildren().add(wrap);
    }

    private static void attachHexGlyph(Group g, Circle c){
        double R=5;
        Polygon p = new Polygon(
                R,0, R/2, Math.sqrt(3)*R/2, -R/2, Math.sqrt(3)*R/2,
                -R,0, -R/2,-Math.sqrt(3)*R/2, R/2,-Math.sqrt(3)*R/2
        );
        p.setFill(Color.web("#FFF59D")); p.setStroke(Color.BLACK); p.setStrokeWidth(0.6);
        Group wrap = new Group(p);
        wrap.setMouseTransparent(true);
        wrap.layoutXProperty().bind(c.centerXProperty());
        wrap.layoutYProperty().bind(c.centerYProperty());
        g.getChildren().add(wrap);
    }

    private static double cY(Rectangle r) { return r.getY() + r.getHeight()/2.0; }
    private static double gap2(Rectangle r){ return r.getHeight()/6.0; }
    private static double gap3(Rectangle r){ return r.getHeight()/4.0; }
    private static double leftCx(Rectangle r){  return r.getX() - 6; }
    private static double rightCx(Rectangle r){ return r.getX() + r.getWidth() + 6; }

    @Override
    public LevelView build() {
        Group g = new Group();

        Rectangle cStart  = card( 40, 170, W, H, Color.web("#1F4733"), Color.web("#3BF07B"));
        Rectangle cVpnTop = card(180, 120, W, H, Color.web("#1B2327"), Color.web("#49F4FF"));
        Rectangle cVpnBot = card(200, 300, W, H, Color.web("#1B2327"), Color.web("#49F4FF"));
        Rectangle cSpyL   = card(320, 160, W, H, Color.web("#232633"), Color.web("#B782FF"));
        Rectangle cSpyR   = card(440, 160, W, H, Color.web("#232633"), Color.web("#B782FF"));
        Rectangle cDDOS   = card(560, 160, W, H, Color.web("#2B241E"), Color.web("#FF8A00"));
        Rectangle cEnd    = card(680, 170, W, H, Color.web("#2A2020"), Color.web("#FF5C86"));

        Text tStart = centeredLabel("START", cStart);
        Text tVpn1  = centeredLabel("VPN",   cVpnTop);
        Text tVpn2  = centeredLabel("VPN",   cVpnBot);
        Text tSpy1  = centeredLabel("SPY",   cSpyL);
        Text tSpy2  = centeredLabel("SPY",   cSpyR);
        Text tDdos  = centeredLabel("ANTI VIRUS",  cDDOS);
        Text tEnd   = centeredLabel("END",   cEnd);

        Button startBtn = new Button("Start");
        startBtn.setPrefSize(35, 15);
        startBtn.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #FF6F61, #D7263D);" +
                        "-fx-text-fill: white; -fx-background-radius: 10;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.75), 4, 0, 0, 2);"
        );
        startBtn.setLayoutX(cStart.getX() + (cStart.getWidth()-startBtn.getPrefWidth())/2.0);
        startBtn.setLayoutY(cStart.getY() - startBtn.getPrefHeight() - 4);

        double g2S = gap2(cStart), g2T = gap2(cVpnTop), g2B = gap2(cSpyL), g3D = gap3(cDDOS);

        stTri = hitAt(rightCx(cStart), cY(cStart) - g2S);
        attachTriGlyph(g, stTri, true);
        stSq  = smallSquareAt(rightCx(cStart), cY(cStart) + g2S);
        tag(stTri, "M_TRI");  tag(stSq, "M_SQ");

        vpnTopTriL = hitAt(leftCx(cVpnTop),  cY(cVpnTop));
        attachTriGlyph(g, vpnTopTriL, false);
        vpnTopSqR  = smallSquareAt(rightCx(cVpnTop), cY(cVpnTop) - g2T);
        vpnTopTriR = hitAt(rightCx(cVpnTop), cY(cVpnTop) + g2T);
        attachTriGlyph(g, vpnTopTriR, true);
        tag(vpnTopTriL, "M_TRI"); tag(vpnTopSqR, "M_SQ"); tag(vpnTopTriR, "M_TRI");

        vpnBotSqL  = smallSquareAt(leftCx(cVpnBot), cY(cVpnBot));
        vpnBotHexR = hitAt(rightCx(cVpnBot), cY(cVpnBot));
        attachHexGlyph(g, vpnBotHexR);
        tag(vpnBotSqL, "M_SQ");   tag(vpnBotHexR, "M_HEX");

        // SPY چپ
        spyL_sqL  = smallSquareAt(leftCx(cSpyL),  cY(cSpyL) - g2B);
        spyL_triL = hitAt(leftCx(cSpyL),          cY(cSpyL) + g2B);
        attachTriGlyph(g, spyL_triL, false);
        tag(spyL_sqL, "M_SQ");    tag(spyL_triL, "M_TRI");
        spyL_sqR  = smallSquareAt(rightCx(cSpyL), cY(cSpyL) - g2B);
        spyL_triR = hitAt(rightCx(cSpyL),         cY(cSpyL) + g2B);
        tag(spyL_sqR, "M_SQ");    tag(spyL_triR, "M_TRI");
        // بدون گلیف؛ نامرئی و غیرقابل‌کلیک
        spyL_triR.setMouseTransparent(true);


        // SPY راست
        spyR_sqL  = smallSquareAt(leftCx(cSpyR),  cY(cSpyR) - g2B);
        spyR_triL = hitAt(leftCx(cSpyR),          cY(cSpyR) + g2B);
        // بدون گلیف؛ نامرئی و غیرقابل‌کلیک
        spyR_triL.setMouseTransparent(true);
        spyR_sqR  = smallSquareAt(rightCx(cSpyR), cY(cSpyR) - g2B);
        spyR_triR = hitAt(rightCx(cSpyR),         cY(cSpyR) + g2B);
        attachTriGlyph(g, spyR_triR, true);
        tag(spyR_sqR, "M_SQ");    tag(spyR_triR, "M_TRI");
        tag(spyR_sqL, "M_SQ");    tag(spyR_triL, "M_TRI"); // خاموش‌ها

        ddL_sq  = smallSquareAt(leftCx(cDDOS),  cY(cDDOS) - g3D);
        ddL_tri = hitAt(leftCx(cDDOS),          cY(cDDOS));
        attachTriGlyph(g, ddL_tri, false);
        ddL_hex = hitAt(leftCx(cDDOS),          cY(cDDOS) + g3D);
        attachHexGlyph(g, ddL_hex);


        ddR_tri = hitAt(rightCx(cDDOS),         cY(cDDOS) - g3D);
        attachTriGlyph(g, ddR_tri, true);
        ddR_sq  = smallSquareAt(rightCx(cDDOS), cY(cDDOS));
        ddR_hex = hitAt(rightCx(cDDOS),         cY(cDDOS) + g3D);
        attachHexGlyph(g, ddR_hex);
        // ANTI VIRUS (DDOS)
        tag(ddL_sq,  "M_SQ");     tag(ddL_tri, "M_TRI");   tag(ddL_hex, "M_HEX");
        tag(ddR_sq,  "M_SQ");     tag(ddR_tri, "M_TRI");   tag(ddR_hex, "M_HEX");


        end_hex = hitAt(leftCx(cEnd),           cY(cEnd) - g3D);
        attachHexGlyph(g, end_hex);
        end_sq  = smallSquareAt(leftCx(cEnd),   cY(cEnd));
        end_tri = hitAt(leftCx(cEnd),           cY(cEnd) + g3D);
        attachTriGlyph(g, end_tri, false);
        tag(end_sq,  "M_SQ");     tag(end_tri, "M_TRI");   tag(end_hex, "M_HEX");


        g.getChildren().addAll(
                cStart, cVpnTop, cVpnBot, cSpyL, cSpyR, cDDOS, cEnd,
                tStart, tVpn1, tVpn2, tSpy1, tSpy2, tDdos, tEnd,
                startBtn,
                stSq, vpnTopSqR, vpnBotSqL, spyL_sqL, spyL_sqR, spyR_sqL, spyR_sqR, ddL_sq, ddR_sq, end_sq,
                stTri, vpnTopTriL, vpnTopTriR, vpnBotHexR,
                spyL_triL, spyL_triR, spyR_triL, spyR_triR,
                ddL_tri, ddL_hex, ddR_tri, ddR_hex,
                end_hex, end_tri
        );

        view = new LevelView(
                g,
                List.of(cStart, cVpnTop, cVpnBot, cSpyL, cSpyR, cDDOS, cEnd),
                List.of(stTri, vpnTopTriL, vpnTopTriR, vpnBotHexR,
                        spyL_triL, spyL_triR, spyR_triL, spyR_triR,
                        ddL_tri, ddL_hex, ddR_tri, ddR_hex,
                        end_hex, end_tri),
                List.of(stSq, vpnTopSqR, vpnBotSqL, spyL_sqL, spyL_sqR, spyR_sqL, spyR_sqR, ddL_sq, ddR_sq, end_sq),
                startBtn
        );
        return view;
    }


    @Override
    public void bind(GameController c, Slider timeSlider, Runnable onWin) {
        // ارجاع‌ها
        c.bindStageRefs(view.circles, view.smallRects, timeSlider);

        c.setLevel5Tuning(true);

        // درگ بدنه‌ها + چسباندن پورت‌ها و Start به هر بدنه
        c.enableDragSystem(view.bodies.get(0), // START
                List.of(view.startButton, stTri, stSq));

        c.enableDragSystem(view.bodies.get(1), // VPN(top)
                List.of(vpnTopTriL, vpnTopSqR, vpnTopTriR));

        c.enableDragSystem(view.bodies.get(2), // VPN(bottom)
                List.of(vpnBotSqL, vpnBotHexR));

        c.enableDragSystem(view.bodies.get(3), // SPY(left)
                List.of(spyL_sqL, spyL_triL));

        c.enableDragSystem(view.bodies.get(4), // SPY(right)
                List.of(spyR_sqR, spyR_triR));

        c.enableDragSystem(view.bodies.get(5), // DDOS
                List.of(ddL_sq, ddL_tri, ddL_hex, ddR_tri, ddR_sq, ddR_hex));

        c.enableDragSystem(view.bodies.get(6), // END
                List.of(end_hex, end_sq, end_tri));

        // سیم‌کشی: پورت‌های مثلث/شش‌ضلعی (Circle) و مربع (Rectangle)
        for (Circle cc : view.circles)      cc.setOnMousePressed(c::onStartWireFromCircle);
        for (Rectangle r : view.smallRects) r.setOnMousePressed(c::onStartWireFromSmallRect);

        // Start در ابتدا غیرفعال
        view.startButton.setDisable(true);

        if (spyL_sqR != null) { spyL_sqR.setVisible(false); spyL_sqR.setMouseTransparent(true); }
        if (spyL_triR != null){ spyL_triR.setVisible(false); spyL_triR.setMouseTransparent(true); }
        if (spyR_sqL != null) { spyR_sqL.setVisible(false); spyR_sqL.setMouseTransparent(true); }
        if (spyR_triL != null){ spyR_triL.setVisible(false); spyR_triL.setMouseTransparent(true); }



        // اعتبارسنجی دقیقِ همان الگوی اتصال تصویر
        Runnable revalidate = () -> {
            boolean ok =
                    c.isConnected(stTri, vpnTopTriL) &&
                            c.isConnected(stSq,  vpnBotSqL)  &&

                            c.isConnected(vpnTopSqR,  spyL_sqL)  &&
                            c.isConnected(vpnTopTriR, spyL_triL) &&

                            c.isConnected(spyR_sqR,  ddL_sq)    &&
                            c.isConnected(spyR_triR, ddL_tri)   &&

                            c.isConnected(vpnBotHexR, ddL_hex)  &&

                            c.isConnected(ddR_tri, end_tri)     &&
                            c.isConnected(ddR_sq,  end_sq)      &&
                            c.isConnected(ddR_hex, end_hex);

            view.startButton.setDisable(!ok);
        };

        // تایمر سبک (هر 200ms) برای ارزیابی وضعیت سیم‌کشی
        validateTimer = new Timeline(new KeyFrame(Duration.millis(200), e -> revalidate.run()));
        validateTimer.setCycleCount(Animation.INDEFINITE);
        validateTimer.play();

        // فعلاً با زدن Start می‌رویم به مرحله بعد/Win (بعداً فلوها را اضافه می‌کنیم)
        view.startButton.setOnAction(e -> {
            view.startButton.setDisable(true);

            // شمارنده‌ها
            java.util.concurrent.atomic.AtomicInteger roundsLaunched   = new java.util.concurrent.atomic.AtomicInteger();
            java.util.concurrent.atomic.AtomicInteger triArrivedVpn    = new java.util.concurrent.atomic.AtomicInteger();
            java.util.concurrent.atomic.AtomicInteger sqArrivedVpn     = new java.util.concurrent.atomic.AtomicInteger();
            java.util.concurrent.atomic.AtomicInteger spyInTri         = new java.util.concurrent.atomic.AtomicInteger();
            java.util.concurrent.atomic.AtomicInteger spyInSq          = new java.util.concurrent.atomic.AtomicInteger();
            java.util.concurrent.atomic.AtomicInteger spyPairsTriggered= new java.util.concurrent.atomic.AtomicInteger();
            java.util.concurrent.atomic.AtomicInteger reachedEnd       = new java.util.concurrent.atomic.AtomicInteger();

            Runnable checkWin = () -> {
                if (reachedEnd.get() >= 12) { // 4 دور × 3 نوع جریان
                    onWin.run();
                }
            };

            // وقتی دو ورودی SPY(left) به تعداد برابر رسیدند، خروجی‌های SPY(right) را به AV بفرست
            Runnable maybeFireSpyRight = () -> {
                int ready = Math.min(spyInTri.get(), spyInSq.get());
                while (spyPairsTriggered.get() < ready) {
                    spyPairsTriggered.incrementAndGet();

                    // SPY(right) ▸ AV(left) ▸ AV(right) ▸ END  (مثلث)
                    GameController.FlowHandle fT = c.send(spyR_triR, ddL_tri);
                    c.onArrive(fT, () -> {
                        GameController.FlowHandle toEnd = c.send(ddR_tri, end_tri);
                        c.onArrive(toEnd, () -> { reachedEnd.incrementAndGet(); checkWin.run(); });
                    });

                    // SPY(right) ▸ AV(left) ▸ AV(right) ▸ END  (مربع)
                    GameController.FlowHandle fS = c.send(spyR_sqR, ddL_sq);
                    c.onArrive(fS, () -> {
                        GameController.FlowHandle toEnd = c.send(ddR_sq, end_sq);
                        c.onArrive(toEnd, () -> { reachedEnd.incrementAndGet(); checkWin.run(); });
                    });
                }
            };

            // نگهدارنده‌ی self-referencing برای جلوگیری از ارجاع قبل از اعلان
            java.util.concurrent.atomic.AtomicReference<Runnable> launchOnePair = new java.util.concurrent.atomic.AtomicReference<>();

            // همواره یک جفت در حال حرکت به سمت VPN نگه می‌داریم (pipeline)
            // به محض اینکه یک جفت به VPN رسید، جفت بعدی را استارت کن تا سقف 4 جفت.
            Runnable maybeLaunchNextPair = () -> {
                int arrivedPairs   = Math.min(triArrivedVpn.get(), sqArrivedVpn.get());
                int targetLaunched = Math.min(4, arrivedPairs + 1); // ← یک جفت جلوتر راه بیفتد
                while (roundsLaunched.get() < targetLaunched) {
                    launchOnePair.get().run();
                }
            };

            // ارسال یک «جفت» از START (مثلث به VPN بالا، مربع به VPN پایین)
            launchOnePair.set(new Runnable() {
                @Override public void run() {
                    int r = roundsLaunched.incrementAndGet();
                    if (r > 4) { roundsLaunched.decrementAndGet(); return; }

                    // START ▸ VPN(top).L  (مثلث)
                    GameController.FlowHandle fTri0 = c.send(stTri, vpnTopTriL);
                    c.onArrive(fTri0, () -> {
                        triArrivedVpn.incrementAndGet();

                        // VPN(top) خروجی‌هایش به SPY(left)
                        GameController.FlowHandle fTri1 = c.send(vpnTopTriR, spyL_triL);
                        c.onArrive(fTri1, () -> { spyInTri.incrementAndGet(); maybeFireSpyRight.run(); });

                        GameController.FlowHandle fSq1  = c.send(vpnTopSqR,  spyL_sqL);
                        c.onArrive(fSq1,  () -> { spyInSq.incrementAndGet();  maybeFireSpyRight.run(); });

                        maybeLaunchNextPair.run();
                    });

                    // START ▸ VPN(bottom).L  (مربع)
                    GameController.FlowHandle fSq0 = c.send(stSq, vpnBotSqL);
                    c.onArrive(fSq0, () -> {
                        sqArrivedVpn.incrementAndGet();

                        // VPN(bottom) هگزا را مستقیم به AV(left) می‌فرستد
                        GameController.FlowHandle fHex = c.send(vpnBotHexR, ddL_hex);
                        c.onArrive(fHex, () -> {
                            // در AV توقف نداریم: مستقیم به END
                            GameController.FlowHandle toEnd = c.send(ddR_hex, end_hex);
                            c.onArrive(toEnd, () -> { reachedEnd.incrementAndGet(); checkWin.run(); });
                        });

                        maybeLaunchNextPair.run();
                    });
                }
            });

            // شروع اولین جفت
            launchOnePair.get().run();
        });






    }

    @Override
    public LevelView getView() { return view; }

    @Override
    public void dispose() {
        if (validateTimer != null) {
            validateTimer.stop();
            validateTimer = null;
        }
    }
}