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

/**
 * 
 * Atributos:
 * 		directorio. Ruta donde se encuentran los archivos .wav con las canciones.  
 *		emirosas. Objeto Map donde se guarda el nombre de la emisora con su URL.
 *		cliente. Objeto Socket para recibir peticiones. Por el puerto 5050.
 */
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
    
    /**
     * Método run. Del hilo que se genera al aceptar una petición del cliente. 
     * Proporciona información en función del siguiente protocolo.
     * 	FETCH SONGS				Envía una lista con las canciones.
     *  FETCH PODCAST			Envía una lista con las emisoras.
     *  PLAY SONG <canción> 	Envía un archivo .wav con la canción <canción>
     *  PLAY PODCAST <emisora>	Envía un buffer con la emisora <emisora> para su reproducción. No implementado*
     */
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
                    /*En caso de que la posiciÃ³n recibida sea mayor que cero, envÃ­a la nueva
                                    Cabecera WAVE del fichero y, a continuaciÃ³n, el fichero a partir de la posiciÃ³n
                                    solicitada.
                                    En caso contrario envÃ­a el fichero desde el principio.*/
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

    /**
     * Reconstruye la cabecera del archivo wav original cambiando los bytes que indican su tamaño del 5 al 8
     * por el nuevo tamaño del fichero.
     * @param os
     * @param f
     * @param size
     */
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
            byte[] tamanio = ByteBuffer.allocate(4).putInt((int) (f.length() - size + 44)).array();
            for (int j = tamanio.length; j > 0; j--) {
                header[7 - j + 1] = tamanio[j - 1] & 0xff;
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

    /**
     * Método principal.
     * Indica la ruta donde están los wav de las canciones, "src/public_canciones".
     * Indica el puerto por donde va a recibir las peticiones, 5050.
     * Genera un objeto Map con el nombre de la emisora y su URL. Se desea utilizar archivos XML en un futuro.
     * Genera un hilo Persistencia por cada petición aceptada. 
     * @param args
     */
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

    /**
     * Método que devuelve una lista con los nombres de las canciones disponibles en la ruta "directorio".
     * @return
     */
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
    
    /**
     * Método que devuelve una lista con los nombres de las emisoras disponibles.
     * @return
     */
    private List<String> getEmisoras() {
        String[] em = new String[emisoras.keySet().size()];
        em = emisoras.keySet().toArray(em);
        List<String> emisoras = new ArrayList<>();
        for (int i = 0, n = em.length; i < n; i++) {
            emisoras.add(em[i]);
        }
        return emisoras;
    }

    /**
     * Método para sacar una canción específica.
     * @param c .Nombre de la canción.wav
     * @return Devuelve el archivo .wav con la canción.
     */
    private File getCancion(String c){
        return new File(directorio, c);
    }
    
    /**
     * Método para obtener la url de una emisora ya almacenada a partir de su clave.
     * @param clave con la que está mapeada la url de la emisora.
     * @return la URL de la URL
     */
    private URL getEmisora(String k) {
        URL emisora = emisoras.get(k);
        return emisora;
    }

    /**
     * Método auxiliar para cerrar objetos closeables.
     */
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
