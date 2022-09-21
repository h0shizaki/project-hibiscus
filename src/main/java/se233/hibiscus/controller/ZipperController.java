package se233.hibiscus.controller;

import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.stage.Popup;
import javafx.stage.Stage;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.EncryptionMethod;
import se233.hibiscus.Launcher;
import se233.hibiscus.model.Zipper;

import java.io.File;
import java.util.ArrayList;

public class ZipperController  {
    public ZipperController() {

    }
    public void createZipFile(ArrayList<String> fileList, String password , String destOutput) {
        if(password.length() > 0){
            ZipParameters zipParameters = new ZipParameters();
            zipParameters.setEncryptFiles(true);
            zipParameters.setEncryptionMethod(EncryptionMethod.AES);
            ZipFile zipFile = new ZipFile(destOutput,password.toCharArray());

            fileList.forEach( file -> {
                try {
                    zipFile.addFile(file, zipParameters);
                } catch (ZipException e) {
                    throw new RuntimeException(e);
                }

            } );

        }else{
            ZipFile zipFile = new ZipFile(destOutput);

            fileList.forEach( file -> {
                try {
                    zipFile.addFile(file);
                } catch (ZipException e) {
                    throw new RuntimeException(e);
                }

            } );
        }
    }

}
