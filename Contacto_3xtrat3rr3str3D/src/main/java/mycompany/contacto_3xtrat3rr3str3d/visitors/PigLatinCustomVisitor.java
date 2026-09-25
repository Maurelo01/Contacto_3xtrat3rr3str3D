package mycompany.contacto_3xtrat3rr3str3d.visitors;

import mycompany.contacto_3xtrat3rr3str3d.simbolos.*;
import mycompany.contacto_3xtrat3rr3str3d.c3d.GeneradorFuncionesNativas;
import mycompany.contacto_3xtrat3rr3str3d.c3d.GeneradorC3D;
import javax.swing.JTextArea;
import mycompany.contacto_3xtrat3rr3str3d.PigLatinBaseVisitor;
import mycompany.contacto_3xtrat3rr3str3d.PigLatinParser;
import mycompany.contacto_3xtrat3rr3str3d.utils.*;

public class PigLatinCustomVisitor extends PigLatinBaseVisitor<Object>
{
    private TablaSimbolos tabla;
    private JTextArea consola;
    public boolean hayErroresSemanticos = false;
    private GeneradorC3D generador = GeneradorC3D.getInstancia();
    public PigLatinCustomVisitor(TablaSimbolos tabla, JTextArea consola)
    {
        this.tabla = tabla;
        this.consola = consola;
    }
    
    // Ambitos e Inicio
    
    @Override
    public Object visitPrograma(PigLatinParser.ProgramaContext ctx)
    {
        if (ctx.seccionDeclaraciones() != null)
        {
            visit(ctx.seccionDeclaraciones());
        }
        tabla.resetearOffsetLocal();
        tabla.entrarAmbito();
        visit(ctx.seccionCodigo());
        tabla.salirAmbito();
        return null;
    }
    
    // Declaraciones de variables
    
