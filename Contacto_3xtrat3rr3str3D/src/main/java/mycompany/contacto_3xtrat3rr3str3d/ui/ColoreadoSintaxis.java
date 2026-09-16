package mycompany.contacto_3xtrat3rr3str3d.ui;

import java.awt.Color;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.SwingUtilities;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

public class ColoreadoSintaxis extends DefaultStyledDocument
{
    private final StyleContext contexto = StyleContext.getDefaultStyleContext();
    private final AttributeSet estiloNormal = contexto.addAttribute(contexto.getEmptySet(), StyleConstants.Foreground, Color.BLACK);
    private final AttributeSet estiloPalabraReservada = contexto.addAttribute(contexto.getEmptySet(), StyleConstants.Foreground, new Color(0, 0, 200)); // Azul
    private final AttributeSet estiloCadena = contexto.addAttribute(contexto.getEmptySet(), StyleConstants.Foreground, new Color(206, 115, 0)); // Naranja
    private final AttributeSet estiloComentario = contexto.addAttribute(contexto.getEmptySet(), StyleConstants.Foreground, new Color(128, 128, 128)); // Gris
    private final AttributeSet estiloNumero = contexto.addAttribute(contexto.getEmptySet(), StyleConstants.Foreground, new Color(9, 134, 88)); // Verde 
    private final AttributeSet estiloSimbolo = contexto.addAttribute(contexto.getEmptySet(), StyleConstants.Foreground, new Color(128, 0, 128)); // Morado
    private final AttributeSet estiloEtiqueta = contexto.addAttribute(contexto.getEmptySet(), StyleConstants.Foreground, new Color(204, 0, 0)); // Rojo
    private final String reservadas = "\\b(public|class|new|if|else|switch|case|default|break|for|while|do|continue|return|println|print|readln|null|true|false|int|double|boolean|char|String|void|"
                                    + "estructuras|funciones|estructura|definir|funcion|si|entonces|sino|contrario|elegir|siempre|romper|para|mientras|hacer|imprimir|leer|retornar|entero|cadena|flotante|caracter|bool|verdadero|falso|"
                                    + "esto|series|aliter|dum|facere|per|perge|interrumpe|non|import|novus|z|y|verum|falsus|numerus|textum|decimalis|littera)\\b";
    private final String etiquetas = "\\b(finis|FINIS)\\b|(VARIABILES>|MAIOR>)";
    private final Pattern patronEtiquetas = Pattern.compile(etiquetas);
    private final Pattern patronReservadas = Pattern.compile(reservadas);
    private final Pattern patronCadenas = Pattern.compile("(\"[^\"]*\")|('[^']*')"); 
    private final Pattern patronNumeros = Pattern.compile("\\b\\d+(\\.\\d+)?\\b");
    private final Pattern patronComentarios = Pattern.compile("(//[^\\n]*)|(/\\*[\\s\\S]*?\\*/)|(##[\\s\\S]*?##)");
    private final Pattern patronSimbolos = Pattern.compile("[\\+\\-\\*\\/\\%\\=\\<\\>\\!\\&\\|\\?\\:\\;\\,\\.\\{\\}\\[\\]\\(\\)]");
    
    @Override
    public void insertString(int offset, String str, AttributeSet a) throws BadLocationException
    {
        super.insertString(offset, str, a);
        colorearTexto();
    }

    @Override
    public void remove(int offs, int len) throws BadLocationException
    {
        super.remove(offs, len);
        colorearTexto();
    }
    private void colorearTexto()
    {
        SwingUtilities.invokeLater(() ->
        {
            try
            {
                String texto = getText(0, getLength());
                setCharacterAttributes(0, getLength(), estiloNormal, false);
                Matcher matcherSimbolos = patronSimbolos.matcher(texto);
                while (matcherSimbolos.find())
                {
                    setCharacterAttributes(matcherSimbolos.start(), matcherSimbolos.end() - matcherSimbolos.start(), estiloSimbolo, false);
                }
                Matcher matcherNumeros = patronNumeros.matcher(texto);
                while (matcherNumeros.find())
                {
                    setCharacterAttributes(matcherNumeros.start(), matcherNumeros.end() - matcherNumeros.start(), estiloNumero, false);
                }
                Matcher matcherEtiquetas = patronEtiquetas.matcher(texto);
                while (matcherEtiquetas.find())
                {
                    setCharacterAttributes(matcherEtiquetas.start(), matcherEtiquetas.end() - matcherEtiquetas.start(), estiloEtiqueta, false);
                }
                Matcher matcherReservadas = patronReservadas.matcher(texto);
                while (matcherReservadas.find())
                {
                    setCharacterAttributes(matcherReservadas.start(), matcherReservadas.end() - matcherReservadas.start(), estiloPalabraReservada, false);
                }
                Matcher matcherCadenas = patronCadenas.matcher(texto);
                while (matcherCadenas.find())
                {
                    setCharacterAttributes(matcherCadenas.start(), matcherCadenas.end() - matcherCadenas.start(), estiloCadena, false);
                }
                Matcher matcherComentarios = patronComentarios.matcher(texto);
                while (matcherComentarios.find())
                {
                    setCharacterAttributes(matcherComentarios.start(), matcherComentarios.end() - matcherComentarios.start(), estiloComentario, false);
                }
            }
            catch (BadLocationException e)
            {
                e.printStackTrace();
            }
        });
    }
}