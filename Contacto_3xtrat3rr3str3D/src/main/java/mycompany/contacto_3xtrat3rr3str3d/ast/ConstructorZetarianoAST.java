package mycompany.contacto_3xtrat3rr3str3d.ast;

import mycompany.contacto_3xtrat3rr3str3d.ZetarianoBaseVisitor;
import mycompany.contacto_3xtrat3rr3str3d.ZetarianoParser;

public class ConstructorZetarianoAST extends ZetarianoBaseVisitor<NodoAST>
{
    @Override
    public NodoAST visitPrograma(ZetarianoParser.ProgramaContext ctx)
    {
        NodoAST raiz = new NodoAST("Programa Zetariano");
        if (ctx.clase() != null) raiz.agregarHijo(visit(ctx.clase()));
        return raiz;
    }
    
    @Override
    public NodoAST visitClase(ZetarianoParser.ClaseContext ctx)
    {
        NodoAST nodoClase = new NodoAST("Clase: " + ctx.ID().getText());
        for (ZetarianoParser.MiembroContext miembro : ctx.miembro())
        {
            NodoAST hijo = visit(miembro);
            if (hijo != null) nodoClase.agregarHijo(hijo);
        }
        return nodoClase;
    }
    @Override
    public NodoAST visitMetodo(ZetarianoParser.MetodoContext ctx)
    {
        NodoAST nodoMetodo = new NodoAST("Metodo: " + ctx.ID().getText() + " -> " + ctx.tipo().getText());
        if (ctx.parametros() != null)
        {
            NodoAST params = new NodoAST("Parametros");
            for (ZetarianoParser.ParametroContext p : ctx.parametros().parametro())
            {
                params.agregarHijo(p.tipo().getText() + " " + p.ID().getText());
            }
            nodoMetodo.agregarHijo(params);
        }
        nodoMetodo.agregarHijo(visit(ctx.bloqueMetodo()));
        return nodoMetodo;
    }
    @Override
    public NodoAST visitBloque(ZetarianoParser.BloqueContext ctx)
    {
        NodoAST nodoBloque = new NodoAST("Bloque");
        for (ZetarianoParser.InstruccionContext inst : ctx.instruccion())
        {
            NodoAST hijo = visit(inst);
            if (hijo != null) nodoBloque.agregarHijo(hijo);
        }
        return nodoBloque;
    }
    @Override
    public NodoAST visitBloqueMetodo(ZetarianoParser.BloqueMetodoContext ctx)
    {
        NodoAST nodoBloque = new NodoAST("Bloque de Metodo");
        for (ZetarianoParser.InstruccionContext inst : ctx.instruccion())
        {
            NodoAST hijo = visit(inst);
            if (hijo != null) nodoBloque.agregarHijo(hijo);
        }
        return nodoBloque;
    }
    
    // ESTRUCTURAS
    
