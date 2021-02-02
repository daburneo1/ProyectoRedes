package com.exemple.rtspserver;

import RSA.RSA;
import com.example.rtsp.RTPpacket;
import com.example.stream.VideoStream;
import java.io.*;
import java.net.*;
import java.awt.*;
import java.util.*;
import java.awt.event.*;
import java.math.BigInteger;
import javax.swing.*;
import javax.swing.Timer;

public class Server extends JFrame implements ActionListener {

    //Variables RTP:
    //----------------
    DatagramSocket RTPsocket; //socket que se utilizará para enviar y recibir paquetes UDP
    DatagramPacket senddp; //Paquete UDP que contiene los fotogramas de video
    InetAddress ClientIPAddr; //Dirección IP del cliente
    int RTP_dest_port = 0; //puerto de destino para paquetes RTP (proporcionado por el cliente RTSP)
    int RTSPSeqNb = 0;
    static BufferedWriter RTSPBufferedWriter;
    static int RTSP_ID = 123456; //ID de la sesión RTSP
    final static String CRLF = "\r\n";
    //GUI:
    //----------------
    JLabel label;
    //Video variables:
    //----------------
    int imagenb = 0; //imagen nb de la imagen transmitida actualmente
    VideoStream video; //Objeto VideoStream utilizado para acceder a cuadros de video
    static int MJPEG_TYPE = 26; //Tipo de carga útil RTP para video MJPEG
    static int FRAME_PERIOD = 100; //Período de fotogramas del video para transmitir, en ms
    static int VIDEO_LENGTH = 500; //duración del video en cuadros
    //obtener el puerto de socket RTSP desde la línea de comando
    static int RTSPport = 8554;
    Timer timer; //temporizador utilizado para enviar las imágenes a la velocidad de fotogramas del video
    byte[] buf; //búfer utilizado para almacenar las imágenes para enviar al cliente
    //RTSP variables
    //----------------
    //rtsp states
    final static int INIT = 0;
    final static int READY = 1;
    final static int PLAYING = 2;
    //tipos de mensajes rtsp
    final static int SETUP = 3;
    final static int PLAY = 4;
    final static int PAUSE = 5;
    final static int TEARDOWN = 6;
    static int state; //Estado del servidor RTSP == INIT o READY o PLAY
    Socket RTSPsocket; //conector utilizado para enviar / recibir mensajes RTSP
    //filtros de flujo de entrada y salida
    static BufferedReader RTSPBufferedReader;
    static String VideoFileName; //archivo de video solicitado al cliente

