package mycompany.contacto_3xtrat3rr3str3d.simbolos;

public class SimboloClase extends Simbolo
{
    private TablaSimbolos entornoInterno;
    private int tamañoHeapObjeto;
    public SimboloClase(String nombre, int linea, int columna)
    {
        super(nombre, TipoDato.OBJETO, linea, columna);
        this.entornoInterno = new TablaSimbolos();
        this.tamañoHeapObjeto = 0;
        this.enHeap = true;
    }

    public TablaSimbolos getEntornoInterno()
    {
        return entornoInterno;
    }

    public int getTamañoHeapObjeto()
    {
        return tamañoHeapObjeto;
    }
    public void setTamañoHeapObjeto(int tamañoHeapObjeto)
    {
        this.tamañoHeapObjeto = tamañoHeapObjeto;
    }

    @Override
    public String getCategoria()
    {
        return "Clase/Estructura";
    }
}
