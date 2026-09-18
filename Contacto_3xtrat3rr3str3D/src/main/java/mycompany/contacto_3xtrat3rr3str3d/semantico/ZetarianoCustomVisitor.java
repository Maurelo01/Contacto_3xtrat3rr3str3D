package mycompany.contacto_3xtrat3rr3str3d.semantico;

import mycompany.contacto_3xtrat3rr3str3d.ZetarianoBaseVisitor;
import mycompany.contacto_3xtrat3rr3str3d.ZetarianoParser;
import javax.swing.JTextArea;

public class ZetarianoCustomVisitor extends ZetarianoBaseVisitor<Object>
{
    private TablaSimbolos tabla;
    private JTextArea consola;
    public boolean hayErroresSemanticos = false;
    public ZetarianoCustomVisitor(TablaSimbolos tabla, JTextArea consola)
    {
        this.tabla = tabla;
        this.consola = consola;
    }
    @Override
    public Object visitClase(ZetarianoParser.ClaseContext ctx)
    {
        String id = ctx.ID().getText();
        int linea = ctx.ID().getSymbol().getLine();
        int columna = ctx.ID().getSymbol().getCharPositionInLine();
        Simbolo simClase = new Simbolo(id, TipoDato.OBJETO.name(), "Clase", linea, columna);
        tabla.insertar(simClase);
        return super.visitClase(ctx);
    }
    @Override
    public Object visitMetodo(ZetarianoParser.MetodoContext ctx)
    {
        String tipoStr = ctx.tipo().tipoBase().getText();
        String id = ctx.ID().getText();
        int linea = ctx.ID().getSymbol().getLine();
        int columna = ctx.ID().getSymbol().getCharPositionInLine();
        TipoDato tipoNormalizado = ControlTipos.normalizarTipo(tipoStr);
        Simbolo simMetodo = new Simbolo(id, tipoNormalizado.name(), "Metodo", linea, columna);
        if (ctx.parametros() != null)
        {
            for (ZetarianoParser.ParametroContext pCtx : ctx.parametros().parametro())
            {
                simMetodo.agregarParametro(ControlTipos.normalizarTipo(pCtx.tipo().getText()));
            }
        }
        if (!tabla.insertar(simMetodo))
        {
            consola.append("Error Semántico en línea " + linea + ", columna " + columna + ": El método '" + id + "' ya existe.\n");
            hayErroresSemanticos = true;
        }
        tabla.entrarAmbito();
        if (ctx.parametros() != null)
        {
            for (ZetarianoParser.ParametroContext pCtx : ctx.parametros().parametro())
            {
                String paramTipo = pCtx.tipo().getText();
                String paramId = pCtx.ID().getText();
                Simbolo simParam = new Simbolo(paramId, ControlTipos.normalizarTipo(paramTipo).name(), "Variable", pCtx.ID().getSymbol().getLine(), pCtx.ID().getSymbol().getCharPositionInLine());
                tabla.insertar(simParam);
            }
        }
        Object resultado = visit(ctx.bloqueMetodo());
        consola.append("\n[RAYOS X] Memoria dentro del método '" + id + "':\n");
        consola.append(tabla.imprimirTabla());
        tabla.salirAmbito();
        return resultado;
    }
    
    @Override
    public Object visitBloque(ZetarianoParser.BloqueContext ctx)
    {
        tabla.entrarAmbito();
        Object resultado = super.visitBloque(ctx);
        tabla.salirAmbito();
        return resultado;
    }
    
