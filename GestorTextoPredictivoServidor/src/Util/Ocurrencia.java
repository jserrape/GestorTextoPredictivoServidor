/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Util;

import java.io.Serializable;

/**
 *
 * @author Xenahort
 */
public class Ocurrencia implements Serializable{

    private int n;
    private final String prediccion;

    public Ocurrencia() {
        this.n = 0;
        this.prediccion = "";
    }

    @Override
    public String toString(){
        return prediccion+" "+n;
    }
    
    
    public Ocurrencia(String pred) {
        this.n = 1;
        this.prediccion = pred;
    }

    public void nuevaOcurrencia() {
        ++n;
    }

    /**
     * @return the n
     */
    public int getN() {
        return n;
    }

    /**
     * @return the prediccion
     */
    public String getPrediccion() {
        return prediccion;
    }

}
