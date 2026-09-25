package mycompany.contacto_3xtrat3rr3str3d.visitors;

import java.util.List;
import java.util.Map;
import mycompany.contacto_3xtrat3rr3str3d.simbolos.*;
import mycompany.contacto_3xtrat3rr3str3d.c3d.GeneradorFuncionesNativas;
import mycompany.contacto_3xtrat3rr3str3d.c3d.GeneradorC3D;
import mycompany.contacto_3xtrat3rr3str3d.ZetarianoBaseVisitor;
import mycompany.contacto_3xtrat3rr3str3d.ZetarianoParser;
import javax.swing.JTextArea;
import mycompany.contacto_3xtrat3rr3str3d.utils.*;
import org.antlr.v4.runtime.tree.TerminalNode;

public class ZetarianoCustomVisitor extends ZetarianoBaseVisitor<Object>
{
    private TablaSimbolos tabla;
    private JTextArea consola;
    public boolean hayErroresSemanticos = false;
    private GeneradorC3D generador = GeneradorC3D.getInstancia();
    public ZetarianoCustomVisitor(TablaSimbolos tabla, JTextArea consola)
    {
        this.tabla = tabla;
        this.consola = consola;
    }
    @Override
    public Object visitClase(ZetarianoParser.ClaseContext ctx)
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
                visit(miembro);
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
                visit(miembro);
            }
        }
        tabla.salirAmbito();
        return null;
    }
    @Override
    public Object visitMetodo(ZetarianoParser.MetodoContext ctx)
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
            consola.append("Error Semántico en línea " + linea + ", columna " + columna + ": El método '" + id + "' ya existe.\n");
            hayErroresSemanticos = true;
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
        Object resultado = visit(ctx.bloqueMetodo());
        tabla.salirAmbito();
        return resultado;
    }
    
    @Override
    public Object visitBloque(ZetarianoParser.BloqueContext ctx)
    {
        tabla.entrarAmbito();
        Object resultado = super.visitBloque(ctx);
        tabla.salirAmbito();
        return resultado;
    }
    
    @Override
    public Object visitDeclSinAsignar(ZetarianoParser.DeclSinAsignarContext ctx)
    {
        String tipoStr = ctx.tipo().tipoBase().getText();
        String id = ctx.ID().getText();
        int dimensiones = ctx.tipo().CORCH_IZQ().size();
        String categoria = (dimensiones > 0) ? "Arreglo" : "Variable";
        int linea = ctx.ID().getSymbol().getLine();
        int columna = ctx.ID().getSymbol().getCharPositionInLine();
        TipoDato tipoNormalizado = ControlTipos.normalizarTipo(tipoStr);
        Simbolo nuevoSimbolo;
        if (dimensiones > 0)
        {
            nuevoSimbolo = new SimboloArreglo(id, tipoNormalizado, linea, columna, dimensiones);
        }
        else
        {
            nuevoSimbolo = new SimboloVariable(id, tipoNormalizado, linea, columna);
        }
        if (tipoNormalizado == TipoDato.OBJETO && nuevoSimbolo instanceof SimboloVariable) ((SimboloVariable) nuevoSimbolo).setReferenciaClase(tipoStr);
        if (tabla.buscar(id) != null)
        {
            consola.append("Error Semántico en línea " + linea + ", columna " + columna + ": La variable '" + id + "' ya ha sido declarada.\n");
            hayErroresSemanticos = true;
        }
        else
        {
            if (nuevoSimbolo instanceof SimboloVariable) ((SimboloVariable)nuevoSimbolo).setInicializado(false);
            tabla.insertar(nuevoSimbolo);
        }
        return null;
    }
    
    @Override
    public Object visitDeclConAsignacion(ZetarianoParser.DeclConAsignacionContext ctx)
    {
        String tipoStr = ctx.tipo().tipoBase().getText();
        String id = ctx.ID().getText();
        int dimensiones = ctx.tipo().CORCH_IZQ().size();
        String categoria = (dimensiones > 0) ? "Arreglo" : "Variable";
        int linea = ctx.ID().getSymbol().getLine();
        int columna = ctx.ID().getSymbol().getCharPositionInLine();
        TipoDato tipoNormalizado = ControlTipos.normalizarTipo(tipoStr);
        Simbolo nuevoSimbolo;
        if (dimensiones > 0)
        {
            nuevoSimbolo = new SimboloArreglo(id, tipoNormalizado, linea, columna, dimensiones);
        }
        else
        {
            nuevoSimbolo = new SimboloVariable(id, tipoNormalizado, linea, columna);
        }
        if (tipoNormalizado == TipoDato.OBJETO && nuevoSimbolo instanceof SimboloVariable) ((SimboloVariable) nuevoSimbolo).setReferenciaClase(tipoStr);
        if (tabla.buscar(id) != null)
        {
            consola.append("Error Semántico en línea " + linea + ", columna " + columna + ": La variable '" + id + "' ya ha sido declarada.\n");
            hayErroresSemanticos = true;
        }
        else
        {
            if(nuevoSimbolo instanceof SimboloVariable) ((SimboloVariable)nuevoSimbolo).setInicializado(true);
            tabla.insertar(nuevoSimbolo);
        }
        ResultadoC3D resExpr = (ResultadoC3D) visit(ctx.expresion());
        if (resExpr != null && resExpr.getTipo() != TipoDato.ERROR)
        {
            if (!ControlTipos.esAsignacionValida(tipoNormalizado, resExpr.getTipo()))
            {
                consola.append("Error Semántico en línea " + linea + ": Tipos incompatibles. No se puede asignar " + resExpr.getTipo() + " a una variable " + tipoNormalizado + ".\n");
                hayErroresSemanticos = true;
            }
            else
            {
                if (nuevoSimbolo.isEnHeap())
                {
                    generador.agregarSetHeap(String.valueOf(nuevoSimbolo.getOffset()), resExpr.getValorC3D());
                }
                else
                {
                    generador.agregarSetStack(String.valueOf(nuevoSimbolo.getOffset()), resExpr.getValorC3D());
                }
            }
        }
        return null;
    }

    @Override
    public Object visitConstructor(ZetarianoParser.ConstructorContext ctx)
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
            consola.append("Error Semántico en línea " + linea + ", columna " + columna + ": El constructor ya existe.\n");
            hayErroresSemanticos = true;
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
        Object resultado = visit(ctx.bloqueMetodo());
        tabla.salirAmbito();
        return resultado;
    }
    
    @Override
    public Object visitNumLiteral(ZetarianoParser.NumLiteralContext ctx)
    {
        return new ResultadoC3D(TipoDato.ENTERO, ctx.getText());
    }
    @Override
    public Object visitDecLiteral(ZetarianoParser.DecLiteralContext ctx)
    {
        return new ResultadoC3D(TipoDato.DECIMAL, ctx.getText());
    }
    @Override
    public Object visitTextoLiteral(ZetarianoParser.TextoLiteralContext ctx)
    {
        String texto = ctx.getText();
        return GestorCadenas.guardarCadenaEnHeap(texto, generador);
    }
    @Override
    public Object visitCharLiteral(ZetarianoParser.CharLiteralContext ctx)
    {
        return new ResultadoC3D(TipoDato.CARACTER, ctx.getText());
    }
    @Override
    public Object visitTrueLiteral(ZetarianoParser.TrueLiteralContext ctx)
    {
        return new ResultadoC3D(TipoDato.BOOLEANO, "1");
    }
    @Override
    public Object visitFalseLiteral(ZetarianoParser.FalseLiteralContext ctx)
    {
        return new ResultadoC3D(TipoDato.BOOLEANO, "0");
    }

    @Override
    public Object visitAccesoVariableOAtributo(ZetarianoParser.AccesoVariableOAtributoContext ctx)
    {
        String idVariable = ctx.acceso().ID(0).getText();
        Simbolo sim = tabla.buscar(idVariable);
        int linea = ctx.acceso().ID(0).getSymbol().getLine();
        int columna = ctx.acceso().ID(0).getSymbol().getCharPositionInLine();
        if (sim == null)
        {
            consola.append("Error Semántico en línea " + linea + ": La variable '" + idVariable + "' no ha sido declarada.\n");
            hayErroresSemanticos = true;
            return new ResultadoC3D(TipoDato.ERROR, "");
        }
        List<TerminalNode> ids = ctx.acceso().ID();
        if (ids.size() == 1)
        {
            if (sim instanceof SimboloVariable && !((SimboloVariable)sim).isInicializado() && !sim.isEnHeap())
            {
                consola.append("Error Semántico en línea " + linea + ": La variable local '" + idVariable + "' podría no haber sido inicializada.\n");
                hayErroresSemanticos = true;
                return new ResultadoC3D(TipoDato.ERROR, "");
            }
            String temporal = generador.generarTemporal();
            if (sim.isEnHeap())
            {
                generador.agregarGetHeap(temporal, String.valueOf(sim.getOffset()));
            }
            else
            {
                generador.agregarGetStack(temporal, String.valueOf(sim.getOffset()));
            }
            return new ResultadoC3D(sim.getTipo(), temporal);
        }
        else
        {
            ResultadoC3D resDireccion = GestorPunteros.obtenerPosicionAtributo(sim, ids, tabla, generador);
            if (resDireccion.getTipo() == TipoDato.ERROR)
            {
                consola.append("Error Semántico en línea " + linea + ": Acceso a atributo inválido en '" + idVariable + "'.\n");
                hayErroresSemanticos = true;
                return new ResultadoC3D(TipoDato.ERROR, "");
            }
            String temporalValor = generador.generarTemporal();
            generador.agregarGetHeap(temporalValor, resDireccion.getValorC3D());
            return new ResultadoC3D(resDireccion.getTipo(), temporalValor);
        }
    }

    @Override
    public Object visitSumaResta(ZetarianoParser.SumaRestaContext ctx)
    {
        ResultadoC3D izq = (ResultadoC3D) visit(ctx.expresion(0));
        ResultadoC3D der = (ResultadoC3D) visit(ctx.expresion(1));
        if (izq.getTipo() == TipoDato.ERROR || der.getTipo() == TipoDato.ERROR) return new ResultadoC3D(TipoDato.ERROR, "");
        TipoDato tipoResultado;
        String operador = ctx.MAS() != null ? "+" : "-";
        if (ctx.MAS() != null)
        {
            tipoResultado = ControlTipos.resolverSuma(izq.getTipo(), der.getTipo());
        }
        else
        {
            tipoResultado = ControlTipos.resolverAritmetica(izq.getTipo(), der.getTipo());
        }
        if (tipoResultado == TipoDato.ERROR)
        {
            int linea = ctx.getStart().getLine();
            consola.append("Error Semántico en línea " + linea + ": Incompatibilidad de tipos en la operación (" + izq.getTipo() + " " + operador + " " + der.getTipo() + ").\n");
            hayErroresSemanticos = true;
            return new ResultadoC3D(TipoDato.ERROR, "");
        }
        String temporal = generador.generarTemporal();
        generador.agregarAsignacion(temporal, izq.getValorC3D(), operador, der.getValorC3D());
        return new ResultadoC3D(tipoResultado, temporal);
    }
    
    @Override
    public Object visitMultDiv(ZetarianoParser.MultDivContext ctx)
    {
        ResultadoC3D izq = (ResultadoC3D) visit(ctx.expresion(0));
        ResultadoC3D der = (ResultadoC3D) visit(ctx.expresion(1));
        if (izq.getTipo() == TipoDato.ERROR || der.getTipo() == TipoDato.ERROR)
        {
            return new ResultadoC3D(TipoDato.ERROR, "");
        }
        TipoDato tipoResultado = ControlTipos.resolverAritmetica(izq.getTipo(), der.getTipo());
        if (tipoResultado == TipoDato.ERROR)
        {
            int linea = ctx.getStart().getLine();
            consola.append("Error Semántico en línea " + linea + ": Incompatibilidad de tipos (" + izq.getTipo() + " y " + der.getTipo() + ").\n");
            hayErroresSemanticos = true;
            return new ResultadoC3D(TipoDato.ERROR, "");
        }
        String temporal = generador.generarTemporal();
        String operador = ctx.POR() != null ? "*" : (ctx.DIVISION() != null ? "/" : "%");
        if (operador.equals("%"))
        {
            generador.agregarAsignacion(temporal, "fmod(" + izq.getValorC3D() + ", " + der.getValorC3D() + ")");
        }
        else
        {
            generador.agregarAsignacion(temporal, izq.getValorC3D(), operador, der.getValorC3D());
        }
        return new ResultadoC3D(tipoResultado, temporal);
    }

    @Override
    public Object visitComparacion(ZetarianoParser.ComparacionContext ctx)
    {
        ResultadoC3D izq = (ResultadoC3D) visit(ctx.expresion(0));
        ResultadoC3D der = (ResultadoC3D) visit(ctx.expresion(1));
        if (izq.getTipo() == TipoDato.ERROR || der.getTipo() == TipoDato.ERROR) return new ResultadoC3D(TipoDato.ERROR, "");
        TipoDato resultado = ControlTipos.resolverRelacional(izq.getTipo(), der.getTipo());
        if (resultado == TipoDato.ERROR)
        {
            int linea = ctx.getStart().getLine();
            consola.append("Error Semántico en línea " + linea + ": No se pueden comparar relacionalmente los tipos " + izq.getTipo() + " y " + der.getTipo() + ".\n");
            hayErroresSemanticos = true;
            return new ResultadoC3D(TipoDato.ERROR, "");
        }
        String operador = ctx.MAYOR() != null ? ">" : ctx.MAYOR_IGUAL() != null ? ">=" : ctx.MENOR() != null ? "<" : "<=";
        String temporal = generador.generarTemporal();
        String etVerdadera = generador.generarEtiqueta();
        String etFalsa = generador.generarEtiqueta();
        String etSalida = generador.generarEtiqueta();
        generador.agregarSaltoCondicional(izq.getValorC3D(), operador, der.getValorC3D(), etVerdadera);
        generador.agregarSaltoIncondicional(etFalsa);
        generador.agregarEtiqueta(etVerdadera);
        generador.agregarAsignacion(temporal, "1");
        generador.agregarSaltoIncondicional(etSalida);
        generador.agregarEtiqueta(etFalsa);
        generador.agregarAsignacion(temporal, "0");
        generador.agregarEtiqueta(etSalida);
        return new ResultadoC3D(resultado, temporal);
    }

    @Override
    public Object visitIgualdad(ZetarianoParser.IgualdadContext ctx)
    {
        ResultadoC3D izq = (ResultadoC3D) visit(ctx.expresion(0));
        ResultadoC3D der = (ResultadoC3D) visit(ctx.expresion(1));
        if (izq.getTipo() == TipoDato.ERROR || der.getTipo() == TipoDato.ERROR) return new ResultadoC3D(TipoDato.ERROR, "");
        TipoDato resultado = ControlTipos.resolverIgualdad(izq.getTipo(), der.getTipo());
        if (resultado == TipoDato.ERROR)
        {
            int linea = ctx.getStart().getLine();
            consola.append("Error Semántico en línea " + linea + ": No se puede evaluar igualdad entre " + izq.getTipo() + " y " + der.getTipo() + ".\n");
            hayErroresSemanticos = true;
            return new ResultadoC3D(TipoDato.ERROR, "");
        }
        String operador = ctx.IGUALIGUAL() != null ? "==" : "!=";
        String temporal = generador.generarTemporal();
        String etVerdadera = generador.generarEtiqueta();
        String etFalsa = generador.generarEtiqueta();
        String etSalida = generador.generarEtiqueta();
        generador.agregarSaltoCondicional(izq.getValorC3D(), operador, der.getValorC3D(), etVerdadera);
        generador.agregarSaltoIncondicional(etFalsa);
        generador.agregarEtiqueta(etVerdadera);
        generador.agregarAsignacion(temporal, "1");
        generador.agregarSaltoIncondicional(etSalida);
        generador.agregarEtiqueta(etFalsa);
        generador.agregarAsignacion(temporal, "0");
        generador.agregarEtiqueta(etSalida);
        return new ResultadoC3D(resultado, temporal);
    }

    @Override
    public Object visitAndLogico(ZetarianoParser.AndLogicoContext ctx)
    {
        String temporal = generador.generarTemporal();
        String etFalsa = generador.generarEtiqueta();
        String etVerdadera = generador.generarEtiqueta();
        String etSalida = generador.generarEtiqueta();
        ResultadoC3D izq = (ResultadoC3D) visit(ctx.expresion(0));
        if (izq.getTipo() == TipoDato.ERROR)
        {
            return new ResultadoC3D(TipoDato.ERROR, "");
        }
        generador.agregarSaltoCondicional(izq.getValorC3D(), "==", "0", etFalsa);
        ResultadoC3D der = (ResultadoC3D) visit(ctx.expresion(1));
        if (der.getTipo() == TipoDato.ERROR)
        {
            return new ResultadoC3D(TipoDato.ERROR, "");
        }
        TipoDato resultado = ControlTipos.resolverLogica(izq.getTipo(), der.getTipo());
        if (resultado == TipoDato.ERROR)
        {
            consola.append("Error Semántico en línea " + ctx.getStart().getLine() + ": El operador && requiere booleanos.\n");
            hayErroresSemanticos = true;
            return new ResultadoC3D(TipoDato.ERROR, "");
        }
        generador.agregarSaltoCondicional(der.getValorC3D(), "==", "1", etVerdadera);
        generador.agregarSaltoIncondicional(etFalsa);
        generador.agregarEtiqueta(etVerdadera);
        generador.agregarAsignacion(temporal, "1");
        generador.agregarSaltoIncondicional(etSalida);
        generador.agregarEtiqueta(etFalsa);
        generador.agregarAsignacion(temporal, "0");
        generador.agregarEtiqueta(etSalida);
        return new ResultadoC3D(resultado, temporal);
    }

    @Override
    public Object visitOrLogico(ZetarianoParser.OrLogicoContext ctx)
    {
        String temporal = generador.generarTemporal();
        String etFalsa = generador.generarEtiqueta();
        String etVerdadera = generador.generarEtiqueta();
        String etSalida = generador.generarEtiqueta();
        ResultadoC3D izq = (ResultadoC3D) visit(ctx.expresion(0));
        if (izq.getTipo() == TipoDato.ERROR)
        {
            return new ResultadoC3D(TipoDato.ERROR, "");
        }
        generador.agregarSaltoCondicional(izq.getValorC3D(), "==", "1", etVerdadera);
        ResultadoC3D der = (ResultadoC3D) visit(ctx.expresion(1));
        if (der.getTipo() == TipoDato.ERROR)
        {
            return new ResultadoC3D(TipoDato.ERROR, "");
        }
        TipoDato resultado = ControlTipos.resolverLogica(izq.getTipo(), der.getTipo());
        if (resultado == TipoDato.ERROR)
        {
            consola.append("Error Semántico en línea " + ctx.getStart().getLine() + ": El operador || requiere booleanos.\n");
            hayErroresSemanticos = true;
            return new ResultadoC3D(TipoDato.ERROR, "");
        }
        generador.agregarSaltoCondicional(der.getValorC3D(), "==", "1", etVerdadera);
        generador.agregarSaltoIncondicional(etFalsa);
        generador.agregarEtiqueta(etVerdadera);
        generador.agregarAsignacion(temporal, "1");
        generador.agregarSaltoIncondicional(etSalida);
        generador.agregarEtiqueta(etFalsa);
        generador.agregarAsignacion(temporal, "0");
        generador.agregarEtiqueta(etSalida);
        return new ResultadoC3D(resultado, temporal);
    }

    @Override
    public Object visitNegacionUnaria(ZetarianoParser.NegacionUnariaContext ctx)
    {
        ResultadoC3D tipo = (ResultadoC3D) visit(ctx.factor());
        TipoDato resultado = ControlTipos.resolverUnariaAritmetica(tipo.getTipo());
        if (resultado == TipoDato.ERROR)
        {
            hayErroresSemanticos = true;
            return new ResultadoC3D(TipoDato.ERROR, "");
        }
        String temporal = generador.generarTemporal();
        generador.agregarAsignacion(temporal, "-" + tipo.getValorC3D());
        return new ResultadoC3D(resultado, temporal);
    }

    @Override
    public Object visitNegacionLogica(ZetarianoParser.NegacionLogicaContext ctx)
    {
        ResultadoC3D tipo = (ResultadoC3D) visit(ctx.factor());
        TipoDato resultado = ControlTipos.resolverUnariaLogica(tipo.getTipo());
        if (resultado == TipoDato.ERROR)
        {
            consola.append("Error Semántico en línea " + ctx.getStart().getLine() + ": No se puede aplicar negación lógica a un tipo " + tipo.getTipo() + ".\n");
            hayErroresSemanticos = true;
            return new ResultadoC3D(TipoDato.ERROR, "");
        }
        String temporal = generador.generarTemporal();
        generador.agregarAsignacion(temporal, "1", "-", tipo.getValorC3D());
        return new ResultadoC3D(resultado, temporal);
    }

    @Override
    public Object visitParentesis(ZetarianoParser.ParentesisContext ctx)
    {
        return visit(ctx.expresion());
    }

    @Override
    public Object visitAsignacion(ZetarianoParser.AsignacionContext ctx)
    {
        String idVariable = ctx.acceso().ID(0).getText();
        Simbolo sim = tabla.buscar(idVariable);
        int linea = ctx.getStart().getLine();
        if (sim == null)
        {
            consola.append("Error Semántico en línea " + linea + ": La variable '" + idVariable + "' no ha sido declarada.\n");
            hayErroresSemanticos = true;
            return null;
        }
        ResultadoC3D resExpr = (ResultadoC3D) visit(ctx.expresion());
        if (resExpr == null || resExpr.getTipo() == TipoDato.ERROR) return null;
        List<TerminalNode> ids = ctx.acceso().ID();
        if (ids.size() == 1)
        {
            if (!ControlTipos.esAsignacionValida(sim.getTipo(), resExpr.getTipo()))
            {
                consola.append("Error Semántico en línea " + linea + ": Tipos incompatibles para '" + idVariable + "'.\n");
                hayErroresSemanticos = true;
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
                    generador.agregarSetStack(String.valueOf(sim.getOffset()), resExpr.getValorC3D());
                }
            }
        }
        else
        {
            ResultadoC3D resDireccion = GestorPunteros.obtenerPosicionAtributo(sim, ids, tabla, generador);
            if (resDireccion.getTipo() == TipoDato.ERROR)
            {
                consola.append("Error Semántico en línea " + linea + ": Acceso inválido a atributo en '" + idVariable + "'.\n");
                hayErroresSemanticos = true;
                return null;
            }
            if (!ControlTipos.esAsignacionValida(resDireccion.getTipo(), resExpr.getTipo()))
            {
                consola.append("Error Semántico en línea " + linea + ": Tipos incompatibles de atributo.\n");
                hayErroresSemanticos = true;
            }
            else
            {
                generador.agregarSetHeap(resDireccion.getValorC3D(), resExpr.getValorC3D());
            }
        }
        return null;
    }
    
    @Override
    public Object visitCondicional(ZetarianoParser.CondicionalContext ctx)
    {
        String etiquetaSalida = generador.generarEtiqueta();
        for (int i = 0; i < ctx.expresion().size(); i++)
        {
            ResultadoC3D resCondicion = (ResultadoC3D) visit(ctx.expresion(i));
            if (resCondicion.getTipo() != TipoDato.BOOLEANO && resCondicion.getTipo() != TipoDato.ERROR)
            {
                consola.append("Error Semántico en línea " + ctx.getStart().getLine() + ": La condición del IF debe ser booleana.\n");
                hayErroresSemanticos = true;
            }
            String etiquetaFalsa = generador.generarEtiqueta();
            generador.agregarSaltoCondicional(resCondicion.getValorC3D(), "==", "0", etiquetaFalsa);
            visit(ctx.bloque(i));
            generador.agregarSaltoIncondicional(etiquetaSalida);
            generador.agregarEtiqueta(etiquetaFalsa);
        }
        if (ctx.ELSE() != null)
        {
            visit(ctx.bloque(ctx.bloque().size() - 1));
        }
        generador.agregarEtiqueta(etiquetaSalida);
        return null;
    }
    
    @Override
    public Object visitBucleWhile(ZetarianoParser.BucleWhileContext ctx)
    {
        String etiquetaInicio = generador.generarEtiqueta();
        String etiquetaSalida = generador.generarEtiqueta();
        generador.agregarEtiqueta(etiquetaInicio);
        ResultadoC3D resCondicion = (ResultadoC3D) visit(ctx.expresion());
        if (resCondicion.getTipo() != TipoDato.BOOLEANO && resCondicion.getTipo() != TipoDato.ERROR)
        {
            consola.append("Error Semántico en línea " + ctx.getStart().getLine() + ": La condición del WHILE debe ser booleana.\n");
            hayErroresSemanticos = true;
        }
        generador.agregarSaltoCondicional(resCondicion.getValorC3D(), "==", "0", etiquetaSalida);
        visit(ctx.bloque());
        generador.agregarSaltoIncondicional(etiquetaInicio);
        generador.agregarEtiqueta(etiquetaSalida);
        return null;
    }
    
    @Override
    public Object visitBucleDoWhile(ZetarianoParser.BucleDoWhileContext ctx)
    {
        String etiquetaInicio = generador.generarEtiqueta();
        generador.agregarEtiqueta(etiquetaInicio);
        visit(ctx.bloqueMetodo());
        ResultadoC3D resCondicion = (ResultadoC3D) visit(ctx.expresion());
        if (resCondicion.getTipo() != TipoDato.BOOLEANO && resCondicion.getTipo() != TipoDato.ERROR)
        {
            consola.append("Error Semántico en línea " + ctx.getStart().getLine() + ": La condición del DO WHILE debe ser booleana.\n");
            hayErroresSemanticos = true;
        }
        generador.agregarSaltoCondicional(resCondicion.getValorC3D(), "==", "1", etiquetaInicio);
        return null;
    }
    
    @Override
    public Object visitBucleFor(ZetarianoParser.BucleForContext ctx)
    {
        tabla.entrarAmbito();
        if (ctx.declaracionFor() != null)
        {
            visit(ctx.declaracionFor());
        }
        String etiquetaInicio = generador.generarEtiqueta();
        String etiquetaSalida = generador.generarEtiqueta();
        generador.agregarEtiqueta(etiquetaInicio);
        if (ctx.expresion() != null)
        {
            ResultadoC3D resCondicion = (ResultadoC3D) visit(ctx.expresion());
            if (resCondicion.getTipo() != TipoDato.BOOLEANO && resCondicion.getTipo() != TipoDato.ERROR)
            {
                consola.append("Error Semántico en línea " + ctx.getStart().getLine() + ": La condición del FOR debe ser booleana.\n");
                hayErroresSemanticos = true;
            }
            generador.agregarSaltoCondicional(resCondicion.getValorC3D(), "==", "0", etiquetaSalida);
        }
        visit(ctx.bloque());
        if (ctx.actualizacionFor() != null)
        {
            visit(ctx.actualizacionFor());
        }
        generador.agregarSaltoIncondicional(etiquetaInicio);
        generador.agregarEtiqueta(etiquetaSalida);
        tabla.salirAmbito();
        return null;
    }
    
    @Override
    public Object visitDeclaracionFor(ZetarianoParser.DeclaracionForContext ctx)
    {
        String tipoStr = ctx.tipo().tipoBase().getText();
        String id = ctx.ID().getText();
        int linea = ctx.ID().getSymbol().getLine();
        int columna = ctx.ID().getSymbol().getCharPositionInLine();
        TipoDato tipoNormalizado = ControlTipos.normalizarTipo(tipoStr);
        SimboloVariable nuevoSimbolo = new SimboloVariable(id, tipoNormalizado, linea, columna);
        nuevoSimbolo.setInicializado(true);
        tabla.insertar(nuevoSimbolo);
        ResultadoC3D resExpr = (ResultadoC3D) visit(ctx.expresion());
        if (resExpr != null && resExpr.getTipo() != TipoDato.ERROR)
        {
            if (!ControlTipos.esAsignacionValida(tipoNormalizado, resExpr.getTipo()))
            {
                consola.append("Error Semántico en línea " + linea + ": Tipos incompatibles en FOR.\n");
                hayErroresSemanticos = true;
            }
            else
            {
                if (nuevoSimbolo.isEnHeap())
                {
                    generador.agregarSetHeap(String.valueOf(nuevoSimbolo.getOffset()), resExpr.getValorC3D());
                }
                else 
                {
                    generador.agregarSetStack(String.valueOf(nuevoSimbolo.getOffset()), resExpr.getValorC3D());
                }
            }
        }
        return null;
    }
    
    @Override
    public Object visitCondicionalSwitch(ZetarianoParser.CondicionalSwitchContext ctx)
    {
        ResultadoC3D resVariable = (ResultadoC3D) visit(ctx.expresion());
        String etiquetaSalida = generador.generarEtiqueta();
        for (ZetarianoParser.CasoContext casoCtx : ctx.caso())
        {
            ResultadoC3D resCaso = (ResultadoC3D) visit(casoCtx.expresion());
            String etiquetaBloque = generador.generarEtiqueta();
            String etiquetaSiguiente = generador.generarEtiqueta();
            generador.agregarSaltoCondicional(resVariable.getValorC3D(), "==", resCaso.getValorC3D(), etiquetaBloque);
            generador.agregarSaltoIncondicional(etiquetaSiguiente);
            generador.agregarEtiqueta(etiquetaBloque);
            for (ZetarianoParser.InstruccionContext inst : casoCtx.instruccion())
            {
                visit(inst);
            }
            generador.agregarSaltoIncondicional(etiquetaSalida);
            generador.agregarEtiqueta(etiquetaSiguiente);
        }
        if (ctx.casoDefault() != null)
        {
            for (ZetarianoParser.InstruccionContext inst : ctx.casoDefault().instruccion())
            {
                visit(inst);
            }
        }
        generador.agregarEtiqueta(etiquetaSalida);
        return null;
    }
    
    @Override
    public Object visitIncremento(ZetarianoParser.IncrementoContext ctx)
    {
        String idVariable = ctx.acceso().ID(0).getText();
        Simbolo sim = tabla.buscar(idVariable);
        int linea = ctx.getStart().getLine();
        if (sim == null)
        {
            consola.append("Error Semántico en línea " + linea + ": La variable '" + idVariable + "' no existe.\n");
            hayErroresSemanticos = true;
            return null;
        }
        if (!sim.getTipo().equals(TipoDato.ENTERO.name()) && !sim.getTipo().equals(TipoDato.DECIMAL.name()))
        {
            consola.append("Error Semántico en línea " + linea + ": Solo se pueden incrementar números.\n");
            hayErroresSemanticos = true;
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
            generador.agregarGetStack(temporalAnterior, String.valueOf(sim.getOffset()));
            generador.agregarAsignacion(temporalNuevo, temporalAnterior, operador, "1");
            generador.agregarSetStack(String.valueOf(sim.getOffset()), temporalNuevo);
        }
        return null;
    }
    
    @Override
    public Object visitFuncionEspecial(ZetarianoParser.FuncionEspecialContext ctx)
    {
        if (ctx.expresion() != null)
        {
            ResultadoC3D resExpr = (ResultadoC3D) visit(ctx.expresion());
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
                consola.append("Error Semántico en línea " + ctx.getStart().getLine() + ": Tipo de dato " + resExpr.getTipo() + " no soportado para impresión.\n");
                hayErroresSemanticos = true;
            }
        }
        if (ctx.PRINTLN() != null)
        {
            generador.agregarPrint("c", "10"); 
        }
        return null;
    }
    
    @Override
    public Object visitInstanciaObjeto(ZetarianoParser.InstanciaObjetoContext ctx)
    {
        String nombreClase = ctx.ID().getText();
        int linea = ctx.getStart().getLine();
        Simbolo simBuscado = tabla.buscar(nombreClase);
        if (simBuscado == null || !(simBuscado instanceof SimboloClase))
        {
            consola.append("Error Semántico en línea " + linea + ": La clase '" + nombreClase + "' no está definida.\n");
            hayErroresSemanticos = true;
            return new ResultadoC3D(TipoDato.ERROR, "");
        }
        SimboloClase clase = (SimboloClase) simBuscado;
        return GestorObjetos.instanciarObjetoEnHeap(clase, generador);
    }
}
