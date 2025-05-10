import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Slider;
import javafx.scene.effect.DropShadow;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import java.io.File;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.scene.paint.Color;
import javafx.scene.input.MouseEvent;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Load main menu background image
        File bgFile = new File("D:\\university\\AP\\project\\Menu background.png");
        Image bgImage = new Image(bgFile.toURI().toString());
        ImageView bgView = new ImageView(bgImage);
        bgView.setFitWidth(700);
        bgView.setFitHeight(600);
        bgView.setPreserveRatio(false);  // Stretch to fit

        // Load settings menu background image
        File bgFile2 = new File("D:/university/AP/project/Setting background.png");
        Image bgImage2 = new Image(bgFile2.toURI().toString());
        ImageView bgView2 = new ImageView(bgImage2);
        bgView2.setFitWidth(700);
        bgView2.setFitHeight(600);
        bgView2.setPreserveRatio(false);

        // Title text
        Text line1 = new Text(155, 100, "BLUEPRINT");
        Text line2 = new Text(275, 180, "HELL");
        line1.setFont(Font.font("Impact", 90));
        line2.setFont(Font.font("Impact", 90));
        line1.setFill(Color.WHITE);
        line2.setFill(Color.WHITE);

        // Load and play background music
        File file = new File("C:/Users/shaya/Desktop/song/202. Bagatelle in A minor, WoO 59, Fur Elise.mp3");
        if (!file.exists()) {
            System.out.println("Music File Path Error");
            return;
        }
        String url = file.toURI().toString();
        Media media = new Media(url);
        MediaPlayer mediaPlayer = new MediaPlayer(media);
        mediaPlayer.play();

        // Create main menu buttons
        Button startButton = new Button("Start");
        startButton.setLayoutX(290);
        startButton.setLayoutY(205);
        startButton.setPrefSize(120, 50);
        startButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 16px;");
        startButton.setOnAction(e -> System.out.println("Start clicked"));

        Button levelButton = new Button("Levels");
        levelButton.setLayoutX(290);
        levelButton.setLayoutY(285);
        levelButton.setPrefSize(120, 50);
        levelButton.setStyle("-fx-background-color: purple; -fx-text-fill: white; -fx-font-size: 16px;");
        levelButton.setOnAction(e -> System.out.println("Levels clicked"));

        Button settingButton = new Button("Settings");
        settingButton.setLayoutX(290);
        settingButton.setLayoutY(365);
        settingButton.setPrefSize(120, 50);
        settingButton.setStyle("-fx-background-color: red; -fx-text-fill: white; -fx-font-size: 16px;");

        Button exitButton = new Button("Exit");
        exitButton.setLayoutX(290);
        exitButton.setLayoutY(445);
        exitButton.setPrefSize(120, 50);
        exitButton.setStyle("-fx-background-color: orange; -fx-text-fill: white; -fx-font-size: 16px;");
        exitButton.setOnAction(e -> Platform.exit());

        // Add hover effect (white shadow) to buttons
        DropShadow shadow = new DropShadow();
        shadow.setRadius(20);
        shadow.setOffsetX(0);
        shadow.setOffsetY(0);
        shadow.setColor(Color.WHITE);

        startButton.setOnMouseEntered(e -> startButton.setEffect(shadow));
        startButton.setOnMouseExited(e -> startButton.setEffect(null));

        levelButton.setOnMouseEntered(e -> levelButton.setEffect(shadow));
        levelButton.setOnMouseExited(e -> levelButton.setEffect(null));

        settingButton.setOnMouseEntered(e -> settingButton.setEffect(shadow));
        settingButton.setOnMouseExited(e -> settingButton.setEffect(null));

        exitButton.setOnMouseEntered(e -> exitButton.setEffect(shadow));
        exitButton.setOnMouseExited(e -> exitButton.setEffect(null));

        // Create the main menu pane and scene
        Pane root = new Pane();
        root.getChildren().add(bgView);
        root.getChildren().addAll(startButton, levelButton, settingButton, exitButton);
        root.getChildren().addAll(line1, line2);
        Scene menuScene = new Scene(root, 700, 600);

        // =============== SECOND SCENE (SETTINGS) ===============

        // Back button to return to main menu
        Button backButton = new Button("Back");
        backButton.setLayoutX(290);
        backButton.setLayoutY(465);
        backButton.setPrefSize(120, 50);
        backButton.setStyle("-fx-background-color: red; -fx-text-fill: white; -fx-font-size: 16px;");
        backButton.setOnAction(e -> primaryStage.setScene(menuScene));
        backButton.setOnMouseEntered(e -> backButton.setEffect(shadow));
        backButton.setOnMouseExited(e -> backButton.setEffect(null));

        // Mute/unmute button
        Button muteButton = new Button("Mute");
        muteButton.setLayoutX(290);
        muteButton.setLayoutY(365);
        muteButton.setPrefSize(120, 50);
        muteButton.setStyle("-fx-background-color: red; -fx-text-fill: white; -fx-font-size: 16px;");
        muteButton.setOnAction(e -> {
            if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                mediaPlayer.pause();
                muteButton.setText("Unmute");
            } else {
                mediaPlayer.play();
                muteButton.setText("Mute");
            }
        });
        muteButton.setOnMouseEntered(e -> muteButton.setEffect(shadow));
        muteButton.setOnMouseExited(e -> muteButton.setEffect(null));

        // Volume control slider
        Slider volumeSlider = new Slider(0, 1, 0.5);  // min, max, initial volume
        volumeSlider.setLayoutX(270);
        volumeSlider.setLayoutY(280);
        volumeSlider.setPrefWidth(160);
        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> mediaPlayer.setVolume(newVal.doubleValue()));
        mediaPlayer.setVolume(volumeSlider.getValue());
        volumeSlider.setShowTickLabels(true);
        volumeSlider.setMajorTickUnit(0.25);
        volumeSlider.setBlockIncrement(0.25);
        volumeSlider.setLayoutX(123);
        volumeSlider.setLayoutY(290);
        volumeSlider.setPrefWidth(470);
        volumeSlider.setStyle(
                "-fx-accent: #4286f4; " +   // changes the filled portion’s color
                        "-fx-control-inner-background: green; " // track color
        );


        // Create settings pane and scene
        Pane settingRoot = new Pane();
        settingRoot.getChildren().add(bgView2);
        settingRoot.getChildren().addAll(backButton, muteButton, volumeSlider);
        Scene settingScene = new Scene(settingRoot, 700, 600);

        // Switch to settings scene when settings button is clicked
        settingButton.setOnAction(e -> primaryStage.setScene(settingScene));

        // Launch the main scene
        Pane mainRoot =new Pane();
        Scene mainScene =new Scene(mainRoot,700,600);
        startButton.setOnAction(actionEvent -> primaryStage.setScene(mainScene));
        Button shopButton=new Button("Shop");
        Button menuButton=new Button("Menu");
        mainRoot.getChildren().add(menuButton);
        mainRoot.getChildren().add(shopButton);

        // Launch the main scene
        primaryStage.setTitle("BluePrint Hell");
        primaryStage.setScene(menuScene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}