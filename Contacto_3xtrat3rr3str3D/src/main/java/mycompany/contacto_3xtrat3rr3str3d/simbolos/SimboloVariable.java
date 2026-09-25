package mycompany.contacto_3xtrat3rr3str3d.simbolos;

public class SimboloVariable extends Simbolo
{
    private boolean inicializado;
    public SimboloVariable(String nombre, TipoDato tipo, int linea, int columna)
    {
        super(nombre, tipo, linea, columna);
        this.inicializado = false;
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
        return "Variable";
    }
}