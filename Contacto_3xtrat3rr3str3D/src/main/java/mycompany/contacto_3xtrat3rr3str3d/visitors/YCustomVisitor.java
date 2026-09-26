package mycompany.contacto_3xtrat3rr3str3d.visitors;

import mycompany.contacto_3xtrat3rr3str3d.simbolos.*;
import mycompany.contacto_3xtrat3rr3str3d.c3d.GeneradorC3D;
import mycompany.contacto_3xtrat3rr3str3d.YBaseVisitor;
import mycompany.contacto_3xtrat3rr3str3d.YParser;
import javax.swing.JTextArea;
import mycompany.contacto_3xtrat3rr3str3d.utils.*;
import mycompany.contacto_3xtrat3rr3str3d.y.*;

public class YCustomVisitor extends YBaseVisitor<Object>
{
    private TablaSimbolos tabla;
    private JTextArea consola;
    public boolean hayErroresSemanticos = false;
    private GeneradorC3D generador = GeneradorC3D.getInstancia();
    
    private GestorEstructurasFuncionesY gestorEstructurasFuncionesY;
    private GestorVariablesY gestorVariablesY;
    private GestorControlFlujoY gestorControlFlujoY;
    private GestorExpresionesY gestorExpresionesY;
    
    public YCustomVisitor(TablaSimbolos tabla, JTextArea consola)
    {
        this.tabla = tabla;
        this.consola = consola;
        this.gestorEstructurasFuncionesY = new GestorEstructurasFuncionesY(this);
        this.gestorVariablesY = new GestorVariablesY(this);
        this.gestorControlFlujoY = new GestorControlFlujoY(this);
        this.gestorExpresionesY = new GestorExpresionesY(this);
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
    public Object visitPrograma(YParser.ProgramaContext ctx)
    {
        generador.limpiar(); 
        return super.visitPrograma(ctx);
    }
    @Override
    public Object visitBloque(YParser.BloqueContext ctx)
    {
        tabla.entrarAmbito();
        Object resultado = super.visitBloque(ctx);
        tabla.salirAmbito();
        return resultado;
    }

    // FUNCIONES/ESTRUCTURAS
    
    @Override
    public Object visitDefinicionFuncion(YParser.DefinicionFuncionContext ctx)
    {
        return gestorEstructurasFuncionesY.procesarDefinicionFuncion(ctx);
    }
    @Override
    public Object visitDefinicionEstructura(YParser.DefinicionEstructuraContext ctx)
    {
        return gestorEstructurasFuncionesY.procesarDefinicionEstructura(ctx);
    }
    @Override
    public Object visitInstruccionImprimir(YParser.InstruccionImprimirContext ctx)
    {
        return gestorEstructurasFuncionesY.procesarInstruccionImprimir(ctx);
    }
    @Override
    public Object visitInstruccionRetornar(YParser.InstruccionRetornarContext ctx)
    {
        return gestorEstructurasFuncionesY.procesarInstruccionRetornar(ctx);
    }
    @Override
    public Object visitLlamadaFuncionExpr(YParser.LlamadaFuncionExprContext ctx)
    {
        return gestorEstructurasFuncionesY.procesarLlamadaFuncion(ctx.llamadaFuncion());
    }
    @Override
    public Object visitInstruccionLlamadaFuncion(YParser.InstruccionLlamadaFuncionContext ctx)
    {
        return gestorEstructurasFuncionesY.procesarLlamadaFuncion(ctx.llamadaFuncion());
    }
    
    // VARIABLES
    
    @Override
    public Object visitDeclVariableAsig(YParser.DeclVariableAsigContext ctx)
    {
        return gestorVariablesY.procesarDeclVariableAsig(ctx);
    }
    @Override
    public Object visitDeclVariable(YParser.DeclVariableContext ctx)
    {
        return gestorVariablesY.procesarDeclVariable(ctx);
    }
    @Override
    public Object visitDeclEstructura(YParser.DeclEstructuraContext ctx)
    {
        return gestorVariablesY.procesarDeclEstructura(ctx);
    }
    @Override
    public Object visitDeclEstructuraAsig(YParser.DeclEstructuraAsigContext ctx)
    {
        return gestorVariablesY.procesarDeclEstructuraAsig(ctx);
    }
    
    // ASIGNACIONES
    
    @Override
    public Object visitAsignacion(YParser.AsignacionContext ctx)
    {
        return gestorVariablesY.procesarAsignacion(ctx);
    }
    @Override
    public Object visitIncremento(YParser.IncrementoContext ctx)
    {
        return gestorVariablesY.procesarIncremento(ctx);
    }

    // CONTROL DE FLUJO
    
    @Override
    public Object visitCondicional(YParser.CondicionalContext ctx)
    {
        return gestorControlFlujoY.procesarCondicional(ctx);
    }
    @Override
    public Object visitBucleMientras(YParser.BucleMientrasContext ctx)
    {
        return gestorControlFlujoY.procesarBucleMientras(ctx);
    }
    @Override
    public Object visitBucleHacer(YParser.BucleHacerContext ctx)
    {
        return gestorControlFlujoY.procesarBucleHacer(ctx);
    }
    @Override
    public Object visitBuclePara(YParser.BucleParaContext ctx)
    {
        return gestorControlFlujoY.procesarBuclePara(ctx);
    }
    @Override
    public Object visitCondicionalElegir(YParser.CondicionalElegirContext ctx)
    {
        return gestorControlFlujoY.procesarCondicionalElegir(ctx);
    }

    // EXPRESIONES
    
    @Override
    public Object visitSumaResta(YParser.SumaRestaContext ctx)
    {
        return gestorExpresionesY.procesarSumaResta(ctx);
    }
    @Override
    public Object visitMultDiv(YParser.MultDivContext ctx)
    {
        return gestorExpresionesY.procesarMultDiv(ctx);
    }
    @Override
    public Object visitComparacion(YParser.ComparacionContext ctx)
    {
        return gestorExpresionesY.procesarComparacion(ctx);
    }
    @Override
    public Object visitIgualdad(YParser.IgualdadContext ctx)
    {
        return gestorExpresionesY.procesarIgualdad(ctx);
    }
    @Override
    public Object visitAndLogico(YParser.AndLogicoContext ctx)
    {
        return gestorExpresionesY.procesarAndLogico(ctx);
    }
    @Override
    public Object visitOrLogico(YParser.OrLogicoContext ctx)
    {
        return gestorExpresionesY.procesarOrLogico(ctx);
    }
    @Override
    public Object visitNegacionUnaria(YParser.NegacionUnariaContext ctx)
    {
        return gestorExpresionesY.procesarNegacionUnaria(ctx);
    }
    @Override
    public Object visitNegacionLogica(YParser.NegacionLogicaContext ctx)
    {
        return gestorExpresionesY.procesarNegacionLogica(ctx);
    }
    @Override
    public Object visitAccesoVariableOAtributo(YParser.AccesoVariableOAtributoContext ctx)
    {
        return gestorExpresionesY.procesarAccesoVariableOAtributo(ctx);
    }
    
    // LITERALES
    
    @Override
    public Object visitNumLiteral(YParser.NumLiteralContext ctx)
    {
        return new ResultadoC3D(TipoDato.ENTERO, ctx.getText());
    }
    @Override
    public Object visitDecLiteral(YParser.DecLiteralContext ctx)
    {
        return new ResultadoC3D(TipoDato.DECIMAL, ctx.getText());
    }
    @Override
    public Object visitTextLiteral(YParser.TextLiteralContext ctx)
    {
        String texto = ctx.getText();
        return GestorCadenas.guardarCadenaEnHeap(texto, generador);
    }
    @Override
    public Object visitCharLiteral(YParser.CharLiteralContext ctx)
    {
        return new ResultadoC3D(TipoDato.CARACTER, ctx.getText());
    }
    @Override
    public Object visitTrueLiteral(YParser.TrueLiteralContext ctx)
    {
        return new ResultadoC3D(TipoDato.BOOLEANO, "1");
    }
    @Override
    public Object visitFalseLiteral(YParser.FalseLiteralContext ctx)
    {
        return new ResultadoC3D(TipoDato.BOOLEANO, "0");
    }
    @Override
    public Object visitToFactor(YParser.ToFactorContext ctx)
    {
        return visit(ctx.factor());
    }
    @Override
    public Object visitParentesis(YParser.ParentesisContext ctx)
    {
        return visit(ctx.expresion());
    }
}