    @Override
    public Object visitDeclSinAsignar(ZetarianoParser.DeclSinAsignarContext ctx)
    {
        String tipoStr = ctx.tipo().tipoBase().getText();
        String id = ctx.ID().getText();
        int dimensiones = ctx.tipo().CORCH_IZQ().size();
        String categoria = (dimensiones > 0) ? "Arreglo" : "Variable";
        int linea = ctx.ID().getSymbol().getLine();
        int columna = ctx.ID().getSymbol().getCharPositionInLine();
        TipoDato tipoNormalizado = ControlTipos.normalizarTipo(tipoStr);
        Simbolo nuevoSimbolo = new Simbolo(id, tipoNormalizado.name(), categoria, linea, columna);
        if (dimensiones > 0)
        {
            nuevoSimbolo.setDimensionArreglo(dimensiones);
        }
        if (tabla.buscar(id) != null)
        {
            consola.append("Error Semántico en línea " + linea + ", columna " + columna + ": La variable '" + id + "' ya ha sido declarada.\n");
            hayErroresSemanticos = true;
        }
        else
        {
            tabla.insertar(nuevoSimbolo);
        }
        return null;
    }
    
    @Override
    public Object visitDeclConAsignacion(ZetarianoParser.DeclConAsignacionContext ctx)
    {
        String tipoStr = ctx.tipo().tipoBase().getText();
        String id = ctx.ID().getText();
        int dimensiones = ctx.tipo().CORCH_IZQ().size();
        String categoria = (dimensiones > 0) ? "Arreglo" : "Variable";
        int linea = ctx.ID().getSymbol().getLine();
        int columna = ctx.ID().getSymbol().getCharPositionInLine();
        TipoDato tipoNormalizado = ControlTipos.normalizarTipo(tipoStr);
        Simbolo nuevoSimbolo = new Simbolo(id, tipoNormalizado.name(), categoria, linea, columna);
        if (dimensiones > 0)
        {
            nuevoSimbolo.setDimensionArreglo(dimensiones);
        }
        if (tabla.buscar(id) != null)
        {
            consola.append("Error Semántico en línea " + linea + ", columna " + columna + ": La variable '" + id + "' ya ha sido declarada.\n");
            hayErroresSemanticos = true;
        }
        else
        {
            tabla.insertar(nuevoSimbolo);
        }
        TipoDato tipoValor = (TipoDato) visit(ctx.expresion());
        if (tipoValor != null && tipoValor != TipoDato.ERROR)
        {
            if (!ControlTipos.esAsignacionValida(tipoNormalizado, tipoValor))
            {
                consola.append("Error Semántico en línea " + linea + ": Tipos incompatibles. No se puede asignar " + tipoValor + " a una variable " + tipoNormalizado + ".\n");
                hayErroresSemanticos = true;
            }
        }
        return null;
    }

    @Override
    public Object visitConstructor(ZetarianoParser.ConstructorContext ctx)
    {
        String id = ctx.ID().getText();
        String idInterno = id + "_constructor";
        int linea = ctx.ID().getSymbol().getLine();
        int columna = ctx.ID().getSymbol().getCharPositionInLine();
        Simbolo simConstructor = new Simbolo(idInterno, TipoDato.OBJETO.name(), "Constructor", linea, columna);
        if (ctx.parametros() != null)
        {
            for (ZetarianoParser.ParametroContext pCtx : ctx.parametros().parametro())
            {
                simConstructor.agregarParametro(ControlTipos.normalizarTipo(pCtx.tipo().tipoBase().getText()));
            }
        }
        if (tabla.buscar(idInterno) != null)
        {
            consola.append("Error Semántico en línea " + linea + ", columna " + columna + ": El constructor ya existe.\n");
            hayErroresSemanticos = true;
        }
        else
        {
            tabla.insertar(simConstructor);
        }
        tabla.entrarAmbito();
        if (ctx.parametros() != null)
        {
            for (ZetarianoParser.ParametroContext pCtx : ctx.parametros().parametro())
            {
                String paramTipo = pCtx.tipo().tipoBase().getText();
                String paramId = pCtx.ID().getText();
                Simbolo simParam = new Simbolo(paramId, ControlTipos.normalizarTipo(paramTipo).name(), "Variable", pCtx.ID().getSymbol().getLine(), pCtx.ID().getSymbol().getCharPositionInLine());
                tabla.insertar(simParam);
            }
        }
        Object resultado = visit(ctx.bloqueMetodo());
        consola.append("\n[RAYOS X] Memoria dentro del constructor '" + id + "':\n");
        consola.append(tabla.imprimirTabla());
        tabla.salirAmbito();
        return resultado;
    }
    
