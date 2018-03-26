/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Util;

import java.net.*;
import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.apache.commons.codec.digest.DigestUtils;

public class hiloCliente extends Thread {

    private Socket socket = null;
    private String identificadorUsuario;
    private PrintWriter out;
    private BufferedReader in;

    private String dataSetCargado;
    private ConfiguracionDataSet configuracion;
    private Predictor predictor;

    private String remitente;
    private String passw;

    private String args[];

    private HiloSeriabilizacion hiloSeriabilizar;

    /**
     * Constructor parametrizado de la clase hiloCliente
     *
     * @param socket Socket de la conexion
     * @param args
     */
    public hiloCliente(Socket socket, String[] args) {
        super("HiloServidor");
        this.args = args;
        if (args.length == 6) {
            remitente = args[3];
            passw = args[4];
        } else {
            remitente = args[4];
            passw = args[5];
        }
        this.dataSetCargado = "";
        this.socket = socket;
        this.configuracion = new ConfiguracionDataSet();
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException ex) {
            Logger.getLogger(hiloCliente.class.getName()).log(Level.SEVERE, null, ex);
        }
        predictor = new Predictor(this.configuracion);
        hiloSeriabilizar = new HiloSeriabilizacion();
    }

    /**
     * Función de lectura de peticiones del cliente
     */
    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String mensaje;
            while ((mensaje = in.readLine()) != null) {
                System.out.println("Mensaje del cliente: " + mensaje);
                if (mensaje.length() == 1) {
                    elegirAccion(mensaje.charAt(0), "");
                } else {
                    if (mensaje.length() != 0) {
                        elegirAccion(mensaje.charAt(0), mensaje.substring(1, mensaje.length()));
                    }
                }
            }
            out.close();
            in.close();
            socket.close();
            System.out.println("Cierro la conexion del cliente " + identificadorUsuario);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Función que elige una accion segun el mensaje enviado por el cliente
     *
     * @param accion Carácter que indica que accion realizar
     * @param mensaje Mensaje del cliente
     * @throws IOException Error en E/S
     */
    private void elegirAccion(char accion, String mensaje) throws IOException {
        switch (accion) {
            case '0':
                this.desconectar();
                break;
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
                System.out.println("Cliente solicitando cargar un documento"); //ESTUDIAR LA SITUACION SI ESTOY SERIABILIZANDO
                cargarTexto(mensaje);
                break;
            case '7':
                System.out.println("Cliente solicitando una prediccion");
                realizarPrediccion(mensaje.charAt(0), mensaje.substring(1, mensaje.length()));
                break;
            case '8':
                System.out.println("Cliente solicitando login");
                login(mensaje);
                break;
            case '9':
                System.out.println("Cliente solicitando registro");
                registro(mensaje);
                break;
        }
    }

    /**
     * Se desconecta del cliente
     *
     * @throws IOException Error en E/S
     */
    private void desconectar() throws IOException {
        out.close();
        in.close();
        socket.close();
        this.interrupt();
    }

    /**
     * Conprueba los conjuntos de dtaos del cliente
     */
    private void comprobarDataSet() {
        if (comprobarDataSet2()) {
            String contenido = getDataSets();
            if (!"[]".equals(contenido)) {
                out.println(contenido);
            } else {
                out.println("-1");
            }
        } else {
            out.println("-1");
        }
    }

    /**
     * Comprueba si el cliente tiene directorio base
     *
     * @return True o False en función de si tiene directorio o no
     */
    private boolean comprobarDataSet2() {
        String sDirectorio = "./dataSets/" + identificadorUsuario;
        File f = new File(sDirectorio);

        if (f.exists()) {
            return true;
        } else {
            File directorio = new File("./dataSets/" + identificadorUsuario);
            directorio.mkdir();
            return false;
        }
    }

    /**
     * Crea un nuevo conjunto de datos
     *
     * @param nombre Nombre del conjunto
     */
    private void crearDataSet(String nombre) {
        String[] parts = nombre.split("#");
        File fich = new File("./dataSets/" + identificadorUsuario + "/" + parts[0]);
        try {
            fich.createNewFile();
        } catch (IOException ex) {
            Logger.getLogger(hiloCliente.class.getName()).log(Level.SEVERE, null, ex);
        }

        File archivo = new File("./dataSets/" + identificadorUsuario + "/~" + parts[0]);
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(archivo));
            bw.write(parts[2]);
            bw.newLine();
            bw.write(parts[3]);
            bw.newLine();
            bw.write(parts[4]);
            bw.newLine();
            bw.write(parts[5]);
            bw.newLine();
            bw.write(parts[1]);
            bw.close();
        } catch (IOException ex) {
            Logger.getLogger(hiloCliente.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Elimina un dataSet
     *
     * @param nombre Nombre del dataSet a borrar
     */
    private void eliminarDataSet(String nombre) {
        File fichero = new File("./dataSets/" + identificadorUsuario + "/" + nombre);
        if (fichero.delete()) {
            fichero = new File("./dataSets/" + identificadorUsuario + "/~" + nombre);
            fichero.delete();
        }
    }

    /**
     * Comprueba el dataSet cargado
     */
    private void dataSetCargado() {
        if ("".equals(dataSetCargado)) {
            out.println("-1");
        } else {
            out.println(dataSetCargado);
        }
    }

    /**
     * Carga un dataSet
     *
     * @param mensaje Nombre del dataSet a cargar
     */
    private void cargarDataSet(String mensaje) {
        System.out.println(mensaje);
        this.dataSetCargado = mensaje;
        out.println(dataSetCargado);

        if (hiloSeriabilizar.isAlive()) {
            hiloSeriabilizar.interrumpirHilo();
        }
        this.hiloSeriabilizar = new HiloSeriabilizacion(identificadorUsuario, mensaje, configuracion, predictor, this);
        hiloSeriabilizar.start();
    }

    /**
     * Cambia la configuracion del predictor
     *
     * @param configuracion Nueva configuracion
     */
    public void cambiarConfiguracion(ConfiguracionDataSet configuracion) {
        this.configuracion = configuracion;
    }

    /**
     * Cambia el predictor
     *
     * @param predictor Nuevo predictor
     */
    public void cambiarPredictor(Predictor predictor) {
        this.predictor = predictor;
    }

    /**
     * Carga un texto en el conjunto de datos cargado
     *
     * @param mensaje Texto a cargar
     * @throws IOException Error en E/S
     */
    private void cargarTexto(String mensaje) throws IOException {
        predictor.insertarTexto(mensaje);
    }

    /**
     * Realiza una predicción
     *
     * @param completa Indica si la predicción está completa
     * @param mensaje Semilla en base a la que predecir
     */
    private void realizarPrediccion(char completa, String mensaje) {
        if (hiloSeriabilizar.isAlive()) {
            out.println("");
            return;
        }
        System.out.println("Completa:" + completa);
        System.out.println("Mensaje:" + mensaje);
        String pred = this.predictor.realizarPrediccion(completa, mensaje);
        System.out.println("Prediccion realizada:" + pred);
        out.println(pred);
    }

    /**
     * Obtiene los dataSets del cliente
     *
     * @return Lista con los dataSets
     */
    private String getDataSets() {
        String sDirectorio = "./dataSets/" + identificadorUsuario;
        File f = new File(sDirectorio);

        File[] ficheros = f.listFiles();

        String[] fichs = new String[ficheros.length / 2];
        for (int i = 0; i < ficheros.length; i++) {
            if (ficheros[i].getName().charAt(0) != '~') {
                String cadena = "---";
                FileReader ff;
                try {
                    ff = new FileReader("./dataSets/" + identificadorUsuario + "/~" + ficheros[i].getName());
                    BufferedReader bb = new BufferedReader(ff);
                    for (int x = 0; x < 5; x++) {
                        cadena = bb.readLine();
                    }
                    fichs[i] = ficheros[i].getName() + "#" + cadena;
                    bb.close();
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(hiloCliente.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(hiloCliente.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        }
        return Arrays.toString(fichs);
    }

    /**
     * Realiza el login del cliente
     *
     * @param mensaje Datos de inicio de sesión del cliente
     */
    private void login(String mensaje) {
        String[] parts = mensaje.split("#");
        String sql = "SELECT * FROM usuario WHERE Correo='" + parts[0] + "' AND PASS='" + parts[1] + "'";
        try {
            ConexionBBDD con = new ConexionBBDD();
            Connection cn = con.conexion(this.args);
            Statement st = cn.createStatement();
            ResultSet rs = st.executeQuery(sql);

            if (rs.next()) {
                out.println("1");
                this.identificadorUsuario = rs.getString(3);
            } else {
                out.println("-1");
            }
        } catch (SQLException ex) {
            Logger.getLogger(hiloCliente.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * Registra a un nuevo cliente
     *
     * @param mensaje Datos de creación de la cuenta
     */
    private void registro(String mensaje) {
        String[] parts = mensaje.split("#");
        String nombre = parts[0];
        String apellidos = parts[1];
        String correo = parts[2];
        String password = "";
        for (int i = 0; i < 5; i++) {
            password += (int) (Math.random() * 9);
        }

        if (usuarioYaRegistrado(parts[2])) {
            out.println("-1");
            return;
        }

        ConexionBBDD con = new ConexionBBDD();
        Connection cn = con.conexion(this.args);
        try {
            PreparedStatement pps = cn.prepareStatement("INSERT INTO usuario (Nombre,Apellidos,Correo,Pass) VALUES(?,?,?,?)");
            pps.setString(1, nombre);
            pps.setString(2, apellidos);
            pps.setString(3, correo);
            pps.setString(4, DigestUtils.sha1Hex(password));
            pps.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(hiloCliente.class.getName()).log(Level.SEVERE, null, ex);
        }

        out.println("1"); //<---- Contesto diciendo que ha ido bien el registro

        Properties props = System.getProperties();
        props.put("mail.smtp.host", "smtp.gmail.com");  //El servidor SMTP de Google
        props.put("mail.smtp.user", remitente);
        props.put("mail.smtp.clave", passw);    //La clave de la cuenta
        props.put("mail.smtp.auth", "true");    //Usar autenticación mediante usuario y clave
        props.put("mail.smtp.starttls.enable", "true"); //Para conectar de manera segura al servidor SMTP
        props.put("mail.smtp.port", "587"); //El puerto SMTP seguro de Google

        Session session = Session.getDefaultInstance(props);
        MimeMessage message = new MimeMessage(session);
        try {
            message.setFrom(new InternetAddress(remitente));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(correo));   //Se podrían añadir varios de la misma manera
            message.setSubject("Registro en gestor de texto predictivo");
            String texto = "Hola, " + nombre + " " + apellidos + ":\n\nGracias por registrarse, sus datos de inicio de sesión son\n"
                    + "       Correo: " + correo + "\n       Contraseña: " + password;
            message.setText(texto);
            Transport transport = session.getTransport("smtp");
            transport.connect("smtp.gmail.com", remitente, passw);
            transport.sendMessage(message, message.getAllRecipients());
            transport.close();
        } catch (MessagingException me) {
            System.out.println("Error");
        }
    }

    /**
     * Comprueba si un usuario ya está registrado
     *
     * @param mail Identificador único de los usuarios
     * @return Valor true o false en función de si el usuario está registrado o
     * no
     */
    private boolean usuarioYaRegistrado(String mail) {
        String sql = "SELECT * FROM usuario WHERE Correo='" + mail + "'";
        try {
            ConexionBBDD con = new ConexionBBDD();
            Connection cn = con.conexion(this.args);
            Statement st = cn.createStatement();
            ResultSet rs = st.executeQuery(sql);
            return rs.next();
        } catch (SQLException ex) {
            Logger.getLogger(hiloCliente.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
}
