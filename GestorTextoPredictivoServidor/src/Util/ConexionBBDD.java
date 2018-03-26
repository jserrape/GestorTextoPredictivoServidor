/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Util;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jcsp0003
 */
public class ConexionBBDD {

    Connection cn;

    /**
     * Crea una conexion a la bbdd
     *
     * @param args
     * @return Devuelve la conexión
     */
    public Connection conexion(String args[]) {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            if (args.length == 6) {
                cn = DriverManager.getConnection("jdbc:mysql://" + args[0] + "/" + args[1], args[2], "");
            } else {
                cn = DriverManager.getConnection("jdbc:mysql://" + args[0] + "/" + args[1], args[2], args[3]);
            }
            System.out.println("Conexion con la bbdd existosa");
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(ConexionBBDD.class.getName()).log(Level.SEVERE, null, ex);
        }
        return cn;
    }

    Statement createStatement() {
        throw new UnsupportedOperationException("No soportado");
    }
}
