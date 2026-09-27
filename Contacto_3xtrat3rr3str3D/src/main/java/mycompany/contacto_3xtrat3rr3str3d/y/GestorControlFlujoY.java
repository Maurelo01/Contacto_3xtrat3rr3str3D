package mycompany.contacto_3xtrat3rr3str3d.y;

import java.util.ArrayList;
import java.util.List;
import mycompany.contacto_3xtrat3rr3str3d.YParser;
import mycompany.contacto_3xtrat3rr3str3d.simbolos.TipoDato;
import mycompany.contacto_3xtrat3rr3str3d.utils.ResultadoC3D;
import mycompany.contacto_3xtrat3rr3str3d.visitors.YCustomVisitor;
import org.antlr.v4.runtime.ParserRuleContext;

public class GestorControlFlujoY extends YGestorBase
{
    public GestorControlFlujoY(YCustomVisitor visitor)
    {
        super(visitor);
    }

    private YCustomVisitor v()
    {
        return (YCustomVisitor) visitor;
    }

    public Object procesarBreak(ParserRuleContext ctx)
    {
        int linea = ctx.getStart().getLine();
        int columna = ctx.getStart().getCharPositionInLine();
        if (v().getPilaBreak().isEmpty())
        {
            reportarError(linea, columna, "La instrucción romper solo puede usarse dentro de un ciclo o elegir.");
            return null;
        }
        generador.agregarSaltoIncondicional(v().getPilaBreak().peek());
        return null;
    }

    public Object procesarContinue(ParserRuleContext ctx)
    {
        int linea = ctx.getStart().getLine();
        int columna = ctx.getStart().getCharPositionInLine();
        if (v().getPilaContinue().isEmpty())
        {
            reportarError(linea, columna, "La instrucción continuar solo puede usarse dentro de un ciclo.");
            return null;
        }
        generador.agregarSaltoIncondicional(v().getPilaContinue().peek());
        return null;
    }
    
    public Object procesarCondicional(YParser.CondicionalContext ctx)
    {
        String etiquetaSalida = generador.generarEtiqueta();
        for (int i = 0; i < ctx.expresion().size(); i++)
        {
            ResultadoC3D resCondicion = (ResultadoC3D) visitor.visit(ctx.expresion(i));
            if (resCondicion.getTipo() != TipoDato.BOOLEANO && resCondicion.getTipo() != TipoDato.ERROR) reportarError(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(),"La condición SI debe ser booleana.");
            String etiquetaFalsa = generador.generarEtiqueta();
            generador.agregarSaltoCondicional(resCondicion.getValorC3D(), "==", "0", etiquetaFalsa);
            visitor.visit(ctx.bloque(i));
            generador.agregarSaltoIncondicional(etiquetaSalida);
            generador.agregarEtiqueta(etiquetaFalsa);
        }
        if (ctx.CONTRARIO() != null) visitor.visit(ctx.bloque(ctx.bloque().size() - 1));
        generador.agregarEtiqueta(etiquetaSalida);
        return null;
    }
    
    public Object procesarBucleMientras(YParser.BucleMientrasContext ctx)
    {
        String etiquetaInicio = generador.generarEtiqueta();
        String etiquetaSalida = generador.generarEtiqueta();
        v().getPilaBreak().push(etiquetaSalida);
        v().getPilaContinue().push(etiquetaInicio);
        try
        {
            generador.agregarEtiqueta(etiquetaInicio);
            ResultadoC3D resCondicion = (ResultadoC3D) visitor.visit(ctx.expresion());
            if (resCondicion.getTipo() != TipoDato.BOOLEANO && resCondicion.getTipo() != TipoDato.ERROR) reportarError(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(), "La condición MIENTRAS debe ser booleana.");
            generador.agregarSaltoCondicional(resCondicion.getValorC3D(), "==", "0", etiquetaSalida);
            visitor.visit(ctx.bloque());
            generador.agregarSaltoIncondicional(etiquetaInicio);
            generador.agregarEtiqueta(etiquetaSalida);
        }
        finally
        {
            v().getPilaContinue().pop();
            v().getPilaBreak().pop();
        }
        return null;
    }
    
