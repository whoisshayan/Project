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
    private boolean validDrag=false;


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

        circle1.setOnMousePressed(e -> startDrag(circle1, root, e.getX(), e.getY()));
        circle2.setOnMousePressed(e -> startDrag(circle2, root, e.getX(), e.getY()));

        Button startbutton=new Button("Start");
        startbutton.setLayoutX(600);
        startbutton.setLayoutY(600);
        root.getChildren().add(startbutton);


        Scene scene = new Scene(root, 900, 700);

        scene.setOnMouseDragged(e -> {
            if (dragging && tempLine != null) {
                tempLine.setEndX(e.getX());
                tempLine.setEndY(e.getY());
            }
        });

        scene.setOnMouseReleased(e -> {
            if (dragging && tempLine != null && startCircle != null) {
                Circle target = (startCircle == circle1) ? circle2 : circle1;
                double releaseX = e.getX();
                double releaseY = e.getY();

                double dx = releaseX - target.getCenterX();
                double dy = releaseY - target.getCenterY();
                double distance = Math.hypot(dx, dy);

                if (distance <= target.getRadius()) {
                    tempLine.setEndX(target.getCenterX());
                    tempLine.setEndY(target.getCenterY());
                    startbutton.setStyle(
                            "-fx-background-color: #2196F3; " +
                                    "-fx-text-fill: yellow; " +
                                    "-fx-background-radius: 5; " +
                                    "-fx-font-size: 14px;"
                    );
                    startbutton.setOnAction(actionEvent -> {
                        Circle steamCircle=new Circle(100,100,5);
                        steamCircle.setFill(Color.YELLOW);
                        steamCircle.setStroke(Color.BLACK);
                        steamCircle.setStrokeWidth(1);
                        root.getChildren().add(steamCircle);
                        TranslateTransition tt=new TranslateTransition(Duration.seconds(2),steamCircle);
                        tt.setByX(300);
                        tt.setByY(300);
                        tt.setCycleCount(1);
                        tt.setAutoReverse(false);
                        tt.play();
                    });
                } else {
                    root.getChildren().remove(tempLine);
                }

                tempLine = null;
                dragging = false;
                startCircle = null;
            }
        });


        stage.setScene(scene);
        stage.setTitle("Connect with Drag");
        stage.show();
    }

    private void startDrag(Circle circle, Pane root, double x, double y) {
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