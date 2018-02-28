/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Util;

import java.net.*;
import java.io.*;

public class Main {

    public static void main(String[] args) throws IOException {
        boolean listening = true;
        comprobarDirectorioDataSet();
        ServerSocket serverSocket = new ServerSocket(4444);

        Socket socket;
        System.out.println("Servidor listo");
        while (listening) {
            socket = serverSocket.accept();
            System.out.println("Nueva conexion de " + socket);
            new hiloCliente(socket).start();
        }
        serverSocket.close();
    }

    private static void comprobarDirectorioDataSet() {
        File directorio = new File("./dataSets");
        if (!directorio.exists()) {
            directorio.mkdir();
        }
    }
}
