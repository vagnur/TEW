package tew;

import com.opencsv.CSVReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;


public class Index {
    
    public static void crearIndice() throws IOException{
        Directory dir = FSDirectory.open(Paths.get("index/"));
        Analyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        IndexWriter writer = new IndexWriter(dir,config);
        //Hacer por cada archivo
        File directory = new File("info/");
        String names[] = directory.list();
        for (String name : names){
            if (new File("info/" + name).isDirectory()){
                String csvFilename = "info/"+name+"/expertos";
                CSVReader csvReader = new CSVReader(new FileReader(csvFilename));
                String[] row = null;
                while((row = csvReader.readNext()) != null) {
                    if(row.length==4){
                        Document doc = new Document();
                        doc.add(new TextField("experto", row[0], Field.Store.YES));
                        doc.add(new TextField("nota experto", row[1], Field.Store.YES));
                        doc.add(new TextField("comentario experto", row[2], Field.Store.YES));
                        doc.add(new TextField("fecha", row[3], Field.Store.YES));
                        doc.add(new TextField("estilo",name,Field.Store.YES));
                        writer.addDocument(doc);
                    }
                }
                csvReader.close();
                csvFilename = "info/"+name+"/normal";
                CSVReader csvReader2 = new CSVReader(new FileReader(csvFilename));
                row = null;
                while((row = csvReader2.readNext()) != null) {
                     Document doc = new Document();
                     doc.add(new TextField("nota normal", row[0], Field.Store.YES));
                     doc.add(new TextField("comentario normal", row[1], Field.Store.YES));
                     doc.add(new TextField("likes", row[2], Field.Store.YES));
                     doc.add(new TextField("reacciones", row[3], Field.Store.YES));
                     doc.add(new TextField("fecha", row[4], Field.Store.YES));
                     doc.add(new TextField("estilo",name,Field.Store.YES));
                     writer.addDocument(doc);
                }
                csvReader2.close();
            }
        }
        writer.close();
    }
    
    public static float calculoDistancia(String termino, int cantidad,String estilo1,String estilo2) throws IOException, ParseException{
        float coincidencias = 0;
        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get("index/")));
        IndexSearcher searcher = new IndexSearcher(reader);
        Analyzer analyzer = new StandardAnalyzer();
        
        QueryParser parser = new QueryParser("comentario experto", analyzer);
        Query query = parser.parse(termino);
        
        TopDocs results = searcher.search(query,cantidad);
        ScoreDoc[] hits = results.scoreDocs;
        
        for (int i=0;i<hits.length;i++){
            Document doc = searcher.doc(hits[i].doc);
            if(doc.get("estilo").equals(estilo1) || doc.get("estilo").equals(estilo2))
                coincidencias ++;
            //System.out.println((i+1)+".- autor="+doc.get("experto")+" nota="+doc.get("nota experto")+" estilo="+doc.get("estilo")+" fecha="+doc.get("fecha"));
            //System.out.println((i+1)+".- score="+hits[i].score+" doc="+hits[i].doc+" comentario="+doc.get("comentario experto"));
        }
        
        QueryParser parserN = new QueryParser("comentario normal", analyzer);
        Query queryN = parserN.parse(termino);
        
        results = searcher.search(queryN,cantidad);
        hits = results.scoreDocs;
        
        for (int i=0;i<hits.length;i++){
            Document doc = searcher.doc(hits[i].doc);
            if(doc.get("estilo").equals(estilo1) || doc.get("estilo").equals(estilo2)){
                coincidencias ++;
            }
            //System.out.println((i+1)+".- nota normal="+doc.get("nota normal")+" likes/reacciones"+doc.get("likes")+"/"+doc.get("reacciones")+" fecha="+doc.get("fecha")+" estilo="+doc.get("estilo"));
            //System.out.println((i+1)+".- score="+hits[i].score+" doc="+hits[i].doc+" comentario="+doc.get("comentario normal"));
        }
        
        return coincidencias/(float)cantidad;
    }
    
}