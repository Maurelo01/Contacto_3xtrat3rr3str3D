package mycompany.contacto_3xtrat3rr3str3d.y;

import mycompany.contacto_3xtrat3rr3str3d.YParser;
import mycompany.contacto_3xtrat3rr3str3d.simbolos.TipoDato;
import mycompany.contacto_3xtrat3rr3str3d.utils.ResultadoC3D;
import mycompany.contacto_3xtrat3rr3str3d.visitors.YCustomVisitor;

public class GestorControlFlujoY extends YGestorBase
{
    public GestorControlFlujoY(YCustomVisitor visitor)
    {
        super(visitor);
    }
    
    public Object procesarCondicional(YParser.CondicionalContext ctx)
    {
        String etiquetaSalida = generador.generarEtiqueta();
        for (int i = 0; i < ctx.expresion().size(); i++)
        {
            ResultadoC3D resCondicion = (ResultadoC3D) visitor.visit(ctx.expresion(i));
            if (resCondicion.getTipo() != TipoDato.BOOLEANO && resCondicion.getTipo() != TipoDato.ERROR) reportarError(ctx.getStart().getLine(), "La condición SI debe ser booleana.");
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
        generador.agregarEtiqueta(etiquetaInicio);
        ResultadoC3D resCondicion = (ResultadoC3D) visitor.visit(ctx.expresion());
        if (resCondicion.getTipo() != TipoDato.BOOLEANO && resCondicion.getTipo() != TipoDato.ERROR) reportarError(ctx.getStart().getLine(), "La condición MIENTRAS debe ser booleana.");
        generador.agregarSaltoCondicional(resCondicion.getValorC3D(), "==", "0", etiquetaSalida);
        visitor.visit(ctx.bloque());
        generador.agregarSaltoIncondicional(etiquetaInicio);
        generador.agregarEtiqueta(etiquetaSalida);
        return null;
    }
    
    public Object procesarBucleHacer(YParser.BucleHacerContext ctx)
    {
        String etiquetaInicio = generador.generarEtiqueta();
        generador.agregarEtiqueta(etiquetaInicio);
        visitor.visit(ctx.bloque());
        ResultadoC3D resCondicion = (ResultadoC3D) visitor.visit(ctx.expresion());
        if (resCondicion.getTipo() != TipoDato.BOOLEANO && resCondicion.getTipo() != TipoDato.ERROR) reportarError(ctx.getStart().getLine(), "La condición HACER MIENTRAS debe ser booleana.");
        generador.agregarSaltoCondicional(resCondicion.getValorC3D(), "==", "1", etiquetaInicio);
        return null;
    }
    
    public Object procesarBuclePara(YParser.BucleParaContext ctx)
    {
        tabla.entrarAmbito();
        if (ctx.declaracion() != null) visitor.visit(ctx.declaracion());
        String etiquetaInicio = generador.generarEtiqueta();
        String etiquetaSalida = generador.generarEtiqueta();
        generador.agregarEtiqueta(etiquetaInicio);
        if (ctx.expresion() != null)
        {
            ResultadoC3D resCondicion = (ResultadoC3D) visitor.visit(ctx.expresion());
            if (resCondicion.getTipo() != TipoDato.BOOLEANO && resCondicion.getTipo() != TipoDato.ERROR) reportarError(ctx.getStart().getLine(), "La condición PARA debe ser booleana.");
            generador.agregarSaltoCondicional(resCondicion.getValorC3D(), "==", "0", etiquetaSalida);
        }
        visitor.visit(ctx.bloque());
        if (ctx.actualizacionFor() != null) visitor.visit(ctx.actualizacionFor());
        generador.agregarSaltoIncondicional(etiquetaInicio);
        generador.agregarEtiqueta(etiquetaSalida);
        tabla.salirAmbito();
        return null;
    }
    
    public Object procesarCondicionalElegir(YParser.CondicionalElegirContext ctx)
    {
        ResultadoC3D resVariable = (ResultadoC3D) visitor.visit(ctx.expresion());
        String etiquetaSalida = generador.generarEtiqueta();
        for (YParser.CasoContext casoCtx : ctx.caso())
        {
            ResultadoC3D resCaso = (ResultadoC3D) visitor.visit(casoCtx.expresion());
            String etiquetaBloque = generador.generarEtiqueta();
            String etiquetaSiguiente = generador.generarEtiqueta();
            generador.agregarSaltoCondicional(resVariable.getValorC3D(), "==", resCaso.getValorC3D(), etiquetaBloque);
            generador.agregarSaltoIncondicional(etiquetaSiguiente);
            generador.agregarEtiqueta(etiquetaBloque);
            visitor.visit(casoCtx.bloque());
            generador.agregarSaltoIncondicional(etiquetaSalida);
            generador.agregarEtiqueta(etiquetaSiguiente);
        }
        if (ctx.casoDefault() != null) visitor.visit(ctx.casoDefault().bloque());
        generador.agregarEtiqueta(etiquetaSalida);
        return null;
    }
}
