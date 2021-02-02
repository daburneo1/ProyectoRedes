package com.example.stream;

import java.io.FileInputStream;

public class VideoStream {

    FileInputStream fis; //se utiliza para leer archivos
    int frame_nb; //cuadro actual nb
    //-----------------------------------
    //constructor
    //-----------------------------------

    public VideoStream(String filename) throws Exception {
        //direccion del archivo
        fis = new FileInputStream(filename);
        frame_nb = 0;
    }
    //-----------------------------------
    // getnextframe
    //devuelve el siguiente marco como una matriz de bytes y el tamaño del marco
    //-----------------------------------

    public int getnextframe(byte[] frame) throws Exception {
        int length = 0;
        String length_string;
        byte[] frame_length = new byte[5];
        //leer la longitud del cuadro actual
        fis.read(frame_length, 0, 5);
        //transformar el tamaño del frame en entero
        length_string = new String(frame_length);
        length = Integer.parseInt(length_string);
        // lectura del archivo y enviando al frame
        return (fis.read(frame, 0, length));
    }
}
