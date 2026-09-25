package mycompany.contacto_3xtrat3rr3str3d.utils;

import mycompany.contacto_3xtrat3rr3str3d.c3d.GeneradorC3D;
import mycompany.contacto_3xtrat3rr3str3d.c3d.GeneradorC3D;
import mycompany.contacto_3xtrat3rr3str3d.simbolos.TipoDato;

public class GestorCadenas
{
    public static ResultadoC3D guardarCadenaEnHeap(String texto, GeneradorC3D generador)
    {
        if (texto.startsWith("\"") && texto.endsWith("\""))
        {
            texto = texto.substring(1, texto.length() - 1);
        }
        String tempPuntero = generador.generarTemporal();
        generador.agregarAsignacion(tempPuntero, "punteroHeap");
        for (int i = 0; i < texto.length(); i++)
        {
            int CodigoASCII = (int) texto.charAt(i);
            generador.agregarSetHeap("punteroHeap", String.valueOf(CodigoASCII));
            generador.agregarAsignacion("punteroHeap", "punteroHeap + 1");
        }
        generador.agregarSetHeap("punteroHeap", "-1");
        generador.agregarAsignacion("punteroHeap", "punteroHeap + 1");
        return new ResultadoC3D(TipoDato.CADENA, tempPuntero);
    }
}