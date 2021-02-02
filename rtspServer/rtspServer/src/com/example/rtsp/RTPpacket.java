package com.example.rtsp;

public class RTPpacket {
    // tamaño del encabezado RTP:
    static int HEADER_SIZE = 12;
    // Campos que componen el encabezado RTP
    public int Version;
    public int Padding;
    public int Extension;
    public int CC;
    public int Marker;
    public int PayloadType;
    public int SequenceNumber;
    public int TimeStamp;
    public int Ssrc;
    // Bitstream del encabezado RTP
    public byte[] header;
    // tamaño de la carga útil RTP
    public int payload_size;
    // Bitstream de la carga RTP
    public byte[] payload;
    /* --------------------------
    Constructor de un objeto RTPpacket a partir de campos de encabezado y 
    flujo de bits de carga útil
    --------------------------*/
    public RTPpacket(int PType, int Framenb, int Time, byte[] data,
            int data_length) {
        // rellenar por defecto los campos de encabezado:
        Version = 2;
        Padding = 0;
        Extension = 0;
        CC = 0;
        Marker = 0;
        Ssrc = 0;
        // rellenar campos de encabezado cambiantes:
        SequenceNumber = Framenb;
        TimeStamp = Time;
        PayloadType = PType;
        // construir el encabezado bistream:
        // --------------------------
        header = new byte[HEADER_SIZE];
        header[1] = (byte) ((Marker << 7) | PayloadType);
        header[2] = (byte) (SequenceNumber >> 8);
        header[3] = (byte) (SequenceNumber);
        for (int i = 0; i < 4; i++) {
            header[7 - i] = (byte) (TimeStamp >> (8 * i));
        }
        for (int i = 0; i < 4; i++) {
            header[11 - i] = (byte) (Ssrc >> (8 * i));
        }
        payload_size = data_length;
        payload = new byte[data_length];
        payload = data;
    }

    // --------------------------
    // getlength: devuelve la longitud total del paquete RTP
    // --------------------------
    public int getlength() {
        return (payload_size + HEADER_SIZE);
    }

    // --------------------------
    // getpacket: devuelve el flujo de bits del paquete y su longitud
    // --------------------------
    public int getpacket(byte[] packet) {
        // construir el paquete = encabezado + carga útil
        for (int i = 0; i < HEADER_SIZE; i++) {
            packet[i] = header[i];
        }
        for (int i = 0; i < payload_size; i++) {
            packet[i + HEADER_SIZE] = payload[i];
        }
        // devolver el tamaño total del paquete
        return (payload_size + HEADER_SIZE);
    }

    // --------------------------
    // imprimir encabezados sin el SSRC
    // --------------------------
    public void printheader() {
        for (int i = 0; i < (HEADER_SIZE - 4); i++) {
            for (int j = 7; j >= 0; j--) {
                if (((1 << j) & header[i]) != 0) {
                    System.out.print("1");
                } else {
                    System.out.print("0");
                }
            }
            System.out.print(" ");
        }
        System.out.println();
    }

}
