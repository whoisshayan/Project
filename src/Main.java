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
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.scene.paint.Color;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;
import javafx.scene.Node;


public class Main extends Application {

    private Circle startCircle = null;
    private Rectangle startSmallRect = null;
    private Line currentLine = null;
    private List<Circle> circles;
    private List<Rectangle> smallRects;
    private Pane mainRoot;
    private boolean circlesConnected = false;
    private boolean rectsConnected   = false;

    private double wire_size = 1000;
    private Text remainingWireText;

    private int validSteam=0;

    @Override
    public void start(Stage primaryStage) {
        // Load main menu background image
        File bgFile = new File("D:\\university\\AP\\project\\Menu background.png");
        Image bgImage = new Image(bgFile.toURI().toString());
        ImageView bgView = new ImageView(bgImage);
        bgView.setFitWidth(700);
        bgView.setFitHeight(600);
        bgView.setPreserveRatio(false);

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
        DropShadow shadow = new DropShadow();
        shadow.setRadius(20);
        shadow.setOffsetX(0);
        shadow.setOffsetY(0);
        shadow.setColor(Color.WHITE);

        Button startButton = new Button("Start");
        startButton.setLayoutX(290);
        startButton.setLayoutY(205);
        startButton.setPrefSize(120, 50);
        startButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 16px;");
        startButton.setOnMouseEntered(e -> startButton.setEffect(shadow));
        startButton.setOnMouseExited(e -> startButton.setEffect(null));

        Button levelButton = new Button("Levels");
        levelButton.setLayoutX(290);
        levelButton.setLayoutY(285);
        levelButton.setPrefSize(120, 50);
        levelButton.setStyle("-fx-background-color: purple; -fx-text-fill: white; -fx-font-size: 16px;");
        levelButton.setOnMouseEntered(e -> levelButton.setEffect(shadow));
        levelButton.setOnMouseExited(e -> levelButton.setEffect(null));

        Button settingButton = new Button("Settings");
        settingButton.setLayoutX(290);
        settingButton.setLayoutY(365);
        settingButton.setPrefSize(120, 50);
        settingButton.setStyle("-fx-background-color: red; -fx-text-fill: white; -fx-font-size: 16px;");
        settingButton.setOnMouseEntered(e -> settingButton.setEffect(shadow));
        settingButton.setOnMouseExited(e -> settingButton.setEffect(null));

        Button exitButton = new Button("Exit");
        exitButton.setLayoutX(290);
        exitButton.setLayoutY(445);
        exitButton.setPrefSize(120, 50);
        exitButton.setStyle("-fx-background-color: orange; -fx-text-fill: white; -fx-font-size: 16px;");
        exitButton.setOnAction(e -> Platform.exit());
        exitButton.setOnMouseEntered(e -> exitButton.setEffect(shadow));
        exitButton.setOnMouseExited(e -> exitButton.setEffect(null));

        // Main menu pane
        Pane root = new Pane();
        root.getChildren().add(bgView);
        root.getChildren().addAll(startButton, levelButton, settingButton, exitButton, line1, line2);
        Scene menuScene = new Scene(root, 700, 600);

        // Settings scene
        Button backButton = new Button("Back");
        backButton.setLayoutX(290);
        backButton.setLayoutY(465);
        backButton.setPrefSize(120, 50);
        backButton.setStyle("-fx-background-color: red; -fx-text-fill: white; -fx-font-size: 16px;");
        backButton.setOnAction(e -> primaryStage.setScene(menuScene));
        backButton.setOnMouseEntered(e -> backButton.setEffect(shadow));
        backButton.setOnMouseExited(e -> backButton.setEffect(null));

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

        Slider volumeSlider = new Slider(0, 1, 0);
        volumeSlider.setLayoutX(123);
        volumeSlider.setLayoutY(290);
        volumeSlider.setPrefWidth(470);
        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> mediaPlayer.setVolume(newVal.doubleValue()));
        mediaPlayer.setVolume(volumeSlider.getValue());
        volumeSlider.setShowTickLabels(true);
        volumeSlider.setMajorTickUnit(0.25);
        volumeSlider.setBlockIncrement(0.25);
        volumeSlider.setStyle(
                "-fx-accent: #4286f4; " +
                        "-fx-control-inner-background: green;"
        );

