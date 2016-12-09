package tew;

import com.opencsv.CSVReader;
import java.io.BufferedReader;
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
        }
        
        return coincidencias/(float)cantidad;
    }
    
    public static float rankNormal(String termino, int cantidad, String estilo, SentClassifier sent,int alto, int bajo) throws IOException, ParseException{
        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get("index/")));
        IndexSearcher searcher = new IndexSearcher(reader);
        Analyzer analyzer = new StandardAnalyzer();
        
        QueryParser parser = new QueryParser("comentario normal", analyzer);
        Query query = parser.parse(termino);
        
        TopDocs results = searcher.search(query,cantidad);
        ScoreDoc[] hits = results.scoreDocs;
        
        int maxreac = 0;
        
        float puntaje = 0;
        
        for (int i=0;i<hits.length;i++){
            Document doc = searcher.doc(hits[i].doc);
            if(doc.get("estilo").equals(estilo)){
                if(maxreac<Integer.parseInt(doc.get("reacciones").toString()))
                    maxreac = Integer.parseInt(doc.get("reacciones").toString());
            }
        }
        
        int maxnota = 0;
        
        for (int i=0;i<hits.length;i++){
            Document doc = searcher.doc(hits[i].doc);
            if(doc.get("estilo").equals(estilo)){
                if(maxnota<Integer.parseInt(doc.get("nota normal").toString()))
                    maxnota = Integer.parseInt(doc.get("nota normal").toString());
            }
        }
        
        int total = 0;
        
        for (int i=0;i<hits.length;i++){
            Document doc = searcher.doc(hits[i].doc);
            if(doc.get("estilo").equals(estilo)){
                String fecha = doc.get("fecha");
                String fechas[] = fecha.split(",");
                if(Integer.parseInt(fechas[1].trim())<alto && Integer.parseInt(fechas[1].trim())>bajo){
                    total++;
                    sent.getLabelings(doc.get("comentario normal"));
                    try{
                    if(sent.positivo>sent.negativo && sent.positivo>sent.neutral) 
                        puntaje = (float) (puntaje + (0.3*(sent.positivo)+0.3*Integer.parseInt(doc.get("nota normal").toString())/maxnota)+0.4*(Integer.parseInt(doc.get("likes").toString())/maxreac));
                    if(sent.negativo>sent.positivo && sent.negativo>sent.neutral)
                        puntaje = (float) (puntaje + (0.3*(sent.negativo/4)+0.3*Integer.parseInt(doc.get("nota normal").toString())/maxnota)+0.4*(Integer.parseInt(doc.get("likes").toString())/maxreac));
                    if(sent.neutral>sent.positivo && sent.neutral>sent.negativo)
                        puntaje = (float) (puntaje + (0.3*(sent.neutral/2)+0.3*Integer.parseInt(doc.get("nota normal").toString())/maxnota)+0.4*(Integer.parseInt(doc.get("likes").toString())/maxreac));
                    }
                    catch(Exception e){
                        total --;
                    }
                }
            }
        }
        
        if (total!=0)
            return puntaje/total;
        else
            return puntaje;
        
    }
    
    public static float rankExperto(String termino, int cantidad, String estilo, SentClassifier sent, Graph graph,int alto, int bajo) throws IOException, ParseException{
        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get("index/")));
        IndexSearcher searcher = new IndexSearcher(reader);
        Analyzer analyzer = new StandardAnalyzer();
        
        QueryParser parser = new QueryParser("comentario experto", analyzer);
        Query query = parser.parse(termino);
        
        TopDocs results = searcher.search(query,cantidad);
        ScoreDoc[] hits = results.scoreDocs;
        
        float puntaje = 0;
        
        int maxnota = 0;
        
        for (int i=0;i<hits.length;i++){
            Document doc = searcher.doc(hits[i].doc);
            if(doc.get("estilo").equals(estilo)){
                if(maxnota<Integer.parseInt(doc.get("nota experto").toString()))
                   maxnota = Integer.parseInt(doc.get("nota experto").toString());
            }
        }
        
        int total = 0;
        
        for (int i=0;i<hits.length;i++){
            Document doc = searcher.doc(hits[i].doc);
            if(doc.get("estilo").equals(estilo)){
                String fecha = doc.get("fecha");
                String fechas[] = fecha.split(",");
                if(Integer.parseInt(fechas[1].trim())<alto && Integer.parseInt(fechas[1].trim())>bajo){
                    total++;
                    sent.getLabelings(doc.get("comentario experto"));
                    try{
                        puntaje = puntaje + (float)(0.3*((sent.positivo/2)+(sent.neutral/4)+(sent.negativo/8))+0.2*Integer.parseInt(doc.get("nota experto").toString())/maxnota+0.5*graph.weights.get(doc.get("experto")));
                    }
                    catch(Exception e){
                        total --;
                    }
                }
            }
        }
        
        if (total!=0)
            return puntaje/total;
        else
            return puntaje;
    
    }
    
    public static float[] rankAnios(int cantidad, String estilo, SentClassifier sent, Graph graph,int alto, int bajo) throws IOException, ParseException{
        float puntaje[] = new float[alto-bajo+1];
        
        String bolsa = null;
            String termino = "";
            BufferedReader tags = new BufferedReader(new FileReader("info/"+estilo+"/tags"));
            while((bolsa=tags.readLine())!=null)
                termino = termino + bolsa + " ";
        
        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get("index/")));
        IndexSearcher searcher = new IndexSearcher(reader);
        Analyzer analyzer = new StandardAnalyzer();
        
        QueryParser parser = new QueryParser("comentario experto", analyzer);
        Query query = parser.parse(termino);
        
        TopDocs results = searcher.search(query,cantidad);
        ScoreDoc[] hits = results.scoreDocs;
        
        for (int j=0;j<=alto-bajo;j++){
            float puntajeParcial = 0;
            int total = 0;
            int maxnota = 0;
        
            for (int i=0;i<hits.length;i++){
                Document doc = searcher.doc(hits[i].doc);
                if(doc.get("estilo").equals(estilo)){
                    if(maxnota<Integer.parseInt(doc.get("nota experto").toString()))
                       maxnota = Integer.parseInt(doc.get("nota experto").toString());
                }
            }
            
            for (int i=0;i<hits.length;i++){
                Document doc = searcher.doc(hits[i].doc);
                if(doc.get("estilo").equals(estilo)){
                    String fecha = doc.get("fecha");
                    String fechas[] = fecha.split(",");
                    if(Integer.parseInt(fechas[1].trim())==bajo+j){
                        total++;
                        sent.getLabelings(doc.get("comentario experto"));
                        try{
                            puntajeParcial = puntajeParcial + (float)(0.3*((sent.positivo/2)+(sent.neutral/4)+(sent.negativo/8))+0.2*Integer.parseInt(doc.get("nota experto").toString())/maxnota+0.5*graph.weights.get(doc.get("experto")));
                        }
                        catch(Exception e){
                            total --;
                        }
                    }
                }
            }
            
            if(total!=0) puntajeParcial = puntajeParcial/total;
            
            puntaje[j] = puntajeParcial;
        }
        
        QueryParser parserN = new QueryParser("comentario normal", analyzer);
        Query queryN = parserN.parse(termino);
        
        results = searcher.search(queryN,cantidad);
        hits = results.scoreDocs;
        
        for (int j=0;j<=alto-bajo;j++){
            float puntajeParcial = 0;
            int maxreac = 0;
        
        
            for (int i=0;i<hits.length;i++){
                Document doc = searcher.doc(hits[i].doc);
                if(doc.get("estilo").equals(estilo)){
                    if(maxreac<Integer.parseInt(doc.get("reacciones").toString()))
                        maxreac = Integer.parseInt(doc.get("reacciones").toString());
                }
            }
        
            int maxnota = 0;

            for (int i=0;i<hits.length;i++){
                Document doc = searcher.doc(hits[i].doc);
                if(doc.get("estilo").equals(estilo)){
                    if(maxnota<Integer.parseInt(doc.get("nota normal").toString()))
                        maxnota = Integer.parseInt(doc.get("nota normal").toString());
                }
            }
        
            int total = 0;
        
            for (int i=0;i<hits.length;i++){
                Document doc = searcher.doc(hits[i].doc);
                if(doc.get("estilo").equals(estilo)){
                    String fecha = doc.get("fecha");
                    String fechas[] = fecha.split(",");
                    if(Integer.parseInt(fechas[1].trim())==bajo+j){
                        total++;
                        sent.getLabelings(doc.get("comentario normal"));
                        try{
                        if(sent.positivo>sent.negativo && sent.positivo>sent.neutral) 
                            puntajeParcial = (float) (puntajeParcial + (0.3*(sent.positivo)+0.3*Integer.parseInt(doc.get("nota normal").toString())/maxnota)+0.4*(Integer.parseInt(doc.get("likes").toString())/maxreac));
                        if(sent.negativo>sent.positivo && sent.negativo>sent.neutral)
                            puntajeParcial = (float) (puntajeParcial + (0.3*(sent.negativo/4)+0.3*Integer.parseInt(doc.get("nota normal").toString())/maxnota)+0.4*(Integer.parseInt(doc.get("likes").toString())/maxreac));
                        if(sent.neutral>sent.positivo && sent.neutral>sent.negativo)
                            puntajeParcial = (float) (puntajeParcial + (0.3*(sent.neutral/2)+0.3*Integer.parseInt(doc.get("nota normal").toString())/maxnota)+0.4*(Integer.parseInt(doc.get("likes").toString())/maxreac));
                        }
                        catch(Exception e){
                            total --;
                        }
                    }
                }
            }
            if(total!=0) puntajeParcial = puntajeParcial/total;
            
            puntaje[j] = (float) (0.2*puntaje[j]+0.8*puntajeParcial);
        }
        return puntaje;
    }
    
}