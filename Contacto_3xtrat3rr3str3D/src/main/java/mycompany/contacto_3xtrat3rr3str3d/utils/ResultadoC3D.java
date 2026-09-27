package mycompany.contacto_3xtrat3rr3str3d.utils;

import java.util.ArrayList;
import java.util.List;
import mycompany.contacto_3xtrat3rr3str3d.simbolos.TipoDato;

public class ResultadoC3D
{
    private TipoDato tipo;
    private String valorC3D;
    private List<Integer> tamañosDimensiones;
    public ResultadoC3D(TipoDato tipo, String valorC3D)
    {
        this.tipo = tipo;
        this.valorC3D = valorC3D;
        this.tamañosDimensiones = new ArrayList<>();
    }
    public TipoDato getTipo()
    {
        return tipo;
    }
    public String getValorC3D()
    {
        return valorC3D; 
    }
    public List<Integer> getTamañosDimensiones()
    {
        return tamañosDimensiones;
    }
    public void setTamañosDimensiones(List<Integer> tamañosDimensiones)
    {
        this.tamañosDimensiones = tamañosDimensiones;
    }
}