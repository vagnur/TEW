package tew;

import com.opencsv.CSVReader;
import java.io.FileReader;
import java.io.IOException;
import org.apache.lucene.queryparser.classic.ParseException;

public class Tew {

    public static void main(String[] args) throws IOException, ParseException {
        Index.crearIndice();
        Index.buscarIndice();
    }
    
}
