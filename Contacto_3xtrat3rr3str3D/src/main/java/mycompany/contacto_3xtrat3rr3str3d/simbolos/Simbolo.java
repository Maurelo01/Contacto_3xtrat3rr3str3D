package mycompany.contacto_3xtrat3rr3str3d.simbolos;

public abstract class Simbolo
{
    protected String nombre;
    protected TipoDato tipo;
    protected int linea;
    protected int columna;
    protected int offset;
    protected boolean enHeap;
    public Simbolo(String nombre, TipoDato tipo, int linea, int columna)
    {
        this.nombre = nombre;
        this.tipo = tipo;
        this.linea = linea;
        this.columna = columna;
        this.offset = -1;
        this.enHeap = false;
    }

    public String getNombre()
    {
        return nombre;
    }
    public TipoDato getTipo()
    {
        return tipo;
    }

    public int getLinea()
    {
        return linea;
    }
    public int getColumna()
    {
        return columna;
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
    
    public abstract String getCategoria();
}
