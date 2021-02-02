/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.exemple.server;

import RSA.RSA;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.StringTokenizer;

public class RTSP_request {

    //------------------------------------
    //Enviar solicitud RTSP
    //------------------------------------
    public void send_RTSP_request(String request_type, BufferedReader RTSPBufferedReader,
            BufferedWriter RTSPBufferedWriter, String VideoFileName) {
        //filtros de flujo de entrada y salida
        int RTSPSeqNb = 0; //Número de secuencia de mensajes RTSP dentro de la sesión
        int RTSPid = 0; //ID de la sesión RTSP (proporcionado por el servidor RTSP)
        int RTP_RCV_PORT = 25000; //puerto donde el cliente recibirá los paquetes RTP

        final String CRLF = "\r\n";
        try {
            int tamPrimo = 100;
            RSA rsa = new RSA(tamPrimo);
            String textoPlano = VideoFileName;
            BigInteger[] textoCifrado = rsa.encripta(textoPlano);
            String encript = null;
            String n = rsa.damen().toString(16).toUpperCase();
            String d = rsa.damed().toString(16).toUpperCase();
            for (int i = 0; i < textoCifrado.length; i++) {
                encript =(textoCifrado[i].toString(16).toUpperCase());
            }
            System.out.println("VideoF "+VideoFileName);
            String url2=encript+"@"+n+"@"+d+"@"+VideoFileName;
            System.out.println("URL2");
            System.out.println(url2);
            RTSPBufferedWriter.write(request_type + " " + url2 + " " + "RTSP/1.0" + CRLF);
            RTSPBufferedWriter.write("CSeq: " + RTSPSeqNb + CRLF);
            if (request_type.equals("SETUP")) {
                RTSPBufferedWriter.write("Transport: RTP/UDP; client_port= " + RTP_RCV_PORT + CRLF);
            } else {
                RTSPBufferedWriter.write("Session: " + RTSPid + CRLF);
            }
            RTSPBufferedWriter.flush();
        } catch (IOException ex) {
            System.out.println("Exception caught: " + ex);
        }
    }

    //------------------------------------
    //Analizar respuesta del servidor
    //------------------------------------
    public int parse_server_response(BufferedReader RTSPBufferedReader, int RTSPid) {
        int reply_code = 0;
        try {
            //analizar la línea de estado y extraer el código de respuesta:
            String StatusLine = RTSPBufferedReader.readLine();
            System.out.println(StatusLine);
            StringTokenizer tokens = new StringTokenizer(StatusLine);
            tokens.nextToken(); //omitir la versión RTSP
            reply_code = Integer.parseInt(tokens.nextToken());
            //si el código de respuesta es correcto, obtenga e imprima las otras 2 líneas
            if (reply_code == 200) {
                String SeqNumLine = RTSPBufferedReader.readLine();
                String SessionLine = RTSPBufferedReader.readLine();
                //si el estado == INIT obtiene el ID de sesión de SessionLine
                tokens = new StringTokenizer(SessionLine);
                tokens.nextToken();
                RTSPid = Integer.parseInt(tokens.nextToken());
            
            }
        } catch (IOException | NumberFormatException ex) {
            System.out.println("Exception caught: " + ex);
            System.exit(0);
        }
        return (reply_code);
    }

    public String url(String VideoFileName, String user, String pass) {
        String url = user + "@" + pass + "@" + VideoFileName;
        System.out.println("URL");
        System.out.println(url);
        return url;
    }

}
