package cliente;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Héctor
 */
public class Pruebas {

    public Pruebas() {

    }

    public static void main(String[] args) {
        pruebaFetch();
        pruebaCancion();
        pruebaCancion();
    }

    public static void cadena(String s) {
        System.out.println(s);
    }

    public static void pruebaCancion() {

        ServerSocket ss = null;
        Socket s = null;
        BufferedReader br = null;
        FileInputStream fis = null;
        DataOutputStream dos = null;

        try {
            ss = new ServerSocket(5050);
            s = ss.accept();
            System.out.println("Conectado");
            br = new BufferedReader(new InputStreamReader(s.getInputStream()));
            String cancion = br.readLine().split("PLAY SONG ")[1];
            System.out.println(cancion);
            fis = new FileInputStream(cancion);
            dos = new DataOutputStream(s.getOutputStream());
  
            int leidos;
            byte[] buff = new byte[1024];      
            while ((leidos = fis.read(buff)) != -1) {
                dos.write(buff, 0, leidos);
            }
            dos.flush();
            s.shutdownOutput();

            System.out.println("Enviado");

        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (ss != null) {
                    ss.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            try {
                if (s != null) {
                    s.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            try {
                if (dos != null) {
                    dos.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

    }

    public static void pruebaFetch() {
        ServerSocket ss = null;
        Socket s = null;
        try {
            ss = new ServerSocket(4050);
            System.out.println("Servidor abierto");

                    s = ss.accept();
                    System.out.println("Conectado");
                    BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
                    java.io.OutputStreamWriter osw = new java.io.OutputStreamWriter(s.getOutputStream());

                    String linea = br.readLine();
                    System.out.println(linea);

                    if (linea.equals("FETCH SONGS")) {
                        osw.write("Canals.wav\r\n");
                        osw.write("plane.wav\r\n");
                    } else if (linea.equals("FETCH PODCAST")) {
                        osw.write("Emisora 1");
                    }

                    osw.flush();
                    s.shutdownOutput();
        } catch (IOException ex) {
            Logger.getLogger(Pruebas.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (ss != null) {
                    ss.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            try {
                if (s != null) {
                    ss.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

}
