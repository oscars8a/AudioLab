/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cliente;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.FloatControl;

//Clase Reproducción. Gestiona la reproducción actual
public class Reproduccion extends Thread {
    
     /**
      * Atributos:
      *     BUFFER_SIZE: Tamaño del buffer a utilizar para leer del socket
      *     audioLine: La línea de audio utilizada para reproduccir el stream
      *     cancion: el nombre de la canción a pedir al servidor
      *     posición: la posición en la que se encuentra la reproducción
      *     volumen: el volumen actual de la reproducción.
      */
    
    private int BUFFER_SIZE = 1024;
    private SourceDataLine audioLine;
    volatile boolean interrumpe; 
    private String cancion;
    private long posicion;
    private float volumen;
    
    public Reproduccion(String s, long pos, float vol){
        cancion = s;
        posicion = pos;
        volumen = vol;
    }
 
    /**
     * Pide al servidor que le envíe el stream de la canción seleccionada
     * a partir de la posición especificada y con el volumen establecido.
     */
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
            AudioFormat format = ais.getFormat();
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
 
            audioLine = (SourceDataLine) AudioSystem.getLine(info);
            audioLine.open(format);
            cambiarVolumen(this.getVolumen());
            
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
    
    /**
     * interrumpe la reproducción.
     */
    public void cancel(){
        
        this.interrumpe = true;
    }
    
    /**
     * 
     * @return el nombre de la canción que se está reproduciendo.
     */
    public String getCancion(){
        return this.cancion;
    }
    
    /**
     * 
     * @return pausa la reproducción actual, cerrando este hilo.
     */
    public long pause(){
        this.cancel();
        return this.getPosicion();
    }
    
    /**
     * 
     * @return la última posición guardada de la reproducción 
     */
    public long getPosicion(){
        return this.posicion;
    }
    
    /**
     * 
     * @return la línea de reproducción 
     */
    public SourceDataLine getAudioLine(){
        return this.audioLine;
    }
    
    /**
     * regula el volumen de la reproducción
     * @param vol 
     */
    public void cambiarVolumen(float vol){
        FloatControl control = (FloatControl) this.getAudioLine().getControl(FloatControl.Type.MASTER_GAIN);
        if(vol != 0)
            control.setValue(control.getMinimum() + ((control.getMaximum() - control.getMinimum())* vol/100));
        else
            control.setValue(control.getMinimum());
    }
    
    /**
     * 
     * @return el volumen actual 
     */
    public float getVolumen(){
        return this.volumen;
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
