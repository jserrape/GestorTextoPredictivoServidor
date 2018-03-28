/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Util;

import java.net.*;
import java.io.*;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

//-Xmx4g
//localhost predictorbbdd root gestor.predictivo@gmail.com jcsp0003 1
public class Main {

    private static ServerSocket serverSocket;
    private static SSLServerSocket SSLserverSocket;
    private static final int port = 4444;

    public static void main(String[] args) throws IOException {
        comprobarDirectorioDataSet();
        System.out.println(Arrays.toString(args));
        if ("0".equals(args[args.length - 1])) {
            noSsl(args);
        } else {
            try {
                ssl(args);
            } catch (KeyStoreException | FileNotFoundException | NoSuchAlgorithmException | CertificateException | UnrecoverableKeyException | KeyManagementException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private static void comprobarDirectorioDataSet() {
        File directorio = new File("./dataSets");
        if (!directorio.exists()) {
            directorio.mkdir();
        }
    }

    private static void noSsl(String[] args) throws IOException {
        serverSocket = new ServerSocket(4444);
        Socket socket;
        System.out.println("Servidor listo sin SSL");
        while (true) {
            socket = serverSocket.accept();
            System.out.println("Nueva conexion de " + socket);
            new hiloCliente(socket, args).start();
        }
    }

    private static void ssl(String[] args) throws KeyStoreException, FileNotFoundException, IOException, NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException, KeyManagementException {
        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(new FileInputStream("certs/server/serverKey.jks"), "servpass".toCharArray());

        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, "servpass".toCharArray());

        KeyStore trustedStore = KeyStore.getInstance("JKS");
        trustedStore.load(new FileInputStream("certs/server/serverTrustedCerts.jks"), "servpass".toCharArray());

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustedStore);

        SSLContext sc = SSLContext.getInstance("TLS");
        TrustManager[] trustManagers = tmf.getTrustManagers();
        KeyManager[] keyManagers = kmf.getKeyManagers();
        sc.init(keyManagers, trustManagers, null);

        SSLServerSocketFactory ssf = sc.getServerSocketFactory();
        SSLserverSocket = (SSLServerSocket) ssf.createServerSocket(port);

        Socket socket;
        System.out.println("Servidor listo con SSL");
        while (true) {
            socket = SSLserverSocket.accept();
            System.out.println("Nueva conexion de " + socket);
            new hiloCliente(socket, args).start();
        }
    }
}
