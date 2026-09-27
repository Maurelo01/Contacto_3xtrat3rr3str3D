package mycompany.contacto_3xtrat3rr3str3d.simbolos;

import java.util.ArrayList;
import java.util.List;

public class SimboloFuncion extends Simbolo
{
    private List<SimboloVariable> parametros;
    private String etiquetaInicio;
    private int tamañoEntorno;
    private String etiquetaC3D;
    public SimboloFuncion(String nombre, TipoDato tipo, int linea, int columna)
    {
        super(nombre, tipo, linea, columna);
        this.parametros = new ArrayList<>();
        this.tamañoEntorno = 0;
        this.enHeap = true;
        this.etiquetaC3D = nombre;
    }

    public void agregarParametro(SimboloVariable parametro)
    {
        this.parametros.add(parametro);
    }
    public List<SimboloVariable> getParametros()
    {
        return parametros;
    }

    public String getEtiquetaInicio()
    {
        return etiquetaInicio;
    }
    public void setEtiquetaInicio(String etiquetaInicio)
    {
        this.etiquetaInicio = etiquetaInicio;
    }

    public int getTamañoEntorno()
    {
        return tamañoEntorno;
    }
    public void setTamañoEntorno(int tamañoEntorno)
    {
        this.tamañoEntorno = tamañoEntorno;
    }

    public String getEtiquetaC3D()
    {
        return etiquetaC3D != null ? etiquetaC3D : nombre;
    }
    public void setEtiquetaC3D(String etiquetaC3D)
    {
        this.etiquetaC3D = etiquetaC3D;
    }

    @Override
    public String getCategoria()
    {
        return nombre.endsWith("_constructor") ? "Constructor" : "Metodo";
    }
}
