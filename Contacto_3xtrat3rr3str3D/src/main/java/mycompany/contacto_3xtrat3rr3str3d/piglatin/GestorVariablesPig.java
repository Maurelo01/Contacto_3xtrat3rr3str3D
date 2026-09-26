package mycompany.contacto_3xtrat3rr3str3d.piglatin;

import java.util.List;
import mycompany.contacto_3xtrat3rr3str3d.PigLatinParser;
import mycompany.contacto_3xtrat3rr3str3d.simbolos.*;
import mycompany.contacto_3xtrat3rr3str3d.utils.*;
import mycompany.contacto_3xtrat3rr3str3d.visitors.PigLatinCustomVisitor;
import org.antlr.v4.runtime.tree.TerminalNode;

public class GestorVariablesPig extends PigLatinGestorBase
{
    public GestorVariablesPig(PigLatinCustomVisitor visitor)
    {
        super(visitor);
    }
    
    public Object procesarDeclaracionVariable(PigLatinParser.DeclaracionVariableContext ctx)
    {
        String id = ctx.ID().getText();
        String tipoStr = ctx.tipo().getText();
        int linea = ctx.ID().getSymbol().getLine();
        int columna = ctx.ID().getSymbol().getCharPositionInLine();
        TipoDato tipoNormalizado = ControlTipos.normalizarTipo(tipoStr);
        SimboloVariable nuevoSimbolo = new SimboloVariable(id, tipoNormalizado, linea, columna);
        if (tabla.buscar(id) != null)
        {
            reportarError(linea, "La variable '" + id + "' ya ha sido declarada.");
            return null;
        }
        nuevoSimbolo.setInicializado(ctx.valorInicial() != null);
        if (tipoNormalizado == TipoDato.OBJETO) nuevoSimbolo.setReferenciaClase(tipoStr);
        tabla.insertar(nuevoSimbolo);
        if (ctx.valorInicial() != null)
        {
            ResultadoC3D resExpr = (ResultadoC3D) visitor.visit(ctx.valorInicial());
            if (resExpr != null && resExpr.getTipo() != TipoDato.ERROR)
            {
                if (!ControlTipos.esAsignacionValida(tipoNormalizado, resExpr.getTipo())) reportarError(linea, "No se puede asignar " + resExpr.getTipo() + " a " + tipoNormalizado + ".");
                else
                {
                    if (nuevoSimbolo.isEnHeap()) generador.agregarSetHeap(String.valueOf(nuevoSimbolo.getOffset()), resExpr.getValorC3D());
                    else
                    {
                        String tempIndice = generador.generarTemporal();
                        generador.agregarAsignacion(tempIndice, "punteroStack", "+", String.valueOf(nuevoSimbolo.getOffset()));
                        generador.agregarSetStack(tempIndice, resExpr.getValorC3D());
                    }
                }
            }
        }
        return null;
    }
    
