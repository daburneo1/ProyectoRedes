/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package RSA;

import javafx.util.converter.BigIntegerStringConverter;

import java.math.BigInteger;

public class RSA {

    BigInteger e, d;
    BigInteger n, q, p;

    public String desencripta(BigInteger[] encriptado, String n, String d, String acceso) {
        try {
            BigInteger[] desencriptado = new BigInteger[encriptado.length];
            for (int i = 0; i < desencriptado.length; i++) {
                System.out.println(desencriptado[i]);
                desencriptado[i] = encriptado[i];
            }
            char[] charArray = new char[desencriptado.length];
            for (int i = 0; i < charArray.length; i++) {
                charArray[i] = (char) (desencriptado[i].intValue());
            }
            acceso = new String(charArray);
            return (new String(charArray));
        } catch (Exception e) {
        }
        return acceso;

    }

/*
    public String desencripta(BigInteger[] encriptado, String n, String d) {
        BigInteger Bn = new BigInteger(n);
        BigInteger Bd = new BigInteger(d);
        BigInteger[] desencriptado = new BigInteger[encriptado.length];
        for (int i = 0; i < desencriptado.length; i++) {
            System.out.println(desencriptado[i].modPow(Bd,Bn));
            desencriptado[i] = encriptado[i];
        }
        char[] charArray = new char[desencriptado.length];
        for (int i = 0; i < charArray.length; i++) {
            charArray[i] = (char) (desencriptado[i].intValue());
        }
        String acceso = new String(charArray);
        System.out.println("Acceso; " + acceso);
        return acceso;
    }*/
}
