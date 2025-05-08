import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.layout.Pane;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import java.io.File;
import javafx.scene.media.Media;


public class test extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        String path="C:/Users/shaya/Desktop/song/Shervin-hajiaghapoor.Bahar-omad(320).mp3";
        File file=new File(path);
        if(!file.exists())
            System.out.println("Not Ok!");
        String url=file.toURI().toString();
        Media media =new Media(url);
        MediaPlayer mediaPlayer=new MediaPlayer(media);
        mediaPlayer.setAutoPlay(false);

        // PlatButton
        Button playbutton=new Button("Play");
        playbutton.setOnAction(actionEvent -> mediaPlayer.play());
        playbutton.setLayoutX(200);
        playbutton.setLayoutY(150);
        playbutton.setPrefWidth(100);

        // SettingButton
        Button settingbutton=new Button("Setting");
        settingbutton.setLayoutX(200);
        settingbutton.setLayoutY(200);
        settingbutton.setPrefWidth(100);

        // PauseButton
        Button pausebutton=new Button("Pause");
        pausebutton.setOnAction(actionEvent -> mediaPlayer.pause());
        pausebutton.setLayoutX(200);
        pausebutton.setLayoutY(250);
        pausebutton.setPrefWidth(100);

        // Slider
        Slider volumeSlider = new Slider(0, 1, 0.5);
        volumeSlider.setShowTickLabels(true);
        volumeSlider.setShowTickMarks(true);
        volumeSlider.setMajorTickUnit(0.25);
        volumeSlider.setBlockIncrement(0.1);
        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) ->
                mediaPlayer.setVolume(newVal.doubleValue())
        );
        mediaPlayer.setVolume(volumeSlider.getValue());
        volumeSlider.setLayoutX(180);
        volumeSlider.setLayoutY(200);

        Button backbutton=new Button("Back");
        backbutton.setLayoutX(400);
        backbutton.setLayoutY(400);

        Pane root=new Pane(pausebutton,playbutton,settingbutton);
        Pane settingpane=new Pane(backbutton,volumeSlider);

        Scene mainscene=new Scene(root,500,500);
        Scene settingscene=new Scene(settingpane,500,500);

        settingbutton.setOnAction(actionEvent -> stage.setScene(settingscene));
        backbutton.setOnAction(actionEvent -> stage.setScene(mainscene));
        stage.setScene(mainscene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}