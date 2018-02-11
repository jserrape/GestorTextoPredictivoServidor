/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Util;

import java.io.Serializable;

/**
 *
 * @author jcsp0003
 */
public class Ocurrencia implements Serializable {

    private int n;
    private final String prediccion;

    /**
     * Constructor por defecto de la clase Ocurrencia
     */
    public Ocurrencia() {
        this.n = 0;
        this.prediccion = "";
    }

    /**
     * Constructor parametrizado de la clase Ocurrencia
     * 
     * @param pred Predicción nueva
     */
    public Ocurrencia(String pred) {
        this.n = 1;
        this.prediccion = pred;
    }

    /**
     * Concatena la predicción y el número de repeticiones
     *
     * @return Concatenación de la predicción y el número de repeticiones
     */
    @Override
    public String toString() {
        return prediccion + " " + n;
    }

    /**
     * Aumenta el número de ocurrencias de la predicción en uno
     */
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
