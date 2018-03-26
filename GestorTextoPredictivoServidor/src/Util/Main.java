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

//args localhost predictorbbdd root gestor.predictivo@gmail.com jcsp0003

public class Main {

    private static SSLServerSocket serverSocket;
    private static final int port = 4444;

    public static void main(String[] args) throws IOException {
        System.out.println(Arrays.toString(args));
        try {
            ssl();
        } catch (KeyStoreException | FileNotFoundException | NoSuchAlgorithmException | CertificateException | UnrecoverableKeyException | KeyManagementException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        comprobarDirectorioDataSet();

        Socket socket;
        System.out.println("Servidor listo");
        while (true) {
            socket = serverSocket.accept();
            System.out.println("Nueva conexion de " + socket);
            new hiloCliente(socket,args).start();
        }
    }

    private static void comprobarDirectorioDataSet() {
        File directorio = new File("./dataSets");
        if (!directorio.exists()) {
            directorio.mkdir();
        }
    }

    private static void ssl() throws KeyStoreException, FileNotFoundException, IOException, NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException, KeyManagementException {
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
        serverSocket = (SSLServerSocket) ssf.createServerSocket(port);
    }
}
