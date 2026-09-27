package mycompany.contacto_3xtrat3rr3str3d.zetariano;

import java.util.ArrayList;
import java.util.List;
import mycompany.contacto_3xtrat3rr3str3d.ZetarianoParser;
import mycompany.contacto_3xtrat3rr3str3d.simbolos.SimboloVariable;
import mycompany.contacto_3xtrat3rr3str3d.simbolos.TipoDato;
import mycompany.contacto_3xtrat3rr3str3d.utils.ControlTipos;
import mycompany.contacto_3xtrat3rr3str3d.utils.ResultadoC3D;
import mycompany.contacto_3xtrat3rr3str3d.visitors.ZetarianoCustomVisitor;
import org.antlr.v4.runtime.ParserRuleContext;

public class GestorControlFlujoZ extends ZetarianoGestorBase
{
    public GestorControlFlujoZ(ZetarianoCustomVisitor visitor)
    {
        super(visitor);
    }

    private ZetarianoCustomVisitor v()
    {
        return (ZetarianoCustomVisitor) visitor;
    }

    public Object procesarBreak(ParserRuleContext ctx)
    {
        int linea = ctx.getStart().getLine();
        int columna = ctx.getStart().getCharPositionInLine();
        if (v().getPilaBreak().isEmpty())
        {
            reportarError(linea, columna, "La instrucción break solo puede usarse dentro de un ciclo o switch.");
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
            reportarError(linea, columna, "La instrucción continue solo puede usarse dentro de un ciclo.");
            return null;
        }
        generador.agregarSaltoIncondicional(v().getPilaContinue().peek());
        return null;
    }
    
    public Object procesarCondicional(ZetarianoParser.CondicionalContext ctx)
    {
        String etiquetaSalida = generador.generarEtiqueta();
        for (int i = 0; i < ctx.expresion().size(); i++)
        {
            ResultadoC3D resCondicion = (ResultadoC3D) visitor.visit(ctx.expresion(i));
            if (resCondicion.getTipo() != TipoDato.BOOLEANO && resCondicion.getTipo() != TipoDato.ERROR)
            {
                reportarError(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(), "La condición del IF debe ser booleana.");
            }
            String etiquetaFalsa = generador.generarEtiqueta();
            generador.agregarSaltoCondicional(resCondicion.getValorC3D(), "==", "0", etiquetaFalsa);
            visitor.visit(ctx.bloque(i));
            generador.agregarSaltoIncondicional(etiquetaSalida);
            generador.agregarEtiqueta(etiquetaFalsa);
        }
        if (ctx.ELSE() != null) visitor.visit(ctx.bloque(ctx.bloque().size() - 1));
        generador.agregarEtiqueta(etiquetaSalida);
        return null;
    }

    public Object procesarCondicionalSwitch(ZetarianoParser.CondicionalSwitchContext ctx)
    {
        ResultadoC3D resVariable = (ResultadoC3D) visitor.visit(ctx.expresion());
        List<ResultadoC3D> valoresCasos = new ArrayList<>();
        for (ZetarianoParser.CasoContext casoCtx : ctx.caso())
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
                ZetarianoParser.CasoContext casoCtx = ctx.caso(i);
                generador.agregarEtiqueta(etCuerpo.get(i));
                for (ZetarianoParser.InstruccionContext inst : casoCtx.instruccion()) visitor.visit(inst);
            }
            if (tieneDefault)
            {
                generador.agregarEtiqueta(etCuerpoDefault);
                for (ZetarianoParser.InstruccionContext inst : ctx.casoDefault().instruccion()) visitor.visit(inst);
            }
            generador.agregarEtiqueta(etiquetaSalida);
        }
        finally
        {
            v().getPilaBreak().pop();
        }
        return null;
    }

    public Object procesarBucleWhile(ZetarianoParser.BucleWhileContext ctx)
    {
        String etiquetaInicio = generador.generarEtiqueta();
        String etiquetaSalida = generador.generarEtiqueta();
        v().getPilaBreak().push(etiquetaSalida);
        v().getPilaContinue().push(etiquetaInicio);
        try
        {
            generador.agregarEtiqueta(etiquetaInicio);
            ResultadoC3D resCondicion = (ResultadoC3D) visitor.visit(ctx.expresion());
            if (resCondicion.getTipo() != TipoDato.BOOLEANO && resCondicion.getTipo() != TipoDato.ERROR)
            {
                reportarError(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(), "La condición del WHILE debe ser booleana.");
            }
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

    public Object procesarBucleDoWhile(ZetarianoParser.BucleDoWhileContext ctx)
    {
        String etiquetaInicio = generador.generarEtiqueta();
        String etiquetaCondicion = generador.generarEtiqueta();
        String etiquetaSalida = generador.generarEtiqueta();
        v().getPilaBreak().push(etiquetaSalida);
        v().getPilaContinue().push(etiquetaCondicion);
        try
        {
            generador.agregarEtiqueta(etiquetaInicio);
            visitor.visit(ctx.bloqueMetodo());
            generador.agregarEtiqueta(etiquetaCondicion);
            ResultadoC3D resCondicion = (ResultadoC3D) visitor.visit(ctx.expresion());
            if (resCondicion.getTipo() != TipoDato.BOOLEANO && resCondicion.getTipo() != TipoDato.ERROR)
            {
                reportarError(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(), "La condición del DO WHILE debe ser booleana.");
            }
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

    public Object procesarBucleFor(ZetarianoParser.BucleForContext ctx)
    {
        tabla.entrarAmbito();
        if (ctx.declaracionFor() != null) visitor.visit(ctx.declaracionFor());
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
                if (resCondicion.getTipo() != TipoDato.BOOLEANO && resCondicion.getTipo() != TipoDato.ERROR)
                {
                    reportarError(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(), "La condición del FOR debe ser booleana.");
                }
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

    public Object procesarDeclaracionFor(ZetarianoParser.DeclaracionForContext ctx)
    {
        if (ctx.asignacion() != null)
        {
            return visitor.visit(ctx.asignacion());
        }
        String tipoStr = ctx.tipo().tipoBase().getText();
        String id = ctx.ID().getText();
        int linea = ctx.ID().getSymbol().getLine();
        int columna = ctx.ID().getSymbol().getCharPositionInLine();
        TipoDato tipoNormalizado = ControlTipos.normalizarTipo(tipoStr);
        SimboloVariable nuevoSimbolo = new SimboloVariable(id, tipoNormalizado, linea, columna);
        nuevoSimbolo.setInicializado(true);
        tabla.insertar(nuevoSimbolo);
        ResultadoC3D resExpr = (ResultadoC3D) visitor.visit(ctx.expresion());
        if (resExpr != null && resExpr.getTipo() != TipoDato.ERROR)
        {
            if (!ControlTipos.esAsignacionValida(tipoNormalizado, resExpr.getTipo()))
            {
                reportarError(linea, columna, "Tipos incompatibles en FOR.");
            }
            else
            {
                if (nuevoSimbolo.isEnHeap())
                {
                    generador.agregarSetHeap(String.valueOf(nuevoSimbolo.getOffset()), resExpr.getValorC3D());
                }
                else
                {
                    String tempIndice = generador.generarTemporal();
                    generador.agregarAsignacion(tempIndice, "punteroStack", "+", String.valueOf(nuevoSimbolo.getOffset()));
                    generador.agregarSetStack(tempIndice, resExpr.getValorC3D());
                }
            }
        }
        return null;
    }
}