    @Override
    public NodoAST visitConstructor(ZetarianoParser.ConstructorContext ctx)
    {
        NodoAST nodoConst = new NodoAST("Constructor: " + ctx.ID().getText());
        if (ctx.parametros() != null)
        {
            NodoAST params = new NodoAST("Parametros");
            for (ZetarianoParser.ParametroContext p : ctx.parametros().parametro())
            {
                params.agregarHijo(p.tipo().getText() + " " + p.ID().getText());
            }
            nodoConst.agregarHijo(params);
        }
        nodoConst.agregarHijo(visit(ctx.bloqueMetodo())); 
        return nodoConst;
    }
    @Override
    public NodoAST visitInstanciaObjeto(ZetarianoParser.InstanciaObjetoContext ctx)
    {
        NodoAST inst = new NodoAST("Instancia Objeto: " + ctx.ID().getText());
        if (ctx.argumentos() != null)
        {
            NodoAST args = new NodoAST("Argumentos");
            for (ZetarianoParser.ExpresionContext exp : ctx.argumentos().expresion())
            {
                args.agregarHijo(visit(exp));
            }
            inst.agregarHijo(args);
        }
        return inst;
    }
    @Override
    public NodoAST visitInstanciaArray(ZetarianoParser.InstanciaArrayContext ctx)
    {
        NodoAST inst = new NodoAST("Instancia Arreglo: " + ctx.tipoBase().getText());
        for (ZetarianoParser.ExpresionContext exp : ctx.expresion())
        {
            inst.agregarHijo("Dimension: " + exp.getText());
        }
        return inst;
    }
    @Override
    public NodoAST visitLlamadaFuncionOMetodo(ZetarianoParser.LlamadaFuncionOMetodoContext ctx)
    {
        NodoAST llamada = new NodoAST("Llamada: " + ctx.getChild(0).getText());
        if (ctx.argumentos() != null)
        {
            NodoAST args = new NodoAST("Argumentos");
            for (ZetarianoParser.ExpresionContext exp : ctx.argumentos().expresion())
            {
                args.agregarHijo(visit(exp));
            }
            llamada.agregarHijo(args);
        }
        return llamada;
    }
    @Override
    public NodoAST visitInstruccion(ZetarianoParser.InstruccionContext ctx)
    {
        if (ctx.RETURN() != null)
        {
            NodoAST ret = new NodoAST("Retornar");
            if (ctx.expresion() != null) ret.agregarHijo(visit(ctx.expresion()));
            return ret;
        }
        if (ctx.BREAK() != null) return new NodoAST("Romper (Break)");
        if (ctx.CONTINUE() != null) return new NodoAST("Continuar (Continue)");
        if (ctx.PAREN_IZQ() != null && ctx.acceso() != null)
        {
            NodoAST llamada = new NodoAST("Llamada a Metodo: " + ctx.acceso().getText());
            if (ctx.argumentos() != null)
            {
                NodoAST args = new NodoAST("Argumentos");
                for (ZetarianoParser.ExpresionContext exp : ctx.argumentos().expresion())
                {
                    args.agregarHijo(visit(exp));
                }
                llamada.agregarHijo(args);
            }
            return llamada;
        }
        return visit(ctx.getChild(0));
    }
    
    // VARIABLES
    
    @Override
    public NodoAST visitDeclSinAsignar(ZetarianoParser.DeclSinAsignarContext ctx)
    {
        return new NodoAST("Declaracion: " + ctx.tipo().getText() + " " + ctx.ID().getText());
    }
    @Override
    public NodoAST visitDeclConAsignacion(ZetarianoParser.DeclConAsignacionContext ctx)
    {
        NodoAST nodoDecl = new NodoAST("Declaracion: " + ctx.tipo().getText() + " " + ctx.ID().getText());
        NodoAST valor = visit(ctx.expresion());
        if (valor != null) nodoDecl.agregarHijo(valor);
        return nodoDecl;
    }
    @Override
    public NodoAST visitIncremento(ZetarianoParser.IncrementoContext ctx)
    {
        return new NodoAST("Incremento/Decremento: " + ctx.getText());
    }
    @Override
    public NodoAST visitDeclArrayLiteral(ZetarianoParser.DeclArrayLiteralContext ctx)
    {
        NodoAST decl = new NodoAST("Arreglo Literal: " + ctx.tipo().getText() + " " + ctx.ID().getText());
        if (ctx.argumentos() != null)
        {
            NodoAST args = new NodoAST("Valores Iniciales");
            for (ZetarianoParser.ExpresionContext exp : ctx.argumentos().expresion())
            {
                args.agregarHijo(visit(exp));
            }
            decl.agregarHijo(args);
        }
        return decl;
    }
    
    // ASIGNACION
    
