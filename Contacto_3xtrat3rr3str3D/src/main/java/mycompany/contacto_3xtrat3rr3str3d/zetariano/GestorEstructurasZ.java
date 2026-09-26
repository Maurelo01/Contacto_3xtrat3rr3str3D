package mycompany.contacto_3xtrat3rr3str3d.zetariano;

import java.util.Map;
import mycompany.contacto_3xtrat3rr3str3d.ZetarianoParser;
import mycompany.contacto_3xtrat3rr3str3d.c3d.GeneradorFuncionesNativas;
import mycompany.contacto_3xtrat3rr3str3d.simbolos.*;
import mycompany.contacto_3xtrat3rr3str3d.utils.ControlTipos;
import mycompany.contacto_3xtrat3rr3str3d.utils.GestorObjetos;
import mycompany.contacto_3xtrat3rr3str3d.utils.ResultadoC3D;
import mycompany.contacto_3xtrat3rr3str3d.visitors.ZetarianoCustomVisitor;

public class GestorEstructurasZ extends ZetarianoGestorBase
{
    
    public GestorEstructurasZ(ZetarianoCustomVisitor visitor)
    {
        super(visitor);
    }
    
    public Object procesarClase(ZetarianoParser.ClaseContext ctx)
    {
        String id = ctx.ID().getText();
        int linea = ctx.ID().getSymbol().getLine();
        int columna = ctx.ID().getSymbol().getCharPositionInLine();
        SimboloClase simClase = new SimboloClase(id, linea, columna);
        int contadorAtributos = 0;
        for (ZetarianoParser.MiembroContext miembro : ctx.miembro())
        {
            if (miembro instanceof ZetarianoParser.MiembroDeclaracionContext)
            {
                contadorAtributos++;
            }
        }
        simClase.setTamañoHeapObjeto(contadorAtributos);
        tabla.insertar(simClase);
        tabla.resetearOffsetLocal();
        tabla.entrarAmbito();
        for (ZetarianoParser.MiembroContext miembro : ctx.miembro())
        {
            if (miembro instanceof ZetarianoParser.MiembroDeclaracionContext)
            {
                visitor.visit(miembro);
            }
        }
        Map<String, Simbolo> atributosLocales = tabla.obtenerAmbitoActual();
        for (Simbolo sim : atributosLocales.values())
        {
            simClase.getEntornoInterno().obtenerAmbitoActual().put(sim.getNombre(), sim);
        }
        for (ZetarianoParser.MiembroContext miembro : ctx.miembro())
        {
            if (!(miembro instanceof ZetarianoParser.MiembroDeclaracionContext))
            {
                visitor.visit(miembro);
            }
        }
        tabla.salirAmbito();
        return null;
    }

    public Object procesarMetodo(ZetarianoParser.MetodoContext ctx)
    {
        String tipoStr = ctx.tipo().tipoBase().getText();
        String id = ctx.ID().getText();
        int linea = ctx.ID().getSymbol().getLine();
        int columna = ctx.ID().getSymbol().getCharPositionInLine();
        TipoDato tipoNormalizado = ControlTipos.normalizarTipo(tipoStr);
        SimboloFuncion simMetodo = new SimboloFuncion(id, tipoNormalizado, linea, columna);
        if (ctx.parametros() != null)
        {
            for (ZetarianoParser.ParametroContext pCtx : ctx.parametros().parametro())
            {
                simMetodo.agregarParametro(new SimboloVariable(pCtx.ID().getText(), ControlTipos.normalizarTipo(pCtx.tipo().getText()), linea, columna));
            }
        }
        if (!tabla.insertar(simMetodo))
        {
            reportarError(linea, "El método '" + id + "' ya existe.");
        }
        tabla.resetearOffsetLocal();
        tabla.entrarAmbito();
        if (ctx.parametros() != null)
        {
            for (ZetarianoParser.ParametroContext pCtx : ctx.parametros().parametro())
            {
                String paramTipo = pCtx.tipo().getText();
                String paramId = pCtx.ID().getText();
                SimboloVariable simParam = new SimboloVariable(paramId, ControlTipos.normalizarTipo(paramTipo), pCtx.ID().getSymbol().getLine(), pCtx.ID().getSymbol().getCharPositionInLine());
                simParam.setInicializado(true);
                tabla.insertar(simParam);
            }
        }
        Object resultado = null;
        if (id.equals("principal"))
        {
            resultado = visitor.visit(ctx.bloqueMetodo());
        }
        else
        {
            generador.iniciarMetodo(id);
            resultado = visitor.visit(ctx.bloqueMetodo());
            generador.cerrarMetodo();
        }
        tabla.salirAmbito();
        return resultado;
    }

