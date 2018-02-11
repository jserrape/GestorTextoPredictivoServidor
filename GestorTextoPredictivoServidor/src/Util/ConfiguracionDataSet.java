/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jcsp0003
 */
public class ConfiguracionDataSet implements Serializable {

    private String correo;
    private String nombre;
    private int tamMaxSemilla;
    private int tamMinSemilla;
    private int tamPrediccion;
    private int maxPredicciones;

    /**
     * Cambia la configuracion
     * 
     * @param correo Identificador del dueño del dataSet
     * @param nombre Nombre del dataSet
     */
    public void cambiarConfiguracionDataSet(String correo, String nombre) {
        this.nombre = nombre;
        this.correo = correo;
        cargarConfiguracionDataSet(correo);
    }

    /**
     * Carga el fichero de configuracion del dataSet
     * 
     * @param correo Identificador del dueño del dataSet
     */
    private void cargarConfiguracionDataSet(String correo) {
        System.out.print("Leyendo el fichero de configuracion");
        File archivo = new File("./dataSets/" + correo + "/~" + getNombre());

        try {
            FileReader fr = new FileReader(archivo);
            BufferedReader br = new BufferedReader(fr);

            // Lectura del fichero
            tamMaxSemilla = Integer.parseInt(br.readLine());
            tamMinSemilla = Integer.parseInt(br.readLine());
            tamPrediccion = Integer.parseInt(br.readLine());
            maxPredicciones = Integer.parseInt(br.readLine());
            fr.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ConfiguracionDataSet.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ConfiguracionDataSet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * @return the nombre
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * @return the tamMaxSemilla
     */
    public int getTamMaxSemilla() {
        return tamMaxSemilla;
    }

    /**
     * @return the tamMinSemilla
     */
    public int getTamMinSemilla() {
        return tamMinSemilla;
    }

    /**
     * @return the tamPrediccion
     */
    public int getTamPrediccion() {
        return tamPrediccion;
    }

    /**
     * @return the maxPredicciones
     */
    public int getMaxPredicciones() {
        return maxPredicciones;
    }

    /**
     * @return the mac
     */
    public String getMac() {
        return correo;
    }

}
