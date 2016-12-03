/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tew;

/**
 *
 * @author fernanda
 */
public class Estilo {
    private String nombre;
    private float[] rankingNormalMes;
    private float[] rankingExpertoMes;
    
    public Estilo(){
        nombre = "";
        rankingNormalMes = new float[9];
        rankingExpertoMes = new float[9];
    }
    
    public void setNombre(String nombre){
        this.nombre = nombre;
    }
    public String getNombre(){
        return nombre;
    }
    public void setRankingNormalMes(float[] rankingNormalMes){
        this.rankingNormalMes= rankingNormalMes;
    }
    public float[] getRankingNormalMes(){
        return rankingNormalMes;
    }
    public void setRankingExpertoMes(float[] rankingExpertoMes){
        this.rankingExpertoMes= rankingExpertoMes;
    }
    public float[] getRankingExpertoMes(){
        return rankingExpertoMes;
    } 
}