        Pane settingRoot = new Pane();
        settingRoot.getChildren().add(bgView2);
        settingRoot.getChildren().addAll(backButton, muteButton, volumeSlider);
        Scene settingScene = new Scene(settingRoot, 700, 600);
        settingButton.setOnAction(e -> primaryStage.setScene(settingScene));

        // Main game scene
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

        Button startSteam = new Button("Start");
        startSteam.setLayoutX(230);
        startSteam.setLayoutY(240);
        startSteam.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #FF6F61, #D7263D);" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 10;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.75), 4, 0, 0, 2);"
        );
        startSteam.setOnMouseEntered(e -> startSteam.setEffect(shadow));
        startSteam.setOnMouseExited(e -> startSteam.setEffect(null));

        Group group=new Group(circle,littlerec,mainrect2,circle2,littlerec2);
        mainRoot.getChildren().add(group);
        startSteam.setOnAction(actionEvent -> {
            if (circlesConnected && rectsConnected) {
                Circle steamCircle = new Circle(circle.getCenterX(), circle.getCenterY(), 5);
                steamCircle.setFill(Color.YELLOW);
                steamCircle.setStroke(Color.BLACK);
                steamCircle.setStrokeWidth(1);
                mainRoot.getChildren().add(steamCircle);
                TranslateTransition tt = new TranslateTransition(Duration.seconds(2), steamCircle);
                tt.setByX(circle2.getCenterX()-circle.getCenterX());
                tt.setByY(circle2.getCenterY()-circle.getCenterY());
                tt.setCycleCount(1);
                tt.setAutoReverse(false);
                tt.setOnFinished(ev -> {
                    steamCircle.setVisible(false);
                    validSteam++;
                    if (validSteam == 2) {
                        System.out.println("OK!");
                        secondLevel(primaryStage,mainRoot,menuScene,group);
                    }
                });
                tt.play();

                Rectangle steamRectangle = new Rectangle(littlerec.getX()+2, littlerec.getY()+5, 5, 10);
                steamRectangle.setFill(Color.YELLOW);
                steamRectangle.setStroke(Color.BLACK);
                steamRectangle.setStrokeWidth(1);
                mainRoot.getChildren().add(steamRectangle);
                TranslateTransition tt2 = new TranslateTransition(Duration.seconds(2), steamRectangle);
                tt2.setByX(littlerec2.getX()-littlerec.getX());
                tt2.setByY(littlerec2.getY()-littlerec.getY());
                tt2.setCycleCount(1);
                tt2.setAutoReverse(false);
                tt2.setOnFinished(ev -> {
                    steamRectangle.setVisible(false);
                    validSteam++;
                    if (validSteam == 2) {
                        System.out.println("OK!");
                        secondLevel(primaryStage,mainRoot,menuScene,group);
                    }
                });
                tt2.play();
            }
        });

        mainRoot.getChildren().add(startSteam);
        Rectangle upRectangle = new Rectangle(0, 0, 900, 115);
        upRectangle.setArcWidth(30);
        upRectangle.setArcHeight(30);
        upRectangle.setFill(Color.web("#663399"));
        upRectangle.setStroke(Color.BLACK);
        upRectangle.setStrokeWidth(2);
        mainRoot.getChildren().add(upRectangle);

        // Remaining Wire UI
        Rectangle remainingWireBox = new Rectangle(100, 8, 120, 100);
        remainingWireBox.setFill(Color.PURPLE);
        remainingWireBox.setArcWidth(10);
        remainingWireBox.setArcHeight(10);

        remainingWireText = new Text("Remaining\n     Wire:\n      " + (int)wire_size);
        remainingWireText.setLayoutX(106);
        remainingWireText.setLayoutY(40);
        remainingWireText.setFont(Font.font("Verdana", FontWeight.BOLD, FontPosture.ITALIC, 16));
        remainingWireText.setFill(Color.web("#FFE4E1"));
        DropShadow dsWire = new DropShadow();
        dsWire.setOffsetY(3.0);
        dsWire.setOffsetX(3.0);
        dsWire.setColor(Color.color(0, 0, 0, 0.4));
        remainingWireText.setEffect(dsWire);

        mainRoot.getChildren().addAll(remainingWireBox, remainingWireText);

        // Time Progress UI
        Rectangle timeProgressBox = new Rectangle(300, 8, 120, 100);
        timeProgressBox.setFill(Color.PURPLE);
        timeProgressBox.setArcWidth(10);
        timeProgressBox.setArcHeight(10);
        Text timeProgressText = new Text("    Time\nProgress:");
        timeProgressText.setLayoutX(316);
        timeProgressText.setLayoutY(40);
        timeProgressText.setFont(Font.font("Verdana", FontWeight.BOLD, FontPosture.ITALIC, 16));
        timeProgressText.setFill(Color.web("#FFE4E1"));
        DropShadow ds2 = new DropShadow();
        ds2.setOffsetY(3.0);
        ds2.setOffsetX(3.0);
        ds2.setColor(Color.color(0, 0, 0, 0.4));
        timeProgressText.setEffect(ds2);
        mainRoot.getChildren().addAll(timeProgressBox, timeProgressText);

        // Packet loss UI
        Rectangle packetLossBox = new Rectangle(500, 8, 120, 100);
        packetLossBox.setFill(Color.PURPLE);
        packetLossBox.setArcWidth(10);
        packetLossBox.setArcHeight(10);
        Text packetLossText = new Text("Packet Loss:");
        packetLossText.setLayoutX(514);
        packetLossText.setLayoutY(40);
        packetLossText.setFont(Font.font("Verdana", FontWeight.BOLD, FontPosture.ITALIC, 14));
        packetLossText.setFill(Color.web("#FFE4E1"));
        DropShadow ds3 = new DropShadow();
        ds3.setOffsetY(3.0);
        ds3.setOffsetX(3.0);
        ds3.setColor(Color.color(0, 0, 0, 0.4));
        packetLossText.setEffect(ds3);
        mainRoot.getChildren().addAll(packetLossBox, packetLossText);

        // Coins UI
        Rectangle coinsBox = new Rectangle(700, 8, 120, 100);
        coinsBox.setFill(Color.PURPLE);
        coinsBox.setArcWidth(10);
        coinsBox.setArcHeight(10);
        Text coinsText = new Text("    Coins:");
        coinsText.setLayoutX(714);
        coinsText.setLayoutY(40);
        coinsText.setFont(Font.font("Verdana", FontWeight.BOLD, FontPosture.ITALIC, 14));
        coinsText.setFill(Color.web("#FFE4E1"));
        DropShadow ds4 = new DropShadow();
        ds4.setOffsetY(3.0);
        ds4.setOffsetX(3.0);
        ds4.setColor(Color.color(0, 0, 0, 0.4));
        coinsText.setEffect(ds4);
        mainRoot.getChildren().addAll(coinsBox, coinsText);

        // Down UI
        Rectangle downRectangle = new Rectangle(0, 600, 900, 115);
        downRectangle.setArcWidth(30);
        downRectangle.setArcHeight(30);
        downRectangle.setFill(Color.web("#663399"));
        downRectangle.setStroke(Color.BLACK);
        downRectangle.setStrokeWidth(2);
        mainRoot.getChildren().add(downRectangle);

        // Back to menu from main
        Button backToMenuButton = new Button("Back");
        backToMenuButton.setStyle("-fx-background-color: red; -fx-text-fill: white; -fx-font-size: 16px;");
        backToMenuButton.setLayoutX(650);
        backToMenuButton.setLayoutY(610);
        backToMenuButton.setPrefWidth(160);
        backToMenuButton.setPrefHeight(80);
        backToMenuButton.setOnAction(actionEvent -> primaryStage.setScene(menuScene));
        backToMenuButton.setOnMouseEntered(e -> backToMenuButton.setEffect(shadow));
        backToMenuButton.setOnMouseExited(e -> backToMenuButton.setEffect(null));
        mainRoot.getChildren().add(backToMenuButton);

        // Shop Button
        Button shopButton = new Button("Shop");
        shopButton.setStyle("-fx-background-color: red; -fx-text-fill: white; -fx-font-size: 16px;");
        shopButton.setLayoutX(80);
        shopButton.setLayoutY(610);
        shopButton.setPrefWidth(160);
        shopButton.setPrefHeight(80);
        shopButton.setOnMouseEntered(e -> shopButton.setEffect(shadow));
        shopButton.setOnMouseExited(e -> shopButton.setEffect(null));
        mainRoot.getChildren().add(shopButton);

        Scene mainScene = new Scene(mainRoot, 900, 700);
        startButton.setOnAction(actionEvent -> primaryStage.setScene(mainScene));

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
                if (target == startCircle) continue;
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
                if (target == startSmallRect) continue;
                double centerX = target.getX() + target.getWidth()/2;
                double centerY = target.getY() + target.getHeight()/2;
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

        if (hitCircle) circlesConnected = true;
        if (hitRect) rectsConnected = true;

        if ((hitCircle || hitRect) && wire_size > 0) {
            double dx = currentLine.getEndX() - currentLine.getStartX();
            double dy = currentLine.getEndY() - currentLine.getStartY();
            double length = Math.hypot(dx, dy);
            wire_size -= length;
            if (wire_size < 0) wire_size = 0;
            remainingWireText.setText("Remaining\n     Wire:\n      " + (int)wire_size);
        }

        currentLine    = null;
        startCircle    = null;
        startSmallRect = null;
        e.consume();
    }

    private void secondLevel(Stage stage,Pane mainRoot,Scene menuscene,Group group){

        // Win Box
        Rectangle winShowRectangle=new Rectangle(270,260,300,180);
        winShowRectangle.setFill(Color.web("#663399"));
        winShowRectangle.setArcWidth(30);
        winShowRectangle.setArcHeight(30);
        mainRoot.getChildren().add(winShowRectangle);

        // Text For Win Box
        Text winText=new Text("You Win!");
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


        DropShadow shadow = new DropShadow();
        shadow.setRadius(8);
        shadow.setOffsetY(4);
        shadow.setColor(Color.color(0, 0, 0, 0.6));

        // Back to menu from win box button
        Button backToMenuFromWinBoxButton=new Button("Back");
        backToMenuFromWinBoxButton.setLayoutX(290);
        backToMenuFromWinBoxButton.setLayoutY(365);
        backToMenuFromWinBoxButton.setPrefHeight(35);
        backToMenuFromWinBoxButton.setPrefSize(120, 50);
        backToMenuFromWinBoxButton.setStyle("-fx-background-color: purple; -fx-text-fill: white; -fx-font-size: 16px;");
        backToMenuFromWinBoxButton.addEventHandler(MouseEvent.MOUSE_ENTERED, e -> backToMenuFromWinBoxButton.setEffect(shadow));
        backToMenuFromWinBoxButton.addEventHandler(MouseEvent.MOUSE_EXITED, e -> backToMenuFromWinBoxButton.setEffect(null));
        backToMenuFromWinBoxButton.setOnAction(actionEvent -> stage.setScene(menuscene));
        mainRoot.getChildren().add(backToMenuFromWinBoxButton);

        // Next level Button
        Button nextLevelButton=new Button("Next Level");
        nextLevelButton.setLayoutX(420);
        nextLevelButton.setLayoutY(365);
        nextLevelButton.setPrefHeight(35);
        nextLevelButton.setPrefSize(120, 50);
        nextLevelButton.setStyle("-fx-background-color: purple; -fx-text-fill: white; -fx-font-size: 16px;");
        nextLevelButton.addEventHandler(MouseEvent.MOUSE_ENTERED, e -> nextLevelButton.setEffect(shadow));
        nextLevelButton.addEventHandler(MouseEvent.MOUSE_EXITED, e -> nextLevelButton.setEffect(null));
        mainRoot.getChildren().add(nextLevelButton);

        // Second Scene
        nextLevelButton.setOnAction(actionEvent -> {
            mainRoot.getChildren().removeIf(node -> node instanceof Line);
            mainRoot.getChildren().remove(group);
            mainRoot.getChildren().removeAll(winText,winShowRectangle,nextLevelButton,backToMenuFromWinBoxButton);
            circlesConnected = false;
            rectsConnected   = false;
            validSteam       = 0;
            wire_size        = 1000;
            remainingWireText.setText("Remaining\n     Wire:\n      " + (int)wire_size);

//            Rectangle mainrect = new Rectangle(200, 250, 100, 200);
//            mainrect.setFill(Color.CORAL);
//            mainrect.setStroke(Color.BLACK);
//            mainrect.setStrokeWidth(2);
//            mainrect.setArcWidth(20);
//            mainrect.setArcHeight(20);

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

            Rectangle mainrect2 = new Rectangle(380, 380, 100, 200);
            mainrect2.setFill(Color.CORAL);
            mainrect2.setStroke(Color.BLACK);
            mainrect2.setStrokeWidth(2);
            mainrect2.setArcWidth(20);
            mainrect2.setArcHeight(20);

            Circle circle2 = new Circle(380, 420, 10);
            circle2.setFill(Color.LIGHTGREEN);
            circle2.setStroke(Color.DARKGREEN);
            circle2.setStrokeWidth(3);

            Rectangle littlerec2 = new Rectangle(375, 500, 10, 20);
            littlerec2.setFill(Color.GREEN);
            littlerec2.setStroke(Color.BLACK);
            littlerec2.setStrokeWidth(2);
            littlerec2.setArcWidth(2);
            littlerec2.setArcHeight(2);

            Rectangle littlerec3 = new Rectangle(475, 410, 10, 20);
            littlerec3.setFill(Color.GREEN);
            littlerec3.setStroke(Color.BLACK);
            littlerec3.setStrokeWidth(2);
            littlerec3.setArcWidth(2);
            littlerec3.setArcHeight(2);

            Circle circle3 = new Circle(480, 510, 10);
            circle3.setFill(Color.LIGHTGREEN);
            circle3.setStroke(Color.DARKGREEN);
            circle3.setStrokeWidth(3);


            Rectangle mainrect3 = new Rectangle(610, 250, 100, 200);
            mainrect3.setFill(Color.CORAL);
            mainrect3.setStroke(Color.BLACK);
            mainrect3.setStrokeWidth(2);
            mainrect3.setArcWidth(20);
            mainrect3.setArcHeight(20);

            Circle circle4 = new Circle(610, 290, 10);
            circle4.setFill(Color.LIGHTGREEN);
            circle4.setStroke(Color.DARKGREEN);
            circle4.setStrokeWidth(3);

            Rectangle littlerec4 = new Rectangle(605, 370, 10, 20);
            littlerec4.setFill(Color.GREEN);
            littlerec4.setStroke(Color.BLACK);
            littlerec4.setStrokeWidth(2);
            littlerec4.setArcWidth(2);
            littlerec4.setArcHeight(2);


            mainRoot.getChildren().addAll(mainrect2,circle2,circle,littlerec,littlerec2,circle3,littlerec3,mainrect3,circle4,littlerec4);

        });

    }

    public static void main(String[] args) {
        launch(args);
    }
}