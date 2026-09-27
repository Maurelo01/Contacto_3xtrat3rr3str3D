package mycompany.contacto_3xtrat3rr3str3d.piglatin;

import mycompany.contacto_3xtrat3rr3str3d.PigLatinParser;
import mycompany.contacto_3xtrat3rr3str3d.simbolos.Simbolo;
import mycompany.contacto_3xtrat3rr3str3d.simbolos.SimboloVariable;
import mycompany.contacto_3xtrat3rr3str3d.simbolos.TipoDato;
import mycompany.contacto_3xtrat3rr3str3d.utils.ControlTipos;
import mycompany.contacto_3xtrat3rr3str3d.utils.ResultadoC3D;
import mycompany.contacto_3xtrat3rr3str3d.visitors.PigLatinCustomVisitor;

public class GestorExpresionesPig extends PigLatinGestorBase 
{
    public GestorExpresionesPig(PigLatinCustomVisitor visitor)
    {
        super(visitor);
    }
    
    public Object procesarAccesoVariableOAtributo(PigLatinParser.AccesoVariableOAtributoContext ctx)
    {
        boolean esAccesoSimple = ctx.acceso().getChildCount() == 1;
        if (esAccesoSimple)
        {
            String idVariable = ctx.acceso().ID(0).getText();
            Simbolo sim = tabla.buscar(idVariable);
            int linea = ctx.acceso().ID(0).getSymbol().getLine();
            int columna = ctx.acceso().ID(0).getSymbol().getCharPositionInLine();
            if (sim == null)
            {
                reportarError(linea, columna, "La variable " + idVariable + " no ha sido declarada.");
                return new ResultadoC3D(TipoDato.ERROR, "");
            }
            if (sim instanceof SimboloVariable && !((SimboloVariable)sim).isInicializado() && !sim.isEnHeap())
            {
                reportarError(linea, columna, "La variable local " + idVariable + " no está inicializada.");
                return new ResultadoC3D(TipoDato.ERROR, "");
            }
            String temporal = generador.generarTemporal();
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
            ResultadoC3D resDireccion = calcularDireccionAcceso(ctx.acceso());
            if (resDireccion.getTipo() == TipoDato.ERROR) return resDireccion;
            String tempValor = generador.generarTemporal();
            generador.agregarGetHeap(tempValor, resDireccion.getValorC3D());
            return new ResultadoC3D(resDireccion.getTipo(), tempValor);
        }
    }
    
    public Object procesarSumaResta(PigLatinParser.SumaRestaContext ctx)
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
            reportarError(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(), "Incompatibilidad de tipos.");
            return new ResultadoC3D(TipoDato.ERROR, "");
        }
        String temporal = generador.generarTemporal();
        generador.agregarAsignacion(temporal, izq.getValorC3D(), operador, der.getValorC3D());
        return new ResultadoC3D(tipoResultado, temporal);
    }
    
    public Object procesarMultDiv(PigLatinParser.MultDivContext ctx)
    {
        ResultadoC3D izq = (ResultadoC3D) visitor.visit(ctx.expresion(0));
        ResultadoC3D der = (ResultadoC3D) visitor.visit(ctx.expresion(1));
        if (izq.getTipo() == TipoDato.ERROR || der.getTipo() == TipoDato.ERROR) return new ResultadoC3D(TipoDato.ERROR, "");
        TipoDato tipoResultado = ControlTipos.resolverAritmetica(izq.getTipo(), der.getTipo());
        if (tipoResultado == TipoDato.ERROR)
        {
            reportarError(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(), "Incompatibilidad de tipos.");
            return new ResultadoC3D(TipoDato.ERROR, "");
        }
        String temporal = generador.generarTemporal();
        String operador = ctx.POR() != null ? "*" : (ctx.DIVISION() != null ? "/" : "%");
        if (operador.equals("%")) generador.agregarAsignacion(temporal, "fmod(" + izq.getValorC3D() + ", " + der.getValorC3D() + ")");
        else generador.agregarAsignacion(temporal, izq.getValorC3D(), operador, der.getValorC3D());
        return new ResultadoC3D(tipoResultado, temporal);
    }
    
    public Object procesarComparacion(PigLatinParser.ComparacionContext ctx)
    {
        ResultadoC3D izq = (ResultadoC3D) visitor.visit(ctx.expresion(0));
        ResultadoC3D der = (ResultadoC3D) visitor.visit(ctx.expresion(1));
        if (izq.getTipo() == TipoDato.ERROR || der.getTipo() == TipoDato.ERROR) return new ResultadoC3D(TipoDato.ERROR, "");
        TipoDato resultado = ControlTipos.resolverRelacional(izq.getTipo(), der.getTipo());
        if (resultado == TipoDato.ERROR)
        {
            reportarError(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(), "Comparación inválida.");
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
    
    public Object procesarIgualdad(PigLatinParser.IgualdadContext ctx)
    {
        ResultadoC3D izq = (ResultadoC3D) visitor.visit(ctx.expresion(0));
        ResultadoC3D der = (ResultadoC3D) visitor.visit(ctx.expresion(1));
        if (izq.getTipo() == TipoDato.ERROR || der.getTipo() == TipoDato.ERROR) return new ResultadoC3D(TipoDato.ERROR, "");
        TipoDato resultado = ControlTipos.resolverIgualdad(izq.getTipo(), der.getTipo());
        if (resultado == TipoDato.ERROR)
        {
            reportarError(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(), "Igualdad inválida.");
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
    
    public Object procesarAndLogico(PigLatinParser.AndLogicoContext ctx)
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
    
    public Object procesarOrLogico(PigLatinParser.OrLogicoContext ctx)
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

    public Object procesarNegacion(PigLatinParser.NegacionContext ctx)
    {
        ResultadoC3D tipo = (ResultadoC3D) visitor.visit(ctx.factor());
        TipoDato resultado = ControlTipos.resolverUnariaLogica(tipo.getTipo());
        if (resultado == TipoDato.ERROR)
        {
            reportarError(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(), "Negación inválida.");
            return new ResultadoC3D(TipoDato.ERROR, "");
        }
        String temporal = generador.generarTemporal();
        generador.agregarAsignacion(temporal, "1", "-", tipo.getValorC3D());
        return new ResultadoC3D(resultado, temporal);
    }
}
