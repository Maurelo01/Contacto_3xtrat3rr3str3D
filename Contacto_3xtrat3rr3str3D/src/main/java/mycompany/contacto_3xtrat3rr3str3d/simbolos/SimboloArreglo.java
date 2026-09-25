package mycompany.contacto_3xtrat3rr3str3d.simbolos;

public class SimboloArreglo extends Simbolo
{
    private int dimensiones;
    private int tamañoTotal;
    public SimboloArreglo(String nombre, TipoDato tipo, int linea, int columna, int dimensiones)
    {
        super(nombre, tipo, linea, columna);
        this.dimensiones = dimensiones;
        this.tamañoTotal = 0;
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

    @Override
    public String getCategoria()
    {
        return "Arreglo";
    }
}