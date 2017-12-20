package servidor;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Persistencia implements Runnable {

    private String directorio;
    private Map<String, URL> emisoras;
    private final Socket cliente;

    public Persistencia(String directorio, Map<String, URL> emisoras, Socket cliente) {
        super();
        this.directorio = directorio;
        this.emisoras = emisoras;
        this.cliente = cliente;
    }
    
    @Override
    public void run() {

        BufferedReader br = null;
        DataOutputStream dos = null;
        FileInputStream fis = null;
        PrintStream ps = null;
        try {
            br = new BufferedReader(new InputStreamReader(cliente.getInputStream()));
            dos = new DataOutputStream(cliente.getOutputStream());
            ps = new PrintStream(cliente.getOutputStream());
            String linea;
            String pos;
            if ((linea = br.readLine()) != null) {
                //Protocolo:
                //FETCH SONGS
                //FETCH PODCAST
                //PLAY SONG <cancion>
                //PLAY PODCAST <emisora>
                System.out.println(linea);
                if (linea.compareTo("FETCH SONGS") == 0) {
                    List<String> songs = this.getCanciones();
                    for (int i = 0; i < songs.size(); i++) {
                        ps.println(songs.get(i));
                    }
                    ps.flush();
                } else if (linea.compareTo("FETCH PODCAST") == 0) {
                    List<String> emisoras = this.getEmisoras();
                    for (int i = 0; i < emisoras.size(); i++) {
                        ps.println(emisoras.get(i));
                    }
                    ps.flush();
                } else if (linea.startsWith("PLAY SONG")) {
                    /*En caso de que la posición recibida sea mayor que cero, envía la nueva
                                    Cabecera WAVE del fichero y, a continuación, el fichero a partir de la posición
                                    solicitada.
                                    En caso contrario envía el fichero desde el principio.*/
                    String c = linea.split("PLAY SONG ")[1];
                    pos = br.readLine().split("FROM ")[1];
                    File f = this.getCancion(c);
                    System.out.println(pos);
                    byte[] buff = new byte[1024];
                    if (Integer.parseInt(pos) > 0) {
                        enviarWavHeader(dos, f, Long.parseLong(pos));
                    }
                    RandomAccessFile cancion = new RandomAccessFile(f, "r");
                    cancion.seek(Long.parseLong(pos));
                    int leidos;
                    while ((leidos = cancion.read(buff)) != -1) {
                        dos.write(buff, 0, leidos);
                    }
                    dos.flush();
                } else if (linea.startsWith("PLAY PODCAST")) {
                    String p = linea.split("PLAY PODCAST ")[1];
//					URLConnection url =  
                }
                cliente.shutdownOutput();
                System.out.println("Enviando...");
            }

        } catch (IOException e) {
            if (e.getClass().equals(SocketException.class)) {
                System.out.println("Cierre de conexion con cliente");
            } else {
                e.printStackTrace();
            }
        } finally {
            cerrar(dos);
            cerrar(fis);
            cerrar(br);
            cerrar(ps);
        }

    }

    //Construyo la cabecera WAVE para enviar streams de ficheros WAV que no comiencen desde el principio.
    private static void enviarWavHeader(DataOutputStream os, File f, long size) {

        DataInputStream dis = null;

        try {
            dis = new DataInputStream(new FileInputStream(f));
            int[] header = new int[44];
            for (int i = 0; i < 44; i++) {
                header[i] = dis.read();
            }

            //La clase ByteBuffer ha sido necesaria para realizar esta tarea. Es java.nio
            byte[] tamaño = ByteBuffer.allocate(4).putInt((int) (f.length() - size + 44)).array();
            for (int j = tamaño.length; j > 0; j--) {
                header[7 - j + 1] = tamaño[j - 1] & 0xff;
            }

            for (int k = 0; k < 44; k++) {
                System.out.println(header[k]);
                os.write(header[k]);
            }
            os.flush();

        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            cerrar(dis);
        }

    }

    public static void main(String[] args) {
        try {
            String directorio = "src/public_canciones";
            int puerto = 5050;

            Map<String, URL> emisoras = new HashMap<String, URL>();
            emisoras.put("rock_fm", new URL("http://player.rockfm.fm/"));
            emisoras.put("40_principales", new URL("https://play.los40.com/"));
            emisoras.put("lory_money", new URL("https://youtu.be/Gxgwizczm48"));

            ExecutorService pool = Executors.newCachedThreadPool();
            ServerSocket server = null;
            try {
                server = new ServerSocket(puerto);
                while (true) {
                    try {
                        final Socket cliente = server.accept();
                        System.out.println("Cliente aceptado...");
                        Persistencia pst = new Persistencia(directorio, emisoras, cliente);
                        pool.execute(pst);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                cerrar(server);
                pool.shutdown();
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

    }

    //Todos los archivos .wav en la carpeta this.directorio
    private List<String> getCanciones() {
        List<String> canciones = new ArrayList<>();
        File p_canciones = new File(this.directorio);
        File[] dire = p_canciones.listFiles();
        if (p_canciones.exists()) {
            for (int i = 0, n = dire.length; i < n; i++) {
                canciones.add(dire[i].getName());//.split(".wav")[0]
            }
        }
        return canciones;
    }

//	private File getCancion(String c) {
//		File cancion = new File(directorio, c+".wav");
//		if(!cancion.exists())System.out.println("El archivo no existe"); //Solo para pruebas.
//		return cancion;
//	}
    private List<String> getEmisoras() {
        String[] em = new String[emisoras.keySet().size()];
        em = emisoras.keySet().toArray(em);
        List<String> emisoras = new ArrayList<>();
        for (int i = 0, n = em.length; i < n; i++) {
            emisoras.add(em[i]);
        }
        return emisoras;
    }

    private File getCancion(String c){
        return new File(directorio, c);
    }
    
    private URL getEmisora(String k) {
        URL emisora = emisoras.get(k);
        return emisora;
    }

    private static void cerrar(Closeable o) {
        try {
            if (o != null) {
                o.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
