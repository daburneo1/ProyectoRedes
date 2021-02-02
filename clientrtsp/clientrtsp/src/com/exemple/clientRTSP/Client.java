package com.exemple.clientRTSP;

import com.exemple.controller.ButtonPeticion;
import com.exemple.controller.Temporizador;
import com.exemple.server.RTSP_request;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStreamWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.Hashtable;
import javax.swing.*;

public class Client {

    // servidor
    RTSP_request rtsp = new RTSP_request();
    // perticione al servidor
    ButtonPeticion bp = new ButtonPeticion();
    // varianles que se cambian
    static String VideoFileName = "media/movie.mjpeg";//archivo de video para solicitar al servidor
    static String ServerHost = "1.1.1.1"; //argv[0];
    static String usuario= "";
    static String pass="";
    // temporizador
    Temporizador t = new Temporizador();

    
    //  sentecnias para crear la interfaz
    static JFrame f = new JFrame("Client");
    // creación de botones
    JButton setupButton = new JButton("Setup");
    JButton playButton = new JButton("Play");
    JButton pauseButton = new JButton("Pause");
    JButton tearButton = new JButton("Teardown");
    // creacion de contenedor de componentes
    JPanel mainPanel = new JPanel();
    JPanel buttonPanel = new JPanel();
    // visiulizador de texto
    JLabel iconLabel = new JLabel();
    // implementar interface de imagen
    ImageIcon icon;
//RTP variables:
//----------------
    DatagramPacket rcvdp; //Paquete UDP recibido del servidor
    DatagramSocket RTPsocket; //socket que se utilizará para enviar y recibir paquetes UDP
    static int RTP_RCV_PORT = 25000; //puerto donde el cliente recibirá los paquetes RTP
    Timer timer; //temporizador utilizado para recibir datos del socket UDP
    byte[] buf; //búfer utilizado para almacenar datos recibidos del servidor
//RTSP variables
//----------------
//estados rtsp
    final static int INIT = 0;
    final static int READY = 1;
    final static int PLAYING = 2;
    static int state; //Estado RTSP == INIT o READY o PLAYING
    Socket RTSPsocket; //conector utilizado para enviar / recibir mensajes RTSP
//filtros de flujo de entrada y salida
    static BufferedReader RTSPBufferedReader;
    static BufferedWriter RTSPBufferedWriter;

    int RTSPSeqNb = 0; //Número de secuencia de mensajes RTSP dentro de la sesión
    int RTSPid = 0; //ID de la sesión RTSP (proporcionado por el servidor RTSP)

//--------------------------
//Constructor
//--------------------------

    public Client() {
        //construir GUI
        //--------------------------
        //Frame
        f.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        //botones
        buttonPanel.setLayout(new GridLayout(1, 0));
        buttonPanel.add(setupButton);
        buttonPanel.add(playButton);
        buttonPanel.add(pauseButton);
        buttonPanel.add(tearButton);
        setupButton.addActionListener(new setupButtonListener());
        playButton.addActionListener(new playButtonListener());
        pauseButton.addActionListener(new pauseButtonListener());
        tearButton.addActionListener(new tearButtonListener());
        //Etiqueta de visualización de imágenes
        iconLabel.setIcon(null);
        //diseño del marco
        mainPanel.setLayout(null);
        mainPanel.add(iconLabel);
        mainPanel.add(buttonPanel);
        iconLabel.setBounds(0, 0, 380, 280);
        buttonPanel.setBounds(0, 280, 380, 50);
        f.getContentPane().add(mainPanel, BorderLayout.CENTER);
        f.setSize(new Dimension(390, 370));
        f.setVisible(true);
        //init timer
        //--------------------------
        timer = new Timer(20, new timerListener());
        timer.setInitialDelay(0);
        timer.setCoalesce(true);
        //Asignar memoria para el búfer utilizado para recibir datos del servidor.
        buf = new byte[15000];
    }
//------------------------------------
//main
//------------------------------------

    public static void main(String argv[]) throws Exception {
        ServerHost = JOptionPane.showInputDialog("Ingrese la direccion ip del Servidor");
        login();
        if(ServerHost != null){
            //Crear un objeto de cliente
            Client theClient = new Client();
            //Establecer una conexión TCP con el servidor para intercambiar mensajes RTSP
            theClient.RTSPsocket = new Socket(ServerHost, 8554);
            //Establecer filtros de flujo de entrada y salida:
            RTSPBufferedReader = new BufferedReader(new InputStreamReader(theClient.RTSPsocket.getInputStream()));
            RTSPBufferedWriter = new BufferedWriter(new OutputStreamWriter(theClient.RTSPsocket.getOutputStream()));
            state = INIT;
        }
    }

