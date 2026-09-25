package mycompany.contacto_3xtrat3rr3str3d.c3d;

import java.util.ArrayList;
import java.util.List;

public class InstruccionesC3D
{
    private List<String> codigo;
    public InstruccionesC3D()
    {
        this.codigo = new ArrayList<>();
    }

    public void limpiar()
    {
        this.codigo.clear();
    }

    public void agregarEtiqueta(String etiqueta)
    {
        codigo.add(etiqueta + ":");
    }

    public void agregarSaltoIncondicional(String etiqueta)
    {
        codigo.add("goto " + etiqueta + ";");
    }

    public void agregarSaltoCondicional(String arg1, String op, String arg2, String etiqueta)
    {
        codigo.add("if (" + arg1 + " " + op + " " + arg2 + ") goto " + etiqueta + ";");
    }

    public void agregarSetStack(String posicion, String valor)
    {
        codigo.add("stack[(int)" + posicion + "] = " + valor + ";");
    }

    public void agregarGetStack(String destino, String posicion)
    {
        codigo.add(destino + " = stack[(int)" + posicion + "];");
    }

    public void agregarSetHeap(String posicion, String valor)
    {
        codigo.add("heap[(int)" + posicion + "] = " + valor + ";");
    }

    public void agregarGetHeap(String destino, String posicion)
    {
        codigo.add(destino + " = heap[(int)" + posicion + "];");
    }

    public void agregarAsignacion(String destino, String arg1, String op, String arg2)
    {
        codigo.add(destino + " = " + arg1 + " " + op + " " + arg2 + ";");
    }

    public void agregarAsignacion(String destino, String valor)
    {
        codigo.add(destino + " = " + valor + ";");
    }

    public void agregarComentario(String comentario)
    {
        codigo.add("// " + comentario + "");
    }

    public void agregarPrint(String formato, String valor)
    {
        codigo.add("printf(\"%" + formato + "\", " + valor + ");");
    }

    public List<String> getCodigo()
    {
        return codigo;
    }
}
