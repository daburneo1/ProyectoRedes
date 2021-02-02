/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package RSA;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Random;


public class RSA {

    int tamPrimo;
    BigInteger n, q, p;
    BigInteger totient;
    BigInteger e, d;

    public RSA(int tamPrimo) {
        this.tamPrimo = tamPrimo;

        generaPrimos();             //Genera p y q
        generaClaves();             //Genera e y d    
    }

    public void generaPrimos() {
        p = new BigInteger(tamPrimo, 10, new Random());
        do {
            q = new BigInteger(tamPrimo, 10, new Random());
        } while (q.compareTo(p) == 0);
        System.out.println("p=" +p);
        System.out.println("q=" +q);
    }

    public void generaClaves() {
        n = p.multiply(q);
        totient = p.subtract(BigInteger.valueOf(1));
        totient = totient.multiply(q.subtract(BigInteger.valueOf(1)));
        do {
            e = new BigInteger(2 * tamPrimo, new Random());
        } while ((e.compareTo(totient) != -1) || (e.gcd(totient).compareTo(BigInteger.valueOf(1)) != 0));
        d = e.modInverse(totient);
        
        System.out.println("Llave pública = ("+n+","+e+")");
        System.out.println("Llave privada = ("+n+","+d+")");
    }

    public BigInteger[] encripta(String mensaje) {
        int i;
        byte[] temp = new byte[1];
        byte[] digitos = mensaje.getBytes();
        BigInteger[] bigdigitos = new BigInteger[digitos.length];
        for (i = 0; i < bigdigitos.length; i++) {
            temp[0] = digitos[i];
            bigdigitos[i] = new BigInteger(temp);
        }
        BigInteger[] encriptado = new BigInteger[bigdigitos.length];
        for (i = 0; i < bigdigitos.length; i++) {
            encriptado[i] = bigdigitos[i].modPow(e, n);
        }
        
        return (encriptado);
    }

    public BigInteger damep() {
        return (p);
    }

    public BigInteger dameq() {
        return (q);
    }

    public BigInteger dametotient() {
        return (totient);
    }

    public BigInteger damen() {
        return (n);
    }

    public BigInteger damee() {
        return (e);
    }

    public BigInteger damed() {
        return (d);
    }
}
