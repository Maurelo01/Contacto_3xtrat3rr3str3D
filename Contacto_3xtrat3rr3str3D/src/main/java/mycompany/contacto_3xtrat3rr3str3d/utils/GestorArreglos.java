package mycompany.contacto_3xtrat3rr3str3d.utils;

import java.util.List;
import mycompany.contacto_3xtrat3rr3str3d.c3d.GeneradorC3D;
import mycompany.contacto_3xtrat3rr3str3d.simbolos.TipoDato;

public class GestorArreglos
{
    public static ResultadoC3D instanciarArregloVacio(int tamañoTotal, TipoDato tipo, GeneradorC3D generador)
    {
        String tempPuntero = generador.generarTemporal();
        generador.agregarComentario("Instanciando arreglo/matriz de tamaño: " + tamañoTotal);
        generador.agregarAsignacion(tempPuntero, "punteroHeap");
        String valorDefecto = ControlTipos.obtenerValorPorDefecto(tipo);
        for (int i = 0; i < tamañoTotal; i++)
        {
            generador.agregarSetHeap("punteroHeap", valorDefecto);
            generador.agregarAsignacion("punteroHeap", "punteroHeap + 1");
        }
        return new ResultadoC3D(tipo, tempPuntero);
    }

    public static ResultadoC3D instanciarArregloConValores(List<ResultadoC3D> valores, TipoDato tipo, GeneradorC3D generador)
    {
        String tempPuntero = generador.generarTemporal();
        generador.agregarComentario("Instanciando arreglo con " + valores.size() + " valores literales");
        generador.agregarAsignacion(tempPuntero, "punteroHeap");
        for (ResultadoC3D val : valores)
        {
            generador.agregarSetHeap("punteroHeap", val.getValorC3D());
            generador.agregarAsignacion("punteroHeap", "punteroHeap + 1");
        }
        return new ResultadoC3D(tipo, tempPuntero);
    }
}
