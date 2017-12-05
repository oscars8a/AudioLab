package servidor;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;



public class Persistencia implements Runnable {
	
	private String directorio;
	private Map<String,URL> emisoras;
	
	@Override
	public void run() {
		

	}

	public static void main(String[] args) {
		try {
			String directorio = "musica";
			Map<String,URL> emisoras = null;
			emisoras.put("RockFM", new URL("http://player.rockfm.fm/"));
			emisoras.put("EMisora", new URL("nueva emisora"));
			
			
			ExecutorService pool = Executors.newCachedThreadPool();
			ServerSocket server = null;
			try {
				server = new ServerSocket(8080);
				while (true) {
					try {
						final Socket cliente = server.accept();
						Persistencia pst = new Persistencia(directorio,emisoras);
						pool.execute(pst);
					}catch (IOException e) {e.printStackTrace();}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}finally {
				cerrar(server);
				pool.shutdown();
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

	}
	
	
	
	public Persistencia(String directorio, Map<String,URL> emisoras) {
		super();
		this.directorio = directorio;
		this.emisoras = emisoras;
	}

	//Falta
	private List<String> getCanciones(){
		List<String> canciones = null;
		
		return canciones;
	}
	
	//Falta
	private File getCancion(String c) {
		File cancion = null;
		return cancion;
	}
	
	private List<String> getEmisoras(){
		List<String> emisoras = null;
		emisoras = (List<String>) this.emisoras.keySet();
		return emisoras;
	}
	
	private URL getEmisora(String k) {
		URL emisora = this.emisoras.get(k);
		return emisora;
	}
	
	private static void cerrar(Closeable o) {
		try {
			if (o != null)
				o.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
