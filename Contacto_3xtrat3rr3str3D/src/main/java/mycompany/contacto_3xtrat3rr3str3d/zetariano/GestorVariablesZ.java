package mycompany.contacto_3xtrat3rr3str3d.zetariano;

import java.util.List;
import mycompany.contacto_3xtrat3rr3str3d.ZetarianoParser;
import mycompany.contacto_3xtrat3rr3str3d.simbolos.*;
import mycompany.contacto_3xtrat3rr3str3d.utils.ControlTipos;
import mycompany.contacto_3xtrat3rr3str3d.utils.GestorPunteros;
import mycompany.contacto_3xtrat3rr3str3d.utils.ResultadoC3D;
import mycompany.contacto_3xtrat3rr3str3d.visitors.ZetarianoCustomVisitor;
import org.antlr.v4.runtime.tree.TerminalNode;

public class GestorVariablesZ extends ZetarianoGestorBase
{
    public GestorVariablesZ(ZetarianoCustomVisitor visitor)
    {
        super(visitor);
    }
    
    public Object procesarDeclSinAsignar(ZetarianoParser.DeclSinAsignarContext ctx)
    {
        String tipoStr = ctx.tipo().tipoBase().getText();
        String id = ctx.ID().getText();
        int dimensiones = ctx.tipo().CORCH_IZQ().size();
        int linea = ctx.ID().getSymbol().getLine();
        int columna = ctx.ID().getSymbol().getCharPositionInLine();
        TipoDato tipoNormalizado = ControlTipos.normalizarTipo(tipoStr);
        Simbolo nuevoSimbolo;
        if (dimensiones > 0) nuevoSimbolo = new SimboloArreglo(id, tipoNormalizado, linea, columna, dimensiones);
        else nuevoSimbolo = new SimboloVariable(id, tipoNormalizado, linea, columna);
        if (tipoNormalizado == TipoDato.OBJETO && nuevoSimbolo instanceof SimboloVariable) ((SimboloVariable) nuevoSimbolo).setReferenciaClase(tipoStr);
        if (tabla.buscar(id) != null) reportarError(linea, "La variable '" + id + "' ya ha sido declarada.");
        else
        {
            if (nuevoSimbolo instanceof SimboloVariable) ((SimboloVariable)nuevoSimbolo).setInicializado(false);
            tabla.insertar(nuevoSimbolo);
        }
        return null;
    }

    public Object procesarDeclConAsignacion(ZetarianoParser.DeclConAsignacionContext ctx)
    {
        String tipoStr = ctx.tipo().tipoBase().getText();
        String id = ctx.ID().getText();
        int dimensiones = ctx.tipo().CORCH_IZQ().size();
        int linea = ctx.ID().getSymbol().getLine();
        int columna = ctx.ID().getSymbol().getCharPositionInLine();
        TipoDato tipoNormalizado = ControlTipos.normalizarTipo(tipoStr);
        Simbolo nuevoSimbolo;
        if (dimensiones > 0) nuevoSimbolo = new SimboloArreglo(id, tipoNormalizado, linea, columna, dimensiones);
        else nuevoSimbolo = new SimboloVariable(id, tipoNormalizado, linea, columna);
        if (tipoNormalizado == TipoDato.OBJETO && nuevoSimbolo instanceof SimboloVariable) ((SimboloVariable) nuevoSimbolo).setReferenciaClase(tipoStr);
        if (tabla.buscar(id) != null) reportarError(linea, "La variable '" + id + "' ya ha sido declarada.");
        else
        {
            if (nuevoSimbolo instanceof SimboloVariable) ((SimboloVariable)nuevoSimbolo).setInicializado(true);
            tabla.insertar(nuevoSimbolo);
        }
        ResultadoC3D resExpr = (ResultadoC3D) visitor.visit(ctx.expresion());
        if (resExpr != null && resExpr.getTipo() != TipoDato.ERROR)
        {
            if (!ControlTipos.esAsignacionValida(tipoNormalizado, resExpr.getTipo()))
            {
                reportarError(linea, "Tipos incompatibles. No se puede asignar " + resExpr.getTipo() + " a una variable " + tipoNormalizado + ".");
            }
            else
            {
                if (nuevoSimbolo.isEnHeap())
                {
                    reportarError(linea, "Los atributos de clase no pueden inicializarse en la declaración. Deben ser inicializados dentro del constructor.");
                }
                else
                {
                    String tempIndice = generador.generarTemporal();
                    generador.agregarAsignacion(tempIndice, "punteroStack", "+", String.valueOf(nuevoSimbolo.getOffset()));
                    generador.agregarSetStack(tempIndice, resExpr.getValorC3D());
                }
            }
        }
        return null;
    }

