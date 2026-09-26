package mycompany.contacto_3xtrat3rr3str3d.zetariano;

import java.util.List;
import mycompany.contacto_3xtrat3rr3str3d.ZetarianoParser;
import mycompany.contacto_3xtrat3rr3str3d.simbolos.Simbolo;
import mycompany.contacto_3xtrat3rr3str3d.simbolos.SimboloVariable;
import mycompany.contacto_3xtrat3rr3str3d.simbolos.TipoDato;
import mycompany.contacto_3xtrat3rr3str3d.utils.ControlTipos;
import mycompany.contacto_3xtrat3rr3str3d.utils.GestorPunteros;
import mycompany.contacto_3xtrat3rr3str3d.utils.ResultadoC3D;
import mycompany.contacto_3xtrat3rr3str3d.visitors.ZetarianoCustomVisitor;
import org.antlr.v4.runtime.tree.TerminalNode;

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
            reportarError(ctx.getStart().getLine(), "Incompatibilidad de tipos en la operación (" + izq.getTipo() + " " + operador + " " + der.getTipo() + ").");
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
            reportarError(ctx.getStart().getLine(), "Incompatibilidad de tipos (" + izq.getTipo() + " y " + der.getTipo() + ").");
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
            reportarError(ctx.getStart().getLine(), "No se pueden comparar relacionalmente " + izq.getTipo() + " y " + der.getTipo() + ".");
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
            reportarError(ctx.getStart().getLine(), "No se puede evaluar igualdad entre " + izq.getTipo() + " y " + der.getTipo() + ".");
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
            reportarError(ctx.getStart().getLine(), "El operador && requiere booleanos.");
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
            reportarError(ctx.getStart().getLine(), "El operador || requiere booleanos.");
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
            reportarError(ctx.getStart().getLine(), "No se puede aplicar negación lógica a un tipo " + tipo.getTipo() + ".");
            return new ResultadoC3D(TipoDato.ERROR, "");
        }
        String temporal = generador.generarTemporal();
        generador.agregarAsignacion(temporal, "1", "-", tipo.getValorC3D());
        return new ResultadoC3D(resultado, temporal);
    }
    
    public Object procesarAccesoVariableOAtributo(ZetarianoParser.AccesoVariableOAtributoContext ctx)
    {
        String idVariable = ctx.acceso().ID(0).getText();
        Simbolo sim = tabla.buscar(idVariable);
        int linea = ctx.acceso().ID(0).getSymbol().getLine();
        if (sim == null)
        {
            reportarError(linea, "La variable '" + idVariable + "' no ha sido declarada.");
            return new ResultadoC3D(TipoDato.ERROR, "");
        }
        List<TerminalNode> ids = ctx.acceso().ID();
        if (ids.size() == 1)
        {
            if (sim instanceof SimboloVariable && !((SimboloVariable)sim).isInicializado() && !sim.isEnHeap())
            {
                reportarError(linea, "La variable local '" + idVariable + "' podría no haber sido inicializada.");
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
                reportarError(linea, "Acceso a atributo inválido en '" + idVariable + "'.");
                return new ResultadoC3D(TipoDato.ERROR, "");
            }
            String temporalValor = generador.generarTemporal();
            generador.agregarGetHeap(temporalValor, resDireccion.getValorC3D());
            return new ResultadoC3D(resDireccion.getTipo(), temporalValor);
        }
    }
}
