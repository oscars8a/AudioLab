/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cliente;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import javax.sound.sampled.Clip;

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
        try (Socket server = new Socket("localhost", 8080);
                PrintWriter pw = new PrintWriter(server.getOutputStream())){
            
            pw.write("PLAY SONG "+ s);
            
            
        }
    }
    
    protected void resumePause(){
        
    }
    
    
}
