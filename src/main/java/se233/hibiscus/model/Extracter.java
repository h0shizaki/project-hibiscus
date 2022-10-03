package se233.hibiscus.model;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.TextInputDialog;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

public class Extracter extends Task<Void> {
    private  List<File> files ;
    private String destPath ;
    private Map<File, String> passwordMap ;
    public Extracter (List<File> files, String destPath, Map<File,String> passwordMap) {
        this.files = files ;
        this.destPath = destPath ;
        this.passwordMap = passwordMap ;
    }

    @Override
    public Void call() {
        if (files.size() == 0) {
            updateProgress(1,1);
            return null ;
        }

        for(int i = 0 ; i < files.size() ; i++){
            File file = files.get(i) ;
            try{

            ZipFile zipFile = new ZipFile(file.getAbsolutePath());
            if(zipFile.isEncrypted()){
                zipFile = new ZipFile(file.getAbsolutePath(), passwordMap.get(file).toCharArray());
            }

            zipFile.extractAll(destPath);
            }catch (ZipException ex){
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        Alert alert = new Alert(Alert.AlertType.WARNING);
                        alert.setTitle(ex.getMessage());
                        alert.setHeaderText(null);
                        alert.setContentText(ex.getMessage()+" for " + file.getName());
                        alert.showAndWait();
                    }
                });
            }catch(Exception e){
                throw new RuntimeException(e);
            }

            updateProgress(i+1,files.size());
        }

        return  null ;
    }

    public List<File> getFiles() {
        return files;
    }

}