    @Override
    public Object visitDeclaracionVariable(PigLatinParser.DeclaracionVariableContext ctx)
    {
        String id = ctx.ID().getText();
        String tipoStr = ctx.tipo().getText();
        int linea = ctx.ID().getSymbol().getLine();
        int columna = ctx.ID().getSymbol().getCharPositionInLine();
        TipoDato tipoNormalizado = ControlTipos.normalizarTipo(tipoStr);
        SimboloVariable nuevoSimbolo = new SimboloVariable(id, tipoNormalizado, linea, columna);
        if (tabla.buscar(id) != null)
        {
            consola.append("Error Semántico en línea " + linea + ": La variable '" + id + "' ya ha sido declarada.\n");
            hayErroresSemanticos = true;
            return null;
        }
        if (ctx.valorInicial() == null)
        {
            nuevoSimbolo.setInicializado(false);
            tabla.insertar(nuevoSimbolo);
            return null;
        }
        nuevoSimbolo.setInicializado(true);
        tabla.insertar(nuevoSimbolo);
        ResultadoC3D resExpr = (ResultadoC3D) visit(ctx.valorInicial());
        if (resExpr != null && resExpr.getTipo() != TipoDato.ERROR)
        {
            if (!ControlTipos.esAsignacionValida(tipoNormalizado, resExpr.getTipo()))
            {
                consola.append("Error Semántico en línea " + linea + ": No se puede asignar " + resExpr.getTipo() + " a " + tipoNormalizado + ".\n");
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
    
    // Lectura Memoria
    
    @Override
    public Object visitToFactor(PigLatinParser.ToFactorContext ctx)
    {
        return visit(ctx.factor());
    }

    @Override
    public Object visitAccesoVariableOAtributo(PigLatinParser.AccesoVariableOAtributoContext ctx)
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
        if (sim instanceof SimboloVariable && !((SimboloVariable)sim).isInicializado() && !sim.isEnHeap())
        {
            consola.append("Error Semántico en línea " + linea + ": La variable local '" + idVariable + "' no está inicializada.\n");
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
    
    // Impresion
    
    @Override
    public Object visitImpresion(PigLatinParser.ImpresionContext ctx)
    {
        for (PigLatinParser.ExpresionContext exprCtx : ctx.expresion())
        {
            ResultadoC3D resExpr = (ResultadoC3D) visit(exprCtx);
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
    
    // Asignaciones
    
    @Override
    public Object visitAsigGeneral(PigLatinParser.AsigGeneralContext ctx)
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
        return null;
    }
    
    // Matematicas
    
    @Override
    public Object visitIncrementoGeneral(PigLatinParser.IncrementoGeneralContext ctx)
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
        String operador = ctx.MAS_ABREVIADO() != null ? "+" : "-";
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
    public Object visitSumaResta(PigLatinParser.SumaRestaContext ctx)
    {
        ResultadoC3D izq = (ResultadoC3D) visit(ctx.expresion(0));
        ResultadoC3D der = (ResultadoC3D) visit(ctx.expresion(1));
        if (izq.getTipo() == TipoDato.ERROR || der.getTipo() == TipoDato.ERROR)
        {
            return new ResultadoC3D(TipoDato.ERROR, "");
        }

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
            consola.append("Error Semántico en línea " + ctx.getStart().getLine() + ": Incompatibilidad de tipos.\n");
            hayErroresSemanticos = true;
            return new ResultadoC3D(TipoDato.ERROR, "");
        }
        String temporal = generador.generarTemporal();
        generador.agregarAsignacion(temporal, izq.getValorC3D(), operador, der.getValorC3D());
        return new ResultadoC3D(tipoResultado, temporal);
    }

    @Override
    public Object visitMultDiv(PigLatinParser.MultDivContext ctx)
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
            consola.append("Error Semántico en línea " + ctx.getStart().getLine() + ": Incompatibilidad de tipos.\n");
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
    
    // Logica booleana
    
    @Override
    public Object visitComparacion(PigLatinParser.ComparacionContext ctx)
    {
        ResultadoC3D izq = (ResultadoC3D) visit(ctx.expresion(0));
        ResultadoC3D der = (ResultadoC3D) visit(ctx.expresion(1));
        if (izq.getTipo() == TipoDato.ERROR || der.getTipo() == TipoDato.ERROR)
        {
            return new ResultadoC3D(TipoDato.ERROR, "");
        }
        TipoDato resultado = ControlTipos.resolverRelacional(izq.getTipo(), der.getTipo());
        if (resultado == TipoDato.ERROR)
        {
            consola.append("Error Semántico en línea " + ctx.getStart().getLine() + ": Comparación inválida.\n");
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
    public Object visitIgualdad(PigLatinParser.IgualdadContext ctx)
    {
        ResultadoC3D izq = (ResultadoC3D) visit(ctx.expresion(0));
        ResultadoC3D der = (ResultadoC3D) visit(ctx.expresion(1));
        if (izq.getTipo() == TipoDato.ERROR || der.getTipo() == TipoDato.ERROR)
        {
            return new ResultadoC3D(TipoDato.ERROR, "");
        }
        TipoDato resultado = ControlTipos.resolverIgualdad(izq.getTipo(), der.getTipo());
        if (resultado == TipoDato.ERROR)
        {
            consola.append("Error Semántico en línea " + ctx.getStart().getLine() + ": Igualdad inválida.\n");
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
    public Object visitAndLogico(PigLatinParser.AndLogicoContext ctx)
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
        generador.agregarSaltoCondicional(der.getValorC3D(), "==", "1", etVerdadera);
        generador.agregarSaltoIncondicional(etFalsa);
        generador.agregarEtiqueta(etVerdadera);
        generador.agregarAsignacion(temporal, "1");
        generador.agregarSaltoIncondicional(etSalida);
        generador.agregarEtiqueta(etFalsa);
        generador.agregarAsignacion(temporal, "0");
        generador.agregarEtiqueta(etSalida);
        return new ResultadoC3D(TipoDato.BOOLEANO, temporal);
    }
    @Override
    public Object visitOrLogico(PigLatinParser.OrLogicoContext ctx)
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
        generador.agregarSaltoCondicional(der.getValorC3D(), "==", "1", etVerdadera);
        generador.agregarSaltoIncondicional(etFalsa);
        generador.agregarEtiqueta(etVerdadera);
        generador.agregarAsignacion(temporal, "1");
        generador.agregarSaltoIncondicional(etSalida);
        generador.agregarEtiqueta(etFalsa);
        generador.agregarAsignacion(temporal, "0");
        generador.agregarEtiqueta(etSalida);
        return new ResultadoC3D(TipoDato.BOOLEANO, temporal);
    }
    @Override
    public Object visitNegacion(PigLatinParser.NegacionContext ctx)
    {
        ResultadoC3D tipo = (ResultadoC3D) visit(ctx.factor());
        TipoDato resultado = ControlTipos.resolverUnariaLogica(tipo.getTipo());
        if (resultado == TipoDato.ERROR)
        {
            consola.append("Error Semántico en línea " + ctx.getStart().getLine() + ": Negación inválida.\n");
            hayErroresSemanticos = true;
            return new ResultadoC3D(TipoDato.ERROR, "");
        }
        String temporal = generador.generarTemporal();
        generador.agregarAsignacion(temporal, "1", "-", tipo.getValorC3D());
        return new ResultadoC3D(resultado, temporal);
    }
    @Override
    public Object visitParentesis(PigLatinParser.ParentesisContext ctx)
    {
        return visit(ctx.expresion());
    }
    
    // Estructuras de control
    
    @Override
    public Object visitCondicional(PigLatinParser.CondicionalContext ctx)
    {
        String etiquetaSalida = generador.generarEtiqueta();
        for (int i = 0; i < ctx.expresion().size(); i++)
        {
            ResultadoC3D resCondicion = (ResultadoC3D) visit(ctx.expresion(i));
            if (resCondicion.getTipo() != TipoDato.BOOLEANO && resCondicion.getTipo() != TipoDato.ERROR)
            {
                consola.append("Error Semántico en línea " + ctx.getStart().getLine() + ": Condición SI debe ser booleana.\n");
                hayErroresSemanticos = true;
            }
            String etiquetaFalsa = generador.generarEtiqueta();
            generador.agregarSaltoCondicional(resCondicion.getValorC3D(), "==", "0", etiquetaFalsa);
            visit(ctx.bloque(i));
            generador.agregarSaltoIncondicional(etiquetaSalida);
            generador.agregarEtiqueta(etiquetaFalsa);
        }
        // Bloque ALITER final
        if (ctx.bloque().size() > ctx.expresion().size())
        {
            visit(ctx.bloque(ctx.bloque().size() - 1));
        }
        generador.agregarEtiqueta(etiquetaSalida);
        return null;
    }
    @Override
    public Object visitBucleDum(PigLatinParser.BucleDumContext ctx)
    {
        String etiquetaInicio = generador.generarEtiqueta();
        String etiquetaSalida = generador.generarEtiqueta();
        generador.agregarEtiqueta(etiquetaInicio);
        ResultadoC3D resCondicion = (ResultadoC3D) visit(ctx.expresion());
        if (resCondicion.getTipo() != TipoDato.BOOLEANO && resCondicion.getTipo() != TipoDato.ERROR)
        {
            consola.append("Error Semántico en línea " + ctx.getStart().getLine() + ": Condición DUM debe ser booleana.\n");
            hayErroresSemanticos = true;
        }
        generador.agregarSaltoCondicional(resCondicion.getValorC3D(), "==", "0", etiquetaSalida);
        visit(ctx.bloque());
        generador.agregarSaltoIncondicional(etiquetaInicio);
        generador.agregarEtiqueta(etiquetaSalida);
        return null;
    }
    @Override
    public Object visitBucleFacere(PigLatinParser.BucleFacereContext ctx)
    {
        String etiquetaInicio = generador.generarEtiqueta();
        generador.agregarEtiqueta(etiquetaInicio);
        visit(ctx.bloque());
        ResultadoC3D resCondicion = (ResultadoC3D) visit(ctx.expresion());
        if (resCondicion.getTipo() != TipoDato.BOOLEANO && resCondicion.getTipo() != TipoDato.ERROR)
        {
            consola.append("Error Semántico en línea " + ctx.getStart().getLine() + ": Condición FACERE DUM debe ser booleana.\n");
            hayErroresSemanticos = true;
        }
        generador.agregarSaltoCondicional(resCondicion.getValorC3D(), "==", "1", etiquetaInicio);
        return null;
    }
    @Override
    public Object visitBuclePer(PigLatinParser.BuclePerContext ctx)
    {
        tabla.entrarAmbito();
        visit(ctx.declaracion());
        String etiquetaInicio = generador.generarEtiqueta();
        String etiquetaSalida = generador.generarEtiqueta();
        generador.agregarEtiqueta(etiquetaInicio);
        ResultadoC3D resCondicion = (ResultadoC3D) visit(ctx.expresion());
        if (resCondicion.getTipo() != TipoDato.BOOLEANO && resCondicion.getTipo() != TipoDato.ERROR)
        {
            consola.append("Error Semántico en línea " + ctx.getStart().getLine() + ": Condición PER debe ser booleana.\n");
            hayErroresSemanticos = true;
        }
        generador.agregarSaltoCondicional(resCondicion.getValorC3D(), "==", "0", etiquetaSalida);
        visit(ctx.bloque());
        visit(ctx.actualizacion());
        generador.agregarSaltoIncondicional(etiquetaInicio);
        generador.agregarEtiqueta(etiquetaSalida);
        tabla.salirAmbito();
        return null;
    }
    
    @Override
    public Object visitActualizacion(PigLatinParser.ActualizacionContext ctx)
    {
        if (ctx.asignacion() != null)
        {
            return visit(ctx.asignacion());
        }
        else
        {
            String idVariable = ctx.ID().getText();
            Simbolo sim = tabla.buscar(idVariable);
            int linea = ctx.getStart().getLine();
            if (sim == null)
            {
                consola.append("Error Semántico en línea " + linea + ": La variable '" + idVariable + "' no existe.\n");
                hayErroresSemanticos = true;
                return null;
            }
            String temporalAnterior = generador.generarTemporal();
            String temporalNuevo = generador.generarTemporal();
            String operador = ctx.MAS_ABREVIADO() != null ? "+" : "-";
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
    }
    
    // Literales
    
    @Override
    public Object visitNumLiteral(PigLatinParser.NumLiteralContext ctx)
    {
        return new ResultadoC3D(TipoDato.ENTERO, ctx.getText());
    }   
    @Override
    public Object visitDecLiteral(PigLatinParser.DecLiteralContext ctx)
    {
        return new ResultadoC3D(TipoDato.DECIMAL, ctx.getText());
    }
    @Override
    public Object visitTextLiteral(PigLatinParser.TextLiteralContext ctx)
    {
        return new ResultadoC3D(TipoDato.CADENA, ctx.getText());
    }
    @Override
    public Object visitCharLiteral(PigLatinParser.CharLiteralContext ctx)
    {
        return new ResultadoC3D(TipoDato.CARACTER, ctx.getText());
    }
    @Override
    public Object visitTrueLiteral(PigLatinParser.TrueLiteralContext ctx)
    {
        return new ResultadoC3D(TipoDato.BOOLEANO, "1");
    }

    @Override
    public Object visitFalseLiteral(PigLatinParser.FalseLiteralContext ctx)
    {
        return new ResultadoC3D(TipoDato.BOOLEANO, "0");
    }
}
