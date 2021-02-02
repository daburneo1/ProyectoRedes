/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.exemple.controller;

import com.exemple.server.RTSP_request;
import java.io.BufferedReader;
import javax.swing.Timer;

public class ButtonPeticion {

    public boolean verificarConexion(RTSP_request rtsp,
            BufferedReader RTSPBufferedReader, int RTSPid) {
        if (rtsp.parse_server_response(RTSPBufferedReader, RTSPid) != 200) {
            System.out.println("Invalid Server Response");
            // si existe fallo en la conexion retorna false
            return false;
        } else {
            // si la solicitud es exitosa retorna true
            return true;
        }

    }

    public void teardown(Boolean conexion, Timer timer) {
        // conexion es exitossa
        if (conexion = true) {
            //detener el temporizador
            timer.stop();
            //salir
            System.exit(0);
        }
    }

    public void play(Boolean conexion, Timer timer) {
        // conexion es exitossa
        if (conexion = true) {
            System.out.println("New RTSP state: PLAYING");
            //  iniciar el temporizador
            timer.start();
        }
    }

    public void pause(Boolean conexion, Timer timer) {
        // conexion es exitossa
        if (conexion = true) {
            //parar el temporizador
            timer.stop();
        }
    }
    
}
