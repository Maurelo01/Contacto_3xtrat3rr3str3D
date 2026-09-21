package mycompany.contacto_3xtrat3rr3str3d.semantico;

import mycompany.contacto_3xtrat3rr3str3d.YBaseVisitor;
import mycompany.contacto_3xtrat3rr3str3d.YParser;
import javax.swing.JTextArea;

public class YCustomVisitor extends YBaseVisitor<Object>
{
    private TablaSimbolos tabla;
    private JTextArea consola;
    public boolean hayErroresSemanticos = false;
    private GeneradorC3D gen = GeneradorC3D.getInstancia();
    public YCustomVisitor(TablaSimbolos tabla, JTextArea consola)
    {
        this.tabla = tabla;
        this.consola = consola;
    }

    // Funciones y bloques
    
    @Override
    public Object visitDefinicionFuncion(YParser.DefinicionFuncionContext ctx)
    {
        String id = ctx.ID().getText();
        int linea = ctx.ID().getSymbol().getLine();
        int columna = ctx.ID().getSymbol().getCharPositionInLine();
        String tipoStr = (ctx.FLECHA() != null) ? ctx.tipo().getText() : "VOID";
        TipoDato tipoNormalizado = tipoStr.equals("VOID") ? TipoDato.VOID : ControlTipos.normalizarTipo(tipoStr);
        Simbolo simFuncion = new Simbolo(id, tipoNormalizado.name(), "Funcion", linea, columna);
        if (!tabla.insertar(simFuncion))
        {
            consola.append("Error Semántico en línea " + linea + ": La función '" + id + "' ya existe.\n");
            hayErroresSemanticos = true;
        }
        tabla.resetearOffsetLocal();
        tabla.entrarAmbito();
        Object resultado = visit(ctx.bloque());
        tabla.salirAmbito();
        return resultado;
    }

    // Declaracion de variables y asignacion

    @Override
    public Object visitDeclVariableAsig(YParser.DeclVariableAsigContext ctx)
    {
        String tipoStr = ctx.tipo().getText();
        String id = ctx.ID().getText();
        int linea = ctx.ID().getSymbol().getLine();
        int columna = ctx.ID().getSymbol().getCharPositionInLine();
        TipoDato tipoNormalizado = ControlTipos.normalizarTipo(tipoStr);
        Simbolo nuevoSimbolo = new Simbolo(id, tipoNormalizado.name(), "Variable", linea, columna);
        if (tabla.buscar(id) != null)
        {
            consola.append("Error Semántico en línea " + linea + ": La variable '" + id + "' ya fue declarada.\n");
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
                consola.append("Error Semántico en línea " + linea + ": Tipos incompatibles. No se puede asignar " + resExpr.getTipo() + " a " + tipoNormalizado + ".\n");
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
    public Object visitDeclVariable(YParser.DeclVariableContext ctx)
    {
        String tipoStr = ctx.tipo().getText();
        String id = ctx.ID().getText();
        int linea = ctx.ID().getSymbol().getLine();
        int columna = ctx.ID().getSymbol().getCharPositionInLine();
        TipoDato tipoNormalizado = ControlTipos.normalizarTipo(tipoStr);
        Simbolo nuevoSimbolo = new Simbolo(id, tipoNormalizado.name(), "Variable", linea, columna);
        if (tabla.buscar(id) != null)
        {
            consola.append("Error Semántico en línea " + linea + ": La variable '" + id + "' ya ha sido declarada.\n");
            hayErroresSemanticos = true;
        }
        else
        {
            nuevoSimbolo.setInicializado(false);
            tabla.insertar(nuevoSimbolo);
        }
        return null;
    }

    // Instrucciones
    
    @Override
    public Object visitInstruccionImprimir(YParser.InstruccionImprimirContext ctx)
    {
        if (ctx.expresion() != null)
        {
            ResultadoC3D resExpr = (ResultadoC3D) visit(ctx.expresion());
            if (resExpr != null) 
            {
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
                    consola.append("Error Semántico en línea " + ctx.getStart().getLine() + ": Tipo " + resExpr.getTipo() + " no soportado para impresión.\n");
                    hayErroresSemanticos = true;
                }
            }
        }
        gen.agregarPrint("c", "10"); 
        return null;
    }

    // Literales
    
    @Override
    public Object visitNumLiteral(YParser.NumLiteralContext ctx)
    {
        return new ResultadoC3D(TipoDato.ENTERO, ctx.getText());
    }
    
    @Override
    public Object visitDecLiteral(YParser.DecLiteralContext ctx)
    {
        return new ResultadoC3D(TipoDato.DECIMAL, ctx.getText());
    }
    
    @Override
    public Object visitTextLiteral(YParser.TextLiteralContext ctx)
    {
        return new ResultadoC3D(TipoDato.CADENA, ctx.getText());
    }
    
    @Override
    public Object visitCharLiteral(YParser.CharLiteralContext ctx)
    {
        return new ResultadoC3D(TipoDato.CARACTER, ctx.getText());
    }
    
    @Override
    public Object visitTrueLiteral(YParser.TrueLiteralContext ctx)
    {
        return new ResultadoC3D(TipoDato.BOOLEANO, "1");
    }
    
    @Override
    public Object visitFalseLiteral(YParser.FalseLiteralContext ctx)
    {
        return new ResultadoC3D(TipoDato.BOOLEANO, "0");
    }
    
    // Acceso variables/expresiones
    
    @Override
    public Object visitToFactor(YParser.ToFactorContext ctx)
    {
        return visit(ctx.factor());
    }

    @Override
    public Object visitAccesoVariableOAtributo(YParser.AccesoVariableOAtributoContext ctx)
    {
        String idVariable = ctx.acceso().ID(0).getText();
        Simbolo sim = tabla.buscar(idVariable);
        int linea = ctx.acceso().ID(0).getSymbol().getLine();
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
    
    // Expresiones Matematicas y Logicas
    
    @Override
    public Object visitSumaResta(YParser.SumaRestaContext ctx)
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
            consola.append("Error Semántico en línea " + ctx.getStart().getLine() + ": Incompatibilidad de tipos (" + izq.getTipo() + " " + operador + " " + der.getTipo() + ").\n");
            hayErroresSemanticos = true;
            return new ResultadoC3D(TipoDato.ERROR, "");
        }
        String temporal = gen.generarTemporal();
        gen.agregarAsignacion(temporal, izq.getValorC3D(), operador, der.getValorC3D());
        return new ResultadoC3D(tipoResultado, temporal);
    }
    