    class setupButtonListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (state == INIT) {
                //Inicie RTPsocket sin bloqueo que se utilizará para recibir datos
                try {
                    //construir un nuevo DatagramSocket para recibir paquetes RTP desde el servidor, en el puerto RTP_RCV_PORT
                    RTPsocket = new DatagramSocket(RTP_RCV_PORT);
                    //establezca el valor de TimeOut del socket en 5 ms.
                    RTPsocket.setSoTimeout(5);
                } catch (SocketException se) {
                    System.out.println("Socket exception: " + se);
                    System.exit(0);
                }
                //init Número de secuencia de RTSP
                RTSPSeqNb = 1;
                //Enviar mensaje SETUP al servidor
                rtsp.send_RTSP_request("SETUP", RTSPBufferedReader, RTSPBufferedWriter,rtsp.url(VideoFileName, usuario, pass));
                //Espera la respuesta
                if (rtsp.parse_server_response(RTSPBufferedReader, RTSPid) == 200) {
                    //cambiar el estado de RTSP e imprimir un nuevo estado 
                    state = READY;
                    System.out.println("New RTSP state: READY");
                } else {                   
                    System.out.println("Invalid Server Response");
                    JOptionPane.showMessageDialog(f, "Credenciales incorrectas");
                    System.exit(0);
                }
            }
        }
    }

//Controlador para el botón Reproducir
//-----------------------
    class playButtonListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            System.out.println("Play Button pressed !");
            if (state == READY) {
                //aumentar el número de secuencia de RTSP
                RTSPSeqNb++;
                //Enviar mensaje PLAY al servidor
                rtsp.send_RTSP_request("PLAY",RTSPBufferedReader, RTSPBufferedWriter, rtsp.url(VideoFileName, usuario, pass));
                // verificar conexion
                Boolean conexion = bp.verificarConexion(rtsp, RTSPBufferedReader, RTSPid);
                if (conexion = true) {
                    //   cambiar el estado de RTSP e imprimir un nuevo estado
                    state = PLAYING;
                    // Espera la respuesta
                    bp.play(conexion, timer);
                    // 0cultar boton
                    playButton.setVisible(false);
                }
            }
        }
    }

//Controlador para el botón de pausa
//-----------------------
    class pauseButtonListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (state == PLAYING) {
                //Enviar mensaje PAUSA al servidor
                rtsp.send_RTSP_request("PAUSE",RTSPBufferedReader, RTSPBufferedWriter, rtsp.url(VideoFileName, usuario, pass));
                // Espera la respuesta
                Boolean conexion = bp.verificarConexion(rtsp, RTSPBufferedReader, RTSPid);
                if (conexion = true) {
                    // Cambiar el estado de RTSP e imprimir un nuevo estado
                    state = PLAYING;
                    // Espera la respuesta
                    bp.pause(conexion, timer);
                    // Ocultar boton
                    playButton.setVisible(false);
                }
                pauseButton.setVisible(false);
            }
        }
    }

//Controlador para botón de desmontaje
//-----------------------
    class tearButtonListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            //Enviar mensaje TEARDOWN al servidor
            rtsp.send_RTSP_request("TEARDOWN",RTSPBufferedReader, RTSPBufferedWriter, rtsp.url(VideoFileName, usuario, pass));
            // Espera la respuesta
            // verificar conexion
            Boolean conexion = bp.verificarConexion(rtsp, RTSPBufferedReader, RTSPid);
            if (conexion = true) {
                // Espera la respuesta
                bp.teardown(conexion, timer);
            }
        }
    }

//------------------------------------
//escuchar el temporizador
//------------------------------------
    class timerListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            // Construya un DatagramPacket para recibir datos del socket UDP
            rcvdp = new DatagramPacket(buf, buf.length);
            try {
                Temporizador.controlador(rcvdp, RTPsocket, icon, iconLabel);
            } catch (InterruptedIOException iioe) {
            } catch (IOException ioe) {
                System.out.println("Exception caught: " + ioe);
            }
        }
    }
    
    public static void login() {

    JPanel panel = new JPanel(new BorderLayout(5, 5));

    JPanel label = new JPanel(new GridLayout(0, 1, 2, 2));
    label.add(new JLabel("Usuario", SwingConstants.RIGHT));
    label.add(new JLabel("Contraseña", SwingConstants.RIGHT));
    panel.add(label, BorderLayout.WEST);

    JPanel controls = new JPanel(new GridLayout(0, 1, 2, 2));
    JTextField username = new JTextField();
    controls.add(username);
    JPasswordField password = new JPasswordField();
    controls.add(password);
    panel.add(controls, BorderLayout.CENTER);

    JOptionPane.showMessageDialog(f, panel, "login", JOptionPane.QUESTION_MESSAGE);

    usuario = (username.getText());
    pass = (new String(password.getPassword()));
}
}
