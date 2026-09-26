package mycompany.contacto_3xtrat3rr3str3d.y;

import java.util.List;
import mycompany.contacto_3xtrat3rr3str3d.YParser;
import mycompany.contacto_3xtrat3rr3str3d.simbolos.Simbolo;
import mycompany.contacto_3xtrat3rr3str3d.simbolos.SimboloClase;
import mycompany.contacto_3xtrat3rr3str3d.simbolos.SimboloVariable;
import mycompany.contacto_3xtrat3rr3str3d.simbolos.TipoDato;
import mycompany.contacto_3xtrat3rr3str3d.utils.*;
import mycompany.contacto_3xtrat3rr3str3d.visitors.YCustomVisitor;
import org.antlr.v4.runtime.tree.TerminalNode;

public class GestorVariablesY extends YGestorBase
{
    public GestorVariablesY(YCustomVisitor visitor)
    {
        super(visitor);
    }
    
    public Object procesarDeclVariableAsig(YParser.DeclVariableAsigContext ctx)
    {
        String tipoStr = ctx.tipo().getText();
        String id = ctx.ID().getText();
        int linea = ctx.ID().getSymbol().getLine();
        int columna = ctx.ID().getSymbol().getCharPositionInLine();
        TipoDato tipoNormalizado = ControlTipos.normalizarTipo(tipoStr);
        SimboloVariable nuevoSimbolo = new SimboloVariable(id, tipoNormalizado, linea, columna);
        if (tabla.buscar(id) != null) reportarError(linea, "La variable '" + id + "' ya fue declarada.");
        else
        {
            nuevoSimbolo.setInicializado(true);
            tabla.insertar(nuevoSimbolo);
        }
        ResultadoC3D resExpr = (ResultadoC3D) visitor.visit(ctx.expresion());
        if (resExpr != null && resExpr.getTipo() != TipoDato.ERROR)
        {
            if (!ControlTipos.esAsignacionValida(tipoNormalizado, resExpr.getTipo())) reportarError(linea, "Tipos incompatibles. No se puede asignar " + resExpr.getTipo() + " a " + tipoNormalizado + ".");
            else
            {
                String tempIndice = generador.generarTemporal();
                generador.agregarAsignacion(tempIndice, "punteroStack", "+", String.valueOf(nuevoSimbolo.getOffset()));
                generador.agregarSetStack(tempIndice, resExpr.getValorC3D());
            }
        }
        return null;
    }
    
    public Object procesarDeclVariable(YParser.DeclVariableContext ctx)
    {
        String tipoStr = ctx.tipo().getText();
        String id = ctx.ID().getText();
        int linea = ctx.ID().getSymbol().getLine();
        int columna = ctx.ID().getSymbol().getCharPositionInLine();
        TipoDato tipoNormalizado = ControlTipos.normalizarTipo(tipoStr);
        SimboloVariable nuevoSimbolo = new SimboloVariable(id, tipoNormalizado, linea, columna);
        if (tabla.buscar(id) != null) reportarError(linea, "La variable '" + id + "' ya ha sido declarada.");
        else
        {
            nuevoSimbolo.setInicializado(false);
            tabla.insertar(nuevoSimbolo);
        }
        return null;
    }
    
    public Object procesarDeclEstructura(YParser.DeclEstructuraContext ctx)
    {
        String tipoStr = ctx.ID(0).getText();
        String id = ctx.ID(1).getText();
        int linea = ctx.ID(1).getSymbol().getLine();
        int columna = ctx.ID(1).getSymbol().getCharPositionInLine();
        SimboloVariable nuevoSimbolo = new SimboloVariable(id, TipoDato.OBJETO, linea, columna);
        nuevoSimbolo.setReferenciaClase(tipoStr);
        if (tabla.buscar(id) != null)
        {
            reportarError(linea, "La variable '" + id + "' ya ha sido declarada.");
            return null;
        }
        nuevoSimbolo.setInicializado(true);
        tabla.insertar(nuevoSimbolo);
        SimboloClase plantilla = (SimboloClase) tabla.buscar(tipoStr);
        if (plantilla != null)
        {
            ResultadoC3D resInstancia = GestorObjetos.instanciarObjetoEnHeap(plantilla, generador);
            String tempIndice = generador.generarTemporal();
            generador.agregarAsignacion(tempIndice, "punteroStack", "+", String.valueOf(nuevoSimbolo.getOffset()));
            generador.agregarSetStack(tempIndice, resInstancia.getValorC3D());
        }
        else
        {
            reportarError(linea, "La estructura '" + tipoStr + "' no existe.");
        }
        return null;
    }
    
    public Object procesarDeclEstructuraAsig(YParser.DeclEstructuraAsigContext ctx)
    {
        String tipoStr = ctx.ID(0).getText();
        String id = ctx.ID(1).getText();
        int linea = ctx.ID(1).getSymbol().getLine();
        int columna = ctx.ID(1).getSymbol().getCharPositionInLine();
        SimboloVariable nuevoSimbolo = new SimboloVariable(id, TipoDato.OBJETO, linea, columna);
        nuevoSimbolo.setReferenciaClase(tipoStr);
        if (tabla.buscar(id) != null)
        {
            reportarError(linea, "La variable '" + id + "' ya ha sido declarada.");
            return null;
        }
        nuevoSimbolo.setInicializado(true);
        tabla.insertar(nuevoSimbolo);
        SimboloClase plantilla = (SimboloClase) tabla.buscar(tipoStr);
        if (plantilla != null)
        {
            ResultadoC3D resInstancia = GestorObjetos.instanciarObjetoEnHeap(plantilla, generador);
            String tempIndice = generador.generarTemporal();
            generador.agregarAsignacion(tempIndice, "punteroStack", "+", String.valueOf(nuevoSimbolo.getOffset()));
            generador.agregarSetStack(tempIndice, resInstancia.getValorC3D());
            if (ctx.argumentos() != null)
            {
                List<YParser.ExpresionContext> args = ctx.argumentos().expresion();
                for (int i = 0; i < args.size(); i++)
                {
                    ResultadoC3D resArg = (ResultadoC3D) visitor.visit(args.get(i));
                    String tempPosAttr = generador.generarTemporal();
                    generador.agregarAsignacion(tempPosAttr, resInstancia.getValorC3D(), "+", String.valueOf(i));
                    generador.agregarSetHeap(tempPosAttr, resArg.getValorC3D());
                }
            }
        }
        else reportarError(linea, "La estructura '" + tipoStr + "' no existe.");
        return null;
    }
    
    public Object procesarAsignacion(YParser.AsignacionContext ctx)
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
                if (sim instanceof SimboloVariable) ((SimboloVariable) sim).setInicializado(true);
                String tempIndice = generador.generarTemporal();
                generador.agregarAsignacion(tempIndice, "punteroStack", "+", String.valueOf(sim.getOffset()));
                generador.agregarSetStack(tempIndice, resExpr.getValorC3D());
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
    
    public Object procesarIncremento(YParser.IncrementoContext ctx)
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
        String tempIndice = generador.generarTemporal();
        generador.agregarAsignacion(tempIndice, "punteroStack", "+", String.valueOf(sim.getOffset()));
        generador.agregarGetStack(temporalAnterior, tempIndice);
        generador.agregarAsignacion(temporalNuevo, temporalAnterior, operador, "1");
        generador.agregarSetStack(tempIndice, temporalNuevo);
        return null;
    }
}
