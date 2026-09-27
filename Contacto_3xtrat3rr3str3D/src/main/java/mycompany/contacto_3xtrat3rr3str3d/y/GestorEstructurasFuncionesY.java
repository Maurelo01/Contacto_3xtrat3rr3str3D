package mycompany.contacto_3xtrat3rr3str3d.y;

import java.util.Map;
import mycompany.contacto_3xtrat3rr3str3d.YParser;
import mycompany.contacto_3xtrat3rr3str3d.c3d.GeneradorFuncionesNativas;
import mycompany.contacto_3xtrat3rr3str3d.simbolos.*;
import mycompany.contacto_3xtrat3rr3str3d.utils.ControlTipos;
import mycompany.contacto_3xtrat3rr3str3d.utils.ResultadoC3D;
import mycompany.contacto_3xtrat3rr3str3d.visitors.YCustomVisitor;

public class GestorEstructurasFuncionesY extends YGestorBase
{
    public GestorEstructurasFuncionesY(YCustomVisitor visitor)
    {
        super(visitor);
    }

    private YCustomVisitor v()
    {
        return (YCustomVisitor) visitor;
    }
    
    public Object procesarDefinicionFuncion(YParser.DefinicionFuncionContext ctx)
    {
        String id = ctx.ID().getText();
        int linea = ctx.ID().getSymbol().getLine();
        int columna = ctx.ID().getSymbol().getCharPositionInLine();
        String tipoStr = (ctx.FLECHA() != null) ? ctx.tipo().getText() : "VOID";
        TipoDato tipoNormalizado = tipoStr.equals("VOID") ? TipoDato.VOID : ControlTipos.normalizarTipo(tipoStr);
        SimboloFuncion simMetodo = new SimboloFuncion(id, tipoNormalizado, linea, columna);
        if (ctx.parametros() != null)
        {
            for (YParser.ParametroContext pCtx : ctx.parametros().parametro())
            {
                String paramId = "";
                String paramTipoStr = "";
                if (pCtx instanceof YParser.ParamValorContext paramValorContext)
                {
                    paramId = paramValorContext.ID().getText();
                    paramTipoStr = paramValorContext.tipo().getText();
                }
                else if (pCtx instanceof YParser.ParamArregloRefContext paramArregloRefContext)
                {
                    paramId = paramArregloRefContext.ID().getText();
                    paramTipoStr = paramArregloRefContext.tipo().getText();
                }
                else if (pCtx instanceof YParser.ParamStructRefContext paramStructRefContext)
                {
                    paramId = paramStructRefContext.ID(1).getText();
                    paramTipoStr = paramStructRefContext.ID(0).getText();
                }
                simMetodo.agregarParametro(new SimboloVariable(paramId, ControlTipos.normalizarTipo(paramTipoStr), linea, columna));
            }
        }
        if (!tabla.insertar(simMetodo)) reportarError(linea, columna,"La función " + id + " ya existe.");
        tabla.resetearOffsetLocal();
        tabla.entrarAmbito();
        if (ctx.parametros() != null)
        {
            for (YParser.ParametroContext pCtx : ctx.parametros().parametro())
            {
                String paramId = "";
                String paramTipoStr = "";
                if (pCtx instanceof YParser.ParamValorContext)
                {
                    paramId = ((YParser.ParamValorContext) pCtx).ID().getText();
                    paramTipoStr = ((YParser.ParamValorContext) pCtx).tipo().getText();
                }
                else if (pCtx instanceof YParser.ParamArregloRefContext paramArregloRefContext)
                {
                    paramId = paramArregloRefContext.ID().getText();
                    paramTipoStr = paramArregloRefContext.tipo().getText();
                }
                else if (pCtx instanceof YParser.ParamStructRefContext paramStructRefContext)
                {
                    paramId = paramStructRefContext.ID(1).getText();
                    paramTipoStr = paramStructRefContext.ID(0).getText();
                }
                SimboloVariable simParam = new SimboloVariable(paramId, ControlTipos.normalizarTipo(paramTipoStr), linea, columna);
                simParam.setInicializado(true);
                if (ControlTipos.normalizarTipo(paramTipoStr) == TipoDato.OBJETO) simParam.setReferenciaClase(paramTipoStr);
                tabla.insertar(simParam);
            }
        }
        Object resultado = null;
        if (id.equals("principal") || id.equals("main")) resultado = visitor.visit(ctx.bloque());
        else
        {
            String etiquetaRetorno = generador.generarEtiqueta();
            v().getPilaReturn().push(etiquetaRetorno);
            try
            {
                generador.iniciarMetodo(id);
                resultado = visitor.visit(ctx.bloque());
                generador.agregarEtiqueta(etiquetaRetorno);
                generador.cerrarMetodo();
            }
            finally
            {
                v().getPilaReturn().pop();
            }
        }
        tabla.salirAmbito();
        return resultado;
    }
    
