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
import se233.hibiscus.model.Merger;
import se233.hibiscus.model.Zipper;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

            ExecutorService ex = Executors.newFixedThreadPool(2) ;
            CountDownLatch countDownLatch = new CountDownLatch(2);

            if(inputListView.getItems().size() <= 0) return;
            if(nameInput.getText().isEmpty()) return;

            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Add Password");
            dialog.setContentText("Password:");
            dialog.setHeaderText(null);
            dialog.setGraphic(null);

            String password ;
            Optional<String> pwd = dialog.showAndWait();
            password = pwd.get();

                try {
                    DirectoryChooser dc = new DirectoryChooser();
                    String fileExt = getOutputFileExtension();

                    String fileName = nameInput.getText();
                    File destPath = dc.showDialog(new Stage());
                    String output = String.format("%s/%s.%s", destPath, fileName, fileExt);

                    ArrayList<File> fileList = new ArrayList<>();
                    for (int i = 0; i < inputListView.getItems().size(); i++) {
                        fileList.add(new File(fileMap.get(inputListView.getItems().get(i))));
                    }

                    String fileP1 = String.format("%s/%s-part1.%s", destPath, fileName, fileExt) ;
                    String fileP2 = String.format("%s/%s-part2.%s", destPath, fileName, fileExt) ;
                    ArrayList<String> partList = new ArrayList<>() ;
                    partList.add(fileP1);
                    partList.add(fileP2);



                    Zipper part1 = new Zipper(fileList.subList(0,(int)(fileList.size()/2)),fileP1 , countDownLatch);
                    Zipper part2 = new Zipper(fileList.subList((int)(fileList.size()/2),fileList.size()),fileP2 , countDownLatch);

                    Merger merger = new Merger(output,partList , password , countDownLatch);

                    ex.submit(part1);
                    ex.submit(part2);
                    ex.submit(merger);

                    System.out.println(password);
                } catch (Exception e) {
                    e.printStackTrace();
                }
        });

    }

    private String getOutputFileExtension() {
        if(isRar.isSelected()){
            return  "rar" ;
        }
        if(isTar.isSelected()){
            return  "tar" ;
        }
        if(isZip.isSelected()){
            return  "zip" ;
        }
        return "zip";

    }

    private Pane CreateDisplay(File file, String filePath) {
        String ext = Files.getFileExtension(filePath);
        Image img;
        if (ext.toLowerCase().equals("png")) {
            img = new Image(Launcher.class.getResource("pngIcon.png").toString());
        } else if (ext.toLowerCase().equals("rar")) {
            img = new Image(Launcher.class.getResource("rarIcon.png").toString());
        }else if(ext.toLowerCase().equals("zip") || ext.toLowerCase().equals("7zip")){
            img = new Image(Launcher.class.getResource("zipIcon.png").toString());
        }else if(ext.toLowerCase().equals("tar")){
            img = new Image(Launcher.class.getResource("tarIcon.png").toString());
        }else if (ext.toLowerCase().equals("jpg") || ext.toLowerCase().equals("jpeg")) {
            img = new Image(Launcher.class.getResource("jpgIcon.png").toString());
        }else if (ext.toLowerCase().equals("gif")){
            img = new Image(Launcher.class.getResource("gifIcon.png").toString());
        }else if (ext.toLowerCase().equals("pdf")){
            img = new Image(Launcher.class.getResource("pdfIcon.png").toString());
        }else if (ext.toLowerCase().equals("docx") || ext.toLowerCase().equals("doc")) {
            img =  new Image(Launcher.class.getResource("docIcon.png").toString());
        }else if (ext.toLowerCase().equals("mp4")) {
            img =  new Image(Launcher.class.getResource("mp4Icon.png").toString());
        }else if (ext.toLowerCase().equals("java")) {
            img =  new Image(Launcher.class.getResource("javaIcon.png").toString());
        }else if (ext.toLowerCase().equals("pages")||ext.toLowerCase().equals("page")) {
            img =  new Image(Launcher.class.getResource("pagesIcon.png").toString());
        }else if (ext.toLowerCase().equals("numbers")||ext.toLowerCase().equals("number")) {
            img =  new Image(Launcher.class.getResource("numbersIcon.png").toString());
        }else if (ext.toLowerCase().equals("xls")||ext.toLowerCase().equals("xltm")||ext.toLowerCase().equals("xlsm")||ext.toLowerCase().equals("xlsx")||ext.toLowerCase().equals("xltx")||ext.toLowerCase().equals("xlsb")){
            img =  new Image(Launcher.class.getResource("xlsIcon.png").toString());
        }else if (ext.toLowerCase()== ""){
            img =  new Image(Launcher.class.getResource("folderIcon.png").toString());
        }else {
            img =  new Image(Launcher.class.getResource("fileIcon.png").toString());
        }

        ImageView iv = new ImageView(img) ;
        iv.setFitHeight(148);
        iv.setFitWidth(128);
        Label label = new Label(file.getName());

        if(ext.toLowerCase() == ""){
            iv.setFitHeight(148);
            iv.setFitWidth(158);
        }
        VBox vBox = new VBox();
        vBox.setSpacing(5);
        vBox.setPadding(new Insets(0,5,0,5));
        vBox.getChildren().addAll(iv,label);
        Pane myPane = new Pane() ;
        myPane.getChildren().addAll(vBox);

       return myPane ;
    }
}
