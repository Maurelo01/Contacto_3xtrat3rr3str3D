package mycompany.contacto_3xtrat3rr3str3d.c3d;

import java.util.ArrayList;
import java.util.List;

public class InstruccionesC3D
{
    private List<String> codigoMain;
    private List<String> codigoFunciones;
    private boolean escribiendoEnFuncion;

    public InstruccionesC3D()
    {
        this.codigoMain = new ArrayList<>();
        this.codigoFunciones = new ArrayList<>();
        this.escribiendoEnFuncion = false;
    }

    public void limpiar()
    {
        this.codigoMain.clear();
        this.codigoFunciones.clear();
        this.escribiendoEnFuncion = false;
    }

    public void setEscribiendoEnFuncion(boolean enFuncion)
    {
        this.escribiendoEnFuncion = enFuncion;
    }

    private void agregar(String instruccion)
    {
        if (escribiendoEnFuncion)
        {
            codigoFunciones.add(instruccion);
        }
        else
        {
            codigoMain.add(instruccion);
        }
    }

    public void agregarEtiqueta(String etiqueta)
    {
        agregar(etiqueta + ":");
    }
    public void agregarSaltoIncondicional(String etiqueta)
    {
        agregar("goto " + etiqueta + ";");
    }
    public void agregarSaltoCondicional(String arg1, String op, String arg2, String etiqueta)
    {
        agregar("if (" + arg1 + " " + op + " " + arg2 + ") goto " + etiqueta + ";");
    }
    public void agregarSetStack(String posicion, String valor)
    {
        agregar("stack[(int)" + posicion + "] = " + valor + ";");
    }
    public void agregarGetStack(String destino, String posicion)
    {
        agregar(destino + " = stack[(int)" + posicion + "];");
    }
    public void agregarSetHeap(String posicion, String valor)
    {
        agregar("heap[(int)" + posicion + "] = " + valor + ";");
    }
    public void agregarGetHeap(String destino, String posicion)
    {
        agregar(destino + " = heap[(int)" + posicion + "];");
    }
    public void agregarAsignacion(String destino, String arg1, String op, String arg2)
    {
        agregar(destino + " = " + arg1 + " " + op + " " + arg2 + ";");
    }
    public void agregarAsignacion(String destino, String valor)
    {
        agregar(destino + " = " + valor + ";");
    }
    public void agregarComentario(String comentario)
    {
        agregar("// " + comentario);
    }
    public void agregarPrint(String formato, String valor)
    {
        agregar("printf(\"%" + formato + "\", " + valor + ");");
    }
    
    public void agregarCodigoBruto(String codigo)
    {
        agregar(codigo);
    }

    public List<String> getCodigoMain()
    {
        return codigoMain;
    }
    public List<String> getCodigoFunciones()
    {
        return codigoFunciones;
    }
}