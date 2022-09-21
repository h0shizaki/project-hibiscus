package se233.hibiscus.controller;


import com.google.common.io.Files;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import se233.hibiscus.Launcher;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class MainViewController {

    private ArrayList<String> fileList ;
    private HashMap<Pane , String> fileMap = new HashMap<>() ;
    @FXML
    private ListView<Pane> inputListView ;
    @FXML
    private Button importBtn ;
    @FXML
    private Button removeBtn ;

    @FXML
    private AnchorPane anchorPane ;


    public void initialize() {

        inputListView.setOnDragOver(event -> {
            Dragboard db = event.getDragboard();

            if (db.hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            } else {
                event.consume();
            }
        });

        inputListView.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean isSuccesss = false;
            if (db.hasFiles()) {
                isSuccesss = true;
                String filePath;

                int totalFiles = db.getFiles().size();
                for (int i = 0; i < totalFiles; i++) {
                    File file = db.getFiles().get(i);
                    filePath = file.getAbsolutePath();

                    Pane myPane = CreateDisplay(file, filePath);

                    fileMap.put(myPane , filePath) ;
                    inputListView.getItems().add(myPane);
                }

            }

            event.setDropCompleted(isSuccesss);
            event.consume();

        });

        importBtn.setOnAction( event -> {
            try {
                FileChooser fs = new FileChooser();
                File file = fs.showOpenDialog(new Stage());
                String filePath = file.getAbsolutePath() ;
                Pane myPane = CreateDisplay(file, filePath);

                fileMap.put(myPane , filePath) ;
                inputListView.getItems().add(myPane);

            }catch (NullPointerException ex){
                System.out.println("Err: " + ex.getMessage());
            }
        });

        removeBtn.setOnAction( event -> {
            System.out.println(fileMap);
            try{
                Pane selectedItem = inputListView.getSelectionModel().getSelectedItem() ;
                fileMap.remove(selectedItem);
                inputListView.getItems().remove(selectedItem) ;
            }catch (NullPointerException ex){
                ex.printStackTrace();
            }
        });

    }

    private Pane CreateDisplay(File file, String filePath) {
        String ext =  Files.getFileExtension(filePath);
        Image img ;
        if (ext.equals("png") || ext.equals("jpg")|| ext.equals("jpeg") || ext.equals("GIF")) {
            img =  new Image(Launcher.class.getResource("imgIcon.png").toString());
        }else if(ext.equals("rar") || ext.equals("zip") || ext.equals("tar") || ext.equals("7zip")){
            img =  new Image(Launcher.class.getResource("zipIcon.png").toString());
        } else if (ext.equals("pdf") || ext.equals("docx")) {
            img =  new Image(Launcher.class.getResource("docIcon.png").toString());
        }else {
            img =  new Image(Launcher.class.getResource("fileIcon.png").toString());
        }

        ImageView iv = new ImageView(img) ;
        iv.setFitHeight(64);
        iv.setFitWidth(64);
        Label label = new Label(file.getName());

        VBox vBox = new VBox();
        vBox.setSpacing(5);
        vBox.setPadding(new Insets(0,5,0,5));
        vBox.getChildren().addAll(label,iv);
        Pane myPane = new Pane() ;
        myPane.getChildren().addAll(vBox);

       return myPane ;
    }
}
