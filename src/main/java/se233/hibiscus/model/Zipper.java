package se233.hibiscus.model;

import javafx.concurrent.Task;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Zipper extends Task<Void> {

    List<File> fileList ;
    String fileName ;
    public Zipper(List<File> fileList , String fileName){
        this.fileList = fileList ;
        this.fileName = fileName ;
    }
    @Override
    public Void call()  {
        ZipFile zipFile = new ZipFile(this.fileName);

        fileList.forEach( file -> {
            try {
                zipFile.addFile(file);
            } catch (ZipException e) {
                throw new RuntimeException(e);
            }
        } );
        System.out.println("Working!!");
        return null;
    }
}
