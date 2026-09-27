package mycompany.contacto_3xtrat3rr3str3d.visitors;

import mycompany.contacto_3xtrat3rr3str3d.simbolos.*;
import mycompany.contacto_3xtrat3rr3str3d.c3d.GeneradorC3D;
import javax.swing.JTextArea;
import java.io.File;
import java.util.Stack;
import mycompany.contacto_3xtrat3rr3str3d.PigLatinBaseVisitor;
import mycompany.contacto_3xtrat3rr3str3d.PigLatinParser;
import mycompany.contacto_3xtrat3rr3str3d.piglatin.*;
import mycompany.contacto_3xtrat3rr3str3d.servicios.GestorImportaciones;
import mycompany.contacto_3xtrat3rr3str3d.utils.*;

public class PigLatinCustomVisitor extends PigLatinBaseVisitor<Object>
{
    private TablaSimbolos tabla;
    private JTextArea consola;
    public boolean hayErroresSemanticos = false;
    private GeneradorC3D generador = GeneradorC3D.getInstancia();
    private Stack<String> pilaBreak = new Stack<>();
    private Stack<String> pilaContinue = new Stack<>();
    private Stack<String> pilaReturn = new Stack<>();
    private File archivoActual;
    private File carpetaProyecto;
    
    private GestorVariablesPig gestorVariablesPig;
    private GestorFuncionesPig gestorFuncionesPig;
    private GestorControlFlujoPig gestorControlFlujoPig;
    private GestorExpresionesPig gestorExpresionesPig;
    
    public PigLatinCustomVisitor(TablaSimbolos tabla, JTextArea consola)
    {
        this.tabla = tabla;
        this.consola = consola;
        this.gestorVariablesPig = new GestorVariablesPig(this);
        this.gestorFuncionesPig = new GestorFuncionesPig(this);
        this.gestorControlFlujoPig = new GestorControlFlujoPig(this);
        this.gestorExpresionesPig = new GestorExpresionesPig(this);
    }