    @Override
    public NodoAST visitAsignacion(ZetarianoParser.AsignacionContext ctx)
    {
        NodoAST nodoAsig = new NodoAST("Asignacion");
        nodoAsig.agregarHijo("Destino: " + ctx.acceso().getText());
        NodoAST valor = visit(ctx.expresion());
        if (valor != null) nodoAsig.agregarHijo(valor);
        else nodoAsig.agregarHijo("Valor: " + ctx.expresion().getText());
        return nodoAsig;
    }
    @Override
    public NodoAST visitCondicionalSwitch(ZetarianoParser.CondicionalSwitchContext ctx)
    {
        NodoAST nodoSwitch = new NodoAST("Condicional SWITCH");
        for (int i = 0; i < ctx.getChildCount(); i++)
        {
            NodoAST hijo = visit(ctx.getChild(i));
            if (hijo != null) nodoSwitch.agregarHijo(hijo);
        }
        return nodoSwitch;
    }
    @Override
    public NodoAST visitBucleDoWhile(ZetarianoParser.BucleDoWhileContext ctx)
    {
        NodoAST bucle = new NodoAST("Bucle DO WHILE");
        bucle.agregarHijo(visit(ctx.bloqueMetodo()));
        bucle.agregarHijo(visit(ctx.expresion()));
        return bucle;
    }
    @Override
    public NodoAST visitBucleFor(ZetarianoParser.BucleForContext ctx)
    {
        NodoAST bucle = new NodoAST("Bucle FOR");
        for (int i = 0; i < ctx.getChildCount(); i++)
        {
            NodoAST hijo = visit(ctx.getChild(i));
            if (hijo != null) bucle.agregarHijo(hijo);
        }
        return bucle;
    }
    @Override
    public NodoAST visitDeclaracionFor(ZetarianoParser.DeclaracionForContext ctx)
    {
        NodoAST declFor = new NodoAST("Declaracion FOR");
        for (int i = 0; i < ctx.getChildCount(); i++)
        {
            NodoAST hijo = visit(ctx.getChild(i));
            if (hijo != null) declFor.agregarHijo(hijo);
        }
        return declFor;
    }
    
    // CONTROL DE FLUJO
    
    @Override
    public NodoAST visitCondicional(ZetarianoParser.CondicionalContext ctx)
    {
        NodoAST nodoIf = new NodoAST("Condicional IF");
        for (int i = 0; i < ctx.expresion().size(); i++)
        {
            NodoAST rama = new NodoAST(i == 0 ? "SI" : "SINO SI");
            rama.agregarHijo(visit(ctx.expresion(i)));
            rama.agregarHijo(visit(ctx.bloque(i)));
            nodoIf.agregarHijo(rama);
        }
        if (ctx.ELSE() != null)
        {
            NodoAST ramaElse = new NodoAST("SINO (Else)");
            ramaElse.agregarHijo(visit(ctx.bloque(ctx.bloque().size() - 1)));
            nodoIf.agregarHijo(ramaElse);
        }
        return nodoIf;
    }
    @Override
    public NodoAST visitBucleWhile(ZetarianoParser.BucleWhileContext ctx)
    {
        NodoAST nodoWhile = new NodoAST("Bucle WHILE");
        nodoWhile.agregarHijo(visit(ctx.expresion()));
        nodoWhile.agregarHijo(visit(ctx.bloque()));
        return nodoWhile;
    }
    
    // EXPRESIONES
    
