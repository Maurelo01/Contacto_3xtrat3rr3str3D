package mycompany.contacto_3xtrat3rr3str3d.ast;

import mycompany.contacto_3xtrat3rr3str3d.YBaseVisitor;
import mycompany.contacto_3xtrat3rr3str3d.YParser;
import org.antlr.v4.runtime.tree.TerminalNode;

public class ConstructorYAST extends YBaseVisitor<NodoAST>
{
    @Override
    public NodoAST visitPrograma(YParser.ProgramaContext ctx)
    {
        NodoAST raiz = new NodoAST("Programa Y?");
        if (ctx.seccionEstructuras() != null) raiz.agregarHijo(visit(ctx.seccionEstructuras()));
        if (ctx.seccionFunciones() != null) raiz.agregarHijo(visit(ctx.seccionFunciones()));
        return raiz;
    }
    @Override
    public NodoAST visitBloque(YParser.BloqueContext ctx)
    {
        NodoAST nodoBloque = new NodoAST("Bloque Instrucciones");
        for (YParser.InstruccionContext inst : ctx.instruccion())
        {
            NodoAST hijo = visit(inst);
            if (hijo != null) nodoBloque.agregarHijo(hijo);
        }
        return nodoBloque;
    }

    // ESTRUCTURAS

    @Override
    public NodoAST visitSeccionEstructuras(YParser.SeccionEstructurasContext ctx)
    {
        NodoAST nodo = new NodoAST("Seccion Estructuras");
        for (YParser.DefinicionEstructuraContext est : ctx.definicionEstructura())
        {
            nodo.agregarHijo(visit(est));
        }
        return nodo;
    }
    @Override
    public NodoAST visitDefinicionEstructura(YParser.DefinicionEstructuraContext ctx)
    {
        NodoAST nodoEst = new NodoAST("Estructura: " + ctx.ID().getText());
        for (YParser.AtributoEstructuraContext attr : ctx.atributoEstructura())
        {
            nodoEst.agregarHijo(visit(attr));
        }
        return nodoEst;
    }
    @Override
    public NodoAST visitAtributoNormal(YParser.AtributoNormalContext ctx)
    {
        NodoAST attr = new NodoAST("Atributo: " + ctx.tipo().getText() + " " + ctx.ID().getText());
        if (ctx.NUMERO() != null)
        {
            attr.agregarHijo("Tamaño Arreglo: " + ctx.NUMERO().getText());
        }
        return attr;
    }
    @Override
    public NodoAST visitAtributoEstructuraAnidada(YParser.AtributoEstructuraAnidadaContext ctx)
    {
        return new NodoAST("Atributo Estructura: " + ctx.ID(0).getText() + " " + ctx.ID(1).getText());
    }

    // FUNCIONES

    @Override
    public NodoAST visitSeccionFunciones(YParser.SeccionFuncionesContext ctx)
    {
        NodoAST nodo = new NodoAST("Seccion Funciones");
        for (YParser.DefinicionFuncionContext func : ctx.definicionFuncion())
        {
            nodo.agregarHijo(visit(func));
        }
        return nodo;
    }
    @Override
    public NodoAST visitDefinicionFuncion(YParser.DefinicionFuncionContext ctx)
    {
        String tipoRetorno = ctx.FLECHA() != null ? ctx.tipo().getText() : "void";
        NodoAST nodoFunc = new NodoAST("Funcion: " + ctx.ID().getText() + " -> " + tipoRetorno);
        if (ctx.parametros() != null)
        {
            NodoAST params = new NodoAST("Parametros");
            for (YParser.ParametroContext p : ctx.parametros().parametro())
            {
                params.agregarHijo(visit(p));
            }
            nodoFunc.agregarHijo(params);
        }
        nodoFunc.agregarHijo(visit(ctx.bloque()));
        return nodoFunc;
    }
    
    // PARAMETROS
    