    public PigLatinCustomVisitor(TablaSimbolos tabla, JTextArea consola, File archivoActual, File carpetaProyecto)
    {
        this(tabla, consola);
        this.archivoActual = archivoActual;
        this.carpetaProyecto = carpetaProyecto;
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
    public File getArchivoActual()
    {
        return archivoActual;
    }
    public void setArchivoActual(File f)
    {
        this.archivoActual = f;
    }
    public File getCarpetaProyecto()
    {
        return carpetaProyecto;
    }
    public void setCarpetaProyecto(File f)
    {
        this.carpetaProyecto = f;
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
    public Object visitPrograma(PigLatinParser.ProgramaContext ctx)
    {
        generador.limpiar();
        if (ctx.importacion() != null && !ctx.importacion().isEmpty())
        {
            GestorImportaciones.procesarImportaciones(ctx.importacion(), this);
            if (hayErroresSemanticos) return null;
        }
        if (ctx.seccionDeclaraciones() != null) visit(ctx.seccionDeclaraciones());
        int cantidadGlobales = tabla.obtenerAmbitoActual().size();
        if (cantidadGlobales > 0)
        {
            generador.agregarComentario("Protegiendo " + cantidadGlobales + " variables globales en el Heap");
            generador.agregarAsignacion("punteroHeap", String.valueOf(cantidadGlobales));
        }
        tabla.resetearOffsetLocal();
        tabla.entrarAmbito();
        visit(ctx.seccionCodigo());
        tabla.salirAmbito();
        return null;
    }
    
    // VARIABLES
    
    @Override
    public Object visitDeclaracionVariable(PigLatinParser.DeclaracionVariableContext ctx)
    {
        return gestorVariablesPig.procesarDeclaracionVariable(ctx);
    }
    @Override
    public Object visitDeclaracionEstructura(PigLatinParser.DeclaracionEstructuraContext ctx)
    {
        return gestorVariablesPig.procesarDeclaracionEstructura(ctx);
    }
    @Override
    public Object visitDeclaracionObjeto(PigLatinParser.DeclaracionObjetoContext ctx)
    {
        return gestorVariablesPig.procesarDeclaracionObjeto(ctx);
    }
    @Override
    public Object visitDeclaracionArray(PigLatinParser.DeclaracionArrayContext ctx)
    {
        return gestorVariablesPig.procesarDeclaracionArray(ctx);
    }
    
    // ASIGNACIONES
    
    @Override
    public Object visitAsigGeneral(PigLatinParser.AsigGeneralContext ctx)
    {
        return gestorVariablesPig.procesarAsigGeneral(ctx);
    }
    @Override
    public Object visitIncrementoGeneral(PigLatinParser.IncrementoGeneralContext ctx)
    {
        return gestorVariablesPig.procesarIncrementoGeneral(ctx);
    }
    
    // FUNCIONES
    
    @Override
    public Object visitImpresion(PigLatinParser.ImpresionContext ctx)
    {
        return gestorFuncionesPig.procesarImpresion(ctx);
    }
    @Override
    public Object visitLlamadaFuncionOMetodo(PigLatinParser.LlamadaFuncionOMetodoContext ctx)
    {
        return gestorFuncionesPig.procesarLlamadaFuncionOMetodo(ctx);
    }
    @Override public Object visitLecturaDescartar(PigLatinParser.LecturaDescartarContext ctx)
    {
        return gestorFuncionesPig.procesarLecturaDescartar(ctx);
    }
    @Override public Object visitLecturaAsignar(PigLatinParser.LecturaAsignarContext ctx)
    {
        return gestorFuncionesPig.procesarLecturaAsignar(ctx);
    }
    
    // CONTROL DE FLUJO
    
    @Override
    public Object visitCondicional(PigLatinParser.CondicionalContext ctx)
    {
        return gestorControlFlujoPig.procesarCondicional(ctx);
    }
    @Override
    public Object visitBucleDum(PigLatinParser.BucleDumContext ctx)
    {
        return gestorControlFlujoPig.procesarBucleDum(ctx);
    }
    @Override
    public Object visitBucleFacere(PigLatinParser.BucleFacereContext ctx)
    {
        return gestorControlFlujoPig.procesarBucleFacere(ctx);
    }
    @Override
    public Object visitBuclePer(PigLatinParser.BuclePerContext ctx)
    {
        return gestorControlFlujoPig.procesarBuclePer(ctx);
    }
    @Override
    public Object visitActualizacion(PigLatinParser.ActualizacionContext ctx)
    {
        return gestorControlFlujoPig.procesarActualizacion(ctx);
    }
    @Override
    public Object visitInterrupcion(PigLatinParser.InterrupcionContext ctx)
    {
        return gestorControlFlujoPig.procesarInterrupcion(ctx);
    }
    @Override
    public Object visitImportacion(PigLatinParser.ImportacionContext ctx)
    {
        // Las importaciones ya fueron procesadas en visitPrograma (Fase 6); se ignoran aquí
        return null;
    }
    
    // EXPRESIONES
    
    @Override
    public Object visitSumaResta(PigLatinParser.SumaRestaContext ctx)
    {
        return gestorExpresionesPig.procesarSumaResta(ctx);
    }
    @Override
    public Object visitMultDiv(PigLatinParser.MultDivContext ctx)
    {
        return gestorExpresionesPig.procesarMultDiv(ctx);
    }
    @Override
    public Object visitComparacion(PigLatinParser.ComparacionContext ctx)
    {
        return gestorExpresionesPig.procesarComparacion(ctx);
    }
    @Override
    public Object visitIgualdad(PigLatinParser.IgualdadContext ctx)
    {
        return gestorExpresionesPig.procesarIgualdad(ctx);
    }
    @Override
    public Object visitAndLogico(PigLatinParser.AndLogicoContext ctx)
    {
        return gestorExpresionesPig.procesarAndLogico(ctx);
    }
    @Override
    public Object visitOrLogico(PigLatinParser.OrLogicoContext ctx)
    {
        return gestorExpresionesPig.procesarOrLogico(ctx);
    }
    @Override
    public Object visitNegacion(PigLatinParser.NegacionContext ctx)
    {
        return gestorExpresionesPig.procesarNegacion(ctx);
    }
    @Override
    public Object visitAccesoVariableOAtributo(PigLatinParser.AccesoVariableOAtributoContext ctx)
    {
        return gestorExpresionesPig.procesarAccesoVariableOAtributo(ctx);
    }
    
    // LITERALES
    
    @Override
    public Object visitNumLiteral(PigLatinParser.NumLiteralContext ctx)
    {
        return new ResultadoC3D(TipoDato.ENTERO, ctx.getText());
    }   
    @Override
    public Object visitDecLiteral(PigLatinParser.DecLiteralContext ctx)
    {
        return new ResultadoC3D(TipoDato.DECIMAL, ctx.getText());
    }
    @Override
    public Object visitTextLiteral(PigLatinParser.TextLiteralContext ctx)
    {
        return GestorCadenas.guardarCadenaEnHeap(ctx.getText(), generador);
    }
    @Override
    public Object visitCharLiteral(PigLatinParser.CharLiteralContext ctx)
    {
        return new ResultadoC3D(TipoDato.CARACTER, ctx.getText());
    }
    @Override
    public Object visitTrueLiteral(PigLatinParser.TrueLiteralContext ctx)
    {
        return new ResultadoC3D(TipoDato.BOOLEANO, "1");
    }
    @Override
    public Object visitFalseLiteral(PigLatinParser.FalseLiteralContext ctx)
    {
        return new ResultadoC3D(TipoDato.BOOLEANO, "0");
    }
    @Override
    public Object visitToFactor(PigLatinParser.ToFactorContext ctx)
    {
        return visit(ctx.factor());
    }
    @Override
    public Object visitParentesis(PigLatinParser.ParentesisContext ctx)
    {
        return visit(ctx.expresion());
    }
}
