import javafx.animation.Animation;
import javafx.animation.AnimationTimer;
import javafx.animation.PathTransition;
import javafx.animation.ParallelTransition;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
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

    // steam circles as fields
    private Circle steam1, steam2;
    // path-transitions
    private PathTransition pt1, pt2;
    // خط زمان
    private Slider timeSlider;
    // همگام‌سازی اسلایدر در هنگام پخش
    private AnimationTimer syncTimer;

    // مختصات آغاز و پایان
    private double c1x, c1y, c2x, c2y;
    private double c3x, c3y, c4x, c4y;

    // مدت انیمیشن
    private static final double DURATION = 1.3;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        Pane root = new Pane();

        // ایجاد دایره‌های قابل درگ
        Circle c1 = createDraggableCircle(100, 100, Color.RED, root);
        Circle c2 = createDraggableCircle(400, 400, Color.GREEN, root);
        Circle c3 = createDraggableCircle(100, 200, Color.RED, root);
        Circle c4 = createDraggableCircle(400, 300, Color.GREEN, root);

        // ذخیرهٔ مختصات برای محاسبات بعدی
        c1x = c1.getCenterX();  c1y = c1.getCenterY();
        c2x = c2.getCenterX();  c2y = c2.getCenterY();
        c3x = c3.getCenterX();  c3y = c3.getCenterY();
        c4x = c4.getCenterX();  c4y = c4.getCenterY();

        // دکمه‌ی Start (اول غیرفعال)
        Button startButton = new Button("Start");
        startButton.setLayoutX(600);
        startButton.setLayoutY(600);
        startButton.setDisable(true);

        root.getChildren().add(startButton);

        // اسلایدر خط زمان (غیرفعال تا سیم‌ها وصل شوند)
        timeSlider = new Slider(0, DURATION, 0);
        timeSlider.setLayoutX(50);
        timeSlider.setLayoutY(650);
        timeSlider.setPrefWidth(800);
        timeSlider.setDisable(true);
        root.getChildren().add(timeSlider);

        Scene scene = new Scene(root, 900, 700);

        // رسم سیم با کشیدن ماوس
        scene.setOnMouseDragged(e -> {
            if (dragging && tempLine != null) {
                tempLine.setEndX(e.getX());
                tempLine.setEndY(e.getY());
            }
        });

        // وقتی ماوس رها می‌شود: بررسی اتصال و ذخیرهٔ سیم
        scene.setOnMouseReleased(e -> {
            if (!dragging || tempLine == null || startCircle == null) return;

            Circle target = findPair(startCircle, c1, c2, c3, c4);
            if (target != null && hitTarget(e.getX(), e.getY(), target)) {
                tempLine.setEndX(target.getCenterX());
                tempLine.setEndY(target.getCenterY());

                if ((startCircle==c1 && target==c2) || (startCircle==c2 && target==c1)) {
                    wire12 = tempLine;
                } else {
                    wire34 = tempLine;
                }

                // اگر هر دو سیم وصل شدند، آماده‌سازی steamها و اسلایدر
                if (wire12!=null && wire34!=null) {
                    startButton.setDisable(false);
                    timeSlider.setDisable(false);

                    // ساخت کره‌ها
                    steam1 = new Circle(c1x, c1y, 5, Color.YELLOW);
                    steam1.setStroke(Color.BLACK);
                    steam1.setStrokeWidth(1);
                    root.getChildren().add(steam1);

                    steam2 = new Circle(c3x, c3y, 5, Color.YELLOW);
                    steam2.setStroke(Color.BLACK);
                    steam2.setStrokeWidth(1);
                    root.getChildren().add(steam2);

                    // آماده‌سازی PathTransition (قرار در pause تا با اسلایدر کنترل شود)
                    pt1 = new PathTransition(Duration.seconds(DURATION), wire12, steam1);
                    pt1.setOrientation(PathTransition.OrientationType.NONE);
                    pt1.pause();

                    pt2 = new PathTransition(Duration.seconds(DURATION), wire34, steam2);
                    pt2.setOrientation(PathTransition.OrientationType.NONE);
                    pt2.pause();

                    // listener روی اسلایدر برای کنترل دستی موقعیت
                    timeSlider.valueProperty().addListener((obs, oldV, newV) -> {
                        double t = newV.doubleValue() / DURATION;
                        // موقعیت خطی روی سیم اول
                        steam1.setCenterX(c1x + (c2x - c1x) * t);
                        steam1.setCenterY(c1y + (c2y - c1y) * t);
                        // موقعیت خطی روی سیم دوم
                        steam2.setCenterX(c3x + (c4x - c3x) * t);
                        steam2.setCenterY(c3y + (c4y - c3y) * t);
                    });

                    // AnimationTimer برای به‌روز کردن اسلایدر حین پخش انیمیشن
                    syncTimer = new AnimationTimer() {
                        @Override
                        public void handle(long now) {
                            if (pt1.getStatus() == Animation.Status.RUNNING) {
                                timeSlider.setValue(pt1.getCurrentTime().toSeconds());
                            }
                        }
                    };
                    syncTimer.start();

                    // دکمه‌ی Start: از نقطهٔ کنونی اسلایدر پخش کن
                    startButton.setOnAction(ae -> {
                        pt1.playFrom(Duration.seconds(timeSlider.getValue()));
                        pt2.playFrom(Duration.seconds(timeSlider.getValue()));
                    });
                }

            } else {
                root.getChildren().remove(tempLine);
            }

            tempLine = null;
            dragging = false;
            startCircle = null;
        });

        stage.setScene(scene);
        stage.setTitle("Connect with Drag and Interactive Timeline");
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
}
