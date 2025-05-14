import javafx.animation.AnimationTimer;
import javafx.animation.PathTransition;
import javafx.animation.ParallelTransition;
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

    private Line wire12, wire34;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        Pane root = new Pane();

        Circle c1 = createDraggableCircle(100, 100, Color.RED, root);
        Circle c2 = createDraggableCircle(400, 400, Color.GREEN, root);
        Circle c3 = createDraggableCircle(100, 200, Color.RED, root);
        Circle c4 = createDraggableCircle(400, 300, Color.GREEN, root);

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

            Circle target = findPair(startCircle, c1, c2, c3, c4);
            if (target != null && hitTarget(e.getX(), e.getY(), target)) {
                tempLine.setEndX(target.getCenterX());
                tempLine.setEndY(target.getCenterY());

                if ((startCircle == c1 && target == c2) || (startCircle == c2 && target == c1)) {
                    wire12 = tempLine;
                } else {
                    wire34 = tempLine;
                }

                startButton.setStyle(
                        "-fx-background-color: #2196F3; " +
                                "-fx-text-fill: yellow; " +
                                "-fx-background-radius: 5; " +
                                "-fx-font-size: 14px;"
                );
                startButton.setOnAction(ae -> {
                    // ۱) دوباره Steam ها را تولید می‌کنیم تا دسترسی به آبجکت‌شان داشته باشیم
                    Circle steam1 = new Circle(c1.getCenterX(), c1.getCenterY(), 5, Color.YELLOW);
                    steam1.setStroke(Color.BLACK);
                    steam1.setStrokeWidth(1);
                    root.getChildren().add(steam1);

                    Circle steam2 = new Circle(c3.getCenterX(), c3.getCenterY(), 5, Color.YELLOW);
                    steam2.setStroke(Color.BLACK);
                    steam2.setStrokeWidth(1);
                    root.getChildren().add(steam2);

                    // ۲) PathTransition ها روی خطوط ثابتِ wire12 و wire34
                    PathTransition p1 = new PathTransition(Duration.seconds(1.3), wire12, steam1);
                    p1.setOrientation(PathTransition.OrientationType.NONE);
                    PathTransition p2 = new PathTransition(Duration.seconds(1.3), wire34, steam2);
                    p2.setOrientation(PathTransition.OrientationType.NONE);

                    ParallelTransition all = new ParallelTransition(p1, p2);
                    all.play();

                    // ۳) AnimationTimer برای تشخیص برخورد
                    new AnimationTimer() {
                        @Override
                        public void handle(long now) {
                            if (steam1.getBoundsInParent().intersects(steam2.getBoundsInParent())) {
                                // وقتی برخورد اتفاق افتاد، انیمیشن‌های Path را متوقف کن
                                p1.stop();
                                p2.stop();

                                // و هر کدام را کمی عمود بر مسیر منحرف کن
                                deviate(steam1, wire12);
                                deviate(steam2, wire34);

                                this.stop();  // دیگه نیازی به timer نیست
                            }
                        }
                    }.start();
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

    private Circle createDraggableCircle(double x, double y, Color color, Pane root) {
        Circle c = new Circle(x, y, 15, color);
        c.setStroke(Color.BLACK);
        c.setStrokeWidth(1);
        c.setOnMousePressed(e -> {
            dragging = true;
            startCircle = c;
            tempLine = new Line(c.getCenterX(), c.getCenterY(),
                    c.getCenterX(), c.getCenterY());
            tempLine.setStrokeWidth(2);
            tempLine.setStroke(Color.BLUE);
            root.getChildren().add(tempLine);
        });
        root.getChildren().add(c);
        return c;
    }

    private Circle findPair(Circle src, Circle c1, Circle c2, Circle c3, Circle c4) {
        if (src == c1)      return c2;
        else if (src == c2) return c1;
        else if (src == c3) return c4;
        else if (src == c4) return c3;
        else return null;
    }

    private boolean hitTarget(double x, double y, Circle target) {
        double dx = x - target.getCenterX();
        double dy = y - target.getCenterY();
        return Math.hypot(dx, dy) <= target.getRadius();
    }

    private PathTransition animateSteam(Circle from, Circle to, Line path, Pane root) {
        Circle steam = new Circle(from.getCenterX(), from.getCenterY(), 5, Color.YELLOW);
        steam.setStroke(Color.BLACK);
        steam.setStrokeWidth(1);
        root.getChildren().add(steam);

        PathTransition pt = new PathTransition(Duration.seconds(1.3), path, steam);
        pt.setOrientation(PathTransition.OrientationType.NONE);
        return pt;
    }
    private void deviate(Circle steam, Line path) {
        // بردار کلی مسیر
        double dx = path.getEndX() - path.getStartX();
        double dy = path.getEndY() - path.getStartY();
        // بردار عمود واحد
        double len = Math.hypot(dx, dy);
        double ux = -dy / len;
        double uy = dx / len;
        // فاصله منحرف شدن (مثلاً 30 پیکسل)
        double offset = 30;

        TranslateTransition dev = new TranslateTransition(Duration.seconds(0.5), steam);
        dev.setByX(ux * offset);
        dev.setByY(uy * offset);
        dev.play();
    }
}
