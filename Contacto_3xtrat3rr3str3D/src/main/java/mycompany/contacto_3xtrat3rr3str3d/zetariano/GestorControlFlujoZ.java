package mycompany.contacto_3xtrat3rr3str3d.zetariano;

import mycompany.contacto_3xtrat3rr3str3d.ZetarianoParser;
import mycompany.contacto_3xtrat3rr3str3d.simbolos.SimboloVariable;
import mycompany.contacto_3xtrat3rr3str3d.simbolos.TipoDato;
import mycompany.contacto_3xtrat3rr3str3d.utils.ControlTipos;
import mycompany.contacto_3xtrat3rr3str3d.utils.ResultadoC3D;
import mycompany.contacto_3xtrat3rr3str3d.visitors.ZetarianoCustomVisitor;

public class GestorControlFlujoZ extends ZetarianoGestorBase
{
    public GestorControlFlujoZ(ZetarianoCustomVisitor visitor)
    {
        super(visitor);
    }
    
    public Object procesarCondicional(ZetarianoParser.CondicionalContext ctx)
    {
        String etiquetaSalida = generador.generarEtiqueta();
        for (int i = 0; i < ctx.expresion().size(); i++)
        {
            ResultadoC3D resCondicion = (ResultadoC3D) visitor.visit(ctx.expresion(i));
            if (resCondicion.getTipo() != TipoDato.BOOLEANO && resCondicion.getTipo() != TipoDato.ERROR)
            {
                reportarError(ctx.getStart().getLine(), "La condición del IF debe ser booleana.");
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
        String etiquetaSalida = generador.generarEtiqueta();
        for (ZetarianoParser.CasoContext casoCtx : ctx.caso())
        {
            ResultadoC3D resCaso = (ResultadoC3D) visitor.visit(casoCtx.expresion());
            String etiquetaBloque = generador.generarEtiqueta();
            String etiquetaSiguiente = generador.generarEtiqueta();
            generador.agregarSaltoCondicional(resVariable.getValorC3D(), "==", resCaso.getValorC3D(), etiquetaBloque);
            generador.agregarSaltoIncondicional(etiquetaSiguiente);
            generador.agregarEtiqueta(etiquetaBloque);
            for (ZetarianoParser.InstruccionContext inst : casoCtx.instruccion()) visitor.visit(inst);
            generador.agregarSaltoIncondicional(etiquetaSalida);
            generador.agregarEtiqueta(etiquetaSiguiente);
        }
        if (ctx.casoDefault() != null)
        {
            for (ZetarianoParser.InstruccionContext inst : ctx.casoDefault().instruccion()) visitor.visit(inst);
        }
        generador.agregarEtiqueta(etiquetaSalida);
        return null;
    }

    public Object procesarBucleWhile(ZetarianoParser.BucleWhileContext ctx)
    {
        String etiquetaInicio = generador.generarEtiqueta();
        String etiquetaSalida = generador.generarEtiqueta();
        generador.agregarEtiqueta(etiquetaInicio);
        ResultadoC3D resCondicion = (ResultadoC3D) visitor.visit(ctx.expresion());
        if (resCondicion.getTipo() != TipoDato.BOOLEANO && resCondicion.getTipo() != TipoDato.ERROR)
        {
            reportarError(ctx.getStart().getLine(), "La condición del WHILE debe ser booleana.");
        }
        generador.agregarSaltoCondicional(resCondicion.getValorC3D(), "==", "0", etiquetaSalida);
        visitor.visit(ctx.bloque());
        generador.agregarSaltoIncondicional(etiquetaInicio);
        generador.agregarEtiqueta(etiquetaSalida);
        return null;
    }

    public Object procesarBucleDoWhile(ZetarianoParser.BucleDoWhileContext ctx)
    {
        String etiquetaInicio = generador.generarEtiqueta();
        generador.agregarEtiqueta(etiquetaInicio);
        visitor.visit(ctx.bloqueMetodo());
        ResultadoC3D resCondicion = (ResultadoC3D) visitor.visit(ctx.expresion());
        if (resCondicion.getTipo() != TipoDato.BOOLEANO && resCondicion.getTipo() != TipoDato.ERROR)
        {
            reportarError(ctx.getStart().getLine(), "La condición del DO WHILE debe ser booleana.");
        }
        generador.agregarSaltoCondicional(resCondicion.getValorC3D(), "==", "1", etiquetaInicio);
        return null;
    }

    public Object procesarBucleFor(ZetarianoParser.BucleForContext ctx)
    {
        tabla.entrarAmbito();
        if (ctx.declaracionFor() != null) visitor.visit(ctx.declaracionFor());
        String etiquetaInicio = generador.generarEtiqueta();
        String etiquetaSalida = generador.generarEtiqueta();
        generador.agregarEtiqueta(etiquetaInicio);
        if (ctx.expresion() != null)
        {
            ResultadoC3D resCondicion = (ResultadoC3D) visitor.visit(ctx.expresion());
            if (resCondicion.getTipo() != TipoDato.BOOLEANO && resCondicion.getTipo() != TipoDato.ERROR)
            {
                reportarError(ctx.getStart().getLine(), "La condición del FOR debe ser booleana.");
            }
            generador.agregarSaltoCondicional(resCondicion.getValorC3D(), "==", "0", etiquetaSalida);
        }
        visitor.visit(ctx.bloque());
        if (ctx.actualizacionFor() != null) visitor.visit(ctx.actualizacionFor());
        generador.agregarSaltoIncondicional(etiquetaInicio);
        generador.agregarEtiqueta(etiquetaSalida);
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
                reportarError(linea, "Tipos incompatibles en FOR.");
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
