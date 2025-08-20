import javafx.application.Application;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.effect.InnerShadow;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class test extends Application {

    // انواع پورت
    enum Port { SQUARE, TRIANGLE, HEXAGON, CIRCLE }
    enum Side { LEFT, RIGHT }

    // کلاس کارت
    static class NodeCard extends Group {
        final double w, h;
        final Rectangle body;

        NodeCard(double x, double y, double w, double h, Color fill, Color stroke, String... labels) {
            this.w = w; this.h = h;

            body = new Rectangle(w, h);
            body.setArcWidth(22);
            body.setArcHeight(22);
            body.setFill(fill);
            body.setStroke(stroke);
            body.setStrokeWidth(4);

            Circle dot = new Circle(w - 16, 16, 7, Color.web("#111"));
            dot.setEffect(new InnerShadow(3, Color.BLACK));
            getChildren().addAll(body, dot);

            if (labels.length > 0) {
                for (int i = 0; i < labels.length; i++) {
                    Text text = new Text(labels[i]);
                    text.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
                    text.setFill(Color.WHITE);
                    text.setX(w / 2 - text.getLayoutBounds().getWidth() / 2);
                    text.setY((h / (labels.length + 1)) * (i + 1));
                    text.setTextOrigin(VPos.CENTER);
                    getChildren().add(text);
                }
            }

            setLayoutX(x);
            setLayoutY(y);
        }

        void addPort(Side side, Port type, double offsetY) {
            Group g;
            switch (type) {
                case SQUARE:   g = squarePort(); break;
                case TRIANGLE: g = trianglePort(side == Side.RIGHT); break;
                case HEXAGON:  g = hexagonPort(); break;
                case CIRCLE:   g = circlePort(); break;
                default: return;
            }

            double portWidth = prefW(g);
            double x = (side == Side.RIGHT) ? (w - portWidth / 2) : (-portWidth / 2);
            g.setLayoutX(x);
            g.setLayoutY(offsetY);
            getChildren().add(g);
        }

        private Group squarePort() {
            Color stroke = Color.web("#FF93AA");
            Rectangle r = new Rectangle(18, 18);
            r.setArcWidth(4); r.setArcHeight(4);
            r.setFill(Color.TRANSPARENT);
            r.setStroke(stroke);
            r.setStrokeWidth(2.5);
            Group g = new Group(r);
            g.setUserData(18.0);
            return g;
        }

        private Group circlePort() {
            Color stroke = Color.web("#FF93AA");
            Circle c = new Circle(9);
            c.setFill(Color.TRANSPARENT);
            c.setStroke(stroke);
            c.setStrokeWidth(2.5);
            Group g = new Group(c);
            g.setUserData(18.0);
            return g;
        }

        private Group trianglePort(boolean pointRight) {
            Color stroke = Color.web("#FF93AA");
            Polygon p = pointRight
                    ? new Polygon(0.0, 0.0, 16.0, 8.0, 0.0, 16.0)
                    : new Polygon(16.0, 0.0, 0.0, 8.0, 16.0, 16.0);
            p.setFill(Color.TRANSPARENT);
            p.setStroke(stroke);
            p.setStrokeWidth(3);
            return new Group(p);
        }

        private Group hexagonPort() {
            Color stroke = Color.web("#FF93AA");
            Polygon p = new Polygon();
            double size = 10;
            for (int i = 0; i < 6; i++) {
                p.getPoints().addAll(
                        size * Math.cos(i * 2 * Math.PI / 6),
                        size * Math.sin(i * 2 * Math.PI / 6)
                );
            }
            p.setFill(Color.TRANSPARENT);
            p.setStroke(stroke);
            p.setStrokeWidth(2.5);
            Group g = new Group(p);
            g.setUserData(20.0);
            return g;
        }

        private double prefW(Group g) {
            Object v = g.getUserData();
            if (v instanceof Double) return (Double) v;
            return 16.0;
        }
    }

    @Override
    public void start(Stage stage) {
        Group root = new Group();
        Scene scene = new Scene(root, 900, 700, Color.web("#0C1A3A"));

        // سیستم ۱: بالا-چپ (START) - پورت‌ها: مثلث و مربع
        NodeCard startCardTop = new NodeCard(80, 100, 150, 200,
                Color.web("#1F4733"), Color.web("#3BF07B"), "START");
        startCardTop.addPort(Side.RIGHT, Port.TRIANGLE, 80);
        startCardTop.addPort(Side.RIGHT, Port.SQUARE, 120);

        // سیستم ۲: پایین-چپ (START) - پورت: فقط یک شش ضلعی در وسط
        NodeCard startCardBottom = new NodeCard(80, 360, 150, 200,
                Color.web("#1F4733"), Color.web("#3BF07B"), "START");
        startCardBottom.addPort(Side.RIGHT, Port.HEXAGON, 100);

        // سیستم ۳: وسط (DDOS) - پورت‌های جدید در دو طرف
        NodeCard ddosCard = new NodeCard(375, 230, 150, 200,
                Color.web("#4D2E0B"), Color.web("#FF8A00"), "DDOS");
        // سمت چپ: شش ضلعی، مربع، مثلث
        ddosCard.addPort(Side.LEFT, Port.HEXAGON, 60);
        ddosCard.addPort(Side.LEFT, Port.SQUARE, 100);
        ddosCard.addPort(Side.LEFT, Port.TRIANGLE, 140);
        // سمت راست: مثلث، مربع، شش ضلعی
        ddosCard.addPort(Side.RIGHT, Port.TRIANGLE, 60);
        ddosCard.addPort(Side.RIGHT, Port.SQUARE, 100);
        ddosCard.addPort(Side.RIGHT, Port.HEXAGON, 140);

        // سیستم ۴: راست (END) - پورت‌های جدید در سمت چپ
        NodeCard endCard = new NodeCard(670, 230, 150, 200,
                Color.web("#471F1F"), Color.web("#F03B3B"), "END");
        // سمت چپ: شش ضلعی، مربع، مثلث
        endCard.addPort(Side.LEFT, Port.HEXAGON, 60);
        endCard.addPort(Side.LEFT, Port.SQUARE, 100);
        endCard.addPort(Side.LEFT, Port.TRIANGLE, 140);

        root.getChildren().addAll(startCardTop, startCardBottom, ddosCard, endCard);

        stage.setTitle("Level Layout (JavaFX)");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) { launch(args); }
}