    //--------------------------------
    //Constructor
    //--------------------------------
    public Server() throws UnknownHostException, SocketException {
        //init Frame
        super("Server");
        //init Timer
        timer = new Timer(FRAME_PERIOD, this);
        timer.setInitialDelay(0);
        timer.setCoalesce(true);
        //asignar memoria para el búfer de envío
        buf = new byte[15000];
        //Controlador para cerrar la ventana principal
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                //detener el temporizador y salir
                timer.stop();
                System.exit(0);
            }
        });
        //GUI:
        InetAddress ip;
        //obtener ip de la maquina
        ip = InetAddress.getLocalHost();
        //presentar en pantalla
        label = new JLabel("direccion de servidor:        "
                + "\n" + String.valueOf(getip() + ":" + String.valueOf(RTSPport)), JLabel.CENTER);

        getContentPane().add(label, BorderLayout.CENTER);
    }

    private String getip() throws UnknownHostException, SocketException {
        return (String) Collections.list(NetworkInterface.getNetworkInterfaces()).stream().flatMap(i -> Collections.list(i.getInetAddresses()).stream()).filter(ip -> ip instanceof Inet4Address && ip.isSiteLocalAddress()).findFirst().orElseThrow(RuntimeException::new).getHostAddress();
    }

    public static void main(String argv[]) throws Exception {
        Server theServer = new Server();
        //ver GUI:
        theServer.pack();
        theServer.setVisible(true);
        //Iniciar la conexión TCP con el cliente para la sesión RTSP
        ServerSocket listenSocket = new ServerSocket(RTSPport);
        theServer.RTSPsocket = listenSocket.accept();
        listenSocket.close();
        //Obtener la dirección IP del cliente
        theServer.ClientIPAddr = theServer.RTSPsocket.getInetAddress();
        //Iniciar RTSPstate
        state = INIT;
        //Establecer filtros de flujo de entrada y salida:
        RTSPBufferedReader = new BufferedReader(new InputStreamReader(theServer.RTSPsocket.getInputStream()));
        RTSPBufferedWriter = new BufferedWriter(new OutputStreamWriter(theServer.RTSPsocket.getOutputStream()));
        //Espere el mensaje SETUP del cliente
        int request_type;
        boolean done = false;
        while (!done) {
            request_type = theServer.parse_RTSP_request();
            if (request_type == SETUP) {
                // VideoFileName -> patron encriptado de la url del video junto con usery passw
                ArrayList file = theServer.login(VideoFileName);
                if (file.get(0).equals("user1") & file.get(1).equals("user1")) {
                    done = true;
                    //actualizar el estado de RTSP
                    state = READY;
                    System.out.println("New RTSP state: READY");
                    //Enviar respuesta
                    theServer.send_RTSP_response();
                    //objeto VideoStream:
                    System.out.println(VideoFileName);
                    theServer.video = new VideoStream((String) file.get(2));
                    //socket RTP
                    theServer.RTPsocket = new DatagramSocket();
                }else{
                    System.out.println("ERROR DE CREDENCIALES");
                    theServer.send_RTSPERROR_response();
                }

            }
        }

        //bucle para manejar solicitudes RTSP
        while (true) {
            //analizar la solicitud
            request_type = theServer.parse_RTSP_request();
            System.out.println(state);
            if ((request_type == PLAY) && (state == READY)) {
                //enviar respuesta
                theServer.send_RTSP_response();
                //horas de inicio
                theServer.timer.start();
                //estado de actualización
                state = PLAYING;
                System.out.println("New RTSP state: PLAYING");
            } else if ((request_type == PAUSE) && (state == PLAYING)) {
                //enviar respuesta
                theServer.send_RTSP_response();
                //detener el temporizador
                theServer.timer.stop();
                //estado de actualización
                state = READY;
                System.out.println("New RTSP state: READY");
            } else if (request_type == TEARDOWN) {
                //enviar respuesta
                theServer.send_RTSP_response();
                //detener el temporizador
                theServer.timer.stop();
                //cerrar sockets
                theServer.RTSPsocket.close();
                theServer.RTPsocket.close();
                System.exit(0);
            }
        }
    }

    private void send_RTSPERROR_response() {
        try {
            RTSPBufferedWriter.write("RTSP/1.0 400 ERROR" + CRLF);
            RTSPBufferedWriter.write("CSeq: " + RTSPSeqNb + CRLF);
            RTSPBufferedWriter.write("Session: " + RTSP_ID + CRLF);
            RTSPBufferedWriter.flush();
        } catch (Exception ex) {
            System.out.println("Exception caught: " + ex);
            System.exit(0);
        }
    }

    //------------------------
    //Operaciones por tiempo
    //------------------------
    public void actionPerformed(ActionEvent e) {
        //si la imagen actual nb es menor que la duración del video
        if (imagenb < VIDEO_LENGTH) {
            //actualizar imagenb actual
            imagenb++;
            try {
                //obtener el siguiente fotograma para enviar desde el video, así como su tamaño
                int image_length = video.getnextframe(buf);
                //Crea un objeto RTPpacket que contiene el marco
                RTPpacket rtp_packet = new RTPpacket(MJPEG_TYPE, imagenb, imagenb * FRAME_PERIOD, buf, image_length);
                //llegar a la longitud total del paquete rtp completo para enviar
                int packet_length = rtp_packet.getlength();
                //recuperar el flujo de bits del paquete y almacenarlo en una matriz de bytes
                byte[] packet_bits = new byte[packet_length];
                rtp_packet.getpacket(packet_bits);
                //enviar el paquete como un DatagramPacket a través del socket UDP
                senddp = new DatagramPacket(packet_bits, packet_length, ClientIPAddr, RTP_dest_port);
                System.out.print("RTP_dest_port =" + RTP_dest_port + "\n");
                RTPsocket.send(senddp);
                //imprimir el flujo de bits del encabezado
                System.out.println("Send frame #" + imagenb);
                rtp_packet.printheader();
                //update GUI
                label.setText("Send frame #" + imagenb);
            } catch (Exception ex) {
                System.out.println("Exception caught: " + ex);
                System.exit(0);
            }
        } else {
            //si hemos llegado al final del archivo de video, detenga el temporizador
            timer.stop();
        }
    }

    //------------------------------------
    //Analizar solicitud RTSP
    //------------------------------------
    private int parse_RTSP_request() {
        int request_type = -1;
        try {
            //analizar la línea de solicitud y extraer request_type:
            String RequestLine = RTSPBufferedReader.readLine();
            System.out.println(RequestLine);
            StringTokenizer tokens = new StringTokenizer(RequestLine);
            String request_type_string = tokens.nextToken();
            //convertir a la estructura request_type:
            if ((request_type_string).compareTo("SETUP") == 0) {
                request_type = SETUP;
                //extraer VideoFileName de RequestLine
                VideoFileName = tokens.nextToken();
            } else if ((request_type_string).compareTo("PLAY") == 0) {
                request_type = PLAY;
            } else if ((request_type_string).compareTo("PAUSE") == 0) {
                request_type = PAUSE;
            } else if ((request_type_string).compareTo("TEARDOWN") == 0) {
                request_type = TEARDOWN;
            }
            //analizar el SeqNumLine y extraer el campo CSeq
            String SeqNumLine = RTSPBufferedReader.readLine();
            System.out.println(SeqNumLine);
            tokens = new StringTokenizer(SeqNumLine);
            tokens.nextToken();
            RTSPSeqNb = Integer.parseInt(tokens.nextToken());
            //obtener LastLine
            String LastLine = RTSPBufferedReader.readLine();
            if (request_type == SETUP) {
                //extraer RTP_dest_port de LastLine
                tokens = new StringTokenizer(LastLine);
                for (int i = 0; i < 3; i++) {
                    tokens.nextToken(); //omitir las cosas no utilizadas
                }
                RTP_dest_port = Integer.parseInt(tokens.nextToken());
            }
        } catch (Exception ex) {
            System.out.println("Exception caught: " + ex);
            System.exit(0);
        }
        return (request_type);
    }

    //------------------------------------
    //Enviar respuesta RTSP
    //------------------------------------
    public void send_RTSP_response() {
        try {
            RTSPBufferedWriter.write("RTSP/1.0 200 OK" + CRLF);
            RTSPBufferedWriter.write("CSeq: " + RTSPSeqNb + CRLF);
            RTSPBufferedWriter.write("Session: " + RTSP_ID + CRLF);
            RTSPBufferedWriter.flush();
        } catch (Exception ex) {
            System.out.println("Exception caught: " + ex);
            System.exit(0);
        }
    }

    public ArrayList login(String url2) {
        String[] valor = url2.split("@");
        // claves de acceso RSA
        String n = valor[1];
        String d = valor[2];
        // direccion de archivo en servidor
        String url = valor[0];
        // user pasw
        String acceso = valor[3];
        // RSSA
        byte[] temp = new byte[1];
        int i;
        // TENER CUIDADO TAMAÑO CLIENTE
        int tamPrimo = 100;
        RSA rsa = new RSA();
        // DIRECCION DEL VIDEO TRANSFORMAR EN bytes
        byte[] digitos = url.getBytes();
        // rsa trabajo con biginteger
        BigInteger[] bigdigitos = new BigInteger[digitos.length];
        // recorrer para obtener cadena que va a desencriptar
        for (i = 0; i < bigdigitos.length; i++) {
            temp[0] = digitos[i];
            bigdigitos[i] = new BigInteger(temp);
        }
        // obtienes dato de tipo biginteger encriptado
        BigInteger[] encriptado = new BigInteger[bigdigitos.length];
        // metodo para recibir archivo desencriptado
        String recuperarTextoPlano = rsa.desencripta(encriptado,n,d,acceso);
        //String recuperarTextoPlano = rsa.desencripta(encriptado,n,d);
        System.out.println("TP: "+recuperarTextoPlano);
        String[] valor2 = recuperarTextoPlano.split("@");
        ArrayList al = new ArrayList();
        for (int j = 3; j < valor.length; j++) {
            String string = valor[j];
            al.add(valor[j]);
        }
        return al;
    }

}