    public Object procesarDefinicionEstructura(YParser.DefinicionEstructuraContext ctx)
    {
        String id = ctx.ID().getText();
        int linea = ctx.ID().getSymbol().getLine();
        int columna = ctx.ID().getSymbol().getCharPositionInLine();
        SimboloClase simEstructura = new SimboloClase(id, linea, columna);
        tabla.insertar(simEstructura);
        tabla.resetearOffsetLocal();
        tabla.entrarAmbito();
        int offsetEstructura = 0;
        for (YParser.AtributoEstructuraContext atributo : ctx.atributoEstructura())
        {
            if (atributo instanceof YParser.AtributoNormalContext attrNorm)
            {
                String attrId = attrNorm.ID().getText();
                String attrTipo = attrNorm.tipo().getText();
                TipoDato tipoNorm = ControlTipos.normalizarTipo(attrTipo);
                if (attrNorm.NUMERO() != null)
                { 
                    int tamArreglo = Integer.parseInt(attrNorm.NUMERO().getText());
                    SimboloArreglo simArr = new SimboloArreglo(attrId, tipoNorm, linea, columna, 1);
                    simArr.setTamañoTotal(tamArreglo);
                    simArr.agregarTamañoDimension(tamArreglo);
                    simArr.setOffset(offsetEstructura);
                    offsetEstructura += tamArreglo;
                    tabla.insertar(simArr);
                }
                else
                {
                    SimboloVariable simAttr = new SimboloVariable(attrId, tipoNorm, linea, columna);
                    simAttr.setOffset(offsetEstructura++);
                    tabla.insertar(simAttr);
                }
            }
            else if (atributo instanceof YParser.AtributoEstructuraAnidadaContext attrAnidado)
            {
                String attrTipo = attrAnidado.ID(0).getText();
                String attrId = attrAnidado.ID(1).getText();
                SimboloVariable simAttr = new SimboloVariable(attrId, TipoDato.OBJETO, linea, columna);
                simAttr.setReferenciaClase(attrTipo);
                simAttr.setOffset(offsetEstructura++);
                tabla.insertar(simAttr);
            }
        }
        simEstructura.setTamañoHeapObjeto(offsetEstructura);
        Map<String, Simbolo> atributosLocales = tabla.obtenerAmbitoActual();
        for (Simbolo sim : atributosLocales.values())
        {
            simEstructura.getEntornoInterno().obtenerAmbitoActual().put(sim.getNombre(), sim);
        }
        tabla.salirAmbito();
        return null;
    }
    
    public Object procesarInstruccionImprimir(YParser.InstruccionImprimirContext ctx)
    {
        if (ctx.expresion() != null)
        {
            ResultadoC3D resExpr = (ResultadoC3D) visitor.visit(ctx.expresion());
            if (resExpr != null)
            {
                if (resExpr.getTipo() == TipoDato.ENTERO || resExpr.getTipo() == TipoDato.BOOLEANO) generador.agregarPrint("d", "(int)" + resExpr.getValorC3D());
                else if (resExpr.getTipo() == TipoDato.DECIMAL) generador.agregarPrint("f", resExpr.getValorC3D());
                else if (resExpr.getTipo() == TipoDato.CADENA)
                {
                    generador.agregarFuncionNativa(GeneradorFuncionesNativas.getNativaImprimirString());
                    generador.agregarLlamadaNativa("nativa_imprimir_string", resExpr.getValorC3D());
                }
                else reportarError(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(),"Tipo " + resExpr.getTipo() + " no soportado para impresión.");
            }
        }
        generador.agregarPrint("c", "10"); 
        return null;
    }
    
    public Object procesarInstruccionRetornar(YParser.InstruccionRetornarContext ctx)
    {
        if (ctx.expresion() != null)
        {
            ResultadoC3D resExpr = (ResultadoC3D) visitor.visit(ctx.expresion());
            if (resExpr != null && resExpr.getTipo() != TipoDato.ERROR) generador.agregarSetStack("punteroStack", resExpr.getValorC3D());
        }
        if (v().getPilaReturn().isEmpty()) generador.agregarCodigoBruto("    return;");
        else generador.agregarSaltoIncondicional(v().getPilaReturn().peek());
        return null;
    }

    public Object procesarLlamadaFuncion(YParser.LlamadaFuncionContext ctx)
    {
        String idFuncion = ctx.ID().getText();
        Simbolo sim = tabla.buscar(idFuncion);
        if (sim == null || !(sim instanceof SimboloFuncion))
        {
            reportarError(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(),"La función " + idFuncion + " no existe.");
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
}
