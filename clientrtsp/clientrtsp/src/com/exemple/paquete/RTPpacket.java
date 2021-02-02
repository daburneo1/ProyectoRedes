package com.exemple.paquete;

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
    // Bitstream of the RTP payload
    public byte[] payload;

    // --------------------------
    //Constructor de un objeto RTPpacket del paquete bistream
    // --------------------------
    public RTPpacket(byte[] packet, int packet_size) {

        Version = 2;
        Padding = 0;
        Extension = 0;
        CC = 0;
        Marker = 0;
        Ssrc = 0;
        // compruebe si el tamaño total del paquete es menor que el tamaño del encabezado
        if (packet_size >= HEADER_SIZE) {
            // obtener el flujo de bits del encabezado:
            header = new byte[HEADER_SIZE];
            for (int i = 0; i < HEADER_SIZE; i++) {
                header[i] = packet[i];
            }
            // obtener el flujo de bits de la carga útil:
            payload_size = packet_size - HEADER_SIZE;
            payload = new byte[payload_size];
            for (int i = HEADER_SIZE; i < packet_size; i++) {
                payload[i - HEADER_SIZE] = packet[i];
            }
            // interpretar los campos cambiantes del encabezado:
            PayloadType = header[1] & 127;
            SequenceNumber = unsigned_int(header[3]) + 256
                    * unsigned_int(header[2]);
            TimeStamp = unsigned_int(header[7]) + 256 * unsigned_int(header[6])
                    + 65536 * unsigned_int(header[5]) + 16777216
                    * unsigned_int(header[4]);
        }
    }

    // --------------------------
    // getpayload: devuelve el bistream de la carga útil del RTPpacket y su tamaño
    // --------------------------
    public int getpayload(byte[] data) {
        for (int i = 0; i < payload_size; i++) {
            data[i] = payload[i];
        }
        return (payload_size);
    }

    // --------------------------
    // getpayload_length: devuelve la longitud de la carga útil
    // --------------------------
    public int getpayload_length() {
        return (payload_size);
    }

    // --------------------------
    // gettimestamp
    // --------------------------
    public int gettimestamp() {
        return (TimeStamp);
    }

    // --------------------------
    // getsequencenumber
    // --------------------------
    public int getsequencenumber() {
        return (SequenceNumber);
    }

    // --------------------------
    // getpayloadtype
    // --------------------------
    public int getpayloadtype() {
        return (PayloadType);
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

    // devuelve el valor sin signo del entero de 8 bits nb
    static int unsigned_int(int nb) {
        if (nb >= 0) {
            return (nb);
        } else {
            return (256 + nb);
        }
    }

}