    @Override
    public Object visitMultDiv(YParser.MultDivContext ctx)
    {
        ResultadoC3D izq = (ResultadoC3D) visit(ctx.expresion(0));
        ResultadoC3D der = (ResultadoC3D) visit(ctx.expresion(1));
        if (izq.getTipo() == TipoDato.ERROR || der.getTipo() == TipoDato.ERROR) return new ResultadoC3D(TipoDato.ERROR, "");
        TipoDato tipoResultado = ControlTipos.resolverAritmetica(izq.getTipo(), der.getTipo());
        if (tipoResultado == TipoDato.ERROR)
        {
            consola.append("Error Semántico en línea " + ctx.getStart().getLine() + ": Incompatibilidad de tipos (" + izq.getTipo() + " y " + der.getTipo() + ").\n");
            hayErroresSemanticos = true;
            return new ResultadoC3D(TipoDato.ERROR, "");
        }
        String temporal = gen.generarTemporal();
        String operador = ctx.POR() != null ? "*" : "/";
        gen.agregarAsignacion(temporal, izq.getValorC3D(), operador, der.getValorC3D());
        return new ResultadoC3D(tipoResultado, temporal);
    }

    @Override
    public Object visitComparacion(YParser.ComparacionContext ctx)
    {
        ResultadoC3D izq = (ResultadoC3D) visit(ctx.expresion(0));
        ResultadoC3D der = (ResultadoC3D) visit(ctx.expresion(1));
        if (izq.getTipo() == TipoDato.ERROR || der.getTipo() == TipoDato.ERROR) return new ResultadoC3D(TipoDato.ERROR, "");
        TipoDato resultado = ControlTipos.resolverRelacional(izq.getTipo(), der.getTipo());
        if (resultado == TipoDato.ERROR)
        {
            consola.append("Error Semántico en línea " + ctx.getStart().getLine() + ": No se pueden comparar " + izq.getTipo() + " y " + der.getTipo() + ".\n");
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
    public Object visitIgualdad(YParser.IgualdadContext ctx)
    {
        ResultadoC3D izq = (ResultadoC3D) visit(ctx.expresion(0));
        ResultadoC3D der = (ResultadoC3D) visit(ctx.expresion(1));
        if (izq.getTipo() == TipoDato.ERROR || der.getTipo() == TipoDato.ERROR) return new ResultadoC3D(TipoDato.ERROR, "");
        TipoDato resultado = ControlTipos.resolverIgualdad(izq.getTipo(), der.getTipo());
        if (resultado == TipoDato.ERROR)
        {
            consola.append("Error Semántico en línea " + ctx.getStart().getLine() + ": No se puede evaluar igualdad entre " + izq.getTipo() + " y " + der.getTipo() + ".\n");
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
    public Object visitAndLogico(YParser.AndLogicoContext ctx)
    {
        String temporal = gen.generarTemporal();
        String etFalsa = gen.generarEtiqueta();
        String etVerdadera = gen.generarEtiqueta();
        String etSalida = gen.generarEtiqueta();
        ResultadoC3D izq = (ResultadoC3D) visit(ctx.expresion(0));
        if (izq.getTipo() == TipoDato.ERROR) return new ResultadoC3D(TipoDato.ERROR, "");
        gen.agregarSaltoCondicional(izq.getValorC3D(), "==", "0", etFalsa);
        ResultadoC3D der = (ResultadoC3D) visit(ctx.expresion(1));
        if (der.getTipo() == TipoDato.ERROR) return new ResultadoC3D(TipoDato.ERROR, "");
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
    public Object visitOrLogico(YParser.OrLogicoContext ctx)
    {
        String temporal = gen.generarTemporal();
        String etFalsa = gen.generarEtiqueta();
        String etVerdadera = gen.generarEtiqueta();
        String etSalida = gen.generarEtiqueta();
        ResultadoC3D izq = (ResultadoC3D) visit(ctx.expresion(0));
        if (izq.getTipo() == TipoDato.ERROR) return new ResultadoC3D(TipoDato.ERROR, "");
        gen.agregarSaltoCondicional(izq.getValorC3D(), "==", "1", etVerdadera);
        ResultadoC3D der = (ResultadoC3D) visit(ctx.expresion(1));
        if (der.getTipo() == TipoDato.ERROR) return new ResultadoC3D(TipoDato.ERROR, "");
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
    public Object visitNegacionUnaria(YParser.NegacionUnariaContext ctx)
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
    public Object visitNegacionLogica(YParser.NegacionLogicaContext ctx)
    {
        ResultadoC3D tipo = (ResultadoC3D) visit(ctx.factor());
        TipoDato resultado = ControlTipos.resolverUnariaLogica(tipo.getTipo());
        if (resultado == TipoDato.ERROR)
        {
            consola.append("Error Semántico en línea " + ctx.getStart().getLine() + ": No se puede aplicar negación lógica a " + tipo.getTipo() + ".\n");
            hayErroresSemanticos = true;
            return new ResultadoC3D(TipoDato.ERROR, "");
        }
        String temporal = gen.generarTemporal();
        gen.agregarAsignacion(temporal, "1", "-", tipo.getValorC3D());
        return new ResultadoC3D(resultado, temporal);
    }
    @Override
    public Object visitParentesis(YParser.ParentesisContext ctx)
    {
        return visit(ctx.expresion());
    }
    
    // Asignaciones
    
    @Override
    public Object visitAsignacion(YParser.AsignacionContext ctx)
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
                consola.append("Error Semántico en línea " + linea + ": Tipos incompatibles para '" + idVariable + "'.\n");
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
    public Object visitIncremento(YParser.IncrementoContext ctx)
    {
        String idVariable = ctx.acceso().ID(0).getText();
        Simbolo sim = tabla.buscar(idVariable);
        int linea = ctx.getStart().getLine();
        if (sim == null)
        {
            consola.append("Error Semántico en línea " + linea + ": La variable '" + idVariable + "' no existe.\n");
            hayErroresSemanticos = true; return null;
        }
        if (!sim.getTipo().equals(TipoDato.ENTERO.name()) && !sim.getTipo().equals(TipoDato.DECIMAL.name()))
        {
            consola.append("Error Semántico en línea " + linea + ": Solo se pueden incrementar números.\n");
            hayErroresSemanticos = true; return null;
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
    
    // Control de flujo
    
    @Override
    public Object visitCondicional(YParser.CondicionalContext ctx)
    {
        String etiquetaSalida = gen.generarEtiqueta();
        for (int i = 0; i < ctx.expresion().size(); i++)
        {
            ResultadoC3D resCondicion = (ResultadoC3D) visit(ctx.expresion(i));
            if (resCondicion.getTipo() != TipoDato.BOOLEANO && resCondicion.getTipo() != TipoDato.ERROR)
            {
                consola.append("Error Semántico en línea " + ctx.getStart().getLine() + ": La condición SI debe ser booleana.\n");
                hayErroresSemanticos = true;
            }
            String etiquetaFalsa = gen.generarEtiqueta();
            gen.agregarSaltoCondicional(resCondicion.getValorC3D(), "==", "0", etiquetaFalsa);
            visit(ctx.bloque(i));
            gen.agregarSaltoIncondicional(etiquetaSalida);
            gen.agregarEtiqueta(etiquetaFalsa);
        }
        if (ctx.CONTRARIO() != null)
        {
            visit(ctx.bloque(ctx.bloque().size() - 1));
        }
        gen.agregarEtiqueta(etiquetaSalida);
        return null;
    }

    @Override
    public Object visitBucleMientras(YParser.BucleMientrasContext ctx)
    {
        String etiquetaInicio = gen.generarEtiqueta();
        String etiquetaSalida = gen.generarEtiqueta();
        gen.agregarEtiqueta(etiquetaInicio);
        ResultadoC3D resCondicion = (ResultadoC3D) visit(ctx.expresion());
        if (resCondicion.getTipo() != TipoDato.BOOLEANO && resCondicion.getTipo() != TipoDato.ERROR)
        {
            consola.append("Error Semántico en línea " + ctx.getStart().getLine() + ": La condición MIENTRAS debe ser booleana.\n");
            hayErroresSemanticos = true;
        }
        gen.agregarSaltoCondicional(resCondicion.getValorC3D(), "==", "0", etiquetaSalida);
        visit(ctx.bloque());
        gen.agregarSaltoIncondicional(etiquetaInicio);
        gen.agregarEtiqueta(etiquetaSalida);
        return null;
    }

    @Override
    public Object visitBucleHacer(YParser.BucleHacerContext ctx)
    {
        String etiquetaInicio = gen.generarEtiqueta();
        gen.agregarEtiqueta(etiquetaInicio);
        visit(ctx.bloque());
        ResultadoC3D resCondicion = (ResultadoC3D) visit(ctx.expresion());
        if (resCondicion.getTipo() != TipoDato.BOOLEANO && resCondicion.getTipo() != TipoDato.ERROR)
        {
            consola.append("Error Semántico en línea " + ctx.getStart().getLine() + ": La condición HACER MIENTRAS debe ser booleana.\n");
            hayErroresSemanticos = true;
        }
        gen.agregarSaltoCondicional(resCondicion.getValorC3D(), "==", "1", etiquetaInicio);
        return null;
    }

    @Override
    public Object visitBuclePara(YParser.BucleParaContext ctx)
    {
        tabla.entrarAmbito();
        if (ctx.declaracion() != null)
        {
            visit(ctx.declaracion());
        }
        String etiquetaInicio = gen.generarEtiqueta();
        String etiquetaSalida = gen.generarEtiqueta();
        gen.agregarEtiqueta(etiquetaInicio);
        if (ctx.expresion() != null)
        {
            ResultadoC3D resCondicion = (ResultadoC3D) visit(ctx.expresion());
            if (resCondicion.getTipo() != TipoDato.BOOLEANO && resCondicion.getTipo() != TipoDato.ERROR)
            {
                consola.append("Error Semántico en línea " + ctx.getStart().getLine() + ": La condición PARA debe ser booleana.\n");
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
    public Object visitCondicionalElegir(YParser.CondicionalElegirContext ctx)
    {
        ResultadoC3D resVariable = (ResultadoC3D) visit(ctx.expresion());
        String etiquetaSalida = gen.generarEtiqueta();
        for (YParser.CasoContext casoCtx : ctx.caso())
        {
            ResultadoC3D resCaso = (ResultadoC3D) visit(casoCtx.expresion());
            String etiquetaBloque = gen.generarEtiqueta();
            String etiquetaSiguiente = gen.generarEtiqueta();
            gen.agregarSaltoCondicional(resVariable.getValorC3D(), "==", resCaso.getValorC3D(), etiquetaBloque);
            gen.agregarSaltoIncondicional(etiquetaSiguiente);
            gen.agregarEtiqueta(etiquetaBloque);
            visit(casoCtx.bloque());
            gen.agregarSaltoIncondicional(etiquetaSalida);
            gen.agregarEtiqueta(etiquetaSiguiente);
        }
        if (ctx.casoDefault() != null)
        {
            visit(ctx.casoDefault().bloque());
        }
        gen.agregarEtiqueta(etiquetaSalida);
        return null;
    }
}