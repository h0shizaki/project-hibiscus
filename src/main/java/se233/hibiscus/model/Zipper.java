package se233.hibiscus.model;

import javafx.concurrent.Task;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Zipper extends Task<Void> {
    List<String> paths ;
    ArrayList<File> fileList = new ArrayList<>() ;
    String fileName ;
    public Zipper(List<String> paths , String fileName){

        this.paths = paths ;
        this.fileName = fileName ;
        paths.forEach( path -> {
            fileList.add(new File(path)) ;
        });
    }
    @Override
    public Void call()  {
        ZipFile zipFile = new ZipFile("/temp/"+ this.fileName);

        fileList.forEach( file -> {
            try {
                zipFile.addFile(file);
            } catch (ZipException e) {
                throw new RuntimeException(e);
            }
        } );

        return null;
    }
}