    @Override
    public NodoAST visitParamValor(YParser.ParamValorContext ctx)
    {
        return new NodoAST("Param Valor: " + ctx.tipo().getText() + " " + ctx.ID().getText());
    }
    @Override
    public NodoAST visitParamArregloRef(YParser.ParamArregloRefContext ctx)
    {
        return new NodoAST("Param Arreglo Ref: [] " + ctx.tipo().getText() + " " + ctx.ID().getText());
    }
    @Override
    public NodoAST visitParamStructRef(YParser.ParamStructRefContext ctx)
    {
        return new NodoAST("Param Struct Ref: {} " + ctx.ID(0).getText() + " " + ctx.ID(1).getText());
    }
    @Override
    public NodoAST visitLlamadaFuncionExpr(YParser.LlamadaFuncionExprContext ctx)
    {
        return visit(ctx.llamadaFuncion());
    }
    @Override
    public NodoAST visitInstruccionLlamadaFuncion(YParser.InstruccionLlamadaFuncionContext ctx)
    {
        return visit(ctx.llamadaFuncion());
    }
    @Override
    public NodoAST visitLlamadaFuncion(YParser.LlamadaFuncionContext ctx)
    {
        NodoAST llamada = new NodoAST("Llamada Funcion: " + ctx.ID().getText());
        if (ctx.argumentos() != null)
        {
            NodoAST args = new NodoAST("Argumentos");
            for (YParser.ExpresionContext exp : ctx.argumentos().expresion())
            {
                args.agregarHijo(visit(exp));
            }
            llamada.agregarHijo(args);
        }
        return llamada;
    }

    // VARIABLES

    @Override
    public NodoAST visitDeclVariableAsig(YParser.DeclVariableAsigContext ctx)
    {
        NodoAST decl = new NodoAST("Declaracion Var: " + ctx.tipo().getText() + " " + ctx.ID().getText());
        NodoAST valor = visit(ctx.expresion());
        if (valor != null) decl.agregarHijo(valor);
        return decl;
    }
    @Override
    public NodoAST visitDeclVariable(YParser.DeclVariableContext ctx)
    {
        return new NodoAST("Declaracion Var: " + ctx.tipo().getText() + " " + ctx.ID().getText());
    }
    @Override
    public NodoAST visitDeclEstructura(YParser.DeclEstructuraContext ctx)
    {
        return new NodoAST("Instancia Estructura: " + ctx.ID(0).getText() + " " + ctx.ID(1).getText());
    }
    @Override
    public NodoAST visitDeclArreglo(YParser.DeclArregloContext ctx)
    {
        NodoAST decl = new NodoAST("Declaracion Arreglo: " + ctx.tipo().getText() + " " + ctx.ID().getText());
        for (TerminalNode n : ctx.NUMERO())
        {
            decl.agregarHijo("Tamaño: " + n.getText());
        }
        return decl;
    }
    @Override
    public NodoAST visitDeclArregloLiteral(YParser.DeclArregloLiteralContext ctx)
    {
        NodoAST decl = new NodoAST("Arreglo Literal: " + ctx.tipo().getText() + " " + ctx.ID().getText());
        if (ctx.argumentos() != null)
        {
            NodoAST args = new NodoAST("Valores Iniciales");
            for (YParser.ExpresionContext exp : ctx.argumentos().expresion()) args.agregarHijo(visit(exp));
            decl.agregarHijo(args);
        }
        return decl;
    }
    
    // ASIGNACION
    