    public Object procesarConstructor(ZetarianoParser.ConstructorContext ctx)
    {
        String id = ctx.ID().getText();
        String idInterno = id + "_constructor";
        int linea = ctx.ID().getSymbol().getLine();
        int columna = ctx.ID().getSymbol().getCharPositionInLine();
        SimboloFuncion simConstructor = new SimboloFuncion(idInterno, TipoDato.OBJETO, linea, columna);
        if (ctx.parametros() != null)
        {
            for (ZetarianoParser.ParametroContext pCtx : ctx.parametros().parametro())
            {
                simConstructor.agregarParametro(new SimboloVariable(pCtx.ID().getText(), ControlTipos.normalizarTipo(pCtx.tipo().tipoBase().getText()), linea, columna));
            }
        }
        if (tabla.buscar(idInterno) != null)
        {
            reportarError(linea, "El constructor ya existe.");
        }
        else
        {
            tabla.insertar(simConstructor);
        }
        tabla.resetearOffsetLocal();
        tabla.entrarAmbito();
        if (ctx.parametros() != null)
        {
            for (ZetarianoParser.ParametroContext pCtx : ctx.parametros().parametro())
            {
                String paramTipo = pCtx.tipo().tipoBase().getText();
                String paramId = pCtx.ID().getText();
                SimboloVariable simParam = new SimboloVariable(paramId, ControlTipos.normalizarTipo(paramTipo), pCtx.ID().getSymbol().getLine(), pCtx.ID().getSymbol().getCharPositionInLine());
                simParam.setInicializado(true);
                tabla.insertar(simParam);
            }
        }
        Object resultado = visitor.visit(ctx.bloqueMetodo());
        tabla.salirAmbito();
        return resultado;
    }

    public Object procesarInstanciaObjeto(ZetarianoParser.InstanciaObjetoContext ctx)
    {
        String nombreClase = ctx.ID().getText();
        int linea = ctx.getStart().getLine();
        Simbolo simBuscado = tabla.buscar(nombreClase);
        if (simBuscado == null || !(simBuscado instanceof SimboloClase))
        {
            reportarError(linea, "La clase '" + nombreClase + "' no está definida.");
            return new ResultadoC3D(TipoDato.ERROR, "");
        }
        return GestorObjetos.instanciarObjetoEnHeap((SimboloClase) simBuscado, generador);
    }
    
    public Object procesarLlamadaFuncionOMetodo(ZetarianoParser.LlamadaFuncionOMetodoContext ctx)
    {
        String idFuncion = ctx.acceso().ID(0).getText();
        Simbolo sim = tabla.buscar(idFuncion);
        if (sim == null || !(sim instanceof SimboloFuncion))
        {
            reportarError(ctx.getStart().getLine(), "La función '" + idFuncion + "' no existe.");
            return new ResultadoC3D(TipoDato.ERROR, "");
        }
        SimboloFuncion funcion = (SimboloFuncion) sim;
        int tamañoEntornoActual = tabla.obtenerAmbitoActual().size() + 1;
        if (ctx.argumentos() != null)
        {
            for (int i = 0; i < ctx.argumentos().expresion().size(); i++)
            {
                ResultadoC3D resArg = (ResultadoC3D) visitor.visit(ctx.argumentos().expresion(i));
                String tempPos = generador.generarTemporal();
                int offsetDestino = i + 1;
                generador.agregarAsignacion(tempPos, "punteroStack", "+", String.valueOf(tamañoEntornoActual + offsetDestino));
                generador.agregarSetStack(tempPos, resArg.getValorC3D());
            }
        }
        generador.agregarComentario("Inicio llamada a funcion: " + idFuncion);
        generador.agregarAsignacion("punteroStack", "punteroStack", "+", String.valueOf(tamañoEntornoActual));
        generador.agregarLlamadaNativa("metodo_" + idFuncion, "");
        String tempReturn = generador.generarTemporal();
        generador.agregarGetStack(tempReturn, "punteroStack");
        generador.agregarAsignacion("punteroStack", "punteroStack", "-", String.valueOf(tamañoEntornoActual));
        generador.agregarComentario("Fin de llamada a: " + idFuncion);
        return new ResultadoC3D(funcion.getTipo(), tempReturn);
    }

    public Object procesarFuncionEspecial(ZetarianoParser.FuncionEspecialContext ctx)
    {
        if (ctx.expresion() != null)
        {
            ResultadoC3D resExpr = (ResultadoC3D) visitor.visit(ctx.expresion());
            if (resExpr.getTipo() == TipoDato.ENTERO || resExpr.getTipo() == TipoDato.BOOLEANO)
            {
                generador.agregarPrint("d", "(int)" + resExpr.getValorC3D());
            }
            else if (resExpr.getTipo() == TipoDato.DECIMAL)
            {
                generador.agregarPrint("f", resExpr.getValorC3D());
            }
            else if (resExpr.getTipo() == TipoDato.CADENA)
            {
                generador.agregarFuncionNativa(GeneradorFuncionesNativas.getNativaImprimirString());
                generador.agregarLlamadaNativa("nativa_imprimir_string", resExpr.getValorC3D());
            }
            else
            {
                reportarError(ctx.getStart().getLine(), "Tipo de dato " + resExpr.getTipo() + " no soportado para impresión.");
            }
        }
        if (ctx.PRINTLN() != null)
        {
            generador.agregarPrint("c", "10");
        }
        return null;
    }

    public Object procesarReturn(ZetarianoParser.InstruccionContext ctx)
    {
        if (ctx.expresion() != null)
        {
            ResultadoC3D resExpr = (ResultadoC3D) visitor.visit(ctx.expresion());
            if (resExpr != null && resExpr.getTipo() != TipoDato.ERROR)
            {
                generador.agregarSetStack("punteroStack", resExpr.getValorC3D());
            }
        }
        generador.agregarCodigoBruto("    return;");
        return null;
    }
}
