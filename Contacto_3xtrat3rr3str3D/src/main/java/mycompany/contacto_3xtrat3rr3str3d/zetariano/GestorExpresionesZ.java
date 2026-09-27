package mycompany.contacto_3xtrat3rr3str3d.zetariano;

import java.util.ArrayList;
import java.util.List;
import mycompany.contacto_3xtrat3rr3str3d.ZetarianoParser;
import mycompany.contacto_3xtrat3rr3str3d.simbolos.Simbolo;
import mycompany.contacto_3xtrat3rr3str3d.simbolos.SimboloVariable;
import mycompany.contacto_3xtrat3rr3str3d.simbolos.TipoDato;
import mycompany.contacto_3xtrat3rr3str3d.utils.ControlTipos;
import mycompany.contacto_3xtrat3rr3str3d.utils.GestorArreglos;
import mycompany.contacto_3xtrat3rr3str3d.utils.ResultadoC3D;
import mycompany.contacto_3xtrat3rr3str3d.visitors.ZetarianoCustomVisitor;

public class GestorExpresionesZ extends ZetarianoGestorBase
{
    public GestorExpresionesZ(ZetarianoCustomVisitor visitor)
    {
        super(visitor);
    }
    
    public Object procesarSumaResta(ZetarianoParser.SumaRestaContext ctx)
    {
        ResultadoC3D izq = (ResultadoC3D) visitor.visit(ctx.expresion(0));
        ResultadoC3D der = (ResultadoC3D) visitor.visit(ctx.expresion(1));
        if (izq.getTipo() == TipoDato.ERROR || der.getTipo() == TipoDato.ERROR) return new ResultadoC3D(TipoDato.ERROR, "");
        TipoDato tipoResultado;
        String operador = ctx.MAS() != null ? "+" : "-";
        if (ctx.MAS() != null) tipoResultado = ControlTipos.resolverSuma(izq.getTipo(), der.getTipo());
        else tipoResultado = ControlTipos.resolverAritmetica(izq.getTipo(), der.getTipo());
        if (tipoResultado == TipoDato.ERROR)
        {
            reportarError(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(), "Incompatibilidad de tipos en la operación (" + izq.getTipo() + " " + operador + " " + der.getTipo() + ").");
            return new ResultadoC3D(TipoDato.ERROR, "");
        }
        String temporal = generador.generarTemporal();
        generador.agregarAsignacion(temporal, izq.getValorC3D(), operador, der.getValorC3D());
        return new ResultadoC3D(tipoResultado, temporal);
    }

    public Object procesarMultDiv(ZetarianoParser.MultDivContext ctx)
    {
        ResultadoC3D izq = (ResultadoC3D) visitor.visit(ctx.expresion(0));
        ResultadoC3D der = (ResultadoC3D) visitor.visit(ctx.expresion(1));
        if (izq.getTipo() == TipoDato.ERROR || der.getTipo() == TipoDato.ERROR) return new ResultadoC3D(TipoDato.ERROR, "");
        TipoDato tipoResultado = ControlTipos.resolverAritmetica(izq.getTipo(), der.getTipo());
        if (tipoResultado == TipoDato.ERROR)
        {
            reportarError(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(), "Incompatibilidad de tipos (" + izq.getTipo() + " y " + der.getTipo() + ").");
            return new ResultadoC3D(TipoDato.ERROR, "");
        }
        String temporal = generador.generarTemporal();
        String operador = ctx.POR() != null ? "*" : (ctx.DIVISION() != null ? "/" : "%");
        if (operador.equals("%")) generador.agregarAsignacion(temporal, "fmod(" + izq.getValorC3D() + ", " + der.getValorC3D() + ")");
        else generador.agregarAsignacion(temporal, izq.getValorC3D(), operador, der.getValorC3D());
        return new ResultadoC3D(tipoResultado, temporal);
    }

