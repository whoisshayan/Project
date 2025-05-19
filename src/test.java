import java.io.File;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.util.Duration;

public class test extends Application {
    @Override
    public void start(Stage primaryStage) {
        Button btn = new Button("Play Sound");

        // ۱. بررسی وجود فایل و چاپ URI
        File soundFile = new File("D:/university/AP/mixkit-fast-double-click-on-mouse-275.mp3");
        System.out.println("File exists: " + soundFile.exists());
        System.out.println("URI = " + soundFile.toURI());

        // ۲. ساخت Media همراه با هندل خطا
        Media media;
        try {
            media = new Media(soundFile.toURI().toString());
        } catch (MediaException me) {
            System.err.println("MediaException: " + me.getMessage());
            me.printStackTrace();
            return;
        }
        media.setOnError(() -> {
            System.err.println("Media error: " + media.getError());
        });

        // ۳. ساخت MediaPlayer همراه با هندل خطا
        MediaPlayer mediaPlayer = new MediaPlayer(media);
        mediaPlayer.setOnError(() -> {
            System.err.println("MediaPlayer error: " + mediaPlayer.getError());
        });
        mediaPlayer.setOnEndOfMedia(() -> {
            System.out.println("Finished playing");
            mediaPlayer.seek(Duration.ZERO);
        });

        // ۴. پخش صدا با هر بار کلیک
        btn.setOnAction(e -> {
            mediaPlayer.stop();
            mediaPlayer.play();
        });

        StackPane root = new StackPane(btn);
        primaryStage.setScene(new Scene(root, 300, 200));
        primaryStage.setTitle("Sound Test");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