    @Override
    public NodoAST visitDeclEstructuraAsig(YParser.DeclEstructuraAsigContext ctx)
    {
        NodoAST decl = new NodoAST("Instancia Estructura: " + ctx.ID(0).getText() + " " + ctx.ID(1).getText());
        if (ctx.argumentos() != null)
        {
            NodoAST args = new NodoAST("Valores Iniciales");
            for (YParser.ExpresionContext exp : ctx.argumentos().expresion())
            {
                args.agregarHijo(visit(exp));
            }
            decl.agregarHijo(args);
        }
        return decl;
    }
    @Override
    public NodoAST visitAsignacion(YParser.AsignacionContext ctx)
    {
        NodoAST asig = new NodoAST("Asignacion a: " + ctx.acceso().getText());
        NodoAST valor = visit(ctx.expresion());
        if (valor != null) asig.agregarHijo(valor);
        return asig;
    }
    @Override
    public NodoAST visitIncremento(YParser.IncrementoContext ctx)
    {
        String op = ctx.MAS_MAS() != null ? "++" : "--";
        return new NodoAST("Incremento: " + ctx.acceso().getText() + op);
    }

    // FUNCIONES

    @Override
    public NodoAST visitInstruccionImprimir(YParser.InstruccionImprimirContext ctx)
    {
        NodoAST imp = new NodoAST("Imprimir");
        if (ctx.expresion() != null) imp.agregarHijo(visit(ctx.expresion()));
        return imp;
    }
    @Override
    public NodoAST visitInstruccionRetornar(YParser.InstruccionRetornarContext ctx)
    {
        NodoAST ret = new NodoAST("Retornar");
        if (ctx.expresion() != null) ret.agregarHijo(visit(ctx.expresion()));
        return ret;
    }

    // CONTROL DE FLUJO

    @Override
    public NodoAST visitCondicional(YParser.CondicionalContext ctx)
    {
        NodoAST cond = new NodoAST("Condicional SI");
        for (int i = 0; i < ctx.expresion().size(); i++)
        {
            NodoAST rama = new NodoAST(i == 0 ? "SI" : "SINO SI");
            rama.agregarHijo(visit(ctx.expresion(i)));
            rama.agregarHijo(visit(ctx.bloque(i)));
            cond.agregarHijo(rama);
        }
        if (ctx.CONTRARIO() != null)
        {
            NodoAST ramaContra = new NodoAST("CONTRARIO");
            ramaContra.agregarHijo(visit(ctx.bloque(ctx.bloque().size() - 1)));
            cond.agregarHijo(ramaContra);
        }
        return cond;
    }
    @Override
    public NodoAST visitBucleMientras(YParser.BucleMientrasContext ctx)
    {
        NodoAST bucle = new NodoAST("Bucle MIENTRAS");
        bucle.agregarHijo(visit(ctx.expresion()));
        bucle.agregarHijo(visit(ctx.bloque()));
        return bucle;
    }
    @Override
    public NodoAST visitBucleHacer(YParser.BucleHacerContext ctx)
    {
        NodoAST bucle = new NodoAST("Bucle HACER MIENTRAS");
        bucle.agregarHijo(visit(ctx.bloque()));
        bucle.agregarHijo(visit(ctx.expresion()));
        return bucle;
    }

    @Override
    public NodoAST visitBuclePara(YParser.BucleParaContext ctx)
    {
        NodoAST bucle = new NodoAST("Bucle PARA");
        for (int i = 0; i < ctx.getChildCount(); i++)
        {
            NodoAST hijo = visit(ctx.getChild(i));
            if (hijo != null) bucle.agregarHijo(hijo);
        }
        return bucle;
    }
    @Override
    public NodoAST visitCondicionalElegir(YParser.CondicionalElegirContext ctx)
    {
        NodoAST elegir = new NodoAST("Condicional ELEGIR (Switch)");
        for (int i = 0; i < ctx.getChildCount(); i++)
        {
            NodoAST hijo = visit(ctx.getChild(i));
            if (hijo != null) elegir.agregarHijo(hijo);
        }
        return elegir;
    }

    // EXPRESIONES

