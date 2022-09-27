package se233.hibiscus.controller;


import com.google.common.io.Files;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Popup;
import javafx.stage.Stage;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import se233.hibiscus.Launcher;
import se233.hibiscus.model.Extracter;
import se233.hibiscus.model.Merger;
import se233.hibiscus.model.Zipper;

import java.io.File;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
    @FXML
    private Button extractBtn ;
    @FXML
    private ListView<HBox> previewListView ;
    @FXML
    private Label dropLabel ;
    @FXML
    private Button removeAllBtn;

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
                dropLabel.setVisible(false);
                String filePath;

                int totalFiles = db.getFiles().size();
                for (int i = 0; i < totalFiles; i++) {
                    File file = db.getFiles().get(i);
                    filePath = file.getAbsolutePath();

                    Pane myPane = CreateDisplay(file, filePath);

                    fileMap.put(myPane , filePath) ;
                    String ext = Files.getFileExtension(filePath);

                    if(ext.toLowerCase().equals("")){

                    }else {
                        inputListView.getItems().add(myPane);
                        if(inputListView.getItems().size() > 6){
                            removeAllBtn.setVisible(true);
                        }

                    }
                }

            }

            event.setDropCompleted(isSuccesss);
            event.consume();

        });

        removeAllBtn.setOnAction(event ->{
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(null);
            alert.setHeaderText(null);
            alert.setContentText("Are you sure to remove all data; You will not be able to recover data!");
            alert.showAndWait();
            if(alert.getResult().getText().equals("Cancel")){

            }else {
                inputListView.getItems().clear();
                removeAllBtn.setVisible(false);
                dropLabel.setVisible(true);

            }
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
            if(inputListView.getItems().size() < 1){
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle(null);
                alert.setHeaderText(null);
                alert.setContentText("There is no file for remove");
                alert.showAndWait();
            }else {
                try{
                    Pane selectedItem = inputListView.getSelectionModel().getSelectedItem() ;
                    fileMap.remove(selectedItem);
                    inputListView.getItems().remove(selectedItem) ;
                }catch (NullPointerException ex){
                    ex.printStackTrace();
                }
            }

        });

        continueBtn.setOnAction( event -> {

            if(inputListView.getItems().size()<1){
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle(null);
                alert.setHeaderText(null);
                alert.setContentText("There is no file for compost");
                alert.showAndWait();
            }

            ExecutorService ex = Executors.newFixedThreadPool(3) ;
            CountDownLatch countDownLatch = new CountDownLatch(2);

            if(inputListView.getItems().size() <= 0) return;
            if(nameInput.getText().isEmpty()) return;
            String passwordCheck = passwordInput.getText();


            String password;

            if(passwordCheck.isEmpty()){
                password="";
            }else {
                TextInputDialog dialog = new TextInputDialog();
                dialog.setTitle("Confirm Password");
                dialog.setContentText("Password:");
                dialog.setHeaderText(null);
                dialog.setGraphic(null);
                Optional<String> pwd = dialog.showAndWait();
                password = pwd.get();
            }
            if(passwordInput.getText().equals(password)) {

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

                    String fileP1 = String.format("%s/%s-part1.%s", destPath, fileName, fileExt);
                    String fileP2 = String.format("%s/%s-part2.%s", destPath, fileName, fileExt);
                    ArrayList<String> partList = new ArrayList<>();
                    partList.add(fileP1);
                    partList.add(fileP2);

                    Zipper part1 = new Zipper(fileList.subList(0,(int)(fileList.size()/2)),fileP1 , countDownLatch);
                    Zipper part2 = new Zipper(fileList.subList((int)(fileList.size()/2),fileList.size()),fileP2 , countDownLatch);
                    Merger merger = new Merger(output,partList , password , countDownLatch);

                    ex.submit(part1);
                    ex.submit(part2);
                    ex.submit(merger);

                    ex.shutdown();
                    ex.awaitTermination(10, TimeUnit.MINUTES);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle(null);
                alert.setHeaderText(null);
                alert.setContentText("Seem your password is does not match.");
                alert.showAndWait();
            }
        });

        extractBtn.setOnAction( event -> {
            if(inputListView.getItems().size()<1){
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle(null);
                alert.setHeaderText(null);
                alert.setContentText("There is no file for extract");
                alert.showAndWait();
            }else {
                try {
                    ExecutorService es = Executors.newFixedThreadPool(2);

                    ArrayList<File> fileList = new ArrayList<>();
                    for (int i = 0; i < inputListView.getItems().size(); i++) {
                        fileList.add(new File(fileMap.get(inputListView.getItems().get(i))));
                    }

                    List<File> onlyArchiveList = fileList.stream()
                            .filter(file -> {
                                String ext = Files.getFileExtension(file.getAbsolutePath());
                                return ext.equals("zip") ||
                                        ext.equals("rar") ||
                                        ext.equals("tar");
                            })
                            .collect(Collectors.toList());

                    DirectoryChooser dc = new DirectoryChooser();
                    String destPath = dc.showDialog(new Stage()).getAbsolutePath();

                    Map<File, String> zipPassword = new HashMap<>();

                    onlyArchiveList.forEach(file -> {
                        ZipFile zipFile = new ZipFile(file.getAbsolutePath());
                        try {
                            if (zipFile.isEncrypted()) {

                                TextInputDialog dialog = new TextInputDialog();
                                dialog.setTitle(file.getName() + " has password");
                                dialog.setContentText("Enter password for " + file.getName() + ": ");
                                dialog.setHeaderText(null);
                                dialog.setGraphic(null);

                                Optional<String> pwdDialog = dialog.showAndWait();
                                String password = pwdDialog.get();
                                zipPassword.put(file, password);
                            }
                        } catch (ZipException e) {
                            e.printStackTrace();
                        }
                    });

                    List<File> archiveListPart1 = onlyArchiveList.subList(0, (int) (onlyArchiveList.size() / 2));
                    List<File> archiveListPart2 = onlyArchiveList.subList((int) (onlyArchiveList.size() / 2), onlyArchiveList.size());
                    previewListView.getItems().clear();


                    if (archiveListPart2.size() > 0) {
                        Extracter extracterPart2 = new Extracter(archiveListPart2, destPath, zipPassword);
                        previewListView.getItems().add(drawIndicatorOfFile(extracterPart2));
                        es.submit(extracterPart2);
                    }
                    if (archiveListPart1.size() > 0) {
                        Extracter extracterPart1 = new Extracter(archiveListPart1, destPath, zipPassword);
                        previewListView.getItems().add(drawIndicatorOfFile(extracterPart1));
                        es.submit(extracterPart1);
                    }

                    es.shutdown();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    private HBox drawIndicatorOfFile(Extracter extracter){
        HBox myBox = new HBox();
        myBox.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, new CornerRadii(7), BorderWidths.DEFAULT)));

        try {
            VBox progressLabelVBox = new VBox();
            VBox progressBarVbox = new VBox();

            String progressName = "";
            for (File file : extracter.getFiles()) {
                progressName += String.format("%s\t\n", file.getName());
            }

            Label label = new Label(progressName);
            progressLabelVBox.getChildren().add(label);

            ProgressBar pb = new ProgressBar();
            pb.setProgress(0.0);
            pb.progressProperty().bind(extracter.progressProperty());
            progressBarVbox.getChildren().add(pb);
            progressBarVbox.setAlignment(Pos.CENTER_LEFT);

            label.setAlignment(Pos.CENTER_LEFT);
            progressLabelVBox.setPadding(new Insets(0, 5, 0, 5));
            myBox.getChildren().addAll(progressLabelVBox,progressBarVbox);

        }catch (Exception e){
//            e.printStackTrace();
            System.out.println("Err"+ e.getMessage());
        }
        return myBox;

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
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle(null);
            alert.setHeaderText(null);
            alert.setContentText("We are not support "+"'"+file.getName()+"' folder.");
            alert.showAndWait();
        }else {
            img =  new Image(Launcher.class.getResource("fileIcon.png").toString());
        }

        ImageView iv = new ImageView(img) ;
        iv.setFitHeight(148);
        iv.setFitWidth(128);
        String fileName = file.getName();
        if(fileName.length() > 15){
            fileName = fileName.substring(0, Math.min(fileName.length(), 15));
            fileName = fileName+"...";
        }
        Label label = new Label(fileName);


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
