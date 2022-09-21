package se233.hibiscus.model;

import javafx.concurrent.Task;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.model.ZipParameters;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class Merger extends Task<Void> {
    private String fileName;
    private ArrayList<File> splitZipFileList;


    public Merger(String fileName, ArrayList<String> splitFileName){
        this.fileName = fileName;
        this.splitZipFileList = new ArrayList<>();

        for (String filename : splitFileName ){
            splitZipFileList.add(new File(filename));
        }

    }

    @Override
    public Void call() throws  ZipException, IOException{
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
        System.out.println("Merged!");

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

//    @Override
//    public Void call() throws ZipException {
//        ZipFile mergedZipFile = new ZipFile(this.fileName);
//
//        for(File splitFile : splitZipFileList){
//            mergedZipFile.mergeSplitFiles(splitFile);
//        }
//        System.out.println("Merged");
//        return null;
//    }
}
