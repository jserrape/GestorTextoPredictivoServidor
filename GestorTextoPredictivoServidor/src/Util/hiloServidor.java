/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Util;

import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class hiloServidor extends Thread {

    private Socket socket = null;
    private String mac;
    private PrintWriter out;

    private String dataSetCargado;
    private ConfiguracionDataSet configuracion;
    private Predictor predictor;

    public hiloServidor(Socket socket) {
        super("KKMultiServerThread");
        this.dataSetCargado = "";
        this.socket = socket;
        this.configuracion = new ConfiguracionDataSet();
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException ex) {
            Logger.getLogger(hiloServidor.class.getName()).log(Level.SEVERE, null, ex);
        }
        predictor = new Predictor(this.configuracion);
    }

    @Override
    public void run() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            //Verifico que se ha establecido conexion
            out.println("Conexion correcta con el servidor");

            //Compruebo si tengo dataSets suyos
            mac = in.readLine();
            out.println("mac recibida");
            System.out.println("El cliente tiene la mac: " + mac);

            String mensaje;

            while ((mensaje = in.readLine()) != null) {
                System.out.println("Mensaje del cliente: " + mensaje);
                if (mensaje.length() == 1) {
                    elegirAccion(mensaje.charAt(0), "");
                } else {
                    elegirAccion(mensaje.charAt(0), mensaje.substring(1, mensaje.length()));
                }
            }
            out.close();
            in.close();
            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void elegirAccion(char accion, String mensaje) throws IOException {
        switch (accion) {
            case '1':
                System.out.println("Cliente solicitando lista de dataSets");
                comprobarDataSet();
                break;
            case '2':
                System.out.println("Cliente solicitando crear un dataSet");
                crearDataSet(mensaje);
                comprobarDataSet();
                break;
            case '3':
                System.out.println("Cliente solicitando borrar un dataSet");
                eliminarDataSet(mensaje);
                comprobarDataSet();
                break;
            case '4':
                System.out.println("Cliente solicitando saber su dataSet cargado");
                dataSetCargado();
                break;
            case '5':
                System.out.println("Cliente solicitando cargar un dataSet");
                cargarDataSet(mensaje);
                break;
            case '6':
                System.out.println("Cliente solicitando cargar un documento");
                cargarTexto(mensaje);
                break;
            case '7':
                System.out.println("Cliente solicitando una prediccion");
                realizarPrediccion(mensaje.charAt(0), mensaje.substring(1, mensaje.length()));
                break;
        }
    }

    private void comprobarDataSet() {
        if (comprobarDataSet2()) {
            String contenido = getDataSets();
            if (!"[]".equals(contenido)) {
                System.out.println("Hay ficheros: " + contenido);
                out.println(contenido);
            } else {
                System.out.println("NO HAY NADA");
                out.println("-1");
            }
        } else {
            System.out.println("ELSE 1");
            out.println("-1");
        }
    }

    private boolean comprobarDataSet2() {
        String sDirectorio = "./dataSets/" + mac;
        File f = new File(sDirectorio);

        if (f.exists()) {
            return true;
        } else {
            System.out.println("Directorio mac creado");
            File directorio = new File("./dataSets/" + mac);
            directorio.mkdir();
            return false;
        }
    }

    private void crearDataSet(String nombre) {
        String[] parts = nombre.split("#");
        File fich = new File("./dataSets/" + mac + "/" + parts[0]);
        try {
            fich.createNewFile();
        } catch (IOException ex) {
            Logger.getLogger(hiloServidor.class.getName()).log(Level.SEVERE, null, ex);
        }

        File archivo = new File("./dataSets/" + mac + "/~" + parts[0]);
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(archivo));
            bw.write(parts[1]);
            bw.newLine();
            bw.write(parts[2]);
            bw.newLine();
            bw.write(parts[3]);
            bw.newLine();
            bw.write(parts[4]);
            bw.close();
        } catch (IOException ex) {
            Logger.getLogger(hiloServidor.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void eliminarDataSet(String nombre) {
        System.out.println("BORRO DATASET");
        File fichero = new File("./dataSets/" + mac + "/" + nombre);
        if (fichero.delete()) {
            System.out.println("El fichero ha sido borrado satisfactoriamente");
        } else {
            System.out.println("El fichero no puede ser borrado");
        }
    }

    private void dataSetCargado() {
        if ("".equals(dataSetCargado)) {
            System.out.println("No tiene ningun dataSet cargado");
            out.println("-1");
        } else {
            System.out.println("Tiene cargado el dataSet " + dataSetCargado);
            out.println(dataSetCargado);
        }
    }

    public void cargarDataSet(String mensaje) {
        System.out.println(mensaje);
        this.dataSetCargado = mensaje;

        String sFichero = "./dataSets/" + mac + "/" + mensaje;
        File fichero = new File(sFichero);
        out.println(dataSetCargado);
        if (fichero.length() == 0) {
            System.out.println("Ocupa ==0");
            this.configuracion.cambiarConfiguracionDataSet(mac, mensaje);
            this.predictor = new Predictor(this.configuracion);
        } else {
            System.out.println("Ocupa !=0");
            FileInputStream fis;
            try {
                System.out.println("./dataSets/" + mac + "/" + mensaje);
                fis = new FileInputStream("./dataSets/" + mac + "/" + mensaje);
                try (ObjectInputStream ois = new ObjectInputStream(fis)) {
                    predictor.actualizar((Predictor) ois.readObject());
                }
                fis.close();
            } catch (FileNotFoundException ex) {
                Logger.getLogger(hiloServidor.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException | ClassNotFoundException ex) {
                Logger.getLogger(hiloServidor.class.getName()).log(Level.SEVERE, null, ex);
            }
            this.configuracion = predictor.getConfiguracion();
        }
    }

    public void cargarTexto(String mensaje) throws IOException {
        predictor.insertarTexto(mensaje);
        out.println("OK"); //¿?
        //predictor.mostrarContenido();
    }

    public void realizarPrediccion(char completa, String mensaje) {
        System.out.println("Completa:" + completa);
        System.out.println("Mensaje:" + mensaje);
        String pred = this.predictor.realizarPrediccion(completa, mensaje);
        System.out.println("Prediccion realizada:" + pred);
        out.println(pred);
    }

    private String getDataSets() {
        String sDirectorio = "./dataSets/" + mac;
        File f = new File(sDirectorio);

        File[] ficheros = f.listFiles();

        String[] fichs = new String[ficheros.length / 2];
        for (int i = 0; i < ficheros.length; i++) {
            if (ficheros[i].getName().charAt(0) != '~') {
                fichs[i] = ficheros[i].getName();
            }
        }
        return Arrays.toString(fichs);
    }
}
