/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jcsp0003
 */
public class HiloSeriabilizacionEscritura extends Thread {

    private Predictor pred;
    private ConfiguracionDataSet confi;

    /**
     * Constructor por defecto de HiloSeriabilizacionEscritura
     *
     * @param pred
     * @param confi
     */
    public HiloSeriabilizacionEscritura(Predictor pred, ConfiguracionDataSet confi) {
        try {
            this.pred = pred.clone();
            this.confi = confi.clone();
        } catch (CloneNotSupportedException ex) {
            Logger.getLogger(HiloSeriabilizacionEscritura.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void run() {
        if (confi.getCorreo() != null && confi.getNombre() != null) {
            ByteArrayOutputStream bs = new ByteArrayOutputStream();
            ObjectOutputStream os;
            try {
                os = new ObjectOutputStream(bs);
                os.writeObject(pred);
                os.close();
                byte[] bytes = bs.toByteArray();
                Path path = Paths.get("./dataSets/" + confi.getCorreo() + "/" + confi.getNombre());
                Files.write(path, bytes);
            } catch (IOException ex) {
                Logger.getLogger(HiloSeriabilizacionEscritura.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