    public Object procesarAsignacion(ZetarianoParser.AsignacionContext ctx)
    {
        String idVariable = ctx.acceso().ID(0).getText();
        Simbolo sim = tabla.buscar(idVariable);
        int linea = ctx.getStart().getLine();
        if (sim == null)
        {
            reportarError(linea, "La variable '" + idVariable + "' no ha sido declarada.");
            return null;
        }
        ResultadoC3D resExpr = (ResultadoC3D) visitor.visit(ctx.expresion());
        if (resExpr == null || resExpr.getTipo() == TipoDato.ERROR) return null;
        List<TerminalNode> ids = ctx.acceso().ID();
        if (ids.size() == 1)
        {
            if (!ControlTipos.esAsignacionValida(sim.getTipo(), resExpr.getTipo()))
            {
                reportarError(linea, "Tipos incompatibles para '" + idVariable + "'.");
            }
            else
            {
                if (sim instanceof SimboloVariable) ((SimboloVariable) sim).setInicializado(true);
                if (sim.isEnHeap())
                {
                    generador.agregarSetHeap(String.valueOf(sim.getOffset()), resExpr.getValorC3D());
                }
                else
                {
                    String tempIndice = generador.generarTemporal();
                    generador.agregarAsignacion(tempIndice, "punteroStack", "+", String.valueOf(sim.getOffset()));
                    generador.agregarSetStack(tempIndice, resExpr.getValorC3D());
                }
            }
        }
        else
        {
            ResultadoC3D resDireccion = GestorPunteros.obtenerPosicionAtributo(sim, ids, tabla, generador);
            if (resDireccion.getTipo() == TipoDato.ERROR)
            {
                reportarError(linea, "Acceso inválido a atributo en '" + idVariable + "'.");
                return null;
            }
            if (!ControlTipos.esAsignacionValida(resDireccion.getTipo(), resExpr.getTipo()))
            {
                reportarError(linea, "Tipos incompatibles de atributo.");
            }
            else
            {
                generador.agregarSetHeap(resDireccion.getValorC3D(), resExpr.getValorC3D());
            }
        }
        return null;
    }

    public Object procesarIncremento(ZetarianoParser.IncrementoContext ctx)
    {
        String idVariable = ctx.acceso().ID(0).getText();
        Simbolo sim = tabla.buscar(idVariable);
        int linea = ctx.getStart().getLine();
        if (sim == null)
        {
            reportarError(linea, "La variable '" + idVariable + "' no existe.");
            return null;
        }
        if (sim.getTipo() != TipoDato.ENTERO && sim.getTipo() != TipoDato.DECIMAL)
        {
            reportarError(linea, "Solo se pueden incrementar números.");
            return null;
        }
        String temporalAnterior = generador.generarTemporal();
        String temporalNuevo = generador.generarTemporal();
        String operador = ctx.MAS_MAS() != null ? "+" : "-";
        if (sim.isEnHeap())
        {
            generador.agregarGetHeap(temporalAnterior, String.valueOf(sim.getOffset()));
            generador.agregarAsignacion(temporalNuevo, temporalAnterior, operador, "1");
            generador.agregarSetHeap(String.valueOf(sim.getOffset()), temporalNuevo);
        }
        else
        {
            String tempIndice = generador.generarTemporal();
            generador.agregarAsignacion(tempIndice, "punteroStack", "+", String.valueOf(sim.getOffset()));
            generador.agregarGetStack(temporalAnterior, tempIndice);
            generador.agregarAsignacion(temporalNuevo, temporalAnterior, operador, "1");
            generador.agregarSetStack(tempIndice, temporalNuevo);
        }
        return null;
    }
}
