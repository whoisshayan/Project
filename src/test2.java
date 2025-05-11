import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import javafx.util.Duration;

public class test2 extends Application {

    private Line tempLine;
    private boolean dragging = false;
    private Circle startCircle;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {

        Pane root = new Pane();

        Circle circle1 = new Circle(100, 100, 15);
        circle1.setFill(Color.RED);
        circle1.setStroke(Color.BLACK);
        circle1.setStrokeWidth(1);
        root.getChildren().add(circle1);

        Circle circle2 = new Circle(400, 400, 15);
        circle2.setFill(Color.GREEN);
        circle2.setStroke(Color.BLACK);
        circle2.setStrokeWidth(1);
        root.getChildren().add(circle2);

        Circle circle3 = new Circle(100, 200, 15);
        circle3.setFill(Color.RED);
        circle3.setStroke(Color.BLACK);
        circle3.setStrokeWidth(1);
        root.getChildren().add(circle3);

        Circle circle4 = new Circle(400, 300, 15);
        circle4.setFill(Color.GREEN);
        circle4.setStroke(Color.BLACK);
        circle4.setStrokeWidth(1);
        root.getChildren().add(circle4);

        circle1.setOnMousePressed(e -> startDrag(circle1, root));
        circle2.setOnMousePressed(e -> startDrag(circle2, root));
        circle3.setOnMousePressed(e -> startDrag(circle3, root));
        circle4.setOnMousePressed(e -> startDrag(circle4, root));

        Button startButton = new Button("Start");
        startButton.setLayoutX(600);
        startButton.setLayoutY(600);
        root.getChildren().add(startButton);

        Scene scene = new Scene(root, 900, 700);

        scene.setOnMouseDragged(e -> {
            if (dragging && tempLine != null) {
                tempLine.setEndX(e.getX());
                tempLine.setEndY(e.getY());
            }
        });

        scene.setOnMouseReleased(e -> {
            if (!dragging || tempLine == null || startCircle == null) return;

            Circle target = null;
            if (startCircle == circle1)      target = circle2;
            else if (startCircle == circle2) target = circle1;
            else if (startCircle == circle3) target = circle4;
            else if (startCircle == circle4) target = circle3;
            if (target == null) return;

            final Circle finalTarget = target;

            double releaseX = e.getX();
            double releaseY = e.getY();
            double dx = releaseX - target.getCenterX();
            double dy = releaseY - target.getCenterY();
            double distance = Math.hypot(dx, dy);

            if (distance <= target.getRadius()) {
                tempLine.setEndX(target.getCenterX());
                tempLine.setEndY(target.getCenterY());

                startButton.setStyle(
                        "-fx-background-color: #2196F3; " +
                                "-fx-text-fill: yellow; " +
                                "-fx-background-radius: 5; " +
                                "-fx-font-size: 14px;"
                );

                startButton.setOnAction(actionEvent -> {
                    Circle steamCircle1 = new Circle(circle1.getCenterX(), circle1.getCenterY(), 5);
                    steamCircle1.setFill(Color.YELLOW);
                    steamCircle1.setStroke(Color.BLACK);
                    steamCircle1.setStrokeWidth(1);

                    Circle steamCircle2 = new Circle(circle3.getCenterX(), circle3.getCenterY(), 5);
                    steamCircle2.setFill(Color.YELLOW);
                    steamCircle2.setStroke(Color.BLACK);
                    steamCircle2.setStrokeWidth(1);

                    root.getChildren().addAll(steamCircle1, steamCircle2);

                    TranslateTransition tt1 = new TranslateTransition(Duration.seconds(1.7), steamCircle1);
                    tt1.setByX(circle2.getCenterX() - circle1.getCenterX());
                    tt1.setByY(circle2.getCenterY() - circle1.getCenterY());
                    tt1.setCycleCount(10);
                    tt1.setAutoReverse(true);
                    tt1.play();

                    TranslateTransition tt2 = new TranslateTransition(Duration.seconds(1.3), steamCircle2);
                    tt2.setByX(circle4.getCenterX() - circle3.getCenterX());
                    tt2.setByY(circle4.getCenterY() - circle3.getCenterY());
                    tt2.setCycleCount(10);
                    tt2.setAutoReverse(true);
                    tt2.play();

                    AnimationTimer collisionChecker = new AnimationTimer() {
                        @Override
                        public void handle(long now) {
                            double sx1 = steamCircle1.getTranslateX() + steamCircle1.getCenterX();
                            double sy1 = steamCircle1.getTranslateY() + steamCircle1.getCenterY();
                            double sx2 = steamCircle2.getTranslateX() + steamCircle2.getCenterX();
                            double sy2 = steamCircle2.getTranslateY() + steamCircle2.getCenterY();
                            double ddx = sx1 - sx2;
                            double ddy = sy1 - sy2;
                            double dist = Math.hypot(ddx, ddy);

                            if (dist <= steamCircle1.getRadius() + steamCircle2.getRadius()) {
                                Circle explosion = new Circle(
                                        (sx1 + sx2) / 2,
                                        (sy1 + sy2) / 2,
                                        5
                                );
                                explosion.setFill(Color.ORANGE);
                                explosion.setOpacity(0.6);
                                root.getChildren().add(explosion);

                                ScaleTransition scale = new ScaleTransition(Duration.millis(400), explosion);
                                scale.setToX(6);
                                scale.setToY(6);

                                FadeTransition fade = new FadeTransition(Duration.millis(400), explosion);
                                fade.setToValue(0);

                                ParallelTransition wave = new ParallelTransition(scale, fade);
                                wave.setOnFinished(ev -> root.getChildren().remove(explosion));
                                wave.play();

                                TranslateTransition drift1 = new TranslateTransition(Duration.seconds(1), steamCircle1);
                                drift1.setByX(20);
                                drift1.setByY(-15);
                                drift1.play();

                                TranslateTransition drift2 = new TranslateTransition(Duration.seconds(1), steamCircle2);
                                drift2.setByX(-20);
                                drift2.setByY(15);
                                drift2.play();

                                this.stop();
                            }
                        }
                    };
                    collisionChecker.start();
                });

            } else {
                root.getChildren().remove(tempLine);
            }

            tempLine = null;
            dragging = false;
            startCircle = null;
        });

        stage.setScene(scene);
        stage.setTitle("Connect with Drag");
        stage.show();
    }

    private void startDrag(Circle circle, Pane root) {
        dragging = true;
        startCircle = circle;
        double startX = circle.getCenterX();
        double startY = circle.getCenterY();
        tempLine = new Line(startX, startY, startX, startY);
        tempLine.setStrokeWidth(2);
        tempLine.setStroke(Color.BLUE);
        root.getChildren().add(tempLine);
    }
}