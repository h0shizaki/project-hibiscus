package se233.hibiscus.controller;


import com.google.common.io.Files;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Popup;
import javafx.stage.Stage;
import se233.hibiscus.Launcher;
import se233.hibiscus.model.Zipper;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

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
    private ToggleButton isZip ;
    @FXML
    private ToggleButton isRar ;
    @FXML
    private ToggleButton isTar;
    @FXML
    private ToggleButton is7Zip ;

    @FXML
    private TextField nameInput ;
    @FXML
    private TextField passwordInput ;
    @FXML
    private Button continueBtn ;

    ZipperController zipperController = new ZipperController() ;

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

        continueBtn.setOnAction( event -> {

            if(inputListView.getItems().size() <= 0) return;
            if(nameInput.getText().isEmpty()) return;
            String passwordCheck = passwordInput.getText();

            if(passwordCheck.isEmpty()) {
                try {
                    String password = passwordInput.getText();

                    DirectoryChooser dc = new DirectoryChooser();

                    String fileExt = getOutputFileExtension();
                    String fileName = nameInput.getText();
                    File destPath = dc.showDialog(new Stage());
                    String output = String.format("%s/%s.%s", destPath, fileName, fileExt);

                    ArrayList<File> fileList = new ArrayList<>();
                    for (int i = 0; i < inputListView.getItems().size(); i++) {
                        fileList.add(new File(fileMap.get(inputListView.getItems().get(i))));
                    }


                    zipperController.createZipFile(fileList, password, output);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else {
                DirectoryChooser dc = new DirectoryChooser();

                String fileExt = getOutputFileExtension();
                String fileName = nameInput.getText();

                TextInputDialog dialog = new TextInputDialog();
                dialog.setTitle("Confirm Password");
                dialog.setContentText("Password:");
                dialog.setHeaderText(null);
                dialog.setGraphic(null);

                String password ;
                Optional<String> pwd = dialog.showAndWait();
                password = pwd.get().toString();

                File destPath = dc.showDialog(new Stage());
                String output = String.format("%s/%s.%s", destPath, fileName, fileExt);

                ArrayList<File> fileList = new ArrayList<>();
                for (int i = 0; i < inputListView.getItems().size(); i++) {
                    fileList.add(new File(fileMap.get(inputListView.getItems().get(i))));
                }


                zipperController.createZipFile(fileList, password, output);

            }
        });

    }

    private String getOutputFileExtension() {
        if(isRar.isSelected()) return  "rar" ;
        if(isTar.isSelected()) return  "tar" ;
        if(isZip.isSelected()) return  "zip" ;
        return  "zip" ;
    }

    private Pane CreateDisplay(File file, String filePath) {
        String ext =  Files.getFileExtension(filePath);
        Image img ;
        if (ext.toLowerCase().equals("png") || ext.equals("jpg")|| ext.equals("jpeg") || ext.equals("GIF")) {
            img =  new Image(Launcher.class.getResource("imgIcon.png").toString());
        }else if(ext.toLowerCase().equals("rar") || ext.equals("zip") || ext.equals("tar") || ext.equals("7zip")){
            img =  new Image(Launcher.class.getResource("zipIcon.png").toString());
        } else if (ext.toLowerCase().equals("pdf") || ext.equals("docx")) {
            img =  new Image(Launcher.class.getResource("docIcon.png").toString());
        }else {
            img =  new Image(Launcher.class.getResource("fileIcon.png").toString());
        }

        ImageView iv = new ImageView(img) ;
        iv.setFitHeight(148);
        iv.setFitWidth(128);
        Label label = new Label(file.getName());

        VBox vBox = new VBox();
        vBox.setSpacing(5);
        vBox.setPadding(new Insets(0,5,0,5));
        vBox.getChildren().addAll(iv,label);
        Pane myPane = new Pane() ;
        myPane.getChildren().addAll(vBox);

       return myPane ;
    }
}
