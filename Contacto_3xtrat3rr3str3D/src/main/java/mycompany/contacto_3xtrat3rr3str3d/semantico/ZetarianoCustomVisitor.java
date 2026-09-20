package mycompany.contacto_3xtrat3rr3str3d.semantico;

import mycompany.contacto_3xtrat3rr3str3d.ZetarianoBaseVisitor;
import mycompany.contacto_3xtrat3rr3str3d.ZetarianoParser;
import javax.swing.JTextArea;

public class ZetarianoCustomVisitor extends ZetarianoBaseVisitor<Object>
{
    private TablaSimbolos tabla;
    private JTextArea consola;
    public boolean hayErroresSemanticos = false;
    private GeneradorC3D gen = GeneradorC3D.getInstancia();
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
        Simbolo simClase = new Simbolo(id, TipoDato.OBJETO.name(), "Clase", linea, columna);
        tabla.insertar(simClase);
        return super.visitClase(ctx);
    }
    @Override
    public Object visitMetodo(ZetarianoParser.MetodoContext ctx)
    {
        String tipoStr = ctx.tipo().tipoBase().getText();
        String id = ctx.ID().getText();
        int linea = ctx.ID().getSymbol().getLine();
        int columna = ctx.ID().getSymbol().getCharPositionInLine();
        TipoDato tipoNormalizado = ControlTipos.normalizarTipo(tipoStr);
        Simbolo simMetodo = new Simbolo(id, tipoNormalizado.name(), "Metodo", linea, columna);
        if (ctx.parametros() != null)
        {
            for (ZetarianoParser.ParametroContext pCtx : ctx.parametros().parametro())
            {
                simMetodo.agregarParametro(ControlTipos.normalizarTipo(pCtx.tipo().getText()));
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
                Simbolo simParam = new Simbolo(paramId, ControlTipos.normalizarTipo(paramTipo).name(), "Variable", pCtx.ID().getSymbol().getLine(), pCtx.ID().getSymbol().getCharPositionInLine());
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
        Simbolo nuevoSimbolo = new Simbolo(id, tipoNormalizado.name(), categoria, linea, columna);
        if (dimensiones > 0)
        {
            nuevoSimbolo.setDimensionArreglo(dimensiones);
        }
        if (tabla.buscar(id) != null)
        {
            consola.append("Error Semántico en línea " + linea + ", columna " + columna + ": La variable '" + id + "' ya ha sido declarada.\n");
            hayErroresSemanticos = true;
        }
        else
        {
            nuevoSimbolo.setInicializado(false);
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
        Simbolo nuevoSimbolo = new Simbolo(id, tipoNormalizado.name(), categoria, linea, columna);
        if (dimensiones > 0)
        {
            nuevoSimbolo.setDimensionArreglo(dimensiones);
        }
        if (tabla.buscar(id) != null)
        {
            consola.append("Error Semántico en línea " + linea + ", columna " + columna + ": La variable '" + id + "' ya ha sido declarada.\n");
            hayErroresSemanticos = true;
        }
        else
        {
            nuevoSimbolo.setInicializado(true);
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
                    gen.agregarSetHeap(String.valueOf(nuevoSimbolo.getOffset()), resExpr.getValorC3D());
                }
                else
                {
                    gen.agregarSetStack(String.valueOf(nuevoSimbolo.getOffset()), resExpr.getValorC3D());
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
        Simbolo simConstructor = new Simbolo(idInterno, TipoDato.OBJETO.name(), "Constructor", linea, columna);
        if (ctx.parametros() != null)
        {
            for (ZetarianoParser.ParametroContext pCtx : ctx.parametros().parametro())
            {
                simConstructor.agregarParametro(ControlTipos.normalizarTipo(pCtx.tipo().tipoBase().getText()));
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
                Simbolo simParam = new Simbolo(paramId, ControlTipos.normalizarTipo(paramTipo).name(), "Variable", pCtx.ID().getSymbol().getLine(), pCtx.ID().getSymbol().getCharPositionInLine());
                simParam.setInicializado(true);
                tabla.insertar(simParam);
            }
        }
        Object resultado = visit(ctx.bloqueMetodo());
        consola.append("\n[RAYOS X] Memoria dentro del constructor '" + id + "':\n");
        consola.append(tabla.imprimirTabla());
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
        return new ResultadoC3D(TipoDato.CADENA, ctx.getText());
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
        if (!sim.isInicializado() && !sim.isEnHeap())
        {
            consola.append("Error Semántico en línea " + linea + ": La variable local '" + idVariable + "' podría no haber sido inicializada.\n");
            hayErroresSemanticos = true;
            return new ResultadoC3D(TipoDato.ERROR, "");
        }
        String temporal = gen.generarTemporal();
        if (sim.isEnHeap())
        {
            gen.agregarGetHeap(temporal, String.valueOf(sim.getOffset()));
        }
        else
        {
            gen.agregarGetStack(temporal, String.valueOf(sim.getOffset()));
        }
        return new ResultadoC3D(TipoDato.valueOf(sim.getTipo()), temporal);
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
        String temporal = gen.generarTemporal();
        gen.agregarAsignacion(temporal, izq.getValorC3D(), operador, der.getValorC3D());
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
        String temporal = gen.generarTemporal();
        String operador = ctx.POR() != null ? "*" : (ctx.DIVISION() != null ? "/" : "%");
        if (operador.equals("%"))
        {
            gen.agregarAsignacion(temporal, "fmod(" + izq.getValorC3D() + ", " + der.getValorC3D() + ")");
        }
        else
        {
            gen.agregarAsignacion(temporal, izq.getValorC3D(), operador, der.getValorC3D());
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
        String temporal = gen.generarTemporal();
        String etVerdadera = gen.generarEtiqueta();
        String etFalsa = gen.generarEtiqueta();
        String etSalida = gen.generarEtiqueta();
        gen.agregarSaltoCondicional(izq.getValorC3D(), operador, der.getValorC3D(), etVerdadera);
        gen.agregarSaltoIncondicional(etFalsa);
        gen.agregarEtiqueta(etVerdadera);
        gen.agregarAsignacion(temporal, "1");
        gen.agregarSaltoIncondicional(etSalida);
        gen.agregarEtiqueta(etFalsa);
        gen.agregarAsignacion(temporal, "0");
        gen.agregarEtiqueta(etSalida);
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
        String temporal = gen.generarTemporal();
        String etVerdadera = gen.generarEtiqueta();
        String etFalsa = gen.generarEtiqueta();
        String etSalida = gen.generarEtiqueta();
        gen.agregarSaltoCondicional(izq.getValorC3D(), operador, der.getValorC3D(), etVerdadera);
        gen.agregarSaltoIncondicional(etFalsa);
        gen.agregarEtiqueta(etVerdadera);
        gen.agregarAsignacion(temporal, "1");
        gen.agregarSaltoIncondicional(etSalida);
        gen.agregarEtiqueta(etFalsa);
        gen.agregarAsignacion(temporal, "0");
        gen.agregarEtiqueta(etSalida);
        return new ResultadoC3D(resultado, temporal);
    }

    @Override
    public Object visitAndLogico(ZetarianoParser.AndLogicoContext ctx)
    {
        String temporal = gen.generarTemporal();
        String etFalsa = gen.generarEtiqueta();
        String etVerdadera = gen.generarEtiqueta();
        String etSalida = gen.generarEtiqueta();
        ResultadoC3D izq = (ResultadoC3D) visit(ctx.expresion(0));
        if (izq.getTipo() == TipoDato.ERROR)
        {
            return new ResultadoC3D(TipoDato.ERROR, "");
        }
        gen.agregarSaltoCondicional(izq.getValorC3D(), "==", "0", etFalsa);
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
        gen.agregarSaltoCondicional(der.getValorC3D(), "==", "1", etVerdadera);
        gen.agregarSaltoIncondicional(etFalsa);
        gen.agregarEtiqueta(etVerdadera);
        gen.agregarAsignacion(temporal, "1");
        gen.agregarSaltoIncondicional(etSalida);
        gen.agregarEtiqueta(etFalsa);
        gen.agregarAsignacion(temporal, "0");
        gen.agregarEtiqueta(etSalida);
        return new ResultadoC3D(resultado, temporal);
    }

    @Override
    public Object visitOrLogico(ZetarianoParser.OrLogicoContext ctx)
    {
        String temporal = gen.generarTemporal();
        String etFalsa = gen.generarEtiqueta();
        String etVerdadera = gen.generarEtiqueta();
        String etSalida = gen.generarEtiqueta();
        ResultadoC3D izq = (ResultadoC3D) visit(ctx.expresion(0));
        if (izq.getTipo() == TipoDato.ERROR)
        {
            return new ResultadoC3D(TipoDato.ERROR, "");
        }
        gen.agregarSaltoCondicional(izq.getValorC3D(), "==", "1", etVerdadera);
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
        gen.agregarSaltoCondicional(der.getValorC3D(), "==", "1", etVerdadera);
        gen.agregarSaltoIncondicional(etFalsa);
        gen.agregarEtiqueta(etVerdadera);
        gen.agregarAsignacion(temporal, "1");
        gen.agregarSaltoIncondicional(etSalida);
        gen.agregarEtiqueta(etFalsa);
        gen.agregarAsignacion(temporal, "0");
        gen.agregarEtiqueta(etSalida);
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
        String temporal = gen.generarTemporal();
        gen.agregarAsignacion(temporal, "-" + tipo.getValorC3D());
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
        String temporal = gen.generarTemporal();
        gen.agregarAsignacion(temporal, "1", "-", tipo.getValorC3D());
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
        if (resExpr != null && resExpr.getTipo() != TipoDato.ERROR)
        {
            if (!ControlTipos.esAsignacionValida(TipoDato.valueOf(sim.getTipo()), resExpr.getTipo()))
            {
                consola.append("Error Semántico en línea " + linea + ": Tipos incompatibles. No se puede asignar " + resExpr.getTipo() + " a la variable '" + idVariable + "'.\n");
                hayErroresSemanticos = true;
            }
            else
            {
                sim.setInicializado(true);
                if (sim.isEnHeap())
                {
                    gen.agregarSetHeap(String.valueOf(sim.getOffset()), resExpr.getValorC3D());
                }
                else
                {
                    gen.agregarSetStack(String.valueOf(sim.getOffset()), resExpr.getValorC3D());
                }
            }
        }
        return null;
    }
    
    @Override
    public Object visitCondicional(ZetarianoParser.CondicionalContext ctx)
    {
        String etiquetaSalida = gen.generarEtiqueta();
        for (int i = 0; i < ctx.expresion().size(); i++)
        {
            ResultadoC3D resCondicion = (ResultadoC3D) visit(ctx.expresion(i));
            if (resCondicion.getTipo() != TipoDato.BOOLEANO && resCondicion.getTipo() != TipoDato.ERROR)
            {
                consola.append("Error Semántico en línea " + ctx.getStart().getLine() + ": La condición del IF debe ser booleana.\n");
                hayErroresSemanticos = true;
            }
            String etiquetaFalsa = gen.generarEtiqueta();
            gen.agregarSaltoCondicional(resCondicion.getValorC3D(), "==", "0", etiquetaFalsa);
            visit(ctx.bloque(i));
            gen.agregarSaltoIncondicional(etiquetaSalida);
            gen.agregarEtiqueta(etiquetaFalsa);
        }
        if (ctx.ELSE() != null)
        {
            visit(ctx.bloque(ctx.bloque().size() - 1));
        }
        gen.agregarEtiqueta(etiquetaSalida);
        return null;
    }
    
    @Override
    public Object visitBucleWhile(ZetarianoParser.BucleWhileContext ctx)
    {
        String etiquetaInicio = gen.generarEtiqueta();
        String etiquetaSalida = gen.generarEtiqueta();
        gen.agregarEtiqueta(etiquetaInicio);
        ResultadoC3D resCondicion = (ResultadoC3D) visit(ctx.expresion());
        if (resCondicion.getTipo() != TipoDato.BOOLEANO && resCondicion.getTipo() != TipoDato.ERROR)
        {
            consola.append("Error Semántico en línea " + ctx.getStart().getLine() + ": La condición del WHILE debe ser booleana.\n");
            hayErroresSemanticos = true;
        }
        gen.agregarSaltoCondicional(resCondicion.getValorC3D(), "==", "0", etiquetaSalida);
        visit(ctx.bloque());
        gen.agregarSaltoIncondicional(etiquetaInicio);
        gen.agregarEtiqueta(etiquetaSalida);
        return null;
    }
    
    @Override
    public Object visitBucleDoWhile(ZetarianoParser.BucleDoWhileContext ctx)
    {
        String etiquetaInicio = gen.generarEtiqueta();
        gen.agregarEtiqueta(etiquetaInicio);
        visit(ctx.bloqueMetodo());
        ResultadoC3D resCondicion = (ResultadoC3D) visit(ctx.expresion());
        if (resCondicion.getTipo() != TipoDato.BOOLEANO && resCondicion.getTipo() != TipoDato.ERROR)
        {
            consola.append("Error Semántico en línea " + ctx.getStart().getLine() + ": La condición del DO WHILE debe ser booleana.\n");
            hayErroresSemanticos = true;
        }
        gen.agregarSaltoCondicional(resCondicion.getValorC3D(), "==", "1", etiquetaInicio);
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
        String etiquetaInicio = gen.generarEtiqueta();
        String etiquetaSalida = gen.generarEtiqueta();
        gen.agregarEtiqueta(etiquetaInicio);
        if (ctx.expresion() != null)
        {
            ResultadoC3D resCondicion = (ResultadoC3D) visit(ctx.expresion());
            if (resCondicion.getTipo() != TipoDato.BOOLEANO && resCondicion.getTipo() != TipoDato.ERROR)
            {
                consola.append("Error Semántico en línea " + ctx.getStart().getLine() + ": La condición del FOR debe ser booleana.\n");
                hayErroresSemanticos = true;
            }
            gen.agregarSaltoCondicional(resCondicion.getValorC3D(), "==", "0", etiquetaSalida);
        }
        visit(ctx.bloque());
        if (ctx.actualizacionFor() != null)
        {
            visit(ctx.actualizacionFor());
        }
        gen.agregarSaltoIncondicional(etiquetaInicio);
        gen.agregarEtiqueta(etiquetaSalida);
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
        Simbolo nuevoSimbolo = new Simbolo(id, tipoNormalizado.name(), "Variable", linea, columna);
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
                    gen.agregarSetHeap(String.valueOf(nuevoSimbolo.getOffset()), resExpr.getValorC3D());
                }
                else 
                {
                    gen.agregarSetStack(String.valueOf(nuevoSimbolo.getOffset()), resExpr.getValorC3D());
                }
            }
        }
        return null;
    }
    
    @Override
    public Object visitCondicionalSwitch(ZetarianoParser.CondicionalSwitchContext ctx)
    {
        ResultadoC3D resVariable = (ResultadoC3D) visit(ctx.expresion());
        String etiquetaSalida = gen.generarEtiqueta();
        for (ZetarianoParser.CasoContext casoCtx : ctx.caso())
        {
            ResultadoC3D resCaso = (ResultadoC3D) visit(casoCtx.expresion());
            String etiquetaBloque = gen.generarEtiqueta();
            String etiquetaSiguiente = gen.generarEtiqueta();
            gen.agregarSaltoCondicional(resVariable.getValorC3D(), "==", resCaso.getValorC3D(), etiquetaBloque);
            gen.agregarSaltoIncondicional(etiquetaSiguiente);
            gen.agregarEtiqueta(etiquetaBloque);
            for (ZetarianoParser.InstruccionContext inst : casoCtx.instruccion())
            {
                visit(inst);
            }
            gen.agregarSaltoIncondicional(etiquetaSalida);
            gen.agregarEtiqueta(etiquetaSiguiente);
        }
        if (ctx.casoDefault() != null)
        {
            for (ZetarianoParser.InstruccionContext inst : ctx.casoDefault().instruccion())
            {
                visit(inst);
            }
        }
        gen.agregarEtiqueta(etiquetaSalida);
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
        String temporalAnterior = gen.generarTemporal();
        String temporalNuevo = gen.generarTemporal();
        String operador = ctx.MAS_MAS() != null ? "+" : "-";
        if (sim.isEnHeap())
        {
            gen.agregarGetHeap(temporalAnterior, String.valueOf(sim.getOffset()));
            gen.agregarAsignacion(temporalNuevo, temporalAnterior, operador, "1");
            gen.agregarSetHeap(String.valueOf(sim.getOffset()), temporalNuevo);
        }
        else
        {
            gen.agregarGetStack(temporalAnterior, String.valueOf(sim.getOffset()));
            gen.agregarAsignacion(temporalNuevo, temporalAnterior, operador, "1");
            gen.agregarSetStack(String.valueOf(sim.getOffset()), temporalNuevo);
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
                gen.agregarPrint("d", "(int)" + resExpr.getValorC3D());
            }
            else if (resExpr.getTipo() == TipoDato.DECIMAL)
            {
                gen.agregarPrint("f", resExpr.getValorC3D());
            }
            else if (resExpr.getTipo() == TipoDato.CADENA) 
            {
                gen.agregarPrint("s", resExpr.getValorC3D());
            }
            else
            {
                consola.append("Error Semántico en línea " + ctx.getStart().getLine() + ": Tipo de dato " + resExpr.getTipo() + " no soportado para impresión.\n");
                hayErroresSemanticos = true;
            }
        }
        if (ctx.PRINTLN() != null)
        {
            gen.agregarPrint("c", "10"); 
        }
        return null;
    }
}