    @Override
    public NodoAST visitSumaResta(ZetarianoParser.SumaRestaContext ctx)
    {
        String op = ctx.MAS() != null ? "+" : "-";
        NodoAST nodoOp = new NodoAST("Operacion: " + op);
        nodoOp.agregarHijo(visit(ctx.expresion(0)));
        nodoOp.agregarHijo(visit(ctx.expresion(1)));
        return nodoOp;
    }
    @Override
    public NodoAST visitMultDiv(ZetarianoParser.MultDivContext ctx)
    {
        String op = ctx.POR() != null ? "*" : (ctx.DIVISION() != null ? "/" : "%");
        NodoAST nodoOp = new NodoAST("Operacion: " + op);
        nodoOp.agregarHijo(visit(ctx.expresion(0)));
        nodoOp.agregarHijo(visit(ctx.expresion(1)));
        return nodoOp;
    }
    @Override
    public NodoAST visitComparacion(ZetarianoParser.ComparacionContext ctx)
    {
        String op = ctx.MAYOR() != null ? ">" : ctx.MAYOR_IGUAL() != null ? ">=" : ctx.MENOR() != null ? "<" : "<=";
        NodoAST nodoOp = new NodoAST("Comparacion: " + op);
        nodoOp.agregarHijo(visit(ctx.expresion(0)));
        nodoOp.agregarHijo(visit(ctx.expresion(1)));
        return nodoOp;
    }
    @Override
    public NodoAST visitIgualdad(ZetarianoParser.IgualdadContext ctx)
    {
        String op = ctx.IGUALIGUAL() != null ? "==" : "!=";
        NodoAST nodoOp = new NodoAST("Igualdad: " + op);
        nodoOp.agregarHijo(visit(ctx.expresion(0)));
        nodoOp.agregarHijo(visit(ctx.expresion(1)));
        return nodoOp;
    }
    @Override
    public NodoAST visitFuncionEspecial(ZetarianoParser.FuncionEspecialContext ctx)
    {
        NodoAST nodoPrint = new NodoAST("Imprimir (Print)");
        if (ctx.expresion() != null) nodoPrint.agregarHijo(visit(ctx.expresion()));
        return nodoPrint;
    }
    @Override
    public NodoAST visitAndLogico(ZetarianoParser.AndLogicoContext ctx)
    {
        NodoAST expr = new NodoAST("Logico: &&");
        expr.agregarHijo(visit(ctx.expresion(0)));
        expr.agregarHijo(visit(ctx.expresion(1)));
        return expr;
    }
    @Override
    public NodoAST visitOrLogico(ZetarianoParser.OrLogicoContext ctx)
    {
        NodoAST expr = new NodoAST("Logico: ||");
        expr.agregarHijo(visit(ctx.expresion(0)));
        expr.agregarHijo(visit(ctx.expresion(1)));
        return expr;
    }
    @Override
    public NodoAST visitNegacionUnaria(ZetarianoParser.NegacionUnariaContext ctx)
    {
        NodoAST expr = new NodoAST("Negacion Unaria: -");
        expr.agregarHijo(visit(ctx.factor()));
        return expr;
    }
    @Override
    public NodoAST visitNegacionLogica(ZetarianoParser.NegacionLogicaContext ctx)
    {
        NodoAST expr = new NodoAST("Negacion Logica: !");
        expr.agregarHijo(visit(ctx.factor()));
        return expr;
    }

    // LITERALES
    
    @Override
    public NodoAST visitNumLiteral(ZetarianoParser.NumLiteralContext ctx)
    {
        return new NodoAST("Numero: " + ctx.getText());
    }
    @Override
    public NodoAST visitDecLiteral(ZetarianoParser.DecLiteralContext ctx)
    {
        return new NodoAST("Decimal: " + ctx.getText());
    }
    @Override
    public NodoAST visitTextoLiteral(ZetarianoParser.TextoLiteralContext ctx)
    {
        return new NodoAST("Texto: " + ctx.getText());
    }
    @Override
    public NodoAST visitTrueLiteral(ZetarianoParser.TrueLiteralContext ctx)
    {
        return new NodoAST("Booleano: True");
    }
    @Override
    public NodoAST visitFalseLiteral(ZetarianoParser.FalseLiteralContext ctx)
    {
        return new NodoAST("Booleano: False");
    }
    @Override
    public NodoAST visitAccesoVariableOAtributo(ZetarianoParser.AccesoVariableOAtributoContext ctx)
    {
        return new NodoAST("Acceso a: " + ctx.getText());
    }
    @Override
    public NodoAST visitCharLiteral(ZetarianoParser.CharLiteralContext ctx)
    {
        return new NodoAST("Caracter: " + ctx.getText());
    }
    @Override
    public NodoAST visitParentesis(ZetarianoParser.ParentesisContext ctx)
    {
        return visit(ctx.expresion());
    }

    @Override
    protected NodoAST defaultResult()
    {
        return null;
    }
}
