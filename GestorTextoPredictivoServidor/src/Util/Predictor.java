/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author jcsp0003
 */
public class Predictor implements Serializable, Cloneable {

    private ConfiguracionDataSet configuracion;
    private Map<String, ArrayList<Ocurrencia>>[] almacenesSemilla;
    private HiloAnalisisTextos anteriorAnalisis;

    /**
     * Constructor parametrizado de la clase Predictor
     *
     * @param conf Configuración del conjunto de datos
     */
    public Predictor(ConfiguracionDataSet conf) {
        this.configuracion = conf;
        anteriorAnalisis=null;
        this.almacenesSemilla = new Map[this.configuracion.getTamMaxSemilla() + 1];
        for (int i = 1; i < this.configuracion.getTamMaxSemilla() + 1; i++) {
            this.almacenesSemilla[i] = new HashMap<>();
        }
    }
    
    @Override
    public Predictor clone() throws CloneNotSupportedException{
         Predictor clon = (Predictor) super.clone();
         return clon;
    }

    /**
     * Inserta una nueva cadena en el conjunto de datos
     *
     * @param textoFichero Nueva cadena a insertar
     * @throws IOException Excepción de E/S
     */
    public void insertarTexto(String textoFichero) throws IOException {
        HiloAnalisisTextos aux=new HiloAnalisisTextos(this.configuracion,this.almacenesSemilla,textoFichero,this.anteriorAnalisis);
        aux.start();
        this.anteriorAnalisis=aux;
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
                for (int z = 0; z < this.getConfiguracion().getTamPrediccion(); z++) {
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

    /**
     * Realiza una prediccion
     *
     * @param completa Indica si la semilla está acabada o no
     * @param texto Semilla a partir de la cual predecir
     * @return Predicciones
     */
    public String realizarPrediccion(char completa, String texto) {
        if (completa == '1') {
            ArrayList<String> arr = new ArrayList();
            String[] palabras = texto.split("\\s+");
            int limite;
            String semilla;
            boolean contain;

            for (int i = this.getConfiguracion().getTamMaxSemilla(); i > 0; i--) {
                limite = 0;
                semilla = "";
                for (int j = palabras.length - 1; j >= 0; j--) {
                    semilla = palabras[j] + " " + semilla;
                    ++limite;
                    if (limite == i) {
                        if ("".equals(semilla)) {
                            return (new ArrayList()).toString();
                        }
                        break;
                    }
                }
                if ("".equals(semilla.replaceAll(" ", ""))) {
                    return arr.toString();
                }
                semilla = semilla.substring(0, semilla.length() - 1);

                if (almacenesSemilla[i].containsKey(semilla)) {
                    for (int z = 0; z < almacenesSemilla[i].get(semilla).size(); z++) {
                        contain = false;
                        for (int m = 0; m < arr.size(); m++) {
                            if (arr.get(m).equals(almacenesSemilla[i].get(semilla).get(z).getPrediccion())) {
                                contain = true;
                            }
                        }
                        if (!contain) {
                            arr.add(almacenesSemilla[i].get(semilla).get(z).getPrediccion());
                            if (arr.size() == this.getConfiguracion().getMaxPredicciones()) {
                                return arr.toString();
                            }
                        } else {
                            System.out.println("Ya contiene " + almacenesSemilla[i].get(semilla).get(z).getPrediccion());
                        }
                    }
                }
            }
            return arr.toString();
        } else {
            ArrayList<String> arr = new ArrayList();
            String[] palabras = texto.split("\\s+");
            int limite;
            String semillaBase, semillaInacabada;
            boolean contain;
            
            for (int i = this.getConfiguracion().getTamMaxSemilla(); i > 0; i--) {
                limite = 0;
                semillaBase = "";
                semillaInacabada = palabras[palabras.length - 1];
                for (int j = palabras.length - 2; j >= 0; j--) {
                    semillaBase = palabras[j] + " " + semillaBase;
                    ++limite;
                    if (limite == i) {
                        if ("".equals(semillaBase)) {
                            return (new ArrayList()).toString();
                        }
                        break;
                    }
                }
                if (!"".equals(semillaBase)) { // Situación semillaBase no nula
                    semillaBase = semillaBase.substring(0, semillaBase.length() - 1);
                    if (almacenesSemilla[i].containsKey(semillaBase)) {
                        for (int j = 0; j < almacenesSemilla[i].get(semillaBase).size(); j++) {
                            if (almacenesSemilla[i].get(semillaBase).get(j).getPrediccion().indexOf(semillaInacabada) == 0) {
                                contain = false;
                                for (int m = 0; m < arr.size(); m++) {
                                    if (arr.get(m).equals(almacenesSemilla[i].get(semillaBase).get(j).getPrediccion())) {
                                        contain = true;
                                    }
                                }
                                if (!contain) {
                                    arr.add(almacenesSemilla[i].get(semillaBase).get(j).getPrediccion());
                                    if (arr.size() == this.getConfiguracion().getMaxPredicciones()) {
                                        return arr.toString();
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return arr.toString();
        }
    }

    /**
     * Seriabiliza el conjunto de datos
     *
     * @throws IOException Excención de error en E/S
     */
    public void seriabilizar() throws IOException {
        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(bs);
        os.writeObject(this);
        os.close();
        byte[] bytes = bs.toByteArray();
        Path path = Paths.get("./dataSets/" + this.getConfiguracion().getCorreo() + "/" + this.getConfiguracion().getNombre());
        Files.write(path, bytes);
    }

    /**
     * Actualiza las configuraciones del predictor
     *
     * @param pred Predictor nuevo
     */
    public void actualizar(Predictor pred) {
        this.configuracion = pred.configuracion;
        this.almacenesSemilla = pred.almacenesSemilla;
    }

    /**
     * @return the configuracion
     */
    public ConfiguracionDataSet getConfiguracion() {
        return configuracion;
    }

}
