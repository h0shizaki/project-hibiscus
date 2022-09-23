package se233.hibiscus.model;

import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.EncryptionMethod;
import se233.hibiscus.Launcher;

import javax.security.auth.callback.LanguageCallback;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

public class Merger extends Task<Void> {
    private  String password;
    private String fileName;
    private ArrayList<File> splitZipFileList;
    private CountDownLatch startSignal;


    public Merger(String fileName, ArrayList<String> splitFileName, String password , CountDownLatch countDownLatch){
        this.fileName = fileName;
        this.splitZipFileList = new ArrayList<>();
        this.password = password ;
        this.startSignal = countDownLatch ;
        for (String filename : splitFileName ){
            splitZipFileList.add(new File(filename));
        }
    }

    @Override
    public Void call() throws IOException, InterruptedException{

        startSignal.await();
        if(password.length() > 0){
            ZipFile mergedZipFile = new ZipFile(this.fileName,password.toCharArray());

            for(File fileToMerge : splitZipFileList) {
                ZipFile zipFileToMerge = new ZipFile(fileToMerge);

                for(FileHeader fileHeader : zipFileToMerge.getFileHeaders()){
                    try (InputStream inputStream = zipFileToMerge.getInputStream(fileHeader)) {
                        ZipParameters zipParameters = getZipParametersFromFileHeaderWithPassword(fileHeader) ;
                        mergedZipFile.addStream(inputStream,zipParameters);

                    }
                }
            }
        }else{
            ZipFile mergedZipFile = new ZipFile(this.fileName);

            for(File fileToMerge : splitZipFileList) {
                ZipFile zipFileToMerge = new ZipFile(fileToMerge);

                for(FileHeader fileHeader : zipFileToMerge.getFileHeaders()){
                    try (InputStream inputStream = zipFileToMerge.getInputStream(fileHeader)) {
                        ZipParameters zipParameters = getZipParametersFromFileHeader(fileHeader) ;
                        mergedZipFile.addStream(inputStream,zipParameters);

                    }
                }
            }
        }

        splitZipFileList.forEach( tempFile -> {
            tempFile.delete() ;
        });

        System.out.println("Merger processed!");

        return  null ;

    }

    private ZipParameters getZipParametersFromFileHeader(FileHeader fileHeader) {
        ZipParameters zipParameters = new ZipParameters();
        zipParameters.setCompressionMethod(fileHeader.getCompressionMethod());
        zipParameters.setFileNameInZip(fileHeader.getFileName());
        return zipParameters;
    }

    private ZipParameters getZipParametersFromFileHeaderWithPassword(FileHeader fileHeader) {
        ZipParameters zipParameters = new ZipParameters();
        zipParameters.setEncryptFiles(true);
        zipParameters.setEncryptionMethod(EncryptionMethod.AES);
        zipParameters.setCompressionMethod(fileHeader.getCompressionMethod());
        zipParameters.setFileNameInZip(fileHeader.getFileName());
        return zipParameters;
    }

}