    @Override
    public NodoAST visitSumaResta(YParser.SumaRestaContext ctx)
    {
        String op = ctx.MAS() != null ? "+" : "-";
        NodoAST expr = new NodoAST("Operacion: " + op);
        expr.agregarHijo(visit(ctx.expresion(0)));
        expr.agregarHijo(visit(ctx.expresion(1)));
        return expr;
    }
    @Override
    public NodoAST visitMultDiv(YParser.MultDivContext ctx)
    {
        String op = ctx.POR() != null ? "*" : "/";
        NodoAST expr = new NodoAST("Operacion: " + op);
        expr.agregarHijo(visit(ctx.expresion(0)));
        expr.agregarHijo(visit(ctx.expresion(1)));
        return expr;
    }
    @Override
    public NodoAST visitComparacion(YParser.ComparacionContext ctx)
    {
        String op = ctx.MAYOR() != null ? ">" : ctx.MAYOR_IGUAL() != null ? ">=" : ctx.MENOR() != null ? "<" : "<=";
        NodoAST expr = new NodoAST("Comparacion: " + op);
        expr.agregarHijo(visit(ctx.expresion(0)));
        expr.agregarHijo(visit(ctx.expresion(1)));
        return expr;
    }
    @Override
    public NodoAST visitAccesoVariableOAtributo(YParser.AccesoVariableOAtributoContext ctx)
    {
        return new NodoAST("Acceso a: " + ctx.getText());
    }
    @Override
    public NodoAST visitIgualdad(YParser.IgualdadContext ctx)
    {
        String op = ctx.getChild(1).getText(); 
        NodoAST expr = new NodoAST("Igualdad: " + op);
        expr.agregarHijo(visit(ctx.expresion(0)));
        expr.agregarHijo(visit(ctx.expresion(1)));
        return expr;
    }
    @Override
    public NodoAST visitAndLogico(YParser.AndLogicoContext ctx)
    {
        NodoAST expr = new NodoAST("Logico: &&");
        expr.agregarHijo(visit(ctx.expresion(0)));
        expr.agregarHijo(visit(ctx.expresion(1)));
        return expr;
    }
    @Override
    public NodoAST visitOrLogico(YParser.OrLogicoContext ctx)
    {
        NodoAST expr = new NodoAST("Logico: ||");
        expr.agregarHijo(visit(ctx.expresion(0)));
        expr.agregarHijo(visit(ctx.expresion(1)));
        return expr;
    }
    @Override
    public NodoAST visitNegacionUnaria(YParser.NegacionUnariaContext ctx)
    {
        NodoAST expr = new NodoAST("Negacion Unaria: -");
        expr.agregarHijo(visit(ctx.factor()));
        return expr;
    }
    @Override
    public NodoAST visitNegacionLogica(YParser.NegacionLogicaContext ctx)
    {
        NodoAST expr = new NodoAST("Negacion Logica: !");
        expr.agregarHijo(visit(ctx.factor()));
        return expr;
    }

    // LITERALES
    
    @Override
    public NodoAST visitNumLiteral(YParser.NumLiteralContext ctx)
    {
        return new NodoAST("Numero: " + ctx.getText());
    }
    @Override
    public NodoAST visitDecLiteral(YParser.DecLiteralContext ctx)
    {
        return new NodoAST("Decimal: " + ctx.getText());
    }
    @Override
    public NodoAST visitTextLiteral(YParser.TextLiteralContext ctx)
    {
        return new NodoAST("Texto: " + ctx.getText());
    }
    @Override
    public NodoAST visitCharLiteral(YParser.CharLiteralContext ctx)
    {
        return new NodoAST("Caracter: " + ctx.getText());
    }
    @Override
    public NodoAST visitTrueLiteral(YParser.TrueLiteralContext ctx)
    {
        return new NodoAST("Booleano: true");
    }
    @Override
    public NodoAST visitFalseLiteral(YParser.FalseLiteralContext ctx)
    {
        return new NodoAST("Booleano: false");
    }
    @Override
    public NodoAST visitToFactor(YParser.ToFactorContext ctx)
    {
        return visit(ctx.factor());
    }
    @Override
    public NodoAST visitParentesis(YParser.ParentesisContext ctx)
    {
        return visit(ctx.expresion());
    }
    @Override
    protected NodoAST defaultResult()
    {
        return null;
    }
}