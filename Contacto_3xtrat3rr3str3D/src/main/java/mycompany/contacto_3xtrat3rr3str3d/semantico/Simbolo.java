package mycompany.contacto_3xtrat3rr3str3d.semantico;

import java.util.ArrayList;
import java.util.List;

public class Simbolo
{
    private String nombre;
    private String tipo;
    private String categoria;
    private int linea;
    private int columna;
    private int dimensionArreglo; 
    private List<TipoDato> tiposParametros;
    private int offset;
    private boolean enHeap;
    private boolean inicializado;
    public Simbolo(String nombre, String tipo, String categoria, int linea, int columna)
    {
        this.nombre = nombre;
        this.tipo = tipo;
        this.categoria = categoria;
        this.linea = linea;
        this.columna = columna;
        this.dimensionArreglo = 0; 
        this.tiposParametros = new ArrayList<>();
        this.offset = -1;
        this.enHeap = false;
        this.inicializado = false;
    }

    public String getNombre()
    {
        return nombre;
    }
    public String getTipo()
    {
        return tipo;
    }
    public String getCategoria()
    {
        return categoria;
    }
    public int getLinea()
    {
        return linea;
    }
    public int getColumna()
    {
        return columna;
    }
    public int getDimensionArreglo()
    {
        return dimensionArreglo;
    }
    public void setDimensionArreglo(int dimensionArreglo)
    {
        this.dimensionArreglo = dimensionArreglo;
    }
    public List<TipoDato> getTiposParametros()
    {
        return tiposParametros;
    }
    public void agregarParametro(TipoDato tipoParam)
    {
        this.tiposParametros.add(tipoParam);
    }
    
    public int getOffset()
    {
        return offset;
    }
    public void setOffset(int offset)
    {
        this.offset = offset;
    }
    
    public boolean isEnHeap()
    {
        return enHeap;
    }
    public void setEnHeap(boolean enHeap)
    {
        this.enHeap = enHeap;
    }

    public boolean isInicializado()
    {
        return inicializado;
    }
    public void setInicializado(boolean inicializado)
    {
        this.inicializado = inicializado;
    }
}