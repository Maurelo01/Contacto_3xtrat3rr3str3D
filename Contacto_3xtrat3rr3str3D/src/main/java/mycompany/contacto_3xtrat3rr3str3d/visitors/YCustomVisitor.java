package mycompany.contacto_3xtrat3rr3str3d.visitors;

import java.util.List;
import java.util.Map;
import mycompany.contacto_3xtrat3rr3str3d.simbolos.*;
import mycompany.contacto_3xtrat3rr3str3d.c3d.GeneradorFuncionesNativas;
import mycompany.contacto_3xtrat3rr3str3d.c3d.GeneradorC3D;
import mycompany.contacto_3xtrat3rr3str3d.YBaseVisitor;
import mycompany.contacto_3xtrat3rr3str3d.YParser;
import javax.swing.JTextArea;
import mycompany.contacto_3xtrat3rr3str3d.utils.*;
import org.antlr.v4.runtime.tree.TerminalNode;

public class YCustomVisitor extends YBaseVisitor<Object>
{
    private TablaSimbolos tabla;
    private JTextArea consola;
    public boolean hayErroresSemanticos = false;
    private GeneradorC3D generador = GeneradorC3D.getInstancia();
    public YCustomVisitor(TablaSimbolos tabla, JTextArea consola)
    {
        this.tabla = tabla;
        this.consola = consola;
    }
    @Override
    public Object visitPrograma(YParser.ProgramaContext ctx)
    {
        generador.limpiar(); 
        return super.visitPrograma(ctx);
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
        SimboloFuncion simMetodo = new SimboloFuncion(id, tipoNormalizado, linea, columna);
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
                else if (pCtx instanceof YParser.ParamArregloRefContext)
                {
                    paramId = ((YParser.ParamArregloRefContext) pCtx).ID().getText();
                    paramTipoStr = ((YParser.ParamArregloRefContext) pCtx).tipo().getText();
                }
                else if (pCtx instanceof YParser.ParamStructRefContext)
                {
                    paramId = ((YParser.ParamStructRefContext) pCtx).ID(1).getText();
                    paramTipoStr = ((YParser.ParamStructRefContext) pCtx).ID(0).getText();
                }
                simMetodo.agregarParametro(new SimboloVariable(paramId, ControlTipos.normalizarTipo(paramTipoStr), linea, columna));
            }
        }
        if (!tabla.insertar(simMetodo))
        {
            consola.append("Error Semántico en línea " + linea + ": La función '" + id + "' ya existe.\n");
            hayErroresSemanticos = true;
        }
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
                else if (pCtx instanceof YParser.ParamArregloRefContext)
                {
                    paramId = ((YParser.ParamArregloRefContext) pCtx).ID().getText();
                    paramTipoStr = ((YParser.ParamArregloRefContext) pCtx).tipo().getText();
                }
                else if (pCtx instanceof YParser.ParamStructRefContext)
                {
                    paramId = ((YParser.ParamStructRefContext) pCtx).ID(1).getText();
                    paramTipoStr = ((YParser.ParamStructRefContext) pCtx).ID(0).getText();
                }
                SimboloVariable simParam = new SimboloVariable(paramId, ControlTipos.normalizarTipo(paramTipoStr), linea, columna);
                simParam.setInicializado(true);
                if (ControlTipos.normalizarTipo(paramTipoStr) == TipoDato.OBJETO) simParam.setReferenciaClase(paramTipoStr);
                tabla.insertar(simParam);
            }
        }
        Object resultado = null;
        if (id.equals("principal") || id.equals("main"))
        {
            resultado = visit(ctx.bloque());
        }
        else
        {
            generador.iniciarMetodo(id);
            resultado = visit(ctx.bloque());
            generador.cerrarMetodo();
        }
        tabla.salirAmbito();
        return resultado;
    }
    
    @Override
    public Object visitDefinicionEstructura(YParser.DefinicionEstructuraContext ctx)
    {
        String id = ctx.ID().getText();
        int linea = ctx.ID().getSymbol().getLine();
        int columna = ctx.ID().getSymbol().getCharPositionInLine();
        SimboloClase simEstructura = new SimboloClase(id, linea, columna);
        int contadorAtributos = 0;
        for (YParser.AtributoEstructuraContext atributo : ctx.atributoEstructura())
        {
            contadorAtributos++;
        }
        simEstructura.setTamañoHeapObjeto(contadorAtributos);
        tabla.insertar(simEstructura);
        tabla.resetearOffsetLocal();
        tabla.entrarAmbito();
        for (YParser.AtributoEstructuraContext atributo : ctx.atributoEstructura())
        {
            String attrId = "";
            String attrTipo = "";
            if (atributo instanceof YParser.AtributoNormalContext)
            {
                attrId = ((YParser.AtributoNormalContext) atributo).ID().getText();
                attrTipo = ((YParser.AtributoNormalContext) atributo).tipo().getText();
            }
            else if (atributo instanceof YParser.AtributoEstructuraAnidadaContext)
            {
                attrId = ((YParser.AtributoEstructuraAnidadaContext) atributo).ID(1).getText();
                attrTipo = ((YParser.AtributoEstructuraAnidadaContext) atributo).ID(0).getText();
            }
            TipoDato tipoNorm = ControlTipos.normalizarTipo(attrTipo);
            SimboloVariable simAttr = new SimboloVariable(attrId, tipoNorm, linea, columna);
            if (tipoNorm == TipoDato.OBJETO) simAttr.setReferenciaClase(attrTipo);
            tabla.insertar(simAttr);
        }
        
        Map<String, Simbolo> atributosLocales = tabla.obtenerAmbitoActual();
        for (Simbolo sim : atributosLocales.values())
        {
            simEstructura.getEntornoInterno().obtenerAmbitoActual().put(sim.getNombre(), sim);
        }
        tabla.salirAmbito();
        return null;
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
        SimboloVariable nuevoSimbolo = new SimboloVariable(id, tipoNormalizado, linea, columna);
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
    public Object visitDeclVariable(YParser.DeclVariableContext ctx)
    {
        String tipoStr = ctx.tipo().getText();
        String id = ctx.ID().getText();
        int linea = ctx.ID().getSymbol().getLine();
        int columna = ctx.ID().getSymbol().getCharPositionInLine();
        TipoDato tipoNormalizado = ControlTipos.normalizarTipo(tipoStr);
        SimboloVariable nuevoSimbolo = new SimboloVariable(id, tipoNormalizado, linea, columna);
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
    
    @Override
    public Object visitDeclEstructura(YParser.DeclEstructuraContext ctx)
    {
        String tipoStr = ctx.ID(0).getText();
        String id = ctx.ID(1).getText();
        int linea = ctx.ID(1).getSymbol().getLine();
        int columna = ctx.ID(1).getSymbol().getCharPositionInLine();
        SimboloVariable nuevoSimbolo = new SimboloVariable(id, TipoDato.OBJETO, linea, columna);
        nuevoSimbolo.setReferenciaClase(tipoStr);
        if (tabla.buscar(id) != null)
        {
            consola.append("Error Semántico en línea " + linea + ": La variable '" + id + "' ya ha sido declarada.\n");
            hayErroresSemanticos = true;
            return null;
        }
        nuevoSimbolo.setInicializado(true);
        tabla.insertar(nuevoSimbolo);
        SimboloClase plantilla = (SimboloClase) tabla.buscar(tipoStr);
        if (plantilla != null)
        {
            ResultadoC3D resInstancia = GestorObjetos.instanciarObjetoEnHeap(plantilla, generador);
            if (nuevoSimbolo.isEnHeap())
            {
                generador.agregarSetHeap(String.valueOf(nuevoSimbolo.getOffset()), resInstancia.getValorC3D());
            }
            else
            {
                String tempIndice = generador.generarTemporal();
                generador.agregarAsignacion(tempIndice, "punteroStack", "+", String.valueOf(nuevoSimbolo.getOffset()));
                generador.agregarSetStack(tempIndice, resInstancia.getValorC3D());
            }
        }
        else
        {
            consola.append("Error Semántico en línea " + linea + ": La estructura '" + tipoStr + "' no existe.\n");
            hayErroresSemanticos = true;
        }
        
        return null;
    }

    @Override
    public Object visitDeclEstructuraAsig(YParser.DeclEstructuraAsigContext ctx)
    {
        String tipoStr = ctx.ID(0).getText();
        String id = ctx.ID(1).getText();
        int linea = ctx.ID(1).getSymbol().getLine();
        int columna = ctx.ID(1).getSymbol().getCharPositionInLine();
        SimboloVariable nuevoSimbolo = new SimboloVariable(id, TipoDato.OBJETO, linea, columna);
        nuevoSimbolo.setReferenciaClase(tipoStr);
        if (tabla.buscar(id) != null)
        {
            consola.append("Error Semántico en línea " + linea + ": La variable '" + id + "' ya ha sido declarada.\n");
            hayErroresSemanticos = true;
            return null;
        }
        nuevoSimbolo.setInicializado(true);
        tabla.insertar(nuevoSimbolo);
        SimboloClase plantilla = (SimboloClase) tabla.buscar(tipoStr);
        if (plantilla != null)
        {
            ResultadoC3D resInstancia = GestorObjetos.instanciarObjetoEnHeap(plantilla, generador);
            if (nuevoSimbolo.isEnHeap())
            {
                generador.agregarSetHeap(String.valueOf(nuevoSimbolo.getOffset()), resInstancia.getValorC3D());
            }
            else
            {
                String tempIndice = generador.generarTemporal();
                generador.agregarAsignacion(tempIndice, "punteroStack", "+", String.valueOf(nuevoSimbolo.getOffset()));
                generador.agregarSetStack(tempIndice, resInstancia.getValorC3D());
            }
            if (ctx.argumentos() != null)
            {
                java.util.List<YParser.ExpresionContext> args = ctx.argumentos().expresion();
                for (int i = 0; i < args.size(); i++)
                {
                    ResultadoC3D resArg = (ResultadoC3D) visit(args.get(i));
                    String tempPosAttr = generador.generarTemporal();
                    generador.agregarAsignacion(tempPosAttr, resInstancia.getValorC3D(), "+", String.valueOf(i));
                    generador.agregarSetHeap(tempPosAttr, resArg.getValorC3D());
                }
            }
        }
        else
        {
            consola.append("Error Semántico en línea " + linea + ": La estructura '" + tipoStr + "' no existe.\n");
            hayErroresSemanticos = true;
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
                    consola.append("Error Semántico en línea " + ctx.getStart().getLine() + ": Tipo " + resExpr.getTipo() + " no soportado para impresión.\n");
                    hayErroresSemanticos = true;
                }
            }
        }
        generador.agregarPrint("c", "10"); 
        return null;
    }
    @Override
    public Object visitInstruccionRetornar(YParser.InstruccionRetornarContext ctx)
    {
        if (ctx.expresion() != null)
        {
            ResultadoC3D resExpr = (ResultadoC3D) visit(ctx.expresion());
            if (resExpr != null && resExpr.getTipo() != TipoDato.ERROR)
            {
                generador.agregarSetStack("punteroStack", resExpr.getValorC3D());
            }
        }
        generador.agregarCodigoBruto("    return;");
        return null;
    }

    @Override
    public Object visitLlamadaFuncionExpr(YParser.LlamadaFuncionExprContext ctx)
    {
        return procesarLlamadaFuncion(ctx.llamadaFuncion());
    }

    @Override
    public Object visitInstruccionLlamadaFuncion(YParser.InstruccionLlamadaFuncionContext ctx)
    {
        return procesarLlamadaFuncion(ctx.llamadaFuncion());
    }

    private Object procesarLlamadaFuncion(YParser.LlamadaFuncionContext ctx)
    {
        String idFuncion = ctx.ID().getText();
        Simbolo sim = tabla.buscar(idFuncion);
        if (sim == null || !(sim instanceof SimboloFuncion))
        {
            consola.append("Error Semántico en línea " + ctx.getStart().getLine() + ": La función '" + idFuncion + "' no existe.\n");
            hayErroresSemanticos = true;
            return new ResultadoC3D(TipoDato.ERROR, "");
        }
        SimboloFuncion funcion = (SimboloFuncion) sim;
        int tamanoEntornoActual = tabla.obtenerAmbitoActual().size() + 1;
        if (ctx.argumentos() != null)
        {
            for (int i = 0; i < ctx.argumentos().expresion().size(); i++)
            {
                ResultadoC3D resArg = (ResultadoC3D) visit(ctx.argumentos().expresion(i));
                String tempPos = generador.generarTemporal();
                int offsetDestino = i + 1;
                generador.agregarAsignacion(tempPos, "punteroStack", "+", String.valueOf(tamanoEntornoActual + offsetDestino));
                generador.agregarSetStack(tempPos, resArg.getValorC3D());
            }
        }
        generador.agregarComentario("Inicio llamada a funcion: " + idFuncion);
        generador.agregarAsignacion("punteroStack", "punteroStack", "+", String.valueOf(tamanoEntornoActual));
        generador.agregarLlamadaNativa("metodo_" + idFuncion, "");
        String tempReturn = generador.generarTemporal();
        generador.agregarGetStack(tempReturn, "punteroStack");
        generador.agregarAsignacion("punteroStack", "punteroStack", "-", String.valueOf(tamanoEntornoActual));
        generador.agregarComentario("Fin de llamada a: " + idFuncion);
        return new ResultadoC3D(funcion.getTipo(), tempReturn);
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
        String texto = ctx.getText();
        return GestorCadenas.guardarCadenaEnHeap(texto, generador);
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
                String tempIndice = generador.generarTemporal();
                generador.agregarAsignacion(tempIndice, "punteroStack", "+", String.valueOf(sim.getOffset()));
                generador.agregarGetStack(temporal, tempIndice);
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
        String temporal = generador.generarTemporal();
        generador.agregarAsignacion(temporal, izq.getValorC3D(), operador, der.getValorC3D());
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
        String temporal = generador.generarTemporal();
        String operador = ctx.POR() != null ? "*" : "/";
        generador.agregarAsignacion(temporal, izq.getValorC3D(), operador, der.getValorC3D());
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
    public Object visitAndLogico(YParser.AndLogicoContext ctx)
    {
        String temporal = generador.generarTemporal();
        String etFalsa = generador.generarEtiqueta();
        String etVerdadera = generador.generarEtiqueta();
        String etSalida = generador.generarEtiqueta();
        ResultadoC3D izq = (ResultadoC3D) visit(ctx.expresion(0));
        if (izq.getTipo() == TipoDato.ERROR) return new ResultadoC3D(TipoDato.ERROR, "");
        generador.agregarSaltoCondicional(izq.getValorC3D(), "==", "0", etFalsa);
        ResultadoC3D der = (ResultadoC3D) visit(ctx.expresion(1));
        if (der.getTipo() == TipoDato.ERROR) return new ResultadoC3D(TipoDato.ERROR, "");
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
    public Object visitOrLogico(YParser.OrLogicoContext ctx)
    {
        String temporal = generador.generarTemporal();
        String etFalsa = generador.generarEtiqueta();
        String etVerdadera = generador.generarEtiqueta();
        String etSalida = generador.generarEtiqueta();
        ResultadoC3D izq = (ResultadoC3D) visit(ctx.expresion(0));
        if (izq.getTipo() == TipoDato.ERROR) return new ResultadoC3D(TipoDato.ERROR, "");
        generador.agregarSaltoCondicional(izq.getValorC3D(), "==", "1", etVerdadera);
        ResultadoC3D der = (ResultadoC3D) visit(ctx.expresion(1));
        if (der.getTipo() == TipoDato.ERROR) return new ResultadoC3D(TipoDato.ERROR, "");
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
    public Object visitNegacionUnaria(YParser.NegacionUnariaContext ctx)
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
        String temporal = generador.generarTemporal();
        generador.agregarAsignacion(temporal, "1", "-", tipo.getValorC3D());
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
        if (sim.getTipo() != TipoDato.ENTERO && sim.getTipo() != TipoDato.DECIMAL)
        {
            consola.append("Error Semántico en línea " + linea + ": Solo se pueden incrementar números.\n");
            hayErroresSemanticos = true; return null;
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
    
    // Control de flujo
    
    @Override
    public Object visitCondicional(YParser.CondicionalContext ctx)
    {
        String etiquetaSalida = generador.generarEtiqueta();
        for (int i = 0; i < ctx.expresion().size(); i++)
        {
            ResultadoC3D resCondicion = (ResultadoC3D) visit(ctx.expresion(i));
            if (resCondicion.getTipo() != TipoDato.BOOLEANO && resCondicion.getTipo() != TipoDato.ERROR)
            {
                consola.append("Error Semántico en línea " + ctx.getStart().getLine() + ": La condición SI debe ser booleana.\n");
                hayErroresSemanticos = true;
            }
            String etiquetaFalsa = generador.generarEtiqueta();
            generador.agregarSaltoCondicional(resCondicion.getValorC3D(), "==", "0", etiquetaFalsa);
            visit(ctx.bloque(i));
            generador.agregarSaltoIncondicional(etiquetaSalida);
            generador.agregarEtiqueta(etiquetaFalsa);
        }
        if (ctx.CONTRARIO() != null)
        {
            visit(ctx.bloque(ctx.bloque().size() - 1));
        }
        generador.agregarEtiqueta(etiquetaSalida);
        return null;
    }

    @Override
    public Object visitBucleMientras(YParser.BucleMientrasContext ctx)
    {
        String etiquetaInicio = generador.generarEtiqueta();
        String etiquetaSalida = generador.generarEtiqueta();
        generador.agregarEtiqueta(etiquetaInicio);
        ResultadoC3D resCondicion = (ResultadoC3D) visit(ctx.expresion());
        if (resCondicion.getTipo() != TipoDato.BOOLEANO && resCondicion.getTipo() != TipoDato.ERROR)
        {
            consola.append("Error Semántico en línea " + ctx.getStart().getLine() + ": La condición MIENTRAS debe ser booleana.\n");
            hayErroresSemanticos = true;
        }
        generador.agregarSaltoCondicional(resCondicion.getValorC3D(), "==", "0", etiquetaSalida);
        visit(ctx.bloque());
        generador.agregarSaltoIncondicional(etiquetaInicio);
        generador.agregarEtiqueta(etiquetaSalida);
        return null;
    }

    @Override
    public Object visitBucleHacer(YParser.BucleHacerContext ctx)
    {
        String etiquetaInicio = generador.generarEtiqueta();
        generador.agregarEtiqueta(etiquetaInicio);
        visit(ctx.bloque());
        ResultadoC3D resCondicion = (ResultadoC3D) visit(ctx.expresion());
        if (resCondicion.getTipo() != TipoDato.BOOLEANO && resCondicion.getTipo() != TipoDato.ERROR)
        {
            consola.append("Error Semántico en línea " + ctx.getStart().getLine() + ": La condición HACER MIENTRAS debe ser booleana.\n");
            hayErroresSemanticos = true;
        }
        generador.agregarSaltoCondicional(resCondicion.getValorC3D(), "==", "1", etiquetaInicio);
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
        String etiquetaInicio = generador.generarEtiqueta();
        String etiquetaSalida = generador.generarEtiqueta();
        generador.agregarEtiqueta(etiquetaInicio);
        if (ctx.expresion() != null)
        {
            ResultadoC3D resCondicion = (ResultadoC3D) visit(ctx.expresion());
            if (resCondicion.getTipo() != TipoDato.BOOLEANO && resCondicion.getTipo() != TipoDato.ERROR)
            {
                consola.append("Error Semántico en línea " + ctx.getStart().getLine() + ": La condición PARA debe ser booleana.\n");
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
    public Object visitCondicionalElegir(YParser.CondicionalElegirContext ctx)
    {
        ResultadoC3D resVariable = (ResultadoC3D) visit(ctx.expresion());
        String etiquetaSalida = generador.generarEtiqueta();
        for (YParser.CasoContext casoCtx : ctx.caso())
        {
            ResultadoC3D resCaso = (ResultadoC3D) visit(casoCtx.expresion());
            String etiquetaBloque = generador.generarEtiqueta();
            String etiquetaSiguiente = generador.generarEtiqueta();
            generador.agregarSaltoCondicional(resVariable.getValorC3D(), "==", resCaso.getValorC3D(), etiquetaBloque);
            generador.agregarSaltoIncondicional(etiquetaSiguiente);
            generador.agregarEtiqueta(etiquetaBloque);
            visit(casoCtx.bloque());
            generador.agregarSaltoIncondicional(etiquetaSalida);
            generador.agregarEtiqueta(etiquetaSiguiente);
        }
        if (ctx.casoDefault() != null)
        {
            visit(ctx.casoDefault().bloque());
        }
        generador.agregarEtiqueta(etiquetaSalida);
        return null;
    }
}