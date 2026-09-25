package mycompany.contacto_3xtrat3rr3str3d.utils;

import java.util.List;
import mycompany.contacto_3xtrat3rr3str3d.c3d.GeneradorC3D;
import mycompany.contacto_3xtrat3rr3str3d.simbolos.*;
import org.antlr.v4.runtime.tree.TerminalNode;

public class GestorPunteros
{
    public static ResultadoC3D obtenerPosicionAtributo(Simbolo simBase, List<TerminalNode> rutaAccesos, TablaSimbolos tablaGlobal, GeneradorC3D generador)
    {
        if (!esObjetoValido(simBase))
        {
            return resultadoError();
        }
        String tempPunteroActual = generarPunteroBase(simBase, generador);
        Simbolo simActual = simBase;
        for (int i = 1; i < rutaAccesos.size(); i++)
        {
            String idAtributo = rutaAccesos.get(i).getText();
            if (!esObjetoValido(simActual)) return resultadoError();
            String tipoClaseObjeto = ((SimboloVariable) simBase).getReferenciaClase();
            Simbolo atributo = obtenerAtributo(tipoClaseObjeto, idAtributo, tablaGlobal);
            if (atributo == null) return resultadoError();
            String tempNuevaDireccion = generador.generarTemporal();
            generador.agregarAsignacion(tempNuevaDireccion, tempPunteroActual, "+", String.valueOf(atributo.getOffset()));
            tempPunteroActual = tempNuevaDireccion;
            if (i < rutaAccesos.size() - 1)
            {
                String tempSubPuntero = generador.generarTemporal();
                generador.agregarGetHeap(tempSubPuntero, tempPunteroActual);
                tempPunteroActual = tempSubPuntero;
            }
            simActual = atributo;
        }
        return new ResultadoC3D(simActual.getTipo(), tempPunteroActual);
    }
    
    private static boolean esObjetoValido(Simbolo simObjeto)
    {
        return simObjeto != null && simObjeto.getTipo() == TipoDato.OBJETO && simObjeto instanceof SimboloVariable;
    }
    
    private static ResultadoC3D resultadoError()
    {
        return new ResultadoC3D(TipoDato.ERROR, "");
    }
    
    private static Simbolo obtenerAtributo(String nombreClase, String idAtributo, TablaSimbolos tablaGlobal)
    {
        SimboloClase plantillaClase = (SimboloClase) tablaGlobal.buscar(nombreClase);
        if (plantillaClase == null || plantillaClase.getEntornoInterno() == null) return null;
        return plantillaClase.getEntornoInterno().buscar(idAtributo);
    }
    
    private static String generarPunteroBase(Simbolo simObjeto, GeneradorC3D generador)
    {
        String tempPunteroBase = generador.generarTemporal();
        String offset = String.valueOf(simObjeto.getOffset());
        if (simObjeto.isEnHeap())
        {
            generador.agregarGetHeap(tempPunteroBase, offset);
        }
        else
        {
            generador.agregarGetStack(tempPunteroBase, offset);
        }
        return tempPunteroBase;
    }
}
