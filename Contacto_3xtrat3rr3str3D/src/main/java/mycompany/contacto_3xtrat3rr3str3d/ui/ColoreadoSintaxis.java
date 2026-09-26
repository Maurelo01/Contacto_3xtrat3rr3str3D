package mycompany.contacto_3xtrat3rr3str3d.ui;

import java.awt.Color;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.SwingUtilities;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import mycompany.contacto_3xtrat3rr3str3d.PigLatinLexer;
import mycompany.contacto_3xtrat3rr3str3d.YLexer;
import mycompany.contacto_3xtrat3rr3str3d.ZetarianoLexer;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Token;

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
    
    private String tipoLenguaje;
    
    private final Set<String> palabrasReservadas = new HashSet<>(
    Arrays.asList
    (
            "public", "class", "new", "if", "else", "switch", "case", "default", "break", "for", "while", "do", "continue", "return", "println", "print", "readln", "null", "true", "false", "int", "double", "boolean", "char", "String", "void",
            "estructuras", "funciones", "estructura", "definir", "funcion", "si", "entonces", "sino", "contrario", "elegir", "siempre", "romper", "para", "mientras", "hacer", "imprimir", "leer", "retornar", "entero", "cadena", "flotante", "caracter", "bool", "verdadero", "falso",
            "esto", "series", "aliter", "dum", "facere", "per", "perge", "interrumpe", "non", "import", "novus", "z", "y", "verum", "falsus", "numerus", "textum", "decimalis", "littera"
    ));
    private final Set<String> etiquetasEspeciales = new HashSet<>(Arrays.asList("FINIS", "finis", "VARIABILES>", "MAIOR>"));
    
    public void setTipoLenguaje(String tipoLenguaje)
    {
        this.tipoLenguaje = tipoLenguaje.toLowerCase();
        colorearTexto();
    }
    
    public ColoreadoSintaxis(String tipoLenguaje)
    {
        this.tipoLenguaje = tipoLenguaje.toLowerCase();
    }
    
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
                Lexer lexer;
                switch (tipoLenguaje)
                {
                    case "y":
                        lexer = new YLexer(CharStreams.fromString(texto));
                        break;
                    case "pig":
                        lexer = new PigLatinLexer(CharStreams.fromString(texto));
                        break;
                    case "z":
                    default:
                        lexer = new ZetarianoLexer(CharStreams.fromString(texto));
                        break;
                }
                List<? extends Token> tokens = lexer.getAllTokens();
                setCharacterAttributes(0, getLength(), estiloNormal, false);
                for (Token token : tokens)
                {
                    int inicio = token.getStartIndex();
                    int longitud = token.getStopIndex() - inicio + 1;
                    String textoToken = token.getText();
                    String nombreToken = lexer.getVocabulary().getSymbolicName(token.getType());
                    if (nombreToken == null) continue;
                    if (nombreToken.contains("COMENTARIO")) setCharacterAttributes(inicio, longitud, estiloComentario, false);
                    else if (nombreToken.equals("TEXTO") || nombreToken.equals("CARACTER")) setCharacterAttributes(inicio, longitud, estiloCadena, false);
                    else if (nombreToken.equals("NUMERO") || nombreToken.equals("DECIMALES")) setCharacterAttributes(inicio, longitud, estiloNumero, false);
                    else if (etiquetasEspeciales.contains(textoToken)) setCharacterAttributes(inicio, longitud, estiloEtiqueta, false);
                    else if (palabrasReservadas.contains(textoToken)) setCharacterAttributes(inicio, longitud, estiloPalabraReservada, false);
                    else if (!nombreToken.equals("ID") && !nombreToken.equals("WS") && !nombreToken.equals("FIN_LINEA") && !nombreToken.equals("NUEVA_LINEA")) setCharacterAttributes(inicio, longitud, estiloSimbolo, false);
                }
            }
            catch (BadLocationException e)
            {
                e.printStackTrace();
            }
        });
    }
}