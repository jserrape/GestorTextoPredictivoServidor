/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jcsp0003
 */
public class HiloSeriabilizacion extends Thread {

    private String correo;
    private String mensaje;
    private ConfiguracionDataSet configuracion;
    private Predictor predictor;
    private hiloCliente hiloServer;

    private FileInputStream fis;

    /**
     * Constructor por defecto de HiloSeriabilizacion
     */
    public HiloSeriabilizacion() {
    }

    /**
     * Constructor parametrizado de HiloSeriabilizacion
     *
     * @param correo Identificador del dueño del dataSet
     * @param mensaje Cadena de texto a almacenar en el dataSet
     * @param configuracion Configuracion del dataSet
     * @param predictor Clase encargada de gestionar las predicciones
     * @param hiloCliente Hilo que atiende las peticiones de un cliente
     */
    public HiloSeriabilizacion(String correo, String mensaje, ConfiguracionDataSet configuracion, Predictor predictor, hiloCliente hiloCliente) {
        this.correo = correo;
        this.mensaje = mensaje;
        this.configuracion = configuracion;
        this.predictor = predictor;
        this.hiloServer = hiloCliente;
    }

    /**
     * Funcion encargada del análisis de la cadena de texto y de guardar el
     * conjunto de datos actualizado
     */
    @Override
    public void run() {
        String sFichero = "./dataSets/" + correo + "/" + mensaje;
        File fichero = new File(sFichero);

        if (fichero.length() == 0) {
            this.configuracion.cambiarConfiguracionDataSet(correo, mensaje);
            this.predictor = new Predictor(this.configuracion);
            this.hiloServer.cambiarConfiguracion(configuracion);
            this.hiloServer.cambiarPredictor(predictor);
        } else {
            try {
                fis = new FileInputStream("./dataSets/" + correo + "/" + mensaje);
                try (ObjectInputStream ois = new ObjectInputStream(fis)) {
                    predictor.actualizar((Predictor) ois.readObject());
                    this.hiloServer.cambiarPredictor(predictor);
                } catch (IOException ex) {
                    Logger.getLogger(HiloSeriabilizacion.class.getName()).log(Level.SEVERE, null, ex);
                }
                fis.close();
            } catch (FileNotFoundException ex) {
                Logger.getLogger(hiloCliente.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException | ClassNotFoundException ex) {
                Logger.getLogger(hiloCliente.class.getName()).log(Level.SEVERE, null, ex);
            }
            this.configuracion = predictor.getConfiguracion();
            this.hiloServer.cambiarConfiguracion(configuracion);
        }
    }

    /**
     * Función para interrumpir la ejecucion del HiloSeriabilizacion
     */
    public void interrumpirHilo() {
        try {
            fis.close();
        } catch (IOException ex) {
            Logger.getLogger(HiloSeriabilizacion.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.interrupt();
    }

}
