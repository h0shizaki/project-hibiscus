package se233.hibiscus.model;

import javafx.concurrent.Task;
import net.lingala.zip4j.ZipFile;

import java.io.File;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class Extracter extends Task<Void> {
    private  List<File> files ;

    public List<File> getFiles() {
        return files;
    }

    private String destPath ;
    private CountDownLatch doneSignal ;
    public Extracter (List<File> files, String destPath , CountDownLatch doneSignal){
        this.doneSignal = doneSignal ;
        this.files = files ;
        this.destPath = destPath ;
    }

    @Override
    public Void call() {
        if (files.size() == 0) {
            doneSignal.countDown();
            return null ;
        }

        try {
            for (File file : files) {
                ZipFile decomposer = new ZipFile(file.getAbsolutePath());
                decomposer.extractAll(this.destPath);
            }
        }catch (Exception ex){
//            System.out.println("Err" );
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
        System.out.println("Done!");
        doneSignal.countDown();
        return  null ;
    }

}
