package mycompany.contacto_3xtrat3rr3str3d.ast;

import mycompany.contacto_3xtrat3rr3str3d.PigLatinBaseVisitor;
import mycompany.contacto_3xtrat3rr3str3d.PigLatinParser;

public class ConstructorPigAST extends PigLatinBaseVisitor<NodoAST>
{
    @Override
    public NodoAST visitPrograma(PigLatinParser.ProgramaContext ctx)
    {
        NodoAST raiz = new NodoAST("Programa Pig Latin");
        if (ctx.seccionDeclaraciones() != null)
        {
            NodoAST decl = visit(ctx.seccionDeclaraciones());
            if (decl != null) raiz.agregarHijo(decl);
        }
        if (ctx.seccionCodigo() != null)
        {
            NodoAST cod = visit(ctx.seccionCodigo());
            if (cod != null) raiz.agregarHijo(cod);
        }
        
        return raiz;
    }
    @Override
    public NodoAST visitSeccionDeclaraciones(PigLatinParser.SeccionDeclaracionesContext ctx)
    {
        NodoAST nodo = new NodoAST("Seccion Declaraciones (VARIABILES>)");
        for (int i = 0; i < ctx.getChildCount(); i++)
        {
            NodoAST hijo = visit(ctx.getChild(i));
            if (hijo != null) nodo.agregarHijo(hijo);
        }
        return nodo;
    }
    @Override
    public NodoAST visitSeccionCodigo(PigLatinParser.SeccionCodigoContext ctx)
    {
        NodoAST nodo = new NodoAST("Seccion Codigo (MAIOR>)");
        for (int i = 0; i < ctx.getChildCount(); i++)
        {
            NodoAST hijo = visit(ctx.getChild(i));
            if (hijo != null) nodo.agregarHijo(hijo);
        }
        return nodo;
    }
    @Override
    public NodoAST visitBloque(PigLatinParser.BloqueContext ctx)
    {
        NodoAST nodoBloque = new NodoAST("Bloque Instrucciones");
        for (int i = 0; i < ctx.getChildCount(); i++)
        {
            NodoAST hijo = visit(ctx.getChild(i));
            if (hijo != null) nodoBloque.agregarHijo(hijo);
        }
        return nodoBloque;
    }

    // VARIABLES

    @Override
    public NodoAST visitDeclaracionVariable(PigLatinParser.DeclaracionVariableContext ctx)
    {
        NodoAST decl = new NodoAST("Declaracion Var: " + ctx.tipo().getText() + " " + ctx.ID().getText());
        if (ctx.valorInicial() != null)
        {
            decl.agregarHijo(visit(ctx.valorInicial()));
        }
        return decl;
    }
    @Override
    public NodoAST visitDeclaracionEstructura(PigLatinParser.DeclaracionEstructuraContext ctx)
    {
        NodoAST decl = new NodoAST("Instancia Estructura: " + ctx.tipo().getText() + " " + ctx.ID().getText());
        if (ctx.agrupacionValores() != null)
        {
            NodoAST args = new NodoAST("Valores Iniciales");
            for (int i = 0; i < ctx.agrupacionValores().getChildCount(); i++)
            {
                NodoAST hijo = visit(ctx.agrupacionValores().getChild(i));
                if (hijo != null) args.agregarHijo(hijo);
            }
            decl.agregarHijo(args);
        }
        return decl;
    }
    @Override
    public NodoAST visitDeclaracionObjeto(PigLatinParser.DeclaracionObjetoContext ctx)
    {
        return new NodoAST("Instancia Objeto: " + ctx.ID(1).getText() + " " + ctx.ID(0).getText());
    }
    
    // ASIGNACION
    
    @Override
    public NodoAST visitAsigGeneral(PigLatinParser.AsigGeneralContext ctx)
    {
        NodoAST asig = new NodoAST("Asignacion a: " + ctx.acceso().getText());
        NodoAST valor = visit(ctx.expresion());
        if (valor != null) asig.agregarHijo(valor);
        return asig;
    }
    @Override
    public NodoAST visitIncrementoGeneral(PigLatinParser.IncrementoGeneralContext ctx)
    {
        String op = ctx.MAS_ABREVIADO() != null ? "++" : "--";
        return new NodoAST("Incremento/Decremento: " + ctx.acceso().getText() + op);
    }

