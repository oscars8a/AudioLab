/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cliente;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 *
 * @author hejimeno
 */
public class Reproductor {
    
    private Clip media;
    private long posicion;
    
    public Reproductor(){
        this.media = null;
        this.posicion=0;
    }
    

    protected void play(String s) throws IOException{
        
        if(media != null && media.isRunning()){
            media.close();
        }
        
        try (Socket server = new Socket("localhost", 5050);
                OutputStreamWriter osw = new OutputStreamWriter(server.getOutputStream());
                InputStream is = new BufferedInputStream(server.getInputStream())){
            
            osw.write("PLAY SONG "+s+System.getProperty("line.separator"));
            osw.flush();
            
            AudioInputStream ais = AudioSystem.getAudioInputStream(is);
            media = AudioSystem.getClip();
            media.open(ais);
            media.start();
            
        } catch (UnsupportedAudioFileException | LineUnavailableException ex) {
            Logger.getLogger(Reproductor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    //Si el clip está reproduciéndose, se pausa y se guarda la última posición reproducida. 
    //En caso contrario, se reanuda, reproduciendo a partir del momento exacto en que se quedó.
    protected void resumePause(){
        if(media.isRunning()) {
        	media.stop();
        	posicion = media.getMicrosecondPosition();
        }
        else {
        	media.setMicrosecondPosition(posicion);
        	media.start();
        }
    }
    
    //Recoge la lista de todas las canciones o emisoras del servidor.
    protected ArrayList<String> getLista(String option){
        
        ArrayList<String> lista = new ArrayList<>();
        
        try(Socket s = new Socket("localhost", 4050);
                OutputStreamWriter osw = new OutputStreamWriter(s.getOutputStream());
                    BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()))){
            
            String envio = "FETCH SONGS\r\n";
            String linea;
            if(option.equals("Canciones"))
                envio = "FETCH SONGS\r\n";
            else if(option.equals("Emisoras"))
                envio = "FETCH PODCAST\r\n";
            
            if(!envio.isEmpty()){
                osw.write(envio);
                osw.flush();
                s.shutdownOutput();
            
                while((linea = br.readLine())!= null)
                    lista.add(linea);
            }
        } catch (IOException ex) {
            Logger.getLogger(Reproductor.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return lista;
        
    }
    
    
    
}
