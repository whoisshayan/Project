import javafx.animation.*;
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

    private int validSteam=0;

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
                    // ساخت دو دایره‌ی steam مثل قبل
                    Circle steamCircle1 = new Circle(circle1.getCenterX(), circle1.getCenterY(), 5);
                    steamCircle1.setFill(Color.YELLOW);
                    root.getChildren().add(steamCircle1);

                    Circle steamCircle2 = new Circle(circle3.getCenterX(), circle3.getCenterY(), 5);
                    steamCircle2.setFill(Color.YELLOW);
                    root.getChildren().add(steamCircle2);

                    // فرض می‌کنیم tempLine همانی است که بین circle1 و circle2 کشیده شده
                    // و یک tempLine2 هم بین circle3 و circle4 دارید (یا دوباره آن را ایجاد کنید)
                    Line line1 = tempLine;
                    // اگر خط دوم را ذخیره نکرده‌اید، میتوانید دقیقاً مثل startDrag یک tempLine2 بسازید
                    // که ابتدا از circle3 شروع و در انتها روی circle4 قرار گرفته باشد:
                    Line line2 = new Line(circle3.getCenterX(), circle3.getCenterY(),
                            circle4.getCenterX(), circle4.getCenterY());
                    root.getChildren().add(line2);

                    // PathTransition برای دایره‌ی اول
                    PathTransition pt1 = new PathTransition(Duration.seconds(1.7), line1, steamCircle1);
                    pt1.setCycleCount(1);
                    pt1.setAutoReverse(true);
                    pt1.play();

                    // PathTransition برای دایره‌ی دوم
                    PathTransition pt2 = new PathTransition(Duration.seconds(1.3), line2, steamCircle2);
                    pt2.setCycleCount(10);
                    pt2.setAutoReverse(true);
                    pt2.play();
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