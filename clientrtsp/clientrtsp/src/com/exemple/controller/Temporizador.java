/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.exemple.controller;

import com.exemple.paquete.RTPpacket;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

public class Temporizador {

    public static void controlador(DatagramPacket rcvdp,
            DatagramSocket RTPsocket, ImageIcon icon, JLabel iconLabel) throws IOException {
        RTPsocket.receive(rcvdp);
        // crear un objeto RTPpacket desde el DP
        RTPpacket rtp_packet = new RTPpacket(rcvdp.getData(), rcvdp.getLength());
        // imprima los campos de encabezado importantes del paquete RTP recibido:
        System.out.println("Got RTP packet with SeqNum # " + rtp_packet.getsequencenumber() + " TimeStamp " + rtp_packet.gettimestamp() + " ms, of type " + rtp_packet.getpayloadtype());
        // Imprimir flujo de bits del encabezado:
        rtp_packet.printheader();
        // obtener el flujo de bits de la carga útil del objeto RTPpacket
        int payload_length = rtp_packet.getpayload_length();
        byte[] payload = new byte[payload_length];
        rtp_packet.getpayload(payload);
        // obtener un objeto de imagen del flujo de bits de carga útil
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        // crear la imagen
        Image image = toolkit.createImage(payload, 0, payload_length);
        // mostrar la imagen como un objeto ImageIcon
        icon = new ImageIcon(image);
        iconLabel.setIcon(icon);
    }
}
