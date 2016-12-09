package tew;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.apache.lucene.queryparser.classic.ParseException;

public class Tew {
    public static void main(String[] args) throws IOException, ParseException, FileNotFoundException, ClassNotFoundException {
        int mayor = 2016;
        int menor = 1999;
        int documentos = 100;
        if(Files.notExists(Paths.get("index/"))){
            Index.crearIndice();
        }
        Graph graph = new Graph();
        graph.getWeight(mayor,menor);
        SentClassifier sent = new SentClassifier(new File("classifier/sent.classifier"));
        UsageClassifier usage = new UsageClassifier(new File("classifier/usage.classifier"));
        List estilos = new ArrayList();
        BufferedReader br = new BufferedReader(new FileReader("graph/estilos.dat"));
        String current = null;
        while((current=br.readLine())!=null){
            estilos.add(current.trim());
        }
        float distancias[][] = new float[estilos.size()][estilos.size()];
        float tamanos[] = new float[estilos.size()];
        for (int i = 0;i<estilos.size();i++){
            for(int j=0;j<estilos.size();j++){
                if (i!=j){
                    String bolsa = null;
                    String query = "";
                    BufferedReader tags = new BufferedReader(new FileReader("info/"+estilos.get(i).toString()+"/tags"));
                    while((bolsa=tags.readLine())!=null)
                        query = query + bolsa + " ";
                    bolsa = null;
                    BufferedReader tags2 = new BufferedReader(new FileReader("info/"+estilos.get(j).toString()+"/tags"));
                    while((bolsa=tags2.readLine())!=null)
                        query = query + bolsa + " ";
                    distancias[i][j] = Index.calculoDistancia(query, documentos, estilos.get(i).toString(), estilos.get(j).toString());
                }
            }
        }
        
        for (int i = 0;i<estilos.size();i++){
            String bolsa = null;
            String query = "";
            BufferedReader tags = new BufferedReader(new FileReader("info/"+estilos.get(i).toString()+"/tags"));
            while((bolsa=tags.readLine())!=null)
                query = query + bolsa + " ";
            tamanos[i] = (float) (0.8*Index.rankNormal(query, documentos, estilos.get(i).toString(), sent,mayor,menor)+0.2*Index.rankExperto(query, documentos, estilos.get(i).toString(), sent, graph,mayor,menor));
        }
        
        //EJEMPLO DE EVOLUCIÓN DEL ROCK DURANTE LOS AÑOS
        float rock[] = Index.rankAnios(documentos*10, "rock", sent, graph, mayor, menor);
        
    }
    
}
