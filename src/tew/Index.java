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
        Directory dir = FSDirectory.open(Paths.get("indice/"));
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
                    if(row.length>1){
                        Document doc = new Document();
                        doc.add(new TextField("experto", row[0], Field.Store.YES));
                        doc.add(new TextField("nota experto", row[1], Field.Store.YES));
                        doc.add(new TextField("comentario experto", row[2], Field.Store.YES));
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
    
    public static void buscarIndice() throws IOException, ParseException{
        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get("indice/")));
        IndexSearcher searcher = new IndexSearcher(reader);
        Analyzer analyzer = new StandardAnalyzer();
        
        QueryParser parser = new QueryParser("comentario experto", analyzer);
        Query query = parser.parse("job");
        //mallet tiene un jar que puede clasificar los comentarios -> buscar Api para integrarlo con java
        //pasar todas las palabras de la bolsa de palabras -> para comparar dos tópicos
        //investigar set para evaluar la intersección 
        
        //bolsa de palabras -> tópicos definidos 
        TopDocs results = searcher.search(query,10);
        ScoreDoc[] hits = results.scoreDocs;
        
        for (int i=0;i<hits.length;i++){
            Document doc = searcher.doc(hits[i].doc);
            System.out.println((i+1)+".- autor="+doc.get("experto")+" nota="+doc.get("nota experto")+" estilo="+doc.get("estilo"));
            System.out.println((i+1)+".- score="+hits[i].score+" doc="+hits[i].doc+" comentario="+doc.get("comentario experto"));
        }
        
        System.out.println("\n");
        
        QueryParser parserN = new QueryParser("comentario normal", analyzer);
        Query queryN = parserN.parse("job");
        
        results = searcher.search(queryN,10);
        hits = results.scoreDocs;
        
        for (int i=0;i<hits.length;i++){
            Document doc = searcher.doc(hits[i].doc);
            System.out.println((i+1)+".- nota normal="+doc.get("nota normal")+" likes/reacciones"+doc.get("likes")+"/"+doc.get("reacciones")+" fecha="+doc.get("fecha")+" estilo="+doc.get("estilo"));
            System.out.println((i+1)+".- score="+hits[i].score+" doc="+hits[i].doc+" comentario="+doc.get("comentario normal"));
        }
    }
    
    public static void rankingNormal2016() throws IOException{
        
        File direc = new File("info/normal2016");
        String nombreArchivos[] = direc.list();
        Estilo[] estilos = new Estilo[nombreArchivos.length];
        int contador = 0;
        for (String nombre : nombreArchivos){
            //obtener el valor de las reacciones maximas por mes
            int[] reaccion = new int[9]; //meses 2016 (enero a septiembre)
            float[] rankingMes = new float[9];
            String estilo2016 = "info/normal2016/"+nombre;
            estilos[contador] = new Estilo();
            estilos[contador].setNombre(nombre);
            FileReader archivo;
            try{
                archivo = new FileReader(estilo2016);
                BufferedReader buffer = new BufferedReader(archivo);
                String linea = "";
                String[] columna;
                while((linea = buffer.readLine()) != null){
                    columna = linea.split(";");
                    int meGusta = Integer.valueOf(columna[2]);
                    int noMeGusta = Integer.valueOf(columna[3]);
                    String fecha = columna[4];
                    String[] date = fecha.split("/");
                    int i = 0; //indice de fecha mm/dd/yyyy
                    if(date.length == 1 ){
                        // no hay división fecha en formato dd-mm-yyyy
                        date = fecha.split("-");
                        i = 1;
                    }
                    int mes = Integer.valueOf(date[i]) - 1;
                    int maxReacComentario = Integer.max(meGusta, noMeGusta);
                    reaccion[mes] = Integer.max(reaccion[mes], maxReacComentario);
                }
                archivo.close();
                //vuelvo a leer el archivo para hacer el calculo de los comentarios con
                //la cantidad de reacciones maximas por mes
                archivo = new FileReader(estilo2016);
                BufferedReader bufferComentario = new BufferedReader(archivo);
                while((linea = bufferComentario.readLine()) != null){
                    columna = linea.split(";");
                    float nota = Float.valueOf(columna[0]);
                    String comentario = columna[1];
                    float meGusta = Float.valueOf(columna[2]);
                    float noMeGusta = Float.valueOf(columna[3]);
                    String fecha = columna[4];
                    String[] date = fecha.split("/");
                    int i = 0; //indice de fecha mm/dd/yyyy
                    if(date.length == 1 ){
                        // no hay división fecha en formato dd-mm-yyyy
                        date = fecha.split("-");
                        i = 1;
                    }
                    //System.out.println(date[i]);
                    int mes = Integer.valueOf(date[i]) - 1;
                    float ranking = (float) (0.3 * nota);
                    if(reaccion[mes] != 0)
                       ranking = ranking + (float) (0.4 * meGusta / reaccion[mes]);
                    rankingMes[mes] = rankingMes[mes] + ranking;
                }
                estilos[contador].setRankingNormalMes(rankingMes);
                for(float e: estilos[contador].getRankingNormalMes()){
                    System.out.println(e);
                }
                archivo.close();
                contador++;
                
            }catch(IOException ioe){System.out.println(ioe);}
                //obtener el valor de las reacciones maximas por mes
                //int[] reaccion = new int[8]; //meses 2016 (enero a septiembre)
                //while((String linea = archivo.readLine()) != null) {
                  //  System.out.println(row[0]);
                    /*
                    int meGusta = Integer.valueOf(row[2]);
                    int noMeGusta = Integer.valueOf(row[3]);
                    String fecha = row[4];
                    String[] date = fecha.split("/");
                    int i = 0; //indice de fecha
                    if(date.length == 1 ){
                        // no hay división fecha en formato dd-mm-yyyy
                        date = fecha.split("-");
                        i = 1;
                    }
                    int mes = Integer.valueOf(date[i]) - 1;
                    int maxReacComentario = Integer.max(meGusta, noMeGusta);
                    reaccion[mes] = Integer.max(reaccion[mes], maxReacComentario);*/
                }
               //System.out.println(reaccion[0] + reaccion[1] + reaccion[2]);
               //csvReader.close();       
        }
    }
    

