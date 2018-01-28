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

    private String mac;
    private String mensaje;
    private ConfiguracionDataSet configuracion;
    private Predictor predictor;
    private HiloServidor hiloServer;

    private FileInputStream fis;

    public HiloSeriabilizacion() {
    }

    public HiloSeriabilizacion(String mac, String mensaje, ConfiguracionDataSet configuracion, Predictor predictor, HiloServidor hiloServer) {
        this.mac = mac;
        this.mensaje = mensaje;
        this.configuracion = configuracion;
        this.predictor = predictor;
        this.hiloServer = hiloServer;
    }

    @Override
    public void run() {
        System.out.println("Comienzo del hilo de seriabilizacion");
        String sFichero = "./dataSets/" + mac + "/" + mensaje;
        File fichero = new File(sFichero);

        if (fichero.length() == 0) {
            this.configuracion.cambiarConfiguracionDataSet(mac, mensaje);
            this.predictor = new Predictor(this.configuracion);
            this.hiloServer.cambiarConfiguracion(configuracion);
            this.hiloServer.cambiarPredictor(predictor);
        } else {
            try {
                fis = new FileInputStream("./dataSets/" + mac + "/" + mensaje);
                try (ObjectInputStream ois = new ObjectInputStream(fis)) {
                    predictor.actualizar((Predictor) ois.readObject());
                    this.hiloServer.cambiarPredictor(predictor);
                } catch (IOException ex) {
                    Logger.getLogger(HiloSeriabilizacion.class.getName()).log(Level.SEVERE, null, ex);
                }
                fis.close();
            } catch (FileNotFoundException ex) {
                Logger.getLogger(HiloServidor.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException | ClassNotFoundException ex) {
                Logger.getLogger(HiloServidor.class.getName()).log(Level.SEVERE, null, ex);
            }
            this.configuracion = predictor.getConfiguracion();
            this.hiloServer.cambiarConfiguracion(configuracion);
        }
        System.out.println("Final del hilo de seriabilizacion");
    }

    public void interrumpirHilo() {
        try {
            fis.close();
        } catch (IOException ex) {
            Logger.getLogger(HiloSeriabilizacion.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.interrupt();
    }

}
