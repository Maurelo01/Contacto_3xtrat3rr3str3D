package mycompany.contacto_3xtrat3rr3str3d.c3d;

import mycompany.contacto_3xtrat3rr3str3d.c3d.ConectorC3D;
import java.util.ArrayList;
import java.util.List;

public class GeneradorC3D
{
    private static GeneradorC3D instancia;
    private GestorMemoriaC3D gestorMemoria;
    private InstruccionesC3D escritorInstrucciones;
    private List<String> funcionesNativas;

    private GeneradorC3D()
    {
        this.gestorMemoria = new GestorMemoriaC3D();
        this.escritorInstrucciones = new InstruccionesC3D();
        this.funcionesNativas = new ArrayList<>();
    }

    public static GeneradorC3D getInstancia()
    {
        if (instancia == null) instancia = new GeneradorC3D();
        return instancia;
    }

    public void limpiar()
    {
        this.gestorMemoria.limpiar();
        this.escritorInstrucciones.limpiar();
        this.funcionesNativas.clear();
    }

    public String generarTemporal()
    {
        return gestorMemoria.generarTemporal();
    }

    public String generarEtiqueta()
    {
        return gestorMemoria.generarEtiqueta();
    }

    public int moverPunteroStack(int espacios)
    {
        return gestorMemoria.moverPunteroStack(espacios);
    }

    public int moverPunteroHeap(int espacios)
    {
        return gestorMemoria.moverPunteroHeap(espacios);
    }

    public void agregarEtiqueta(String etiqueta)
    {
        escritorInstrucciones.agregarEtiqueta(etiqueta);
    }

    public void agregarSaltoIncondicional(String etiqueta)
    {
        escritorInstrucciones.agregarSaltoIncondicional(etiqueta);
    }

    public void agregarSaltoCondicional(String arg1, String op, String arg2, String etiqueta)
    {
        escritorInstrucciones.agregarSaltoCondicional(arg1, op, arg2, etiqueta);
    }

    public void agregarSetStack(String posicion, String valor)
    {
        escritorInstrucciones.agregarSetStack(posicion, valor);
    }

    public void agregarGetStack(String destino, String posicion)
    {
        escritorInstrucciones.agregarGetStack(destino, posicion);
    }

    public void agregarSetHeap(String posicion, String valor)
    {
        escritorInstrucciones.agregarSetHeap(posicion, valor);
    }

    public void agregarGetHeap(String destino, String posicion)
    {
        escritorInstrucciones.agregarGetHeap(destino, posicion);
    }

    public void agregarAsignacion(String destino, String arg1, String op, String arg2)
    {
        escritorInstrucciones.agregarAsignacion(destino, arg1, op, arg2);
    }

    public void agregarAsignacion(String destino, String valor)
    {
        escritorInstrucciones.agregarAsignacion(destino, valor);
    }

    public void agregarComentario(String comentario)
    {
        escritorInstrucciones.agregarComentario(comentario);
    }

    public void agregarPrint(String formato, String valor)
    {
        escritorInstrucciones.agregarPrint(formato, valor);
    }

    public void agregarFuncionNativa(String codigoNativa)
    {
        if (!funcionesNativas.contains(codigoNativa))
        {
            funcionesNativas.add(codigoNativa);
        }
    }
    
    public void agregarLlamadaNativa(String nombreNativa, String argumento)
    {
        escritorInstrucciones.agregarCodigoBruto(nombreNativa + "(" + argumento + ");");
    }

    public String obtenerCodigoCompilable()
    {
        return ConectorC3D.construirCodigoCompilable(escritorInstrucciones.getCodigoMain(), escritorInstrucciones.getCodigoFunciones(), gestorMemoria.getContadorTemporales(), funcionesNativas);
    }
    
    public void iniciarMetodo(String nombreMetodo)
    {
        escritorInstrucciones.setEscribiendoEnFuncion(true);
        escritorInstrucciones.agregarCodigoBruto("void metodo_" + nombreMetodo + "() {");
    }

    public void cerrarMetodo()
    {
        escritorInstrucciones.agregarCodigoBruto("    return;");
        escritorInstrucciones.agregarCodigoBruto("}\n");
        escritorInstrucciones.setEscribiendoEnFuncion(false);
    }

    public void agregarCodigoBruto(String codigo)
    {
        escritorInstrucciones.agregarCodigoBruto(codigo);
    }
}