    @Override
    public Object visitNumLiteral(ZetarianoParser.NumLiteralContext ctx)
    {
        return TipoDato.ENTERO;
    }
    @Override
    public Object visitDecLiteral(ZetarianoParser.DecLiteralContext ctx)
    {
        return TipoDato.DECIMAL;
    }
    @Override
    public Object visitTextoLiteral(ZetarianoParser.TextoLiteralContext ctx)
    {
        return TipoDato.CADENA;
    }
    @Override
    public Object visitCharLiteral(ZetarianoParser.CharLiteralContext ctx)
    {
        return TipoDato.CARACTER;
    }
    @Override
    public Object visitTrueLiteral(ZetarianoParser.TrueLiteralContext ctx)
    {
        return TipoDato.BOOLEANO;
    }
    @Override
    public Object visitFalseLiteral(ZetarianoParser.FalseLiteralContext ctx)
    {
        return TipoDato.BOOLEANO;
    }

    @Override
    public Object visitAccesoVariableOAtributo(ZetarianoParser.AccesoVariableOAtributoContext ctx)
    {
        String idVariable = ctx.acceso().ID(0).getText();
        Simbolo sim = tabla.buscar(idVariable);
        if (sim == null)
        {
            int linea = ctx.acceso().ID(0).getSymbol().getLine();
            int columna = ctx.acceso().ID(0).getSymbol().getCharPositionInLine();
            consola.append("Error Semántico en línea " + linea + ", columna " + columna + ": La variable '" + idVariable + "' no existe o no ha sido declarada.\n");
            hayErroresSemanticos = true;
            return TipoDato.ERROR;
        }
        return TipoDato.valueOf(sim.getTipo());
    }

    @Override
    public Object visitSumaResta(ZetarianoParser.SumaRestaContext ctx)
    {
        TipoDato izq = (TipoDato) visit(ctx.expresion(0));
        TipoDato der = (TipoDato) visit(ctx.expresion(1));
        TipoDato resultado;
        if (ctx.MAS() != null)
        {
            resultado = ControlTipos.resolverSuma(izq, der);
        }
        else
        {
            resultado = ControlTipos.resolverAritmetica(izq, der);
        }
        if (resultado == TipoDato.ERROR)
        {
            int linea = ctx.getStart().getLine();
            consola.append("Error Semántico en línea " + linea + ": Incompatibilidad de tipos en la operación (" + izq + " y " + der + ").\n");
            hayErroresSemanticos = true;
        }
        return resultado;
    }
    
    @Override
    public Object visitMultDiv(ZetarianoParser.MultDivContext ctx)
    {
        TipoDato izq = (TipoDato) visit(ctx.expresion(0));
        TipoDato der = (TipoDato) visit(ctx.expresion(1));
        TipoDato resultado = ControlTipos.resolverAritmetica(izq, der);
        if (resultado == TipoDato.ERROR)
        {
            int linea = ctx.getStart().getLine();
            consola.append("Error Semántico en línea " + linea + ": Incompatibilidad de tipos en multiplicación/división/módulo (" + izq + " y " + der + ").\n");
            hayErroresSemanticos = true;
        }
        return resultado;
    }

    @Override
    public Object visitComparacion(ZetarianoParser.ComparacionContext ctx)
    {
        TipoDato izq = (TipoDato) visit(ctx.expresion(0));
        TipoDato der = (TipoDato) visit(ctx.expresion(1));
        TipoDato resultado = ControlTipos.resolverRelacional(izq, der);
        if (resultado == TipoDato.ERROR)
        {
            int linea = ctx.getStart().getLine();
            consola.append("Error Semántico en línea " + linea + ": No se pueden comparar relacionalmente los tipos " + izq + " y " + der + ".\n");
            hayErroresSemanticos = true;
        }
        return resultado;
    }

