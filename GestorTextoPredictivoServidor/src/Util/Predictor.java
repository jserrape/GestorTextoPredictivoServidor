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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Xenahort
 */
public class Predictor implements Serializable {

    private ConfiguracionDataSet configuracion;
    private Map<String, ArrayList<Ocurrencia>>[] almacenesSemilla;

    public Predictor(ConfiguracionDataSet conf) {
        this.configuracion = conf;
        this.almacenesSemilla = new Map[this.configuracion.getTamMaxSemilla() + 1];
        for (int i = 1; i < this.configuracion.getTamMaxSemilla() + 1; i++) {
            this.almacenesSemilla[i] = new HashMap<>();
        }
    }

    public void insertarTexto(String textoFichero) throws IOException {
        //Inserto
        for (int i = 1; i < this.getConfiguracion().getTamMaxSemilla() + 1; i++) {
            insertarTexto(textoFichero, i);
        }
        //Ordeno
        for (int i = this.getConfiguracion().getTamMaxSemilla(); i > 0; i--) {
            almacenesSemilla[i].forEach((k, v) -> Collections.sort(v, (Ocurrencia o1, Ocurrencia o2) -> Integer.valueOf(o2.getN()).compareTo(o1.getN())));
        }
        //Seriabilizo la clase
        seriabilizar();
    }

    public void insertarTexto(String textoFichero, int almacen) {
        //System.out.println("Voy a insertar texto en el almacen: " + almacen);
        String conjuntoSemilla, conjuntoPrediccion;
        String[] frases, palabras;

        frases = textoFichero.split("\\.");
        for (String frase : frases) {
            palabras = frase.split("\\s+");
            //System.out.println("palabras:");
            //System.out.println(Arrays.toString(palabras));
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

    private void nuevaPrediccion(ArrayList<Ocurrencia> arr, String pred) {
        for (int i = 0; i < arr.size(); i++) {
            if (arr.get(i).getPrediccion().equals(pred)) {
                arr.get(i).nuevaOcurrencia();
                return;
            }
        }
        arr.add(new Ocurrencia(pred));
    }

    public String realizarPrediccion(char completa, String texto) {
        if (completa == '1') {
            System.out.println("Semilla acabada");
            ArrayList<String> arr = new ArrayList();
            String[] palabras = texto.split("\\s+");
            int limite;
            String semilla;

            for (int i = this.getConfiguracion().getTamMaxSemilla(); i > 0; i--) {
                //System.out.println("---------Semilla tamaño: " + i + "  -----------");
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
                        if (!arr.contains(almacenesSemilla[i].get(semilla).get(z).getPrediccion())) {
                            arr.add(almacenesSemilla[i].get(semilla).get(z).getPrediccion());
                            if (arr.size() == this.getConfiguracion().getMaxPredicciones()) {
                                System.out.println(arr.toString());
                                return arr.toString();
                            }
                        }
                    }
                }
            }
            return arr.toString();
        } else {
            System.out.println("Semilla no acabada");
            ArrayList<String> arr = new ArrayList();
            String[] palabras = texto.split("\\s+");
            int limite;
            String semillaBase, semillaInacabada;
            for (int i = this.getConfiguracion().getTamMaxSemilla(); i > 0; i--) {
                //System.out.println("---------Semilla tamaño: " + i + "  -----------");
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
                System.out.println("Semillabase:" + semillaBase + "TamSemilla:" + i);
                if (!"".equals(semillaBase)) { // Situación semillaBase no nula
                    semillaBase = semillaBase.substring(0, semillaBase.length() - 1);
                    if (almacenesSemilla[i].containsKey(semillaBase)) {
                        for (int j = 0; j < almacenesSemilla[i].get(semillaBase).size(); j++) {
                            if (almacenesSemilla[i].get(semillaBase).get(j).getPrediccion().indexOf(semillaInacabada) == 0) {
                                if (!arr.contains(almacenesSemilla[i].get(semillaBase).get(j).getPrediccion())) {
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

    public void seriabilizar() throws IOException {
        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(bs);
        os.writeObject(this);
        os.close();
        byte[] bytes = bs.toByteArray();
        Path path = Paths.get("./dataSets/" + this.getConfiguracion().getMac() + "/" + this.getConfiguracion().getNombre());
        Files.write(path, bytes);
    }

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
