import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.effect.InnerShadow;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class test extends Application {

    enum Side { LEFT, RIGHT }
    enum Port { SQUARE, TRIANGLE }

    // کارت ساده بدون نئون و بدون متن
    static class NodeCard extends Group {
        final double w, h;
        final Rectangle body;

        NodeCard(double x, double y, double w, double h, Color fill, Color stroke) {
            this.w = w; this.h = h;

            body = new Rectangle(w, h);
            body.setArcWidth(22); body.setArcHeight(22);
            body.setFill(fill);
            body.setStroke(stroke);
            body.setStrokeWidth(3);

            // نقطه‌ی مشکی گوشه راست-بالا (اگر نمی‌خوایش، این دو خط رو حذف کن)
            Circle dot = new Circle(w - 16, 16, 7, Color.web("#111"));
            dot.setEffect(new InnerShadow(3, Color.BLACK));

            getChildren().addAll(body, dot);
            setLayoutX(x); setLayoutY(y);
        }

        // افزودن پورت روی چپ/راست کارت
        void addPort(Side side, Port type, double offsetY) {
            Group g = (type == Port.SQUARE) ? squarePort() : trianglePort(side == Side.RIGHT);
            double x = (side == Side.RIGHT) ? (w - 10) : (-10 - prefW(g));
            g.setLayoutX(x);
            g.setLayoutY(offsetY);
            getChildren().add(g);
        }

        private Group squarePort() {
            Color stroke = Color.web("#FF93AA");
            Rectangle r = new Rectangle(18, 18);
            r.setArcWidth(4); r.setArcHeight(4);
            r.setFill(Color.WHITE);
            r.setStroke(stroke); r.setStrokeWidth(2);
            Group g = new Group(r);
            g.setUserData(18.0);
            return g;
        }

        private Group trianglePort(boolean pointRight) {
            Color stroke = Color.web("#FF93AA");
            Polygon p = pointRight
                    ? new Polygon(0.0, 0.0, 16.0, 8.0, 0.0, 16.0)     // ►
                    : new Polygon(16.0, 0.0, 0.0, 8.0, 16.0, 16.0);   // ◄
            p.setFill(Color.TRANSPARENT);
            p.setStroke(stroke); p.setStrokeWidth(3);
            return new Group(p);
        }

        private double prefW(Group g) {
            Object v = g.getUserData();
            if (v instanceof Double) return (Double) v;
            // مثلث ~ 16px
            return 16.0;
        }
    }

    @Override
    public void start(Stage stage) {
        Group root = new Group();
        // ابعاد جدید
        Scene scene = new Scene(root, 900, 700, Color.web("#0C1A3A"));

        // 1) چپ‌ترین
        NodeCard c1 = new NodeCard(80, 230, 140, 180,
                Color.web("#1F4733"), Color.web("#3BF07B"));
        c1.addPort(Side.RIGHT, Port.SQUARE,   90 - 10);
        c1.addPort(Side.RIGHT, Port.TRIANGLE,120 - 10);

        // 2) دوم از چپ (ورودی‌های چپ)
        NodeCard c2 = new NodeCard(300, 230, 140, 180,
                Color.web("#2C2A2A"), Color.web("#FF3B3B"));
        c2.addPort(Side.LEFT, Port.SQUARE,   90 - 10);
        c2.addPort(Side.LEFT, Port.TRIANGLE,120 - 10);

        // 3) بالاییِ وسط (خروجی راست)
        NodeCard c3 = new NodeCard(560, 120, 140, 180,
                Color.web("#2C2A2A"), Color.web("#FF3B3B"));
        c3.addPort(Side.RIGHT, Port.SQUARE,   90 - 10);
        c3.addPort(Side.RIGHT, Port.TRIANGLE,120 - 10);

        // 4) پایینیِ وسط (خروجی راست)
        NodeCard c4 = new NodeCard(560, 340, 140, 180,
                Color.web("#2C2A2A"), Color.web("#FF3B3B"));
        c4.addPort(Side.RIGHT, Port.SQUARE,   90 - 10);
        c4.addPort(Side.RIGHT, Port.TRIANGLE,120 - 10);

        // 5) راست‌ترین (۴ ورودی چپ: ۲ مربع + ۲ مثلث)
        NodeCard c5 = new NodeCard(760, 230, 140, 180,
                Color.web("#3A2F2F"), Color.web("#FF7FB2"));
        c5.addPort(Side.LEFT, Port.SQUARE,    60);
        c5.addPort(Side.LEFT, Port.TRIANGLE,  85);
        c5.addPort(Side.LEFT, Port.SQUARE,   110);
        c5.addPort(Side.LEFT, Port.TRIANGLE, 135);

        root.getChildren().addAll(c1, c2, c3, c4, c5);

        stage.setTitle("Level Layout (JavaFX)");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) { launch(args); }
}
