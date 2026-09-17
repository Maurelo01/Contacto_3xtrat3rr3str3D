package mycompany.contacto_3xtrat3rr3str3d.ui;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import javax.swing.JTextArea;

public class ControladorErrores extends BaseErrorListener
{
    private final JTextArea consola;
    public boolean hayErrores = false;

    public ControladorErrores(JTextArea consola)
    {
        this.consola = consola;
    }

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e)
    {
        hayErrores = true;
        consola.append("❌ Error sintáctico en línea " + line + ", columna " + charPositionInLine + ": " + msg + "\n");
    }
}