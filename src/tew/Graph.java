package tew;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.neo4j.driver.v1.*;

public class Graph {
    
    Map<String,Float> weights;

    public Graph() {
        this.weights = new HashMap<String,Float>();
    }
    
    
    public void getWeight() throws FileNotFoundException, IOException{
    
        Driver driver = GraphDatabase.driver( "bolt://localhost", AuthTokens.basic( "neo4j", "admin" ) );
        Session session = driver.session();
        
        List estilos = new ArrayList();
        List usuarios = new ArrayList();
        List users = new ArrayList();
        List pesos = new ArrayList();
        
        BufferedReader br = new BufferedReader(new FileReader("graph/estilos.dat"));
        String current = null;
        while((current=br.readLine())!=null){
            estilos.add(current.trim());
        }
        br.close();
        BufferedReader br2 = new BufferedReader(new FileReader("graph/usuarios.dat"));
        current = null;
        while((current=br2.readLine())!=null){
            usuarios.add(current.trim());
        }
        br2.close();
        
        for(int i = 0;i<estilos.size();i++){
            StatementResult result = session.run("match (u:User)-[r:Comments]->(s:Style) where s.name = '"+estilos.get(i)+"' return r, u.name as name");
            while(result.hasNext()){
                Record record = result.next();
                users.add(record.get("name").asString().trim());
            }
        }
        
        for(int i=0;i<usuarios.size();i++){
            int peso = 0;
            String user = (String) usuarios.get(i);
            for(int j=0;j<users.size();j++){
                String usuario = (String) users.get(j);
                if(user.equals(usuario)) peso++;
            }
            pesos.add(peso);
        }
        
        int maximum = 0;
        for (int i = 0;i<pesos.size();i++){
            if((int)pesos.get(i)>maximum) maximum = (int)pesos.get(i);
        }
        
        for (int i = 0;i<pesos.size();i++){
            this.weights.put((String)usuarios.get(i), (Float)((int)pesos.get(i)/(float)maximum));
        }
        
        session.close();
        driver.close();
    }
    
}