    public Object procesarDeclaracionEstructura(PigLatinParser.DeclaracionEstructuraContext ctx)
    {
        String id = ctx.ID().getText();
        String tipoStr = ctx.tipo().getText();
        int linea = ctx.ID().getSymbol().getLine();
        int columna = ctx.ID().getSymbol().getCharPositionInLine();
        SimboloVariable nuevoSimbolo = new SimboloVariable(id, TipoDato.OBJETO, linea, columna);
        if (tabla.buscar(id) != null)
        {
            reportarError(linea, "La variable '" + id + "' ya ha sido declarada.");
            return null;
        }
        nuevoSimbolo.setReferenciaClase(tipoStr);
        nuevoSimbolo.setInicializado(true);
        tabla.insertar(nuevoSimbolo);
        SimboloClase plantilla = (SimboloClase) tabla.buscar(tipoStr);
        if (plantilla != null)
        {
            ResultadoC3D resInstancia = GestorObjetos.instanciarObjetoEnHeap(plantilla, generador);
            if (nuevoSimbolo.isEnHeap()) generador.agregarSetHeap(String.valueOf(nuevoSimbolo.getOffset()), resInstancia.getValorC3D());
            else
            {
                String tempIndice = generador.generarTemporal();
                generador.agregarAsignacion(tempIndice, "punteroStack", "+", String.valueOf(nuevoSimbolo.getOffset()));
                generador.agregarSetStack(tempIndice, resInstancia.getValorC3D());
            }
            if (ctx.agrupacionValores().argumentos() != null)
            {
                List<PigLatinParser.ExpresionContext> args = ctx.agrupacionValores().argumentos().expresion();
                for (int i = 0; i < args.size(); i++)
                {
                    ResultadoC3D resArg = (ResultadoC3D) visitor.visit(args.get(i));
                    String tempPosAttr = generador.generarTemporal();
                    generador.agregarAsignacion(tempPosAttr, resInstancia.getValorC3D(), "+", String.valueOf(i));
                    generador.agregarSetHeap(tempPosAttr, resArg.getValorC3D());
                }
            }
        }
        else reportarError(linea, "La estructura o clase importada '" + tipoStr + "' no existe.");
        return null;
    }
    
    public Object procesarDeclaracionObjeto(PigLatinParser.DeclaracionObjetoContext ctx)
    {
        String idVariable = ctx.ID(0).getText();
        String tipoStr = ctx.ID(1).getText();
        int linea = ctx.ID(0).getSymbol().getLine();
        int columna = ctx.ID(0).getSymbol().getCharPositionInLine();
        SimboloVariable nuevoSimbolo = new SimboloVariable(idVariable, TipoDato.OBJETO, linea, columna);
        if (tabla.buscar(idVariable) != null)
        {
            reportarError(linea, "La variable '" + idVariable + "' ya ha sido declarada.");
            return null;
        }
        nuevoSimbolo.setReferenciaClase(tipoStr);
        nuevoSimbolo.setInicializado(true);
        tabla.insertar(nuevoSimbolo);
        SimboloClase plantilla = (SimboloClase) tabla.buscar(tipoStr);
        if (plantilla != null)
        {
            ResultadoC3D resInstancia = GestorObjetos.instanciarObjetoEnHeap(plantilla, generador);
            if (nuevoSimbolo.isEnHeap()) generador.agregarSetHeap(String.valueOf(nuevoSimbolo.getOffset()), resInstancia.getValorC3D());
            else
            {
                String tempIndice = generador.generarTemporal();
                generador.agregarAsignacion(tempIndice, "punteroStack", "+", String.valueOf(nuevoSimbolo.getOffset()));
                generador.agregarSetStack(tempIndice, resInstancia.getValorC3D());
            }
        }
        else reportarError(linea, "La clase importada '" + tipoStr + "' no existe.");
        return null;
    }
    
    public Object procesarAsigGeneral(PigLatinParser.AsigGeneralContext ctx)
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
            if (!ControlTipos.esAsignacionValida(sim.getTipo(), resExpr.getTipo())) reportarError(linea, "Tipos incompatibles para '" + idVariable + "'.");
            else
            {
                if (sim instanceof SimboloVariable simboloVariable) simboloVariable.setInicializado(true);
                if (sim.isEnHeap()) generador.agregarSetHeap(String.valueOf(sim.getOffset()), resExpr.getValorC3D());
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
            if (!ControlTipos.esAsignacionValida(resDireccion.getTipo(), resExpr.getTipo())) reportarError(linea, "Tipos incompatibles de atributo.");
            else generador.agregarSetHeap(resDireccion.getValorC3D(), resExpr.getValorC3D());
        }
        return null;
    }
    
    public Object procesarIncrementoGeneral(PigLatinParser.IncrementoGeneralContext ctx)
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
        String operador = ctx.MAS_ABREVIADO() != null ? "+" : "-";
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
