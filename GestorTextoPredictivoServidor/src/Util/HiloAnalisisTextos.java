/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jcsp0003
 */
public class HiloAnalisisTextos extends Thread implements Serializable{

    private ConfiguracionDataSet configuracion;
    private Map<String, ArrayList<Ocurrencia>>[] almacenesSemilla;
    private String textoFichero;
    private HiloAnalisisTextos anterior;

    public HiloAnalisisTextos(ConfiguracionDataSet configuracion, Map<String, ArrayList<Ocurrencia>>[] almacenesSemilla, String textoFichero, HiloAnalisisTextos anterior) {
        this.configuracion = configuracion;
        this.almacenesSemilla = almacenesSemilla;
        this.textoFichero = textoFichero;
        this.anterior = anterior;
    }

    @Override
    public synchronized void run() {
        if (anterior != null) {
            try {
                anterior.join();
            } catch (InterruptedException ex) {
                Logger.getLogger(HiloAnalisisTextos.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        System.out.println("Empiezo");
        //Inserto
        for (int i = 1; i < configuracion.getTamMaxSemilla() + 1; i++) {
            insertarTexto(textoFichero, i);
        }
        //Ordeno
        for (int i = configuracion.getTamMaxSemilla(); i > 0; i--) {
            almacenesSemilla[i].forEach((k, v) -> Collections.sort(v, (Ocurrencia o1, Ocurrencia o2) -> Integer.valueOf(o2.getN()).compareTo(o1.getN())));
        }
        System.out.println("Acabo");
        //System.out.println("Fin del analisis del texto");
    }

    /**
     * Inserta una cadena de texto en un almacén determinado
     *
     * @param textoFichero Cadena de texto a almacenar
     * @param almacen Almacen en el que guardar el texto
     */
    public void insertarTexto(String textoFichero, int almacen) {
        String conjuntoSemilla, conjuntoPrediccion;
        String[] frases, palabras;

        frases = textoFichero.split("\\.");
        for (String frase : frases) {
            palabras = frase.split("\\s+");
            for (int j = 0; j < palabras.length - almacen; j++) {
                if ("".equals(palabras[j])) {
                    continue;
                }
                conjuntoSemilla = "";
                conjuntoPrediccion = "";

                //Creo la semilla
                for (int z = 0; z < almacen; z++) {
                    conjuntoSemilla += " " + palabras[j + z];
                }
                conjuntoSemilla = conjuntoSemilla.substring(1, conjuntoSemilla.length());

                //Creo la prediccion
                for (int z = 0; z < this.configuracion.getTamPrediccion(); z++) {
                    if ((j + z + almacen) < palabras.length) {
                        conjuntoPrediccion += " " + palabras[j + z + almacen];
                    }
                }
                conjuntoPrediccion = conjuntoPrediccion.substring(1, conjuntoPrediccion.length());

                if (!almacenesSemilla[almacen].containsKey(conjuntoSemilla)) { //Si no esta la semilla la creo vacia, para despues insertar la prediccion
                    almacenesSemilla[almacen].put(conjuntoSemilla, new ArrayList());
                }
                //AQUI INSERTARÉ LA PREDICCION
                nuevaPrediccion(almacenesSemilla[almacen].get(conjuntoSemilla), conjuntoPrediccion);
            }
        }
    }

    /**
     * Busca la prediccion y aumenta sus ocurrencias, en caso de no existir la
     * crea
     *
     * @param arr Array de ocurrencias de la semilla
     * @param pred Prediccion
     */
    private void nuevaPrediccion(ArrayList<Ocurrencia> arr, String pred) {
        for (int i = 0; i < arr.size(); i++) {
            if (arr.get(i).getPrediccion().equals(pred)) {
                arr.get(i).nuevaOcurrencia();
                return;
            }
        }
        arr.add(new Ocurrencia(pred));
    }

}
