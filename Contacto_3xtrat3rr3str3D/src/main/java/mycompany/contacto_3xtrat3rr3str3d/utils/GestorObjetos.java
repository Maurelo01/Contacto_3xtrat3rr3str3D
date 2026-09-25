package mycompany.contacto_3xtrat3rr3str3d.utils;

import mycompany.contacto_3xtrat3rr3str3d.simbolos.SimboloClase;
import mycompany.contacto_3xtrat3rr3str3d.simbolos.TipoDato;
import mycompany.contacto_3xtrat3rr3str3d.c3d.GeneradorC3D;

public class GestorObjetos
{
    public static ResultadoC3D instanciarObjetoEnHeap(SimboloClase clase, GeneradorC3D generador)
    {
        String tempPunteroObjeto = generador.generarTemporal();
        generador.agregarComentario("Instanciacion de objeto: " + clase.getNombre());
        generador.agregarAsignacion(tempPunteroObjeto, "punteroHeap");
        int tamanoAtributos = clase.getTamañoHeapObjeto();
        for (int i = 0; i < tamanoAtributos; i++)
        {
            generador.agregarSetHeap("punteroHeap", "0");
            generador.agregarAsignacion("punteroHeap", "punteroHeap + 1");
        }
        return new ResultadoC3D(TipoDato.OBJETO, tempPunteroObjeto);
    }
}
