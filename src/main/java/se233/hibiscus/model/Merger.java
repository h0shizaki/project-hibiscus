package se233.hibiscus.model;

import javafx.concurrent.Task;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import java.io.File;
import java.util.ArrayList;

public class Merger extends Task<Void> {
    private String fileName;
    private ArrayList<File> splitZipFileList;

    public Merger(String fileName, ArrayList<File> splitZipFileList){
        this.fileName = fileName;
        this.splitZipFileList = splitZipFileList ;
    }

    @Override
    public Void call()  throws ZipException {
        ZipFile mergedZipFile = new ZipFile(this.fileName);

        for(File splitFile : splitZipFileList){
            mergedZipFile.mergeSplitFiles(splitFile);
        }

        return null;
    }
}
