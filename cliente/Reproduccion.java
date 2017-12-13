/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cliente;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

/**
 *
 * @author hejimeno
 */
public class Reproduccion extends Thread {
    
    private int BUFFER_SIZE = 4096;

    private Socket server;
    private InputStream is;
    private SourceDataLine audioLine;
    private AudioFormat format;
    private AudioInputStream ais;
    volatile boolean interrumpe; 
    
    public Reproduccion(Socket s, InputStream i, SourceDataLine line, AudioFormat formato, AudioInputStream audio){
        this.server =s;
        this.is = i;
        audioLine = line;
        format = formato;
        ais = audio;
        interrumpe = false;
    }
    
    public void run(){
        try {
            audioLine.open(format);
            
            audioLine.start();
             
            System.out.println("Playback started.");
             
            byte[] bytesBuffer = new byte[BUFFER_SIZE];
            int bytesRead = -1;
 
            while (!(interrumpe) && ((bytesRead = ais.read(bytesBuffer)) != -1)) {
                audioLine.write(bytesBuffer, 0, bytesRead);
            }
        } catch (LineUnavailableException | IOException ex) {
            ex.printStackTrace();
        }
        finally{
            audioLine.drain();
            audioLine.stop();
            audioLine.close();
            cerrar(ais);
            cerrar(is);
            cerrar(server);
            }
    }  
    
    public void cancel(){
        this.interrumpe = true;
    }
    
    
    private void cerrar(Closeable o){
        try{
            if(o!=null){
                o.close();
            }
        }
        catch(IOException ex){
            ex.printStackTrace();
        }
    }
    
}
