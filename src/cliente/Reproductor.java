/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cliente;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Reproductor {
    private long posicion;
    private Reproduccion rep;
    private String audioActual;
    private float volumen;
    private boolean isPlaying;

    
    public Reproductor(float vol){
        this.posicion=0;
        this.rep = null;
        this.audioActual = null;
        this.volumen = vol;
        this.isPlaying = false;
    }
      
    protected void play(String s, long posicion){
        this.audioActual = s;
        //cerrarHilo();
        if(rep != null){
            rep.interrupt();
            rep.cancel();
            this.setIsPlaying(false);
        }
        rep = new Reproduccion(s, posicion, volumen);
        rep.start(); 
        this.setIsPlaying(true);
}
    //Si el audio está reproduciéndose, se pausa y se guarda la última posición reproducida. 
    //En caso contrario, se reanuda, reproduciendo a partir del momento exacto en que se quedó.
    protected void resumePause(){
        if(rep.isAlive())
            posicion = rep.pause();
        else{
            this.play(audioActual, posicion);
        }
    }

    public void cambiarVolumen(float vol){
        this.volumen=vol;
        rep.cambiarVolumen(vol);
    }
    
    //Recoge la lista de todas las canciones o emisoras del servidor.
    protected ArrayList<String> getLista(String option){
        
        ArrayList<String> lista = new ArrayList<>();
        
        try(Socket s = new Socket("localhost", 5050);
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
    
     public boolean getIsPlaying(){
         return this.isPlaying;
     }
     
     public void setIsPlaying(boolean b){
         this.isPlaying = b;
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
