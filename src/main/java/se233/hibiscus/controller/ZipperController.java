package se233.hibiscus.controller;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.EncryptionMethod;

import java.io.File;
import java.util.ArrayList;

public class ZipperController  {
    public ZipperController() {

    }
    public void createZipFile(ArrayList<File> fileList, String password , String destOutput) {
        System.out.println(fileList);
        System.out.println(password);
        System.out.println(destOutput);

        if(password.length() > 0){
            ZipParameters zipParameters = new ZipParameters();
            zipParameters.setEncryptFiles(true);
            zipParameters.setEncryptionMethod(EncryptionMethod.AES);
            ZipFile zipFile = new ZipFile(destOutput,password.toCharArray());

            fileList.forEach( file -> {
                try {
                    zipFile.addFile(file.getAbsolutePath(), zipParameters);
                } catch (ZipException e) {
                    throw new RuntimeException(e);
                }

            } );

        }else{
            ZipFile zipFile = new ZipFile(destOutput);

            fileList.forEach( file -> {
                try {
                    zipFile.addFile(file.getAbsolutePath());
                } catch (ZipException e) {
                    throw new RuntimeException(e);
                }

            } );
        }


    }

}
