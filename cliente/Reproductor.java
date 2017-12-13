/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cliente;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.*;

/**
 *
 * @author hejimeno
 */
public class Reproductor {
    
    private SourceDataLine sdl;
    private Clip media;
    private long posicion;
    Reproduccion rep;
    private Socket server;
    private InputStream is;
    
    public Reproductor(){
        this.media = null;
        this.posicion=0;
        this.sdl = null;
        this.rep = null;
        this.server = null;
        this.is = null;
    }
      
    protected void play(String s) throws IOException, InterruptedException{
       	
        //cerrarHilo();
        if(rep != null){
            rep.cancel();
        }
        
        //Tenemos que conseguir que la ejecución de arriba termine antes de entrar en lo de abajo siempre. De otro modo no funcionará.
        
	OutputStreamWriter osw = null;
        
//		try {
//			s = new Socket("localhost", 5050);
//			is = new BufferedInputStream(s.getInputStream());
//			AudioInputStream ais = AudioSystem.getAudioInputStream(is);
//			
//					c = AudioSystem.getClip();
//			    c.open(ais);
//			    try {
//			      c.start();
//			  } finally {
//			    ais.close();
//			  }
//    }
//		catch(IOException | LineUnavailableException | UnsupportedAudioFileException ex) {
//			ex.printStackTrace();
//		}      
           try{
          
           server = new Socket("localhost", 5050);
           osw = new OutputStreamWriter(server.getOutputStream());
           is = new BufferedInputStream(server.getInputStream());
            osw.write("PLAY SONG "+s+System.getProperty("line.separator"));
            osw.flush();
            AudioInputStream ais = AudioSystem.getAudioInputStream(is);     	
 
            AudioFormat format = ais.getFormat();
 
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
 
            SourceDataLine audioLine = (SourceDataLine) AudioSystem.getLine(info);
 
            rep = new Reproduccion(server, is, audioLine, format, ais);
            rep.start();      
        
//        if(media != null && media.isRunning()){
//            media.close();
//        }
//        
//        try (Socket server = new Socket("localhost", 5050);
//                OutputStreamWriter osw = new OutputStreamWriter(server.getOutputStream());
//                InputStream is = new BufferedInputStream(server.getInputStream())){
//            rite("PLAY SONG "+s+System.getProperty("line.separator"));
//            osw.flush();
//            osw.w
//            
//            AudioInputStream ais = AudioSystem.getAudioInputStream(is);
//            media = AudioSystem.getClip();
//            media.open(ais);
//            media.start();
//            
//        } catch (UnsupportedAudioFileException | LineUnavailableException ex) {
//            Logger.getLogger(Reproductor.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }   catch (LineUnavailableException | UnsupportedAudioFileException ex) {
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
