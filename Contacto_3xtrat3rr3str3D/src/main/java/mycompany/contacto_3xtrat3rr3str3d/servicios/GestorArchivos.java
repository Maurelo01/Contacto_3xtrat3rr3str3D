package mycompany.contacto_3xtrat3rr3str3d.servicios;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import javax.swing.tree.DefaultMutableTreeNode;

public class GestorArchivos
{
    public static class NodoArchivo
    {
        public File archivo;
        public NodoArchivo(File archivo)
        {
            this.archivo = archivo;
        }
        
        @Override
        public String toString()
        {
            return archivo.getName();
        }
    }
    
    public static void llenarArbolDirectorios(File carpeta, DefaultMutableTreeNode nodoPadre)
    {
        File[] archivos = carpeta.listFiles();
        if (archivos != null)
        {
            for (File archivo : archivos)
            {
                DefaultMutableTreeNode nodoHijo = new DefaultMutableTreeNode(new NodoArchivo(archivo));
                nodoPadre.add(nodoHijo);
                if (archivo.isDirectory()) llenarArbolDirectorios(archivo, nodoHijo);
            }
        }
    }
    
    public static String leerContenido(File archivo) throws IOException
    {
        return new String(Files.readAllBytes(archivo.toPath()));
    }

    public static void guardarContenido(File archivo, String contenido) throws IOException
    {
        Files.write(archivo.toPath(), contenido.getBytes());
    }

    public static boolean crearCarpeta(File padre, String nombre) throws IOException
    {
        File nueva = new File(padre, nombre);
        if (nueva.exists()) return false;
        return nueva.mkdirs();
    }

    public static boolean eliminarRecursivo(File objetivo) throws IOException
    {
        if (objetivo == null || !objetivo.exists()) return false;
        if (objetivo.isDirectory())
        {
            File[] hijos = objetivo.listFiles();
            if (hijos != null)
            {
                for (File hijo : hijos) eliminarRecursivo(hijo);
            }
        }
        return objetivo.delete();
    }
}
