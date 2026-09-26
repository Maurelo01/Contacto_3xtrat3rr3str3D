package mycompany.contacto_3xtrat3rr3str3d.piglatin;

import javax.swing.JTextArea;
import mycompany.contacto_3xtrat3rr3str3d.c3d.GeneradorC3D;
import mycompany.contacto_3xtrat3rr3str3d.simbolos.TablaSimbolos;
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

    protected void reportarError(int linea, String mensaje) {
        consola.append("Error Semántico en línea " + linea + ": " + mensaje + "\n");
        visitor.setHayErroresSemanticos(true);
    }
}
