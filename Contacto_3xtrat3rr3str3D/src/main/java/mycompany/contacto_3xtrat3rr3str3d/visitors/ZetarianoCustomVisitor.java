package mycompany.contacto_3xtrat3rr3str3d.visitors;

import mycompany.contacto_3xtrat3rr3str3d.simbolos.*;
import mycompany.contacto_3xtrat3rr3str3d.c3d.GeneradorC3D;
import mycompany.contacto_3xtrat3rr3str3d.ZetarianoBaseVisitor;
import mycompany.contacto_3xtrat3rr3str3d.ZetarianoParser;
import javax.swing.JTextArea;
import java.util.Stack;
import mycompany.contacto_3xtrat3rr3str3d.utils.*;
import mycompany.contacto_3xtrat3rr3str3d.zetariano.*;

public class ZetarianoCustomVisitor extends ZetarianoBaseVisitor<Object>
{
    private TablaSimbolos tabla;
    private JTextArea consola;
    public boolean hayErroresSemanticos = false;
    private GeneradorC3D generador = GeneradorC3D.getInstancia();
    private Stack<String> pilaBreak = new Stack<>();
    private Stack<String> pilaContinue = new Stack<>();
    private Stack<String> pilaReturn = new Stack<>();
    private boolean limpiarAlIniciar = true;
    private Stack<String> pilaClaseActual = new Stack<>();
    
    private GestorEstructurasZ gestorEstructurasZ;
    private GestorVariablesZ gestorVariablesZ;
    private GestorControlFlujoZ gestorControlFlujoZ;
    private GestorExpresionesZ gestorExpresionesZ;
    
    public ZetarianoCustomVisitor(TablaSimbolos tabla, JTextArea consola)
    {
        this.tabla = tabla;
        this.consola = consola;
        this.gestorEstructurasZ = new GestorEstructurasZ(this);
        this.gestorControlFlujoZ = new GestorControlFlujoZ(this);
        this.gestorVariablesZ = new GestorVariablesZ(this);
        this.gestorExpresionesZ = new GestorExpresionesZ(this);
    }

    public ZetarianoCustomVisitor(TablaSimbolos tabla, JTextArea consola, boolean limpiarAlIniciar)
    {
        this(tabla, consola);
        this.limpiarAlIniciar = limpiarAlIniciar;
    }

    public Stack<String> getPilaBreak()
    {
        return pilaBreak;
    }
    public Stack<String> getPilaContinue()
    {
        return pilaContinue;
    }
    public Stack<String> getPilaReturn()
    {
        return pilaReturn;
    }
    public Stack<String> getPilaClaseActual()
    {
        return pilaClaseActual;
    }
    public void setLimpiarAlIniciar(boolean v)
    {
        this.limpiarAlIniciar = v;
    }
    
    public TablaSimbolos getTabla()
    {
        return tabla;
    }
    public JTextArea getConsola()
    {
        return consola;
    }
    public GeneradorC3D getGenerador()
    {
        return generador;
    }
    public boolean isHayErroresSemanticos()
    {
        return hayErroresSemanticos;
    }
    public void setHayErroresSemanticos(boolean err)
    {
        this.hayErroresSemanticos = err;
    }
    
    @Override
    public Object visitPrograma(ZetarianoParser.ProgramaContext ctx)
    {
        if (limpiarAlIniciar) generador.limpiar(); 
        return super.visitPrograma(ctx);
    }
    @Override
    public Object visitBloque(ZetarianoParser.BloqueContext ctx)
    {
        tabla.entrarAmbito();
        Object resultado = super.visitBloque(ctx);
        tabla.salirAmbito();
        return resultado;
    }
    
    // ESTRUCTURAS
    
    @Override
    public Object visitClase(ZetarianoParser.ClaseContext ctx)
    {
        return gestorEstructurasZ.procesarClase(ctx);
    }
    @Override
    public Object visitMetodo(ZetarianoParser.MetodoContext ctx)
    {
        return gestorEstructurasZ.procesarMetodo(ctx);
    }
    @Override
    public Object visitConstructor(ZetarianoParser.ConstructorContext ctx)
    {
        return gestorEstructurasZ.procesarConstructor(ctx);
    }
    @Override
    public Object visitInstanciaObjeto(ZetarianoParser.InstanciaObjetoContext ctx)
    {
        return gestorEstructurasZ.procesarInstanciaObjeto(ctx);
    }
    @Override
    public Object visitInstanciaArray(ZetarianoParser.InstanciaArrayContext ctx)
    {
        return gestorExpresionesZ.procesarInstanciaArray(ctx);
    }
    @Override
    public Object visitLlamadaFuncionOMetodo(ZetarianoParser.LlamadaFuncionOMetodoContext ctx)
    {
        return gestorEstructurasZ.procesarLlamadaFuncionOMetodo(ctx);
    }
    @Override
    public Object visitFuncionEspecial(ZetarianoParser.FuncionEspecialContext ctx)
    {
        return gestorEstructurasZ.procesarFuncionEspecial(ctx);
    }
    @Override
    public Object visitInstruccion(ZetarianoParser.InstruccionContext ctx)
    {
        if (ctx.RETURN() != null) return gestorEstructurasZ.procesarReturn(ctx);
        if (ctx.BREAK() != null) return gestorControlFlujoZ.procesarBreak(ctx);
        if (ctx.CONTINUE() != null) return gestorControlFlujoZ.procesarContinue(ctx);
        return super.visitInstruccion(ctx);
    }
    
