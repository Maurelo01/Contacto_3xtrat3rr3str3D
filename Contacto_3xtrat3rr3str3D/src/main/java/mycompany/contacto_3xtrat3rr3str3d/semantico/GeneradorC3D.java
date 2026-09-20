package mycompany.contacto_3xtrat3rr3str3d.semantico;

import java.util.ArrayList;
import java.util.List;

public class GeneradorC3D
{
    private static GeneradorC3D instancia;
    private List<String> codigo;
    private int contadorTemporales;
    private int contadorEtiquetas;
    private int punteroStack;
    private int punteroHeap;
    private GeneradorC3D()
    {
        this.codigo = new ArrayList<>();
        this.contadorTemporales = 0;
        this.contadorEtiquetas = 0;
        this.punteroStack = 0;
        this.punteroHeap = 0;
    }

    public static GeneradorC3D getInstancia()
    {
        if (instancia == null)
        {
            instancia = new GeneradorC3D();
        }
        return instancia;
    }
    public void limpiar()
    {
        this.codigo.clear();
        this.contadorTemporales = 0;
        this.contadorEtiquetas = 0;
        this.punteroStack = 0;
        this.punteroHeap = 0;
    }

    public String generarTemporal()
    {
        return "temp" + (contadorTemporales++);
    }

    public String generarEtiqueta()
    {
        return "et" + (contadorEtiquetas++);
    }

    public int moverPunteroStack(int espacios)
    {
        int actual = punteroStack;
        punteroStack += espacios;
        return actual;
    }

    public int moverPunteroHeap(int espacios)
    {
        int actual = punteroHeap;
        punteroHeap += espacios;
        return actual;
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

    public void agregarSetHeap(String posicion, String valor) {
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
        codigo.add("// --- " + comentario + " ---");
    }

    public void agregarPrint(String formato, String valor)
    {
        codigo.add("printf(\"%" + formato + "\", " + valor + ");");
    }
    public String obtenerCodigo()
    {
        StringBuilder sb = new StringBuilder();
        for (String inst : codigo)
        {
            sb.append(inst).append("\n");
        }
        return sb.toString();
    }   
    public String obtenerCodigoCompilable()
    {
        StringBuilder c3d = new StringBuilder();
        c3d.append("#include <stdio.h>\n\n");
        c3d.append("#include <math.h>\n\n");
        c3d.append("double heap[100000];\n");
        c3d.append("double stack[100000];\n");
        c3d.append("int punteroStack = 0;\n");
        c3d.append("int punteroHeap = 0;\n\n");
        if (contadorTemporales > 0)
        {
            c3d.append("double ");
            for (int i = 0; i < contadorTemporales; i++)
            {
                c3d.append("temp").append(i);
                if (i < contadorTemporales - 1)
                {
                    c3d.append(", ");
                }
                if ((i + 1) % 100 == 0) 
                {
                    c3d.append("\n");
                }
            }
            c3d.append(";\n\n");
        }
        c3d.append("void main() {\n");
        for (String inst : codigo)
        {
            c3d.append("    ").append(inst).append("\n");
        }
        c3d.append("\n    return;\n");
        c3d.append("}\n");
        return c3d.toString();
    }
}
