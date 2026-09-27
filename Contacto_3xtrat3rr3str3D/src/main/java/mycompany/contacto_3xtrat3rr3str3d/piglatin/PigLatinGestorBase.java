package mycompany.contacto_3xtrat3rr3str3d.piglatin;

import javax.swing.JTextArea;
import mycompany.contacto_3xtrat3rr3str3d.PigLatinParser;
import mycompany.contacto_3xtrat3rr3str3d.c3d.GeneradorC3D;
import mycompany.contacto_3xtrat3rr3str3d.simbolos.Simbolo;
import mycompany.contacto_3xtrat3rr3str3d.simbolos.SimboloArreglo;
import mycompany.contacto_3xtrat3rr3str3d.simbolos.SimboloClase;
import mycompany.contacto_3xtrat3rr3str3d.simbolos.SimboloVariable;
import mycompany.contacto_3xtrat3rr3str3d.simbolos.TablaSimbolos;
import mycompany.contacto_3xtrat3rr3str3d.simbolos.TipoDato;
import mycompany.contacto_3xtrat3rr3str3d.utils.ResultadoC3D;
import mycompany.contacto_3xtrat3rr3str3d.visitors.PigLatinCustomVisitor;

public abstract class PigLatinGestorBase
{
    protected PigLatinCustomVisitor visitor;
    protected TablaSimbolos tabla;
    protected GeneradorC3D generador;
    protected JTextArea consola;

    public PigLatinGestorBase(PigLatinCustomVisitor visitor)
    {
        this.visitor = visitor;
        this.tabla = visitor.getTabla();
        this.generador = visitor.getGenerador();
        this.consola = visitor.getConsola();
    }

    protected void reportarError(int linea, int columna, String mensaje)
    {
        consola.append("Error Semántico en línea " + linea + ": " + mensaje + "\n");
        visitor.setHayErroresSemanticos(true);
    }
    
    protected ResultadoC3D calcularDireccionAcceso(PigLatinParser.AccesoContext ctx)
    {
        String idBase = ctx.ID(0).getText();
        Simbolo simBase = tabla.buscar(idBase);
        if (simBase == null)
        {
            reportarError(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(), "Variable " + idBase + " no declarada.");
            return new ResultadoC3D(TipoDato.ERROR, "");
        }
        String tempPuntero = generador.generarTemporal();
        if (simBase.isEnHeap()) generador.agregarGetHeap(tempPuntero, String.valueOf(simBase.getOffset()));
        else
        {
            String tempStack = generador.generarTemporal();
            generador.agregarAsignacion(tempStack, "punteroStack", "+", String.valueOf(simBase.getOffset()));
            generador.agregarGetStack(tempPuntero, tempStack);
        }
        TipoDato tipoActual = simBase.getTipo();
        String claseActual = (simBase instanceof SimboloVariable) ? ((SimboloVariable) simBase).getReferenciaClase() : null;
        SimboloArreglo simArreglo = (simBase instanceof SimboloArreglo) ? ((SimboloArreglo) simBase) : null;
        int exprIndice = 0;
        int idIndice = 1;
        int dimActual = 0;
        for (int i = 1; i < ctx.getChildCount(); i++)
        {
            String token = ctx.getChild(i).getText();
            if (token.equals("["))
            {
                ResultadoC3D resIndice = (ResultadoC3D) visitor.visit(ctx.expresion(exprIndice++));
                String tempDesplazado = generador.generarTemporal();
                if (simArreglo != null && dimActual < simArreglo.getDimensiones() - 1)
                {
                    int productoDimensiones = 1;
                    for (int k = dimActual + 1; k < simArreglo.getDimensiones(); k++)
                    {
                        productoDimensiones *= simArreglo.getTamañosDimensiones().get(k);
                    }
                    String tempMult = generador.generarTemporal();
                    generador.agregarAsignacion(tempMult, resIndice.getValorC3D(), "*", String.valueOf(productoDimensiones));
                    generador.agregarAsignacion(tempDesplazado, tempPuntero, "+", tempMult);
                }
                else generador.agregarAsignacion(tempDesplazado, tempPuntero, "+", resIndice.getValorC3D());
                tempPuntero = tempDesplazado;
                dimActual++;
                i += 2; // Se salta expresion y corchete de cierre
            }
            else if (token.equals("."))
            {
                String idAtributo = ctx.ID(idIndice++).getText();
                Simbolo atributo = null;
                if (claseActual != null)
                {
                    SimboloClase plantilla = (SimboloClase) tabla.buscar(claseActual);
                    if (plantilla != null) atributo = plantilla.getEntornoInterno().buscar(idAtributo);
                }
                if (atributo == null)
                {
                    reportarError(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(),"Atributo " + idAtributo + " no encontrado.");
                    return new ResultadoC3D(TipoDato.ERROR, "");
                }
                String tempNuevoOffset = generador.generarTemporal();
                generador.agregarAsignacion(tempNuevoOffset, tempPuntero, "+", String.valueOf(atributo.getOffset()));
                boolean esUltimoAcceso = (i + 2 >= ctx.getChildCount());
                if (!esUltimoAcceso && atributo.getTipo() == TipoDato.OBJETO)
                {
                    String tempSubPuntero = generador.generarTemporal();
                    generador.agregarGetHeap(tempSubPuntero, tempNuevoOffset);
                    tempPuntero = tempSubPuntero;
                }
                else tempPuntero = tempNuevoOffset;
                tipoActual = atributo.getTipo();
                claseActual = (atributo instanceof SimboloVariable) ? ((SimboloVariable) atributo).getReferenciaClase() : null;
                simArreglo = (atributo instanceof SimboloArreglo) ? ((SimboloArreglo) atributo) : null;
                i += 1;
            }
        }
        return new ResultadoC3D(tipoActual, tempPuntero);
    }
}
