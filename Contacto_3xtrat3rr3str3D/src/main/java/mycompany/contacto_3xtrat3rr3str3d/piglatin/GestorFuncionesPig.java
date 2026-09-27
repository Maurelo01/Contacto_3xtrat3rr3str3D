package mycompany.contacto_3xtrat3rr3str3d.piglatin;

import java.util.List;
import mycompany.contacto_3xtrat3rr3str3d.PigLatinParser;
import mycompany.contacto_3xtrat3rr3str3d.c3d.GeneradorFuncionesNativas;
import mycompany.contacto_3xtrat3rr3str3d.simbolos.Simbolo;
import mycompany.contacto_3xtrat3rr3str3d.simbolos.SimboloFuncion;
import mycompany.contacto_3xtrat3rr3str3d.simbolos.SimboloVariable;
import mycompany.contacto_3xtrat3rr3str3d.simbolos.TipoDato;
import mycompany.contacto_3xtrat3rr3str3d.utils.ResultadoC3D;
import mycompany.contacto_3xtrat3rr3str3d.visitors.PigLatinCustomVisitor;
import org.antlr.v4.runtime.tree.TerminalNode;

public class GestorFuncionesPig extends PigLatinGestorBase
{
    public GestorFuncionesPig(PigLatinCustomVisitor visitor)
    {
        super(visitor);
    }
    
    public Object procesarImpresion(PigLatinParser.ImpresionContext ctx)
    {
        for (PigLatinParser.ExpresionContext exprCtx : ctx.expresion())
        {
            ResultadoC3D resExpr = (ResultadoC3D) visitor.visit(exprCtx);
            if (resExpr != null)
            {
                if (resExpr.getTipo() == TipoDato.ENTERO || resExpr.getTipo() == TipoDato.BOOLEANO) generador.agregarPrint("d", "(int)" + resExpr.getValorC3D());
                else if (resExpr.getTipo() == TipoDato.DECIMAL) generador.agregarPrint("f", resExpr.getValorC3D());
                else if (resExpr.getTipo() == TipoDato.CADENA)
                {
                    generador.agregarFuncionNativa(GeneradorFuncionesNativas.getNativaImprimirString());
                    generador.agregarLlamadaNativa("nativa_imprimir_string", resExpr.getValorC3D());
                }
                else reportarError(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(), "Tipo " + resExpr.getTipo() + " no soportado para impresión.");
            }
        }
        generador.agregarPrint("c", "10");
        return null;
    }
    
    public Object procesarLlamadaFuncionOMetodo(PigLatinParser.LlamadaFuncionOMetodoContext ctx)
    {
        List<TerminalNode> ids = ctx.acceso().ID();
        if (ids.size() == 1)
        {
            String idFuncion = ids.get(0).getText();
            int numArgs = (ctx.argumentos() != null) ? ctx.argumentos().expresion().size() : 0;
            SimboloFuncion funcion = tabla.buscarFuncion(idFuncion, numArgs);
            if (funcion == null)
            {
                Simbolo simLegacy = tabla.buscar(idFuncion);
                if (simLegacy instanceof SimboloFuncion fLegacy && fLegacy.getParametros().size() == numArgs) funcion = fLegacy;
            }
            if (funcion == null)
            {
                if (tabla.existeFuncionBase(idFuncion)) reportarError(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(), "La función externa " + idFuncion + " no tiene sobrecarga con " + numArgs + " argumento(s).");
                else reportarError(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(), "La función externa " + idFuncion + " no existe en el entorno.");
                return new ResultadoC3D(TipoDato.ERROR, "");
            }
            int tamañoEntornoActual = tabla.obtenerAmbitoActual().size() + 1;
            if (ctx.argumentos() != null)
            {
                for (int i = 0; i < ctx.argumentos().expresion().size(); i++)
                {
                    ResultadoC3D resArg = (ResultadoC3D) visitor.visit(ctx.argumentos().expresion(i));
                    String tempPos = generador.generarTemporal();
                    int offsetDestino = i + 1;
                    generador.agregarAsignacion(tempPos, "punteroStack", "+", String.valueOf(tamañoEntornoActual + offsetDestino));
                    generador.agregarSetStack(tempPos, resArg.getValorC3D());
                }
            }
            generador.agregarComentario("Llamando a funcion externa: " + idFuncion);
            generador.agregarAsignacion("punteroStack", "punteroStack", "+", String.valueOf(tamañoEntornoActual));
            generador.agregarLlamadaNativa("metodo_" + funcion.getEtiquetaC3D(), "");
            String tempReturn = generador.generarTemporal();
            generador.agregarGetStack(tempReturn, "punteroStack");
            generador.agregarAsignacion("punteroStack", "punteroStack", "-", String.valueOf(tamañoEntornoActual));
            return new ResultadoC3D(funcion.getTipo(), tempReturn);
        }
        else
        {
            reportarError(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(), "Aún no se soportan métodos de objetos.");
            return new ResultadoC3D(TipoDato.ERROR, "");
        }
    }
    
    public Object procesarLecturaDescartar(PigLatinParser.LecturaDescartarContext ctx)
    {
        generador.agregarComentario("Inicio lectura descartada de consola");
        generador.agregarFuncionNativa(GeneradorFuncionesNativas.getNativaLeerDescartar());
        generador.agregarCodigoBruto("    nativa_leer_descartar();");
        return null;
    }

    public Object procesarLecturaAsignar(PigLatinParser.LecturaAsignarContext ctx)
    {
        String idVariable = ctx.ID().getText();
        Simbolo sim = tabla.buscar(idVariable);
        int linea = ctx.getStart().getLine();
        int columna = ctx.getStart().getCharPositionInLine();
        if (sim == null)
        {
            reportarError(linea, columna, "La variable " + idVariable + " no ha sido declarada para lectura.");
            return null;
        }
        generador.agregarComentario("Lectura desde consola asignada a: " + idVariable);
        String temporalValor = generador.generarTemporal();
        if (sim.getTipo() == TipoDato.ENTERO || sim.getTipo() == TipoDato.DECIMAL || sim.getTipo() == TipoDato.BOOLEANO)
        {
            generador.agregarFuncionNativa(GeneradorFuncionesNativas.getNativaLeerNumero());
            generador.agregarAsignacion(temporalValor, "nativa_leer_numero()");
        }
        else if (sim.getTipo() == TipoDato.CADENA || sim.getTipo() == TipoDato.CARACTER)
        {
            generador.agregarFuncionNativa(GeneradorFuncionesNativas.getNativaLeerCadena());
            generador.agregarAsignacion(temporalValor, "nativa_leer_cadena()");
        }
        else
        {
            reportarError(linea, columna, "No es posible realizar lectura por consola para el tipo de dato: " + sim.getTipo());
            return null;
        }
        if (sim instanceof SimboloVariable simboloVariable) simboloVariable.setInicializado(true);
        if (sim.isEnHeap()) generador.agregarSetHeap(String.valueOf(sim.getOffset()), temporalValor);
        else
        {
            String tempIndice = generador.generarTemporal();
            generador.agregarAsignacion(tempIndice, "punteroStack", "+", String.valueOf(sim.getOffset()));
            generador.agregarSetStack(tempIndice, temporalValor);
        }
        return null;
    }
}