    @Override
    public Object visitIgualdad(ZetarianoParser.IgualdadContext ctx)
    {
        TipoDato izq = (TipoDato) visit(ctx.expresion(0));
        TipoDato der = (TipoDato) visit(ctx.expresion(1));
        TipoDato resultado = ControlTipos.resolverIgualdad(izq, der);
        if (resultado == TipoDato.ERROR)
        {
            int linea = ctx.getStart().getLine();
            consola.append("Error Semántico en línea " + linea + ": No se puede evaluar igualdad entre " + izq + " y " + der + ".\n");
            hayErroresSemanticos = true;
        }
        return resultado;
    }

    @Override
    public Object visitAndLogico(ZetarianoParser.AndLogicoContext ctx)
    {
        return evaluarLogica(ctx.expresion(0), ctx.expresion(1), ctx.getStart().getLine());
    }

    @Override
    public Object visitOrLogico(ZetarianoParser.OrLogicoContext ctx)
    {
        return evaluarLogica(ctx.expresion(0), ctx.expresion(1), ctx.getStart().getLine());
    }

    private TipoDato evaluarLogica(ZetarianoParser.ExpresionContext exprIzq, ZetarianoParser.ExpresionContext exprDer, int linea)
    {
        TipoDato izq = (TipoDato) visit(exprIzq);
        TipoDato der = (TipoDato) visit(exprDer);
        TipoDato resultado = ControlTipos.resolverLogica(izq, der);
        if (resultado == TipoDato.ERROR)
        {
            consola.append("Error Semántico en línea " + linea + ": Los operadores lógicos &&, || requieren booleanos. Se encontró " + izq + " y " + der + ".\n");
            hayErroresSemanticos = true;
        }
        return resultado;
    }

    @Override
    public Object visitNegacionUnaria(ZetarianoParser.NegacionUnariaContext ctx)
    {
        TipoDato tipo = (TipoDato) visit(ctx.factor());
        TipoDato resultado = ControlTipos.resolverUnariaAritmetica(tipo);
        if (resultado == TipoDato.ERROR)
        {
            consola.append("Error Semántico en línea " + ctx.getStart().getLine() + ": No se puede aplicar el signo negativo a un tipo " + tipo + ".\n");
            hayErroresSemanticos = true;
        }
        return resultado;
    }

    @Override
    public Object visitNegacionLogica(ZetarianoParser.NegacionLogicaContext ctx)
    {
        TipoDato tipo = (TipoDato) visit(ctx.factor());
        TipoDato resultado = ControlTipos.resolverUnariaLogica(tipo);
        if (resultado == TipoDato.ERROR)
        {
            consola.append("Error Semántico en línea " + ctx.getStart().getLine() + ": No se puede aplicar negación lógica a un tipo " + tipo + ".\n");
            hayErroresSemanticos = true;
        }
        return resultado;
    }

    @Override
    public Object visitParentesis(ZetarianoParser.ParentesisContext ctx)
    {
        return visit(ctx.expresion());
    }

    @Override
    public Object visitAsignacion(ZetarianoParser.AsignacionContext ctx)
    {
        String idVariable = ctx.acceso().ID(0).getText();
        Simbolo sim = tabla.buscar(idVariable);
        int linea = ctx.getStart().getLine();
        if (sim == null)
        {
            consola.append("Error Semántico en línea " + linea + ": La variable '" + idVariable + "' no ha sido declarada.\n");
            hayErroresSemanticos = true;
            return null;
        }
        TipoDato tipoVariable = TipoDato.valueOf(sim.getTipo());
        TipoDato tipoExpr = (TipoDato) visit(ctx.expresion());
        if (tipoExpr != null && tipoExpr != TipoDato.ERROR)
        {
            if (!ControlTipos.esAsignacionValida(tipoVariable, tipoExpr))
            {
                consola.append("Error Semántico en línea " + linea + ": Tipos incompatibles. No se puede asignar " + tipoExpr + " a la variable '" + idVariable + "' de tipo " + tipoVariable + ".\n");
                hayErroresSemanticos = true;
            }
        }
        return null;
    }
}
