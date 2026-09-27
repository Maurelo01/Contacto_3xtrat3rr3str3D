package mycompany.contacto_3xtrat3rr3str3d.zetariano;

import java.util.ArrayList;
import java.util.List;
import mycompany.contacto_3xtrat3rr3str3d.ZetarianoParser;
import mycompany.contacto_3xtrat3rr3str3d.simbolos.*;
import mycompany.contacto_3xtrat3rr3str3d.utils.ControlTipos;
import mycompany.contacto_3xtrat3rr3str3d.utils.GestorArreglos;
import mycompany.contacto_3xtrat3rr3str3d.utils.ResultadoC3D;
import mycompany.contacto_3xtrat3rr3str3d.visitors.ZetarianoCustomVisitor;

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
        Simbolo nuevoSimbolo = (dimensiones > 0) ? new SimboloArreglo(id, tipoNormalizado, linea, columna, dimensiones) : new SimboloVariable(id, tipoNormalizado, linea, columna);
        if (tipoNormalizado == TipoDato.OBJETO && nuevoSimbolo instanceof SimboloVariable) ((SimboloVariable) nuevoSimbolo).setReferenciaClase(tipoStr);
        if (tabla.buscar(id) != null) reportarError(linea, columna, "La variable " + id + " ya declarada.");
        else tabla.insertar(nuevoSimbolo);
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
        Simbolo nuevoSimbolo = (dimensiones > 0) ? new SimboloArreglo(id, tipoNormalizado, linea, columna, dimensiones) : new SimboloVariable(id, tipoNormalizado, linea, columna);
        if (tipoNormalizado == TipoDato.OBJETO && nuevoSimbolo instanceof SimboloVariable) ((SimboloVariable) nuevoSimbolo).setReferenciaClase(tipoStr);
        if (tabla.buscar(id) != null) reportarError(linea, columna, "Variable " + id + " ya declarada.");
        else
        {
            if (nuevoSimbolo instanceof SimboloVariable) ((SimboloVariable)nuevoSimbolo).setInicializado(true);
            else if (nuevoSimbolo instanceof SimboloArreglo) ((SimboloArreglo)nuevoSimbolo).setInicializado(true);
            tabla.insertar(nuevoSimbolo);
        }
        ResultadoC3D resExpr = (ResultadoC3D) visitor.visit(ctx.expresion());
        if (resExpr != null && resExpr.getTipo() != TipoDato.ERROR)
        {
            if (nuevoSimbolo instanceof SimboloArreglo arr && !resExpr.getTamañosDimensiones().isEmpty())
            {
                for (int t : resExpr.getTamañosDimensiones()) arr.agregarTamañoDimension(t);
                arr.setTamañoTotal(resExpr.getTamañosDimensiones().stream().reduce(1, (a, b) -> a * b));
            }
            
            if (nuevoSimbolo.isEnHeap()) reportarError(linea, columna, "Atributos de clase no pueden inicializarse aquí. Use constructor.");
            else
            {
                String tempIndice = generador.generarTemporal();
                generador.agregarAsignacion(tempIndice, "punteroStack", "+", String.valueOf(nuevoSimbolo.getOffset()));
                generador.agregarSetStack(tempIndice, resExpr.getValorC3D());
            }
        }
        return null;
    }
    
    public Object procesarDeclArrayLiteral(ZetarianoParser.DeclArrayLiteralContext ctx)
    {
        String tipoStr = ctx.tipo().tipoBase().getText();
        String id = ctx.ID().getText();
        int linea = ctx.ID().getSymbol().getLine();
        int columna = ctx.ID().getSymbol().getCharPositionInLine();
        TipoDato tipoNormalizado = ControlTipos.normalizarTipo(tipoStr);
        List<ResultadoC3D> valores = new ArrayList<>();
        if (ctx.argumentos() != null)
        {
            for (ZetarianoParser.ExpresionContext exprCtx : ctx.argumentos().expresion())
            {
                valores.add((ResultadoC3D) visitor.visit(exprCtx));
            }
        }
        int dimensiones = ctx.tipo().CORCH_IZQ().size();
        SimboloArreglo arreglo = new SimboloArreglo(id, tipoNormalizado, linea, columna, dimensiones);
        arreglo.setTamañoTotal(valores.size());
        arreglo.agregarTamañoDimension(valores.size());
        arreglo.setInicializado(true);
        tabla.insertar(arreglo);
        ResultadoC3D resInstancia = GestorArreglos.instanciarArregloConValores(valores, tipoNormalizado, generador);
        String tempIndice = generador.generarTemporal();
        generador.agregarAsignacion(tempIndice, "punteroStack", "+", String.valueOf(arreglo.getOffset()));
        generador.agregarSetStack(tempIndice, resInstancia.getValorC3D());
        return null;
    }

    public Object procesarAsignacion(ZetarianoParser.AsignacionContext ctx)
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
                Simbolo attrThis = resolverAtributoDeThis(idVariable);
                if (attrThis != null)
                {
                    Simbolo simThis = tabla.buscar("this");
                    generador.agregarComentario("Asignación implícita a this." + idVariable + "");
                    String tempThis = generador.generarTemporal();
                    String tempPosThis = generador.generarTemporal();
                    String tempDirFinal = generador.generarTemporal();
                    generador.agregarAsignacion(tempPosThis, "punteroStack", "+", String.valueOf(simThis.getOffset()));
                    generador.agregarGetStack(tempThis, tempPosThis);
                    generador.agregarAsignacion(tempDirFinal, tempThis, "+", String.valueOf(attrThis.getOffset()));
                    generador.agregarSetHeap(tempDirFinal, resExpr.getValorC3D());
                    return null;
                }
                reportarError(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(), "Variable no declarada.");
                return null;
            }
            if (sim instanceof SimboloVariable simboloVariable) simboloVariable.setInicializado(true);
            if (tabla.esGlobal(idVariable)) 
            {
                Simbolo simThis = tabla.buscar("this");
                if (simThis != null) 
                {
                    generador.agregarComentario("Asignación implícita a this." + idVariable + "");
                    String tempThis = generador.generarTemporal();
                    String tempPosThis = generador.generarTemporal();
                    String tempDirFinal = generador.generarTemporal();
                    generador.agregarAsignacion(tempPosThis, "punteroStack", "+", String.valueOf(simThis.getOffset()));
                    generador.agregarGetStack(tempThis, tempPosThis);
                    generador.agregarAsignacion(tempDirFinal, tempThis, "+", String.valueOf(sim.getOffset()));
                    generador.agregarSetHeap(tempDirFinal, resExpr.getValorC3D());
                    return null;
                }
            }
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

    public Object procesarIncremento(ZetarianoParser.IncrementoContext ctx)
    {
        String idVariable = ctx.acceso().ID(0).getText();
        Simbolo sim = tabla.buscar(idVariable);
        int linea = ctx.getStart().getLine();
        int columna = ctx.getStart().getCharPositionInLine();
        if (sim == null)
        {
            reportarError(linea, columna,"La variable " + idVariable + " no existe.");
            return null;
        }
        if (sim.getTipo() != TipoDato.ENTERO && sim.getTipo() != TipoDato.DECIMAL)
        {
            reportarError(linea, columna,"Solo se pueden incrementar números.");
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
