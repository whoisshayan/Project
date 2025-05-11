import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.control.Slider;
import javafx.scene.effect.DropShadow;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import java.io.File;
import java.util.List;

import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.scene.paint.Color;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;

public class Main extends Application {

    private Circle startCircle = null;
    private Rectangle startSmallRect = null;
    private Line currentLine = null;
    private List<Circle> circles;
    private List<Rectangle> smallRects;
    private Pane mainRoot;
    private boolean circlesConnected = false;
    private boolean rectsConnected   = false;



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
        Slider volumeSlider = new Slider(0, 1, 0);  // min, max, initial volume
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
        mainRoot = new Pane();
        mainRoot.setStyle("-fx-background-color: purple;");

        // Shapes
        Rectangle mainrect = new Rectangle(200, 250, 100, 200);
        mainrect.setFill(Color.CORAL);
        mainrect.setStroke(Color.BLACK);
        mainrect.setStrokeWidth(2);
        mainrect.setArcWidth(20);
        mainrect.setArcHeight(20);

        Circle circle = new Circle(300, 290, 10);
        circle.setFill(Color.LIGHTGREEN);
        circle.setStroke(Color.DARKGREEN);
        circle.setStrokeWidth(3);

        Rectangle littlerec = new Rectangle(295, 370, 10, 20);
        littlerec.setFill(Color.GREEN);
        littlerec.setStroke(Color.BLACK);
        littlerec.setStrokeWidth(2);
        littlerec.setArcWidth(2);
        littlerec.setArcHeight(2);

        Rectangle mainrect2 = new Rectangle(500, 250, 100, 200);
        mainrect2.setFill(Color.CORAL);
        mainrect2.setStroke(Color.BLACK);
        mainrect2.setStrokeWidth(2);
        mainrect2.setArcWidth(20);
        mainrect2.setArcHeight(20);

        Circle circle2 = new Circle(500, 290, 10);
        circle2.setFill(Color.LIGHTGREEN);
        circle2.setStroke(Color.DARKGREEN);
        circle2.setStrokeWidth(3);

        Rectangle littlerec2 = new Rectangle(495, 370, 10, 20);
        littlerec2.setFill(Color.GREEN);
        littlerec2.setStroke(Color.BLACK);
        littlerec2.setStrokeWidth(2);
        littlerec2.setArcWidth(2);
        littlerec2.setArcHeight(2);

        mainRoot.getChildren().addAll(
                mainrect, circle, littlerec,
                mainrect2, circle2, littlerec2
        );
        circles = List.of(circle, circle2);
        smallRects = List.of(littlerec, littlerec2);

        for (Circle c : circles) {
            c.setOnMousePressed(this::onStartWireFromCircle);
        }
        for (Rectangle r : smallRects) {
            r.setOnMousePressed(this::onStartWireFromSmallRect);
        }

