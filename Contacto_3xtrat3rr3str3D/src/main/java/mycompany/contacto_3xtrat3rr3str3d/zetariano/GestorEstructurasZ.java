package mycompany.contacto_3xtrat3rr3str3d.zetariano;

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

    private ZetarianoCustomVisitor v()
    {
        return (ZetarianoCustomVisitor) visitor;
    }
    
    public Object procesarClase(ZetarianoParser.ClaseContext ctx)
    {
        String id = ctx.ID().getText();
        int linea = ctx.ID().getSymbol().getLine();
        int columna = ctx.ID().getSymbol().getCharPositionInLine();
        SimboloClase simClase = new SimboloClase(id, linea, columna);
        tabla.insertar(simClase);
        int offsetAtributo = 0;
        for (ZetarianoParser.MiembroContext miembro : ctx.miembro())
        {
            if (miembro instanceof ZetarianoParser.MiembroDeclaracionContext declCtx)
            {
                String attrId = "";
                TipoDato attrTipo = TipoDato.ERROR;
                if (declCtx.declaracion() instanceof ZetarianoParser.DeclSinAsignarContext sinAsig)
                {
                    attrId = sinAsig.ID().getText();
                    attrTipo = ControlTipos.normalizarTipo(sinAsig.tipo().tipoBase().getText());
                }
                else if (declCtx.declaracion() instanceof ZetarianoParser.DeclConAsignacionContext conAsig)
                {
                    attrId = conAsig.ID().getText();
                    attrTipo = ControlTipos.normalizarTipo(conAsig.tipo().tipoBase().getText());
                }
                else if (declCtx.declaracion() instanceof ZetarianoParser.DeclArrayLiteralContext arrLit)
                {
                    attrId = arrLit.ID().getText();
                    attrTipo = ControlTipos.normalizarTipo(arrLit.tipo().tipoBase().getText());
                }
                if (!attrId.isEmpty())
                {
                    SimboloVariable simAttr = new SimboloVariable(attrId, attrTipo, linea, columna);
                    simAttr.setOffset(offsetAtributo++);
                    simAttr.setEnHeap(true);
                    simAttr.setInicializado(true);
                    simClase.getEntornoInterno().obtenerAmbitoActual().put(attrId, simAttr);
                }
            }
        }
        
        simClase.setTamañoHeapObjeto(offsetAtributo);
        v().getPilaClaseActual().push(id);
        try
        {
            for (ZetarianoParser.MiembroContext miembro : ctx.miembro())
            {
                if (!(miembro instanceof ZetarianoParser.MiembroDeclaracionContext))
                {
                    visitor.visit(miembro);
                }
            }
        }
        finally
        {
            v().getPilaClaseActual().pop();
        }
        
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
        int numParams = simMetodo.getParametros().size();
        if (tabla.existeFuncionConAridad(id, numParams)) reportarError(linea, columna, "El método " + id + " con " + numParams + " parámetro(s) ya existe.");
        else if (!tabla.existeFuncionBase(id))
        {
            simMetodo.setEtiquetaC3D(id);
            tabla.insertar(simMetodo);
        }
        else
        {
            simMetodo.setEtiquetaC3D(id + "_ar" + numParams);
            tabla.insertarConClave(id + "#" + numParams, simMetodo);
        }
        String etiquetaMetodo = simMetodo.getEtiquetaC3D();
        tabla.resetearOffsetLocal();
        tabla.entrarAmbito();
        if (!id.equals("principal"))
        {
            SimboloVariable simThis = new SimboloVariable("this", TipoDato.OBJETO, linea, columna);
            simThis.setInicializado(true);
            if (!v().getPilaClaseActual().isEmpty()) simThis.setReferenciaClase(v().getPilaClaseActual().peek());
            tabla.insertar(simThis);
        }
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
        if (id.equals("principal")) resultado = visitor.visit(ctx.bloqueMetodo());
        else
        {
            String etiquetaRetorno = generador.generarEtiqueta();
            v().getPilaReturn().push(etiquetaRetorno);
            try
            {
                generador.iniciarMetodo(etiquetaMetodo);
                resultado = visitor.visit(ctx.bloqueMetodo());
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
        int numParamsConst = simConstructor.getParametros().size();
        if (tabla.existeFuncionConAridad(idInterno, numParamsConst)) reportarError(linea, columna, "El constructor con " + numParamsConst + " parámetro(s) ya existe.");
        else if (!tabla.existeFuncionBase(idInterno))
        {
            simConstructor.setEtiquetaC3D(idInterno);
            tabla.insertar(simConstructor);
        }
        else
        {
            simConstructor.setEtiquetaC3D(idInterno + "_ar" + numParamsConst);
            tabla.insertarConClave(idInterno + "#" + numParamsConst, simConstructor);
        }
        String etiquetaConstructor = simConstructor.getEtiquetaC3D();
        tabla.resetearOffsetLocal();
        tabla.entrarAmbito();
        SimboloVariable simThis = new SimboloVariable("this", TipoDato.OBJETO, linea, columna);
        simThis.setInicializado(true);
        simThis.setReferenciaClase(id);
        tabla.insertar(simThis);
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
        String etiquetaRetorno = generador.generarEtiqueta();
        v().getPilaReturn().push(etiquetaRetorno);
        Object resultado;
        try
        {
            generador.iniciarMetodo(etiquetaConstructor);
            resultado = visitor.visit(ctx.bloqueMetodo());
            generador.agregarEtiqueta(etiquetaRetorno);
            generador.cerrarMetodo();
        }
        finally
        {
            v().getPilaReturn().pop();
        }
        tabla.salirAmbito();
        return resultado;
    }

    public Object procesarInstanciaObjeto(ZetarianoParser.InstanciaObjetoContext ctx)
    {
        String nombreClase = ctx.ID().getText();
        int linea = ctx.getStart().getLine();
        int columna = ctx.getStart().getCharPositionInLine();
        Simbolo simBuscado = tabla.buscar(nombreClase);
        if (simBuscado == null || !(simBuscado instanceof SimboloClase))
        {
            reportarError(linea, columna, "La clase " + nombreClase + " no está definida.");
            return new ResultadoC3D(TipoDato.ERROR, "");
        }
        ResultadoC3D resInstancia = GestorObjetos.instanciarObjetoEnHeap((SimboloClase) simBuscado, generador);
        String idConstructor = nombreClase + "_constructor";
        int numArgsConst = (ctx.argumentos() != null) ? ctx.argumentos().expresion().size() : 0;
        SimboloFuncion simConst = tabla.buscarFuncion(idConstructor, numArgsConst);
        if (simConst == null && tabla.existeFuncionBase(idConstructor))
        {
            reportarError(linea, columna, "No existe constructor de " + nombreClase + " con " + numArgsConst + " argumento(s).");
            return resInstancia;
        }
        if (simConst != null)
        {
            int tamañoEntornoActual = tabla.obtenerAmbitoActual().size() + 1;
            String tempPosThis = generador.generarTemporal();
            generador.agregarAsignacion(tempPosThis, "punteroStack", "+", String.valueOf(tamañoEntornoActual + 1));
            generador.agregarSetStack(tempPosThis, resInstancia.getValorC3D());
            if (ctx.argumentos() != null)
            {
                for (int i = 0; i < ctx.argumentos().expresion().size(); i++)
                {
                    ResultadoC3D resArg = (ResultadoC3D) visitor.visit(ctx.argumentos().expresion(i));
                    String tempPosArg = generador.generarTemporal();
                    int offsetDestino = i + 2;
                    generador.agregarAsignacion(tempPosArg, "punteroStack", "+", String.valueOf(tamañoEntornoActual + offsetDestino));
                    generador.agregarSetStack(tempPosArg, resArg.getValorC3D());
                }
            }
            generador.agregarComentario("Llamando constructor de la clase: " + nombreClase);
            generador.agregarAsignacion("punteroStack", "punteroStack", "+", String.valueOf(tamañoEntornoActual));
            generador.agregarLlamadaNativa("metodo_" + simConst.getEtiquetaC3D(), "");
            generador.agregarAsignacion("punteroStack", "punteroStack", "-", String.valueOf(tamañoEntornoActual));
        }
        return resInstancia;
    }
    
    public Object procesarLlamadaFuncionOMetodo(ZetarianoParser.LlamadaFuncionOMetodoContext ctx)
    {
        String idFuncion = ctx.acceso().ID(0).getText();
        int numArgs = (ctx.argumentos() != null) ? ctx.argumentos().expresion().size() : 0;
        SimboloFuncion funcion = tabla.buscarFuncion(idFuncion, numArgs);
        if (funcion == null)
        {
            if (tabla.existeFuncionBase(idFuncion)) reportarError(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(), "La función " + idFuncion + " no tiene sobrecarga con " + numArgs + " argumento(s).");
            else reportarError(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(), "La función " + idFuncion + " no existe.");
            return new ResultadoC3D(TipoDato.ERROR, "");
        }
        int tamañoEntornoActual = tabla.obtenerAmbitoActual().size() + 1;
        Simbolo simThis = tabla.buscar("this");
        if (simThis != null)
        {
            String tempThis = generador.generarTemporal();
            String tempPosThis = generador.generarTemporal();
            generador.agregarAsignacion(tempPosThis, "punteroStack", "+", String.valueOf(simThis.getOffset()));
            generador.agregarGetStack(tempThis, tempPosThis);
            String tempNuevaPosThis = generador.generarTemporal();
            generador.agregarAsignacion(tempNuevaPosThis, "punteroStack", "+", String.valueOf(tamañoEntornoActual + 1));
            generador.agregarSetStack(tempNuevaPosThis, tempThis);
        }
        if (ctx.argumentos() != null)
        {
            for (int i = 0; i < ctx.argumentos().expresion().size(); i++)
            {
                ResultadoC3D resArg = (ResultadoC3D) visitor.visit(ctx.argumentos().expresion(i));
                String tempPos = generador.generarTemporal();
                int offsetDestino = i + 2;
                generador.agregarAsignacion(tempPos, "punteroStack", "+", String.valueOf(tamañoEntornoActual + offsetDestino));
                generador.agregarSetStack(tempPos, resArg.getValorC3D());
            }
        }
        generador.agregarComentario("Inicio llamada a funcion: " + idFuncion);
        generador.agregarAsignacion("punteroStack", "punteroStack", "+", String.valueOf(tamañoEntornoActual));
        generador.agregarLlamadaNativa("metodo_" + funcion.getEtiquetaC3D(), "");
        String tempReturn = generador.generarTemporal();
        generador.agregarGetStack(tempReturn, "punteroStack");
        generador.agregarAsignacion("punteroStack", "punteroStack", "-", String.valueOf(tamañoEntornoActual));
        
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
                reportarError(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(), "Tipo de dato " + resExpr.getTipo() + " no soportado para impresión.");
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
        if (v().getPilaReturn().isEmpty()) generador.agregarCodigoBruto("    return;");
        else generador.agregarSaltoIncondicional(v().getPilaReturn().peek());
        return null;
    }
}