    // FUNCIONES

    @Override
    public NodoAST visitImpresion(PigLatinParser.ImpresionContext ctx)
    {
        NodoAST imp = new NodoAST("Imprimir (>>)");
        for (PigLatinParser.ExpresionContext exprCtx : ctx.expresion())
        {
            imp.agregarHijo(visit(exprCtx));
        }
        return imp;
    }
    @Override
    public NodoAST visitLecturaDescartar(PigLatinParser.LecturaDescartarContext ctx)
    {
        return new NodoAST("Lectura Descartada (<<)");
    }
    @Override
    public NodoAST visitLecturaAsignar(PigLatinParser.LecturaAsignarContext ctx)
    {
        return new NodoAST("Lectura Asignada a: " + ctx.ID().getText() + " (<<)");
    }
    @Override
    public NodoAST visitLlamadaFuncionOMetodo(PigLatinParser.LlamadaFuncionOMetodoContext ctx)
    {
        NodoAST llamada = new NodoAST("Llamada a: " + ctx.acceso().getText());
        if (ctx.argumentos() != null)
        {
            NodoAST args = new NodoAST("Argumentos");
            for (PigLatinParser.ExpresionContext exp : ctx.argumentos().expresion())
            {
                args.agregarHijo(visit(exp));
            }
            llamada.agregarHijo(args);
        }
        return llamada;
    }

    // CONTROL DE FLUJO

    @Override
    public NodoAST visitCondicional(PigLatinParser.CondicionalContext ctx)
    {
        NodoAST cond = new NodoAST("Condicional SI (si/aliter)");
        for (int i = 0; i < ctx.expresion().size(); i++)
        {
            NodoAST rama = new NodoAST(i == 0 ? "SI" : "SINO SI");
            rama.agregarHijo(visit(ctx.expresion(i)));
            rama.agregarHijo(visit(ctx.bloque(i)));
            cond.agregarHijo(rama);
        }
        if (ctx.bloque().size() > ctx.expresion().size())
        {
            NodoAST ramaContra = new NodoAST("ALITER (sino)");
            ramaContra.agregarHijo(visit(ctx.bloque(ctx.bloque().size() - 1)));
            cond.agregarHijo(ramaContra);
        }
        return cond;
    }
    @Override
    public NodoAST visitBucleDum(PigLatinParser.BucleDumContext ctx)
    {
        NodoAST bucle = new NodoAST("Bucle DUM (mientras)");
        bucle.agregarHijo(visit(ctx.expresion()));
        bucle.agregarHijo(visit(ctx.bloque()));
        return bucle;
    }
    @Override
    public NodoAST visitBucleFacere(PigLatinParser.BucleFacereContext ctx)
    {
        NodoAST bucle = new NodoAST("Bucle FACERE-DUM (hacer-mientras)");
        bucle.agregarHijo(visit(ctx.bloque()));
        bucle.agregarHijo(visit(ctx.expresion()));
        return bucle;
    }
    @Override
    public NodoAST visitBuclePer(PigLatinParser.BuclePerContext ctx)
    {
        NodoAST bucle = new NodoAST("Bucle PER (para)");
        bucle.agregarHijo(visit(ctx.declaracion()));
        bucle.agregarHijo(visit(ctx.expresion()));
        bucle.agregarHijo(visit(ctx.actualizacion()));
        bucle.agregarHijo(visit(ctx.bloque()));
        return bucle;
    }
    @Override
    public NodoAST visitActualizacion(PigLatinParser.ActualizacionContext ctx)
    {
        if (ctx.asignacion() != null) return visit(ctx.asignacion());
        String op = ctx.MAS_ABREVIADO() != null ? "++" : "--";
        return new NodoAST("Actualizacion: " + ctx.ID().getText() + op);
    }

    // EXPRESIONES

