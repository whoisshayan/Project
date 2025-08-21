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

    enum Port { SQUARE, TRIANGLE, HEXAGON, CIRCLE }
    enum Side { LEFT, RIGHT }

    static class NodeCard extends Group {
        final double w, h;
        final Rectangle body;

        NodeCard(double x, double y, double w, double h, Color fill, Color stroke, String... labels) {
            this.w = w / 2.0;
            this.h = h / 2.0;

            body = new Rectangle(this.w, this.h);
            body.setArcWidth(11);
            body.setArcHeight(11);
            body.setFill(fill);
            body.setStroke(stroke);
            body.setStrokeWidth(2);

            Circle dot = new Circle(this.w - 8, 8, 3.5, Color.web("#111"));
            dot.setEffect(new InnerShadow(2, Color.BLACK));
            getChildren().addAll(body, dot);

            // center labels
            if (labels.length > 0) {
                for (int i = 0; i < labels.length; i++) {
                    Text t = new Text(labels[i]);
                    t.setFont(Font.font("Segoe UI", FontWeight.BOLD, 9));
                    t.setFill(Color.WHITE);
                    t.setTextOrigin(VPos.CENTER);
                    double yPos = (this.h / (labels.length + 1)) * (i + 1);
                    t.setY(yPos);
                    getChildren().add(t);
                    t.applyCss();
                    t.setX(this.w / 2 - t.getLayoutBounds().getWidth() / 2);
                    t.layoutBoundsProperty().addListener((obs, o, n) ->
                            t.setX(this.w / 2 - n.getWidth() / 2));
                }
            }

            setLayoutX(x);
            setLayoutY(y);
        }

        /* placement helpers */
        private double centerY() { return h / 2.0; }
        private double gap2()    { return h / 6.0; }
        private double gap3()    { return h / 4.0; }

        void addCentered(Side side, Port p) {
            addPort(side, p, centerY());
        }
        void addSymmetric2(Side side, Port top, Port bottom) {
            double g = gap2();
            addPort(side, top,    centerY() - g);
            addPort(side, bottom, centerY() + g);
        }
        void addSymmetric3(Side side, Port top, Port middle, Port bottom) {
            double g = gap3();
            addPort(side, top,    centerY() - g);
            addPort(side, middle, centerY());
            addPort(side, bottom, centerY() + g);
        }

        /* primitive draw */
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
            double x = (side == Side.RIGHT) ? (w - portWidth / 2.0) : (-portWidth / 2.0);
            g.setLayoutX(x);
            g.setLayoutY(offsetY);
            getChildren().add(g);
        }

        private Group squarePort() {
            Color stroke = Color.web("#FF93AA");
            Rectangle r = new Rectangle(9, 9);
            r.setArcWidth(2); r.setArcHeight(2);
            r.setFill(Color.TRANSPARENT);
            r.setStroke(stroke);
            r.setStrokeWidth(1.25);
            Group g = new Group(r);
            g.setUserData(9.0);
            return g;
        }

        private Group circlePort() {
            Color stroke = Color.web("#FF93AA");
            Circle c = new Circle(4.5);
            c.setFill(Color.TRANSPARENT);
            c.setStroke(stroke);
            c.setStrokeWidth(1.25);
            Group g = new Group(c);
            g.setUserData(9.0);
            return g;
        }

        private Group trianglePort(boolean pointRight) {
            Color stroke = Color.web("#FF93AA");
            Polygon p = pointRight
                    ? new Polygon(0.0, 0.0, 8.0, 4.0, 0.0, 8.0)
                    : new Polygon(8.0, 0.0, 0.0, 4.0, 8.0, 8.0);
            p.setFill(Color.TRANSPARENT);
            p.setStroke(stroke);
            p.setStrokeWidth(1.5);
            Group g = new Group(p);
            g.setUserData(9.0);
            return g;
        }

        private Group hexagonPort() {
            Color stroke = Color.web("#FF93AA");
            Polygon p = new Polygon();
            double size = 5;
            for (int i = 0; i < 6; i++) {
                p.getPoints().addAll(
                        size * Math.cos(i * 2 * Math.PI / 6),
                        size * Math.sin(i * 2 * Math.PI / 6)
                );
            }
            p.setFill(Color.TRANSPARENT);
            p.setStroke(stroke);
            p.setStrokeWidth(1.25);
            Group g = new Group(p);
            g.setUserData(10.0);
            return g;
        }

        private double prefW(Group g) {
            Object v = g.getUserData();
            if (v instanceof Double) return (Double) v;
            return 8.0;
        }
    }

    @Override
    public void start(Stage stage) {
        Group root = new Group();
        Scene scene = new Scene(root, 900, 700, Color.web("#0C1A3A"));

        // START
        NodeCard startCard = new NodeCard(60, 270, 150, 200,
                Color.web("#1F4733"), Color.web("#3BF07B"), "START");
        startCard.addSymmetric2(Side.RIGHT, Port.TRIANGLE, Port.SQUARE);

        // VPN TOP — Left: centered TRIANGLE | Right: symmetric SQUARE + TRIANGLE
        NodeCard vpnTop = new NodeCard(260, 120, 150, 200,
                Color.web("#1B2327"), Color.web("#49F4FF"), "VPN");
        vpnTop.addCentered(Side.LEFT, Port.TRIANGLE);
        vpnTop.addSymmetric2(Side.RIGHT, Port.SQUARE, Port.TRIANGLE);

        // VPN BOTTOM — Left: centered SQUARE | Right: centered HEXAGON
        NodeCard vpnBottom = new NodeCard(260, 360, 150, 200,
                Color.web("#1B2327"), Color.web("#49F4FF"), "VPN");
        vpnBottom.addCentered(Side.LEFT, Port.SQUARE);
        vpnBottom.addCentered(Side.RIGHT, Port.HEXAGON);

        // SPY (قدیمی) — فقط پورت‌های سمت چپ نگه داشته می‌شود
        NodeCard spyLeft = new NodeCard(380, 230, 150, 200,
                Color.web("#232633"), Color.web("#B782FF"), "SPY");
        spyLeft.addSymmetric2(Side.LEFT,  Port.SQUARE,   Port.TRIANGLE);
        // سمت راست این SPY عمداً بدون پورت

        // SPY جدید بین SPY قبلی و DDOS — چپ: بدون پورت | راست: مربع و مثلث
        NodeCard spyRight = new NodeCard(470, 230, 150, 200,
                Color.web("#232633"), Color.web("#B782FF"), "SPY");
        // چپ بدون پورت
        spyRight.addSymmetric2(Side.RIGHT, Port.SQUARE, Port.TRIANGLE);

        // DDOS
        NodeCard ddosCard = new NodeCard(560, 230, 150, 200,
                Color.web("#2B241E"), Color.web("#FF8A00"), "DDOS");
        ddosCard.addSymmetric3(Side.LEFT,  Port.SQUARE,  Port.TRIANGLE, Port.HEXAGON);
        ddosCard.addSymmetric3(Side.RIGHT, Port.TRIANGLE,Port.SQUARE,   Port.HEXAGON);

        // END
        NodeCard endCard = new NodeCard(700, 230, 150, 200,
                Color.web("#2A2020"), Color.web("#FF5C86"), "END");
        endCard.addSymmetric3(Side.LEFT, Port.HEXAGON, Port.SQUARE, Port.TRIANGLE);

        root.getChildren().addAll(startCard, vpnTop, vpnBottom, spyLeft, spyRight, ddosCard, endCard);

        stage.setTitle("Network Layout (JavaFX)");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) { launch(args); }
}
