package se233.hibiscus.controller;


import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

import java.io.File;
import java.util.ArrayList;

public class MainViewController {

    private ArrayList<String> fileList ;
    @FXML
    private ListView<String> inputListView ;

    public void init() {

        inputListView.setOnDragOver( event -> {
            Dragboard db = event.getDragboard() ;

            if(db.hasFiles() ) {
                event.acceptTransferModes(TransferMode.COPY);
            }else{
                event.consume();
            }
        });

        inputListView.setOnDragDropped( event ->  {
            Dragboard db = event.getDragboard() ;
            boolean isSuccesss = false ;
            if(db.hasFiles()){
                isSuccesss = true ;
                String filePath ;

                int totalFiles = db.getFiles().size() ;
                for(int i = 0 ; i < totalFiles ; i++){
                    File file = db.getFiles().get(i);
                    filePath = file.getAbsolutePath();

                    inputListView.getItems().add(filePath) ;
                }
            }

            event.setDropCompleted(isSuccesss);
            event.consume();

        } );

    }


}