    @Override
    public NodoAST visitSumaResta(PigLatinParser.SumaRestaContext ctx)
    {
        String op = ctx.MAS() != null ? "+" : "-";
        NodoAST expr = new NodoAST("Operacion: " + op);
        expr.agregarHijo(visit(ctx.expresion(0)));
        expr.agregarHijo(visit(ctx.expresion(1)));
        return expr;
    }
    @Override
    public NodoAST visitMultDiv(PigLatinParser.MultDivContext ctx)
    {
        String op = ctx.POR() != null ? "*" : (ctx.DIVISION() != null ? "/" : "%");
        NodoAST expr = new NodoAST("Operacion: " + op);
        expr.agregarHijo(visit(ctx.expresion(0)));
        expr.agregarHijo(visit(ctx.expresion(1)));
        return expr;
    }
    @Override
    public NodoAST visitComparacion(PigLatinParser.ComparacionContext ctx)
    {
        String op = ctx.MAYOR() != null ? ">" : ctx.MAYOR_IGUAL() != null ? ">=" : ctx.MENOR() != null ? "<" : "<=";
        NodoAST expr = new NodoAST("Comparacion: " + op);
        expr.agregarHijo(visit(ctx.expresion(0)));
        expr.agregarHijo(visit(ctx.expresion(1)));
        return expr;
    }
    @Override
    public NodoAST visitIgualdad(PigLatinParser.IgualdadContext ctx)
    {
        String op = ctx.IGUALIGUAL() != null ? "==" : "!=";
        NodoAST expr = new NodoAST("Igualdad: " + op);
        expr.agregarHijo(visit(ctx.expresion(0)));
        expr.agregarHijo(visit(ctx.expresion(1)));
        return expr;
    }
    @Override
    public NodoAST visitAndLogico(PigLatinParser.AndLogicoContext ctx)
    {
        NodoAST expr = new NodoAST("Logico: &&");
        expr.agregarHijo(visit(ctx.expresion(0)));
        expr.agregarHijo(visit(ctx.expresion(1)));
        return expr;
    }
    @Override
    public NodoAST visitOrLogico(PigLatinParser.OrLogicoContext ctx)
    {
        NodoAST expr = new NodoAST("Logico: ||");
        expr.agregarHijo(visit(ctx.expresion(0)));
        expr.agregarHijo(visit(ctx.expresion(1)));
        return expr;
    }
    @Override
    public NodoAST visitNegacion(PigLatinParser.NegacionContext ctx)
    {
        NodoAST expr = new NodoAST("Negacion Unaria/Logica");
        expr.agregarHijo(visit(ctx.factor()));
        return expr;
    }
    @Override
    public NodoAST visitAccesoVariableOAtributo(PigLatinParser.AccesoVariableOAtributoContext ctx)
    {
        return new NodoAST("Acceso a: " + ctx.getText());
    }

    // LITERALES

    @Override
    public NodoAST visitNumLiteral(PigLatinParser.NumLiteralContext ctx)
    {
        return new NodoAST("Numero: " + ctx.getText());
    }
    @Override
    public NodoAST visitDecLiteral(PigLatinParser.DecLiteralContext ctx)
    {
        return new NodoAST("Decimal: " + ctx.getText());
    }
    @Override
    public NodoAST visitTextLiteral(PigLatinParser.TextLiteralContext ctx)
    {
        return new NodoAST("Texto: " + ctx.getText());
    }
    @Override
    public NodoAST visitCharLiteral(PigLatinParser.CharLiteralContext ctx)
    {
        return new NodoAST("Caracter: " + ctx.getText());
    }
    @Override
    public NodoAST visitTrueLiteral(PigLatinParser.TrueLiteralContext ctx)
    {
        return new NodoAST("Booleano: verum");
    }
    @Override
    public NodoAST visitFalseLiteral(PigLatinParser.FalseLiteralContext ctx)
    {
        return new NodoAST("Booleano: falsus");
    }
    @Override
    public NodoAST visitToFactor(PigLatinParser.ToFactorContext ctx)
    {
        return visit(ctx.factor());
    }
    @Override
    public NodoAST visitParentesis(PigLatinParser.ParentesisContext ctx)
    {
        return visit(ctx.expresion());
    }
    @Override
    protected NodoAST defaultResult()
    {
        return null;
    }
}