        mainRoot.setOnMouseDragged(this::onDragWire);
        mainRoot.setOnMouseReleased(this::onReleaseWire);
        Button startSteam=new Button("Start");
        startSteam.setLayoutX(230);
        startSteam.setLayoutY(240);
        startSteam.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #FF6F61, #D7263D);" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 10;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.75), 4, 0, 0, 2);"
        );
        mainRoot.getChildren().add(startSteam);
        startSteam.setOnAction(actionEvent -> {
            if(circlesConnected && rectsConnected){
                Circle steamCircle=new Circle(300,290,5);
                steamCircle.setFill(Color.YELLOW);
                steamCircle.setStroke(Color.BLACK);
                steamCircle.setStrokeWidth(1);
                Rectangle steamRectangle=new Rectangle(297.5,375,5,10);
                steamRectangle.setFill(Color.YELLOW);
                steamRectangle.setStroke(Color.BLACK);
                steamRectangle.setStrokeWidth(1);
                mainRoot.getChildren().add(steamCircle);
                mainRoot.getChildren().add(steamRectangle);
                Group steamShapes=new Group(steamCircle,steamRectangle);
                mainRoot.getChildren().add(steamShapes);
                TranslateTransition tt=new TranslateTransition(Duration.seconds(2),steamShapes);
                tt.setByX(200);
//              tt.setByY(300);
                tt.setCycleCount(1);
                tt.setAutoReverse(false);
                tt.play();
            }
        });
        Button backToMenuButton=new Button("Back");
        backToMenuButton.setStyle("-fx-background-color: red; -fx-text-fill: white; -fx-font-size: 16px;");
        backToMenuButton.setLayoutX(800);
        backToMenuButton.setLayoutY(10);
        mainRoot.getChildren().add(backToMenuButton);
        backToMenuButton.setOnAction(actionEvent -> primaryStage.setScene(menuScene));
        Scene mainScene =new Scene(mainRoot,900,700);
        startButton.setOnAction(actionEvent -> primaryStage.setScene(mainScene));


        // Launch the menu scene
        primaryStage.setTitle("BluePrint Hell");
        primaryStage.setScene(menuScene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }


    private void onStartWireFromCircle(MouseEvent e) {
        startCircle = (Circle) e.getSource();
        startSmallRect = null;

        Point2D p = startCircle.localToScene(
                startCircle.getCenterX(),
                startCircle.getCenterY()
        );
        currentLine = new Line(
                p.getX(), p.getY(),
                e.getSceneX(), e.getSceneY()
        );
        currentLine.setStrokeWidth(2);
        mainRoot.getChildren().add(currentLine);
        e.consume();
    }

    private void onStartWireFromSmallRect(MouseEvent e) {
        startSmallRect = (Rectangle) e.getSource();
        startCircle = null;

        double centerX = startSmallRect.getX() + startSmallRect.getWidth()/2;
        double centerY = startSmallRect.getY() + startSmallRect.getHeight()/2;
        Point2D p = startSmallRect.localToScene(centerX, centerY);
        currentLine = new Line(
                p.getX(), p.getY(),
                e.getSceneX(), e.getSceneY()
        );
        currentLine.setStrokeWidth(2);
        mainRoot.getChildren().add(currentLine);
        e.consume();
    }

    private void onDragWire(MouseEvent e) {
        if (currentLine != null) {
            currentLine.setEndX(e.getSceneX());
            currentLine.setEndY(e.getSceneY());
        }
    }

    private void onReleaseWire(MouseEvent e) {
        if (currentLine == null) {
            return;
        }

        boolean hitCircle = false;
        boolean hitRect   = false;

        // try connecting from a circle
        if (startCircle != null) {
            for (Circle target : circles) {
                if (target == startCircle) {
                    continue;
                }
                Point2D center = target.localToScene(
                        target.getCenterX(), target.getCenterY()
                );
                double dx = center.getX() - e.getSceneX();
                double dy = center.getY() - e.getSceneY();
                if (Math.hypot(dx, dy) <= target.getRadius()) {
                    currentLine.setEndX(center.getX());
                    currentLine.setEndY(center.getY());
                    hitCircle = true;
                    break;
                }
            }
        }
        // try connecting from a small rectangle
        else if (startSmallRect != null) {
            Point2D scenePoint = new Point2D(e.getSceneX(), e.getSceneY());
            for (Rectangle target : smallRects) {
                if (target == startSmallRect) {
                    continue;
                }
                double centerX = target.getX() + target.getWidth()  / 2;
                double centerY = target.getY() + target.getHeight() / 2;
                Point2D center = target.localToScene(centerX, centerY);

                Point2D localPoint = target.sceneToLocal(scenePoint);
                if (target.contains(localPoint)) {
                    currentLine.setEndX(center.getX());
                    currentLine.setEndY(center.getY());
                    hitRect = true;
                    break;
                }
            }
        }

        // remove the line if no valid connection was made
        if (!hitCircle && !hitRect) {
            mainRoot.getChildren().remove(currentLine);
        }

        // update connection flags without resetting the other
        if (hitCircle) {
            circlesConnected = true;
        }
        if (hitRect) {
            rectsConnected = true;
        }

        // reset drag state
        currentLine    = null;
        startCircle    = null;
        startSmallRect = null;
        e.consume();
    }






    public static void main(String[] args) {
        launch(args);
    }
}