    public Object procesarComparacion(ZetarianoParser.ComparacionContext ctx)
    {
        ResultadoC3D izq = (ResultadoC3D) visitor.visit(ctx.expresion(0));
        ResultadoC3D der = (ResultadoC3D) visitor.visit(ctx.expresion(1));
        if (izq.getTipo() == TipoDato.ERROR || der.getTipo() == TipoDato.ERROR) return new ResultadoC3D(TipoDato.ERROR, "");
        TipoDato resultado = ControlTipos.resolverRelacional(izq.getTipo(), der.getTipo());
        if (resultado == TipoDato.ERROR)
        {
            reportarError(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(), "No se pueden comparar relacionalmente " + izq.getTipo() + " y " + der.getTipo() + ".");
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

    public Object procesarIgualdad(ZetarianoParser.IgualdadContext ctx)
    {
        ResultadoC3D izq = (ResultadoC3D) visitor.visit(ctx.expresion(0));
        ResultadoC3D der = (ResultadoC3D) visitor.visit(ctx.expresion(1));
        if (izq.getTipo() == TipoDato.ERROR || der.getTipo() == TipoDato.ERROR) return new ResultadoC3D(TipoDato.ERROR, "");
        TipoDato resultado = ControlTipos.resolverIgualdad(izq.getTipo(), der.getTipo());
        if (resultado == TipoDato.ERROR)
        {
            reportarError(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(), "No se puede evaluar igualdad entre " + izq.getTipo() + " y " + der.getTipo() + ".");
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

    public Object procesarAndLogico(ZetarianoParser.AndLogicoContext ctx)
    {
        String temporal = generador.generarTemporal();
        String etFalsa = generador.generarEtiqueta();
        String etVerdadera = generador.generarEtiqueta();
        String etSalida = generador.generarEtiqueta();
        ResultadoC3D izq = (ResultadoC3D) visitor.visit(ctx.expresion(0));
        if (izq.getTipo() == TipoDato.ERROR) return new ResultadoC3D(TipoDato.ERROR, "");
        generador.agregarSaltoCondicional(izq.getValorC3D(), "==", "0", etFalsa);
        ResultadoC3D der = (ResultadoC3D) visitor.visit(ctx.expresion(1));
        if (der.getTipo() == TipoDato.ERROR) return new ResultadoC3D(TipoDato.ERROR, "");
        TipoDato resultado = ControlTipos.resolverLogica(izq.getTipo(), der.getTipo());
        if (resultado == TipoDato.ERROR)
        {
            reportarError(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(), "El operador && requiere booleanos.");
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

    public Object procesarOrLogico(ZetarianoParser.OrLogicoContext ctx)
    {
        String temporal = generador.generarTemporal();
        String etFalsa = generador.generarEtiqueta();
        String etVerdadera = generador.generarEtiqueta();
        String etSalida = generador.generarEtiqueta();
        ResultadoC3D izq = (ResultadoC3D) visitor.visit(ctx.expresion(0));
        if (izq.getTipo() == TipoDato.ERROR) return new ResultadoC3D(TipoDato.ERROR, "");
        generador.agregarSaltoCondicional(izq.getValorC3D(), "==", "1", etVerdadera);
        ResultadoC3D der = (ResultadoC3D) visitor.visit(ctx.expresion(1));
        if (der.getTipo() == TipoDato.ERROR) return new ResultadoC3D(TipoDato.ERROR, "");
        TipoDato resultado = ControlTipos.resolverLogica(izq.getTipo(), der.getTipo());
        if (resultado == TipoDato.ERROR) {
            reportarError(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(), "El operador || requiere booleanos.");
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

    public Object procesarNegacionUnaria(ZetarianoParser.NegacionUnariaContext ctx)
    {
        ResultadoC3D tipo = (ResultadoC3D) visitor.visit(ctx.factor());
        TipoDato resultado = ControlTipos.resolverUnariaAritmetica(tipo.getTipo());
        if (resultado == TipoDato.ERROR)
        {
            visitor.setHayErroresSemanticos(true);
            return new ResultadoC3D(TipoDato.ERROR, "");
        }
        String temporal = generador.generarTemporal();
        generador.agregarAsignacion(temporal, "-" + tipo.getValorC3D());
        return new ResultadoC3D(resultado, temporal);
    }

    public Object procesarNegacionLogica(ZetarianoParser.NegacionLogicaContext ctx)
    {
        ResultadoC3D tipo = (ResultadoC3D) visitor.visit(ctx.factor());
        TipoDato resultado = ControlTipos.resolverUnariaLogica(tipo.getTipo());
        if (resultado == TipoDato.ERROR)
        {
            reportarError(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(), "No se puede aplicar negación lógica a un tipo " + tipo.getTipo() + ".");
            return new ResultadoC3D(TipoDato.ERROR, "");
        }
        String temporal = generador.generarTemporal();
        generador.agregarAsignacion(temporal, "1", "-", tipo.getValorC3D());
        return new ResultadoC3D(resultado, temporal);
    }

    public Object procesarTernario(ZetarianoParser.TernarioContext ctx)
    {
        ResultadoC3D resCond = (ResultadoC3D) visitor.visit(ctx.expresion(0));
        if (resCond == null || resCond.getTipo() == TipoDato.ERROR) return new ResultadoC3D(TipoDato.ERROR, "");
        if (resCond.getTipo() != TipoDato.BOOLEANO)
        {
            reportarError(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(), "La condición del operador ternario debe ser booleana.");
            return new ResultadoC3D(TipoDato.ERROR, "");
        }
        String etVerdadera = generador.generarEtiqueta();
        String etFalsa = generador.generarEtiqueta();
        String etSalida = generador.generarEtiqueta();
        String tempResultado = generador.generarTemporal();
        generador.agregarSaltoCondicional(resCond.getValorC3D(), "==", "1", etVerdadera);
        generador.agregarSaltoIncondicional(etFalsa);
        generador.agregarEtiqueta(etVerdadera);
        ResultadoC3D resV = (ResultadoC3D) visitor.visit(ctx.expresion(1));
        if (resV == null || resV.getTipo() == TipoDato.ERROR) return new ResultadoC3D(TipoDato.ERROR, "");
        generador.agregarAsignacion(tempResultado, resV.getValorC3D());
        generador.agregarSaltoIncondicional(etSalida);
        generador.agregarEtiqueta(etFalsa);
        ResultadoC3D resF = (ResultadoC3D) visitor.visit(ctx.expresion(2));
        if (resF == null || resF.getTipo() == TipoDato.ERROR) return new ResultadoC3D(TipoDato.ERROR, "");
        generador.agregarAsignacion(tempResultado, resF.getValorC3D());
        generador.agregarEtiqueta(etSalida);
        TipoDato tipoResultado = ControlTipos.resolverTernario(resV.getTipo(), resF.getTipo());
        if (tipoResultado == TipoDato.ERROR)
        {
            reportarError(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(), "Tipos incompatibles en operador ternario: " + resV.getTipo() + " y " + resF.getTipo() + ".");
            return new ResultadoC3D(TipoDato.ERROR, "");
        }
        return new ResultadoC3D(tipoResultado, tempResultado);
    }
    
    public Object procesarAccesoVariableOAtributo(ZetarianoParser.AccesoVariableOAtributoContext ctx)
    {
        boolean esAccesoSimple = ctx.acceso().getChildCount() == 1;
        if (esAccesoSimple)
        {
            String idVariable = ctx.acceso().ID(0).getText();
            Simbolo sim = tabla.buscar(idVariable);
            int linea = ctx.acceso().ID(0).getSymbol().getLine();
            int columna = ctx.acceso().ID(0).getSymbol().getCharPositionInLine();
            Simbolo simAtributoThis = null;
            if (sim == null) simAtributoThis = resolverAtributoDeThis(idVariable);
            if (sim == null && simAtributoThis == null)
            {
                reportarError(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(), "Variable no declarada.");
                return new ResultadoC3D(TipoDato.ERROR, "");
            }
            if (simAtributoThis != null)
            {
                Simbolo simThis = tabla.buscar("this");
                generador.agregarComentario("Lectura implícita de this." + idVariable + "");
                String tempThis = generador.generarTemporal();
                String tempPosThis = generador.generarTemporal();
                String tempDirFinal = generador.generarTemporal();
                generador.agregarAsignacion(tempPosThis, "punteroStack", "+", String.valueOf(simThis.getOffset()));
                generador.agregarGetStack(tempThis, tempPosThis);
                generador.agregarAsignacion(tempDirFinal, tempThis, "+", String.valueOf(simAtributoThis.getOffset()));
                String temporal = generador.generarTemporal();
                generador.agregarGetHeap(temporal, tempDirFinal);
                return new ResultadoC3D(simAtributoThis.getTipo(), temporal);
            }
            if (sim instanceof SimboloVariable && !((SimboloVariable)sim).isInicializado() && !sim.isEnHeap())
            {
                reportarError(linea, columna, "La variable local " + idVariable + " podría no haber sido inicializada.");
                return new ResultadoC3D(TipoDato.ERROR, "");
            }
            String temporal = generador.generarTemporal();
            if (tabla.esGlobal(idVariable)) 
            {
                Simbolo simThis = tabla.buscar("this");
                if (simThis != null) 
                {
                    generador.agregarComentario("Lectura implícita de this." + idVariable + "");
                    String tempThis = generador.generarTemporal();
                    String tempPosThis = generador.generarTemporal();
                    String tempDirFinal = generador.generarTemporal();
                    generador.agregarAsignacion(tempPosThis, "punteroStack", "+", String.valueOf(simThis.getOffset()));
                    generador.agregarGetStack(tempThis, tempPosThis);
                    generador.agregarAsignacion(tempDirFinal, tempThis, "+", String.valueOf(sim.getOffset()));
                    generador.agregarGetHeap(temporal, tempDirFinal);
                    return new ResultadoC3D(sim.getTipo(), temporal);
                }
            }
            if (sim.isEnHeap()) generador.agregarGetHeap(temporal, String.valueOf(sim.getOffset()));
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
            ResultadoC3D direccion = calcularDireccionAcceso(ctx.acceso());
            if (direccion.getTipo() == TipoDato.ERROR) return direccion;
            String tempValor = generador.generarTemporal();
            generador.agregarGetHeap(tempValor, direccion.getValorC3D());
            return new ResultadoC3D(direccion.getTipo(), tempValor);
        }
    }
    
    public Object procesarInstanciaArray(ZetarianoParser.InstanciaArrayContext ctx)
    {
        String tipoStr = ctx.tipoBase().getText();
        TipoDato tipoNorm = ControlTipos.normalizarTipo(tipoStr);
        int tamañoTotal = 1;
        List<Integer> tamaños = new ArrayList<>();
        for (ZetarianoParser.ExpresionContext exp : ctx.expresion())
        {
            int tam = Integer.parseInt(exp.getText()); 
            tamaños.add(tam);
            tamañoTotal *= tam;
        }
        ResultadoC3D resInstancia = GestorArreglos.instanciarArregloVacio(tamañoTotal, tipoNorm, generador);
        resInstancia.setTamañosDimensiones(tamaños);
        return resInstancia;
    }
}
