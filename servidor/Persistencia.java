package servidor;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class Persistencia implements Runnable {
	
	private String directorio;
	private Map<String,URL> emisoras;
	
	@Override
	public void run() {
		

	}

	public static void main(String[] args) {
		try {
			String directorio = "src/public_canciones";
			Map<String,URL> emisoras = new HashMap<String, URL>();
			emisoras.put("rock_fm", new URL("http://player.rockfm.fm/"));
			emisoras.put("40_principales", new URL("https://play.los40.com/"));
			emisoras.put("lory_money", new URL("https://youtu.be/Gxgwizczm48"));
			
			Persistencia pst = new Persistencia(directorio,emisoras);
			
			
			System.out.println("###### PRUEBAS ######");
			File f = pst.getCancion("8in8_-_05_-_Ill_Be_My_Mirror");
			System.out.println(f.getName());
			
//			ExecutorService pool = Executors.newCachedThreadPool();
//			ServerSocket server = null;
//			try {
//				server = new ServerSocket(8080);
//				while (true) {
//					try {
//						final Socket cliente = server.accept();
//						Persistencia pst = new Persistencia(directorio,emisoras);
//						pool.execute(pst);
//					}catch (IOException e) {e.printStackTrace();}
//				}
//			} catch (IOException e) {
//				e.printStackTrace();
//			}finally {
//				cerrar(server);
//				pool.shutdown();
//			}
			
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

	}
	
	
	
	public Persistencia(String directorio, Map<String,URL> emisoras) {
		super();
		this.directorio = directorio;
		this.emisoras = emisoras;
	}

	//Todos los archivos .mp3 en la carpeta this.directorio
	private List<String> getCanciones(){
		List<String> canciones = new ArrayList<>();
		File p_canciones = new File(this.directorio);
		File[] dire = p_canciones.listFiles();
		if(p_canciones.exists()) {
			for (int i = 0; i < dire.length; i++) {
				canciones.add(dire[i].getName().split(".mp3")[0]);
			}
		}
		return canciones;
	}
	
	private File getCancion(String c) {
		File cancion = new File(this.directorio, c+".mp3");
		if(!cancion.exists())System.out.println("El archivo no existe"); //Solo para pruebas.
		return cancion;
	}
	
	private List<String> getEmisoras(){
		String[] em = new String[this.emisoras.keySet().size()] ; 
		em = this.emisoras.keySet().toArray(em);
		List<String> emisoras = new ArrayList<>();
		for (int i = 0; i < em.length; i++) {
			emisoras.add(em[i]);
		}
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
