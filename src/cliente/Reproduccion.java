/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cliente;

import com.sun.media.sound.WaveFileReader;
import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 *
 * @author hejimeno
 */
public class Reproduccion extends Thread {
    
    private int BUFFER_SIZE = 1024;
    private Socket server;
    private SourceDataLine audioLine;
    volatile boolean interrumpe; 
    private String cancion;
    private long posicion;
    
    public Reproduccion(String s, long pos){
        cancion = s;
        posicion = pos;
    }
    
//    public Reproduccion(Socket s, InputStream i, SourceDataLine line, AudioFormat formato, AudioInputStream audio){
//        this.server =s;
//        this.is = i;
//        audioLine = line;
//        format = formato;
//        ais = audio;
//        interrumpe = false;
//    }
    
    public void run(){
            
            Socket server = null;
            InputStream is = null;
            OutputStreamWriter osw = null;      
            AudioInputStream ais = null;
            
        try{ 
            server = new Socket("localhost", 5050);
            osw = new OutputStreamWriter(server.getOutputStream());
            is = new BufferedInputStream(server.getInputStream());
            osw.write("PLAY SONG "+cancion + System.getProperty("line.separator"));
            osw.write("FROM " + posicion + System.getProperty("line.separator"));
            osw.flush();
            
            ais = AudioSystem.getAudioInputStream(is);	
            //ais.getAudioInputStream(is);
            AudioFormat format = ais.getFormat();
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
 
            audioLine = (SourceDataLine) AudioSystem.getLine(info);
 
            audioLine.open(format);
            
            audioLine.start();
            System.out.println("Playback started.");
             
            byte[] bytesBuffer = new byte[BUFFER_SIZE];
            int bytesRead = -1;
            while (!(interrumpe) && ((bytesRead = ais.read(bytesBuffer)) != -1)) {
                    audioLine.write(bytesBuffer, 0, bytesRead);
                    posicion = posicion + bytesRead;
            }       
        } catch (LineUnavailableException | IOException | UnsupportedAudioFileException ex) {
            ex.printStackTrace();
        }
        finally{
            audioLine.drain();
            audioLine.stop();
            audioLine.close();
            cerrar(ais);
            cerrar(osw);
            cerrar(is);
            cerrar(server);
            }
    }  
    
    public void cancel(){
        
        this.interrumpe = true;
    }
    
    public String getCancion(){
        return this.cancion;
    }
    
    public long pause(){
        this.cancel();
        //posicion = totalLeido;
        return this.getPosicion();
    }
    
    public long getPosicion(){
        return this.posicion;
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
