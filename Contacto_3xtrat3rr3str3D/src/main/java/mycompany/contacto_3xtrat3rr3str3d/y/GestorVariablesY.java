package mycompany.contacto_3xtrat3rr3str3d.y;

import java.util.ArrayList;
import java.util.List;
import mycompany.contacto_3xtrat3rr3str3d.YParser;
import mycompany.contacto_3xtrat3rr3str3d.simbolos.Simbolo;
import mycompany.contacto_3xtrat3rr3str3d.simbolos.SimboloArreglo;
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
        if (tabla.buscar(id) != null) reportarError(linea, columna,"La variable " + id + " ya fue declarada.");
        else
        {
            nuevoSimbolo.setInicializado(true);
            tabla.insertar(nuevoSimbolo);
        }
        ResultadoC3D resExpr = (ResultadoC3D) visitor.visit(ctx.expresion());
        if (resExpr != null && resExpr.getTipo() != TipoDato.ERROR)
        {
            if (!ControlTipos.esAsignacionValida(tipoNormalizado, resExpr.getTipo())) reportarError(linea, columna, "Tipos incompatibles. No se puede asignar " + resExpr.getTipo() + " a " + tipoNormalizado + ".");
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
        if (tabla.buscar(id) != null) reportarError(linea, columna, "La variable " + id + " ya ha sido declarada.");
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
            reportarError(linea, columna, "La variable " + id + " ya ha sido declarada.");
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
            reportarError(linea, columna, "La estructura " + tipoStr + " no existe.");
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
            reportarError(linea, columna, "La variable " + id + " ya ha sido declarada.");
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
        else reportarError(linea, columna, "La estructura " + tipoStr + " no existe.");
        return null;
    }
    
    public Object procesarAsignacion(YParser.AsignacionContext ctx)
    {
        boolean esAccesoSimple = ctx.acceso().getChildCount() == 1;
        ResultadoC3D resExpr = (ResultadoC3D) visitor.visit(ctx.expresion());
        if (resExpr == null || resExpr.getTipo() == TipoDato.ERROR) return null;
        if (esAccesoSimple)
        {
            String idVariable = ctx.acceso().ID(0).getText();
            Simbolo sim = tabla.buscar(idVariable);
            if (sim == null)
            {
                reportarError(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(), "Variable " + idVariable + " no declarada.");
                return null;
            }
            if (sim instanceof SimboloVariable simboloVariable) simboloVariable.setInicializado(true);
            if (sim.isEnHeap()) generador.agregarSetHeap(String.valueOf(sim.getOffset()), resExpr.getValorC3D());
            else
            {
                String tempIndice = generador.generarTemporal();
                generador.agregarAsignacion(tempIndice, "punteroStack", "+", String.valueOf(sim.getOffset()));
                generador.agregarSetStack(tempIndice, resExpr.getValorC3D());
            }
        }
        else
        {
            ResultadoC3D direccion = calcularDireccionAcceso(ctx.acceso());
            if (direccion.getTipo() != TipoDato.ERROR) generador.agregarSetHeap(direccion.getValorC3D(), resExpr.getValorC3D());
        }
        return null;
    }
    
    public Object procesarIncremento(YParser.IncrementoContext ctx)
    {
        String idVariable = ctx.acceso().ID(0).getText();
        Simbolo sim = tabla.buscar(idVariable);
        int linea = ctx.getStart().getLine();
        int columna = ctx.getStart().getCharPositionInLine();
        if (sim == null)
        {
            reportarError(linea, columna, "La variable " + idVariable + " no existe.");
            return null;
        }
        if (sim.getTipo() != TipoDato.ENTERO && sim.getTipo() != TipoDato.DECIMAL)
        {
            reportarError(linea, columna, "Solo se pueden incrementar números.");
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
    
    public Object procesarDeclArreglo(YParser.DeclArregloContext ctx)
    {
        String tipoStr = ctx.tipo().getText();
        String id = ctx.ID().getText();
        int linea = ctx.ID().getSymbol().getLine();
        int columna = ctx.ID().getSymbol().getCharPositionInLine();
        TipoDato tipoNormalizado = ControlTipos.normalizarTipo(tipoStr);
        int dimensiones = ctx.CORCH_IZQ().size();
        SimboloArreglo arreglo = new SimboloArreglo(id, tipoNormalizado, linea, columna, dimensiones);
        int tamañoTotal = 1;
        for (TerminalNode nodoNum : ctx.NUMERO())
        {
            int tam = Integer.parseInt(nodoNum.getText());
            arreglo.agregarTamañoDimension(tam);
            tamañoTotal *= tam;
        }
        arreglo.setTamañoTotal(tamañoTotal);
        arreglo.setInicializado(true);
        if (tabla.buscar(id) != null)
        {
            reportarError(linea, columna, "La variable " + id + " ya ha sido declarada.");
            return null;
        }
        tabla.insertar(arreglo);
        ResultadoC3D resInstancia = GestorArreglos.instanciarArregloVacio(tamañoTotal, tipoNormalizado, generador);
        if (arreglo.isEnHeap()) generador.agregarSetHeap(String.valueOf(arreglo.getOffset()), resInstancia.getValorC3D());
        else
        {
            String tempIndice = generador.generarTemporal();
            generador.agregarAsignacion(tempIndice, "punteroStack", "+", String.valueOf(arreglo.getOffset()));
            generador.agregarSetStack(tempIndice, resInstancia.getValorC3D());
        }
        return null;
    }
    
    public Object procesarDeclArregloLiteral(YParser.DeclArregloLiteralContext ctx)
    {
        String tipoStr = ctx.tipo().getText();
        String id = ctx.ID().getText();
        int linea = ctx.ID().getSymbol().getLine();
        int columna = ctx.ID().getSymbol().getCharPositionInLine();
        TipoDato tipoNormalizado = ControlTipos.normalizarTipo(tipoStr);
        List<ResultadoC3D> valores = new ArrayList<>();
        if (ctx.argumentos() != null)
        {
            for (YParser.ExpresionContext exprCtx : ctx.argumentos().expresion())
            {
                valores.add((ResultadoC3D) visitor.visit(exprCtx));
            }
        }
        SimboloArreglo arreglo = new SimboloArreglo(id, tipoNormalizado, linea, columna, ctx.CORCH_IZQ().size());
        arreglo.setTamañoTotal(valores.size());
        arreglo.agregarTamañoDimension(valores.size());
        arreglo.setInicializado(true);
        tabla.insertar(arreglo);
        ResultadoC3D resInstancia = GestorArreglos.instanciarArregloConValores(valores, tipoNormalizado, generador);
        if (arreglo.isEnHeap()) generador.agregarSetHeap(String.valueOf(arreglo.getOffset()), resInstancia.getValorC3D());
        else
        {
            String tempIndice = generador.generarTemporal();
            generador.agregarAsignacion(tempIndice, "punteroStack", "+", String.valueOf(arreglo.getOffset()));
            generador.agregarSetStack(tempIndice, resInstancia.getValorC3D());
        }
        return null;
    }
}
