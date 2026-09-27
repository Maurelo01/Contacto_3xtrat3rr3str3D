package mycompany.contacto_3xtrat3rr3str3d.simbolos;

import java.util.ArrayList;
import java.util.List;

public class SimboloArreglo extends Simbolo
{
    private int dimensiones;
    private int tamañoTotal;
    private List<Integer> tamañosDimensiones;
    private boolean inicializado;
    public SimboloArreglo(String nombre, TipoDato tipo, int linea, int columna, int dimensiones)
    {
        super(nombre, tipo, linea, columna);
        this.dimensiones = dimensiones;
        this.tamañoTotal = 0;
        this.tamañosDimensiones = new ArrayList<>();
        this.inicializado = false;
    }

    public int getDimensiones()
    {
        return dimensiones;
    }
    public int getTamañoTotal()
    {
        return tamañoTotal;
    }
    public void setTamañoTotal(int tamañoTotal)
    {
        this.tamañoTotal = tamañoTotal;
    }
    public void agregarTamañoDimension(int tamaño)
    {
        this.tamañosDimensiones.add(tamaño);
    }
    public List<Integer> getTamañosDimensiones()
    {
        return tamañosDimensiones;
    }
    public boolean isInicializado()
    {
        return inicializado;
    }
    public void setInicializado(boolean inicializado)
    {
        this.inicializado = inicializado;
    }

    @Override
    public String getCategoria()
    {
        return "Arreglo";
    }
}