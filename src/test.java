import javafx.application.Application;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import java.util.List;

public class test extends Application {

    private Circle startCircle = null;
    private Rectangle startSmallRect = null;
    private Line currentLine = null;

    private List<Circle> circles;
    private List<Rectangle> smallRects;
    private Pane root;


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        root = new Pane();

        Rectangle mainrect = new Rectangle(50, 100, 100, 200);
        mainrect.setFill(Color.CORAL);
        mainrect.setStroke(Color.BLACK);
        mainrect.setStrokeWidth(2);
        mainrect.setArcWidth(20);
        mainrect.setArcHeight(20);

        Circle circle = new Circle(150, 140, 10);
        circle.setFill(Color.LIGHTGREEN);
        circle.setStroke(Color.DARKGREEN);
        circle.setStrokeWidth(3);

        Rectangle littlerec = new Rectangle(145, 220, 10, 20);
        littlerec.setFill(Color.GREEN);
        littlerec.setStroke(Color.BLACK);
        littlerec.setStrokeWidth(2);
        littlerec.setArcWidth(2);
        littlerec.setArcHeight(2);

        Rectangle mainrect2 = new Rectangle(300, 200, 100, 200);
        mainrect2.setFill(Color.CORAL);
        mainrect2.setStroke(Color.BLACK);
        mainrect2.setStrokeWidth(2);
        mainrect2.setArcWidth(20);
        mainrect2.setArcHeight(20);

        Circle circle2 = new Circle(300, 240, 10);
        circle2.setFill(Color.LIGHTGREEN);
        circle2.setStroke(Color.DARKGREEN);
        circle2.setStrokeWidth(3);

        Rectangle littlerec2 = new Rectangle(295, 320, 10, 20);
        littlerec2.setFill(Color.GREEN);
        littlerec2.setStroke(Color.BLACK);
        littlerec2.setStrokeWidth(2);
        littlerec2.setArcWidth(2);
        littlerec2.setArcHeight(2);

        root.getChildren().addAll(
                mainrect, circle, littlerec,
                mainrect2, circle2, littlerec2
        );

        Button startbutton=new Button("Start");
        startbutton.setLayoutX(81);
        startbutton.setLayoutY(88);
        root.getChildren().add(startbutton);


        circles = List.of(circle, circle2);
        smallRects = List.of(littlerec, littlerec2);

        for (Circle c : circles) {
            c.setOnMousePressed(this::onStartWireFromCircle);
        }
        for (Rectangle r : smallRects) {
            r.setOnMousePressed(this::onStartWireFromSmallRect);
        }

        root.setOnMouseDragged(this::onDragWire);
        root.setOnMouseReleased(this::onReleaseWire);

        Scene scene = new Scene(root, 700, 600);
        stage.setScene(scene);
        stage.setTitle("Wire Drawing Without Groups");
        stage.show();
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
        root.getChildren().add(currentLine);
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
        root.getChildren().add(currentLine);
        e.consume();
    }

    private void onDragWire(MouseEvent e) {
        if (currentLine != null) {
            currentLine.setEndX(e.getSceneX());
            currentLine.setEndY(e.getSceneY());
        }
    }

    private void onReleaseWire(MouseEvent e) {
        if (currentLine == null) return;

        boolean connected = false;

        if (startCircle != null) {
            for (Circle c : circles) {
                if (c != startCircle) {
                    Point2D center = c.localToScene(c.getCenterX(), c.getCenterY());
                    double dx = center.getX() - e.getSceneX();
                    double dy = center.getY() - e.getSceneY();
                    if (Math.hypot(dx, dy) <= c.getRadius()) {
                        currentLine.setEndX(center.getX());
                        currentLine.setEndY(center.getY());
                        connected = true;
                        break;
                    }
                }
            }
        }
        else if (startSmallRect != null) {
            Point2D scenePt = new Point2D(e.getSceneX(), e.getSceneY());

            for (Rectangle r : smallRects) {
                if (r == startSmallRect) continue;

                double cx = r.getX() + r.getWidth()/2;
                double cy = r.getY() + r.getHeight()/2;
                Point2D center = r.localToScene(cx, cy);

                Point2D localPt = r.sceneToLocal(scenePt);

                if (r.contains(localPt)) {
                    currentLine.setEndX(center.getX());
                    currentLine.setEndY(center.getY());
                    connected = true;
                    break;
                }
            }
        }

        if (!connected) {
            root.getChildren().remove(currentLine);
        }

        currentLine = null;
        startCircle = null;
        startSmallRect = null;
        e.consume();
    }
}