    // VARIABLES
    
    @Override
    public Object visitDeclSinAsignar(ZetarianoParser.DeclSinAsignarContext ctx)
    {
        return gestorVariablesZ.procesarDeclSinAsignar(ctx);
    }
    @Override
    public Object visitDeclConAsignacion(ZetarianoParser.DeclConAsignacionContext ctx)
    {
        return gestorVariablesZ.procesarDeclConAsignacion(ctx);
    }
    @Override
    public Object visitDeclArrayLiteral(ZetarianoParser.DeclArrayLiteralContext ctx)
    {
        return gestorVariablesZ.procesarDeclArrayLiteral(ctx);
    }
    @Override
    public Object visitAsignacion(ZetarianoParser.AsignacionContext ctx)
    {
        return gestorVariablesZ.procesarAsignacion(ctx);
    }
        @Override
    public Object visitIncremento(ZetarianoParser.IncrementoContext ctx)
    {
        return gestorVariablesZ.procesarIncremento(ctx);
    }

    // CONTROL DE FLUJO
    
     @Override
    public Object visitCondicional(ZetarianoParser.CondicionalContext ctx)
    {
        return gestorControlFlujoZ.procesarCondicional(ctx);
    }
    @Override
    public Object visitCondicionalSwitch(ZetarianoParser.CondicionalSwitchContext ctx)
    {
        return gestorControlFlujoZ.procesarCondicionalSwitch(ctx);
    }
    @Override
    public Object visitBucleWhile(ZetarianoParser.BucleWhileContext ctx)
    {
        return gestorControlFlujoZ.procesarBucleWhile(ctx);
    }
    @Override
    public Object visitBucleDoWhile(ZetarianoParser.BucleDoWhileContext ctx)
    {
        return gestorControlFlujoZ.procesarBucleDoWhile(ctx);
    }
    @Override
    public Object visitBucleFor(ZetarianoParser.BucleForContext ctx)
    {
        return gestorControlFlujoZ.procesarBucleFor(ctx);
    }
    @Override
    public Object visitDeclaracionFor(ZetarianoParser.DeclaracionForContext ctx)
    {
        return gestorControlFlujoZ.procesarDeclaracionFor(ctx);
    }
    
    // Expresiones
    
    @Override
    public Object visitSumaResta(ZetarianoParser.SumaRestaContext ctx)
    {
        return gestorExpresionesZ.procesarSumaResta(ctx);
    }
    @Override
    public Object visitMultDiv(ZetarianoParser.MultDivContext ctx)
    {
        return gestorExpresionesZ.procesarMultDiv(ctx);
    }
    @Override
    public Object visitComparacion(ZetarianoParser.ComparacionContext ctx)
    {
        return gestorExpresionesZ.procesarComparacion(ctx);
    }
    @Override
    public Object visitIgualdad(ZetarianoParser.IgualdadContext ctx)
    {
        return gestorExpresionesZ.procesarIgualdad(ctx);
    }
    @Override
    public Object visitAndLogico(ZetarianoParser.AndLogicoContext ctx)
    {
        return gestorExpresionesZ.procesarAndLogico(ctx);
    }
    @Override
    public Object visitOrLogico(ZetarianoParser.OrLogicoContext ctx)
    {
        return gestorExpresionesZ.procesarOrLogico(ctx);
    }
    @Override
    public Object visitNegacionUnaria(ZetarianoParser.NegacionUnariaContext ctx)
    {
        return gestorExpresionesZ.procesarNegacionUnaria(ctx);
    }
    @Override
    public Object visitNegacionLogica(ZetarianoParser.NegacionLogicaContext ctx)
    {
        return gestorExpresionesZ.procesarNegacionLogica(ctx);
    }
    @Override
    public Object visitAccesoVariableOAtributo(ZetarianoParser.AccesoVariableOAtributoContext ctx)
    {
        return gestorExpresionesZ.procesarAccesoVariableOAtributo(ctx);
    }
    @Override
    public Object visitTernario(ZetarianoParser.TernarioContext ctx)
    {
        return gestorExpresionesZ.procesarTernario(ctx);
    }
    
    // LITERALES
    
    @Override
    public Object visitNumLiteral(ZetarianoParser.NumLiteralContext ctx)
    {
        return new ResultadoC3D(TipoDato.ENTERO, ctx.getText());
    }
    @Override
    public Object visitDecLiteral(ZetarianoParser.DecLiteralContext ctx)
    {
        return new ResultadoC3D(TipoDato.DECIMAL, ctx.getText());
    }
    @Override
    public Object visitCharLiteral(ZetarianoParser.CharLiteralContext ctx)
    {
        return new ResultadoC3D(TipoDato.CARACTER, ctx.getText());
    }
    @Override
    public Object visitTextoLiteral(ZetarianoParser.TextoLiteralContext ctx)
    {
        return GestorCadenas.guardarCadenaEnHeap(ctx.getText(), generador);
    }
    @Override
    public Object visitTrueLiteral(ZetarianoParser.TrueLiteralContext ctx)
    {
        return new ResultadoC3D(TipoDato.BOOLEANO, "1");
    }
    @Override
    public Object visitFalseLiteral(ZetarianoParser.FalseLiteralContext ctx)
    {
        return new ResultadoC3D(TipoDato.BOOLEANO, "0");
    }
    @Override
    public Object visitParentesis(ZetarianoParser.ParentesisContext ctx)
    {
        return visit(ctx.expresion());
    }
}