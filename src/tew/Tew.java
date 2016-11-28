package tew;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.apache.lucene.queryparser.classic.ParseException;

public class Tew {

    public static void main(String[] args) throws IOException, ParseException, FileNotFoundException, ClassNotFoundException {
        if(Files.notExists(Paths.get("index/"))){
            Index.crearIndice();
        }
        //Index.buscarIndice();
        SentClassifier sent = new SentClassifier(new File("classifier/sent.classifier"));
        UsageClassifier usage = new UsageClassifier(new File("classifier/usage.classifier"));
        Graph graph = new Graph();
        graph.getWeight();
    }
    
}
