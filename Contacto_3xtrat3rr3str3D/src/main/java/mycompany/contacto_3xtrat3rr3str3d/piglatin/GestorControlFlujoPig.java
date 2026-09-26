package mycompany.contacto_3xtrat3rr3str3d.piglatin;

import mycompany.contacto_3xtrat3rr3str3d.PigLatinParser;
import mycompany.contacto_3xtrat3rr3str3d.simbolos.Simbolo;
import mycompany.contacto_3xtrat3rr3str3d.simbolos.TipoDato;
import mycompany.contacto_3xtrat3rr3str3d.utils.ResultadoC3D;
import mycompany.contacto_3xtrat3rr3str3d.visitors.PigLatinCustomVisitor;

public class GestorControlFlujoPig extends PigLatinGestorBase
{
    public GestorControlFlujoPig(PigLatinCustomVisitor visitor)
    {
        super(visitor);
    }
    
    public Object procesarCondicional(PigLatinParser.CondicionalContext ctx)
    {
        String etiquetaSalida = generador.generarEtiqueta();
        for (int i = 0; i < ctx.expresion().size(); i++)
        {
            ResultadoC3D resCondicion = (ResultadoC3D) visitor.visit(ctx.expresion(i));
            if (resCondicion.getTipo() != TipoDato.BOOLEANO && resCondicion.getTipo() != TipoDato.ERROR) reportarError(ctx.getStart().getLine(), "Condición SI debe ser booleana.");
            String etiquetaFalsa = generador.generarEtiqueta();
            generador.agregarSaltoCondicional(resCondicion.getValorC3D(), "==", "0", etiquetaFalsa);
            visitor.visit(ctx.bloque(i));
            generador.agregarSaltoIncondicional(etiquetaSalida);
            generador.agregarEtiqueta(etiquetaFalsa);
        }
        if (ctx.bloque().size() > ctx.expresion().size()) visitor.visit(ctx.bloque(ctx.bloque().size() - 1));
        generador.agregarEtiqueta(etiquetaSalida);
        return null;
    }
    
    public Object procesarBucleDum(PigLatinParser.BucleDumContext ctx)
    {
        String etiquetaInicio = generador.generarEtiqueta();
        String etiquetaSalida = generador.generarEtiqueta();
        generador.agregarEtiqueta(etiquetaInicio);
        ResultadoC3D resCondicion = (ResultadoC3D) visitor.visit(ctx.expresion());
        if (resCondicion.getTipo() != TipoDato.BOOLEANO && resCondicion.getTipo() != TipoDato.ERROR) reportarError(ctx.getStart().getLine(), "Condición DUM debe ser booleana.");
        generador.agregarSaltoCondicional(resCondicion.getValorC3D(), "==", "0", etiquetaSalida);
        visitor.visit(ctx.bloque());
        generador.agregarSaltoIncondicional(etiquetaInicio);
        generador.agregarEtiqueta(etiquetaSalida);
        return null;
    }
    
    public Object procesarBucleFacere(PigLatinParser.BucleFacereContext ctx)
    {
        String etiquetaInicio = generador.generarEtiqueta();
        generador.agregarEtiqueta(etiquetaInicio);
        visitor.visit(ctx.bloque());
        ResultadoC3D resCondicion = (ResultadoC3D) visitor.visit(ctx.expresion());
        if (resCondicion.getTipo() != TipoDato.BOOLEANO && resCondicion.getTipo() != TipoDato.ERROR) reportarError(ctx.getStart().getLine(), "Condición FACERE DUM debe ser booleana.");
        generador.agregarSaltoCondicional(resCondicion.getValorC3D(), "==", "1", etiquetaInicio);
        return null;
    }
    
    public Object procesarBuclePer(PigLatinParser.BuclePerContext ctx)
    {
        tabla.entrarAmbito();
        visitor.visit(ctx.declaracion());
        String etiquetaInicio = generador.generarEtiqueta();
        String etiquetaSalida = generador.generarEtiqueta();
        generador.agregarEtiqueta(etiquetaInicio);
        ResultadoC3D resCondicion = (ResultadoC3D) visitor.visit(ctx.expresion());
        if (resCondicion.getTipo() != TipoDato.BOOLEANO && resCondicion.getTipo() != TipoDato.ERROR) reportarError(ctx.getStart().getLine(), "Condición PER debe ser booleana.");
        generador.agregarSaltoCondicional(resCondicion.getValorC3D(), "==", "0", etiquetaSalida);
        visitor.visit(ctx.bloque());
        visitor.visit(ctx.actualizacion());
        generador.agregarSaltoIncondicional(etiquetaInicio);
        generador.agregarEtiqueta(etiquetaSalida);
        tabla.salirAmbito();
        return null;
    }
    
    public Object procesarActualizacion(PigLatinParser.ActualizacionContext ctx)
    {
        if (ctx.asignacion() != null) return visitor.visit(ctx.asignacion());
        else
        {
            String idVariable = ctx.ID().getText();
            Simbolo sim = tabla.buscar(idVariable);
            int linea = ctx.getStart().getLine();
            if (sim == null)
            {
                reportarError(linea, "La variable '" + idVariable + "' no existe.");
                return null;
            }
            String temporalAnterior = generador.generarTemporal();
            String temporalNuevo = generador.generarTemporal();
            String operador = ctx.MAS_ABREVIADO() != null ? "+" : "-";
            if (sim.isEnHeap())
            {
                generador.agregarGetHeap(temporalAnterior, String.valueOf(sim.getOffset()));
                generador.agregarAsignacion(temporalNuevo, temporalAnterior, operador, "1");
                generador.agregarSetHeap(String.valueOf(sim.getOffset()), temporalNuevo);
            }
            else
            {
                generador.agregarGetStack(temporalAnterior, String.valueOf(sim.getOffset()));
                generador.agregarAsignacion(temporalNuevo, temporalAnterior, operador, "1");
                generador.agregarSetStack(String.valueOf(sim.getOffset()), temporalNuevo);
            }
            return null;
        }
    }
}
