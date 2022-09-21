package se233.hibiscus.model;

import javafx.concurrent.Task;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.EncryptionMethod;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class Merger extends Task<Void> {
    private  String password;
    private String fileName;
    private ArrayList<File> splitZipFileList;


    public Merger(String fileName, ArrayList<String> splitFileName, String password){
        this.fileName = fileName;
        this.splitZipFileList = new ArrayList<>();
        this.password = password ;

        for (String filename : splitFileName ){
            splitZipFileList.add(new File(filename));
        }

    }

    @Override
    public Void call() throws IOException{
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

        System.out.println("Complete");

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
