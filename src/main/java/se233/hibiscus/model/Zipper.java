package se233.hibiscus.model;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Zipper {
    List<String> paths ;
    ArrayList<File> fileList = new ArrayList<>() ;
    String fileName ;
    public Zipper(List<String> paths , String fileName){

        this.paths = paths ;
        this.fileName = fileName ;
    }

    public String getFileName() {
        return fileName;
    }
}