    public Object procesarBucleHacer(YParser.BucleHacerContext ctx)
    {
        String etiquetaInicio = generador.generarEtiqueta();
        String etiquetaSalida = generador.generarEtiqueta();
        String etiquetaCondicion = generador.generarEtiqueta();
        v().getPilaBreak().push(etiquetaSalida);
        v().getPilaContinue().push(etiquetaCondicion);
        try
        {
            generador.agregarEtiqueta(etiquetaInicio);
            visitor.visit(ctx.bloque());
            generador.agregarEtiqueta(etiquetaCondicion);
            ResultadoC3D resCondicion = (ResultadoC3D) visitor.visit(ctx.expresion());
            if (resCondicion.getTipo() != TipoDato.BOOLEANO && resCondicion.getTipo() != TipoDato.ERROR) reportarError(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(), "La condición HACER MIENTRAS debe ser booleana.");
            generador.agregarSaltoCondicional(resCondicion.getValorC3D(), "==", "1", etiquetaInicio);
            generador.agregarEtiqueta(etiquetaSalida);
        }
        finally
        {
            v().getPilaContinue().pop();
            v().getPilaBreak().pop();
        }
        return null;
    }
    
    public Object procesarBuclePara(YParser.BucleParaContext ctx)
    {
        tabla.entrarAmbito();
        if (ctx.declaracion() != null) visitor.visit(ctx.declaracion());
        String etiquetaInicio = generador.generarEtiqueta();
        String etiquetaContinuar = generador.generarEtiqueta();
        String etiquetaSalida = generador.generarEtiqueta();
        v().getPilaBreak().push(etiquetaSalida);
        v().getPilaContinue().push(etiquetaContinuar);
        try
        {
            generador.agregarEtiqueta(etiquetaInicio);
            if (ctx.expresion() != null)
            {
                ResultadoC3D resCondicion = (ResultadoC3D) visitor.visit(ctx.expresion());
                if (resCondicion.getTipo() != TipoDato.BOOLEANO && resCondicion.getTipo() != TipoDato.ERROR) reportarError(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(), "La condición PARA debe ser booleana.");
                generador.agregarSaltoCondicional(resCondicion.getValorC3D(), "==", "0", etiquetaSalida);
            }
            visitor.visit(ctx.bloque());
            generador.agregarEtiqueta(etiquetaContinuar);
            if (ctx.actualizacionFor() != null) visitor.visit(ctx.actualizacionFor());
            generador.agregarSaltoIncondicional(etiquetaInicio);
            generador.agregarEtiqueta(etiquetaSalida);
        }
        finally
        {
            v().getPilaContinue().pop();
            v().getPilaBreak().pop();
        }
        tabla.salirAmbito();
        return null;
    }
    
    public Object procesarCondicionalElegir(YParser.CondicionalElegirContext ctx)
    {
        ResultadoC3D resVariable = (ResultadoC3D) visitor.visit(ctx.expresion());
        List<ResultadoC3D> valoresCasos = new ArrayList<>();
        for (YParser.CasoContext casoCtx : ctx.caso())
        {
            valoresCasos.add((ResultadoC3D) visitor.visit(casoCtx.expresion()));
        }
        int n = ctx.caso().size();
        boolean tieneDefault = ctx.casoDefault() != null;
        String etiquetaSalida = generador.generarEtiqueta();
        List<String> etCuerpo = new ArrayList<>();
        List<String> etTest = new ArrayList<>();
        for (int i = 0; i < n; i++)
        {
            etCuerpo.add(generador.generarEtiqueta());
            etTest.add(generador.generarEtiqueta());
        }
        String etCuerpoDefault = tieneDefault ? generador.generarEtiqueta() : null;
        v().getPilaBreak().push(etiquetaSalida);
        try
        {
            generador.agregarSaltoIncondicional(etTest.isEmpty() ? (tieneDefault ? etCuerpoDefault : etiquetaSalida) : etTest.get(0));
            for (int i = 0; i < n; i++)
            {
                generador.agregarEtiqueta(etTest.get(i));
                generador.agregarSaltoCondicional(resVariable.getValorC3D(), "==", valoresCasos.get(i).getValorC3D(), etCuerpo.get(i));
                if (i + 1 < n) generador.agregarSaltoIncondicional(etTest.get(i + 1));
                else generador.agregarSaltoIncondicional(tieneDefault ? etCuerpoDefault : etiquetaSalida);
            }
            for (int i = 0; i < n; i++)
            {
                YParser.CasoContext casoCtx = ctx.caso(i);
                generador.agregarEtiqueta(etCuerpo.get(i));
                visitor.visit(casoCtx.bloque());
                if (casoCtx.ROMPER() != null) generador.agregarSaltoIncondicional(etiquetaSalida);
            }
            if (tieneDefault)
            {
                generador.agregarEtiqueta(etCuerpoDefault);
                visitor.visit(ctx.casoDefault().bloque());
                if (ctx.casoDefault().ROMPER() != null) generador.agregarSaltoIncondicional(etiquetaSalida);
            }
            generador.agregarEtiqueta(etiquetaSalida);
        }
        finally
        {
            v().getPilaBreak().pop();
        }
        return null;
    }
}
