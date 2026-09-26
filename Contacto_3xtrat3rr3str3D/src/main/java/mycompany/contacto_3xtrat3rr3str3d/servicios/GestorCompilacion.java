package mycompany.contacto_3xtrat3rr3str3d.servicios;

import javax.swing.JTextArea;
import mycompany.contacto_3xtrat3rr3str3d.PigLatinLexer;
import mycompany.contacto_3xtrat3rr3str3d.PigLatinParser;
import mycompany.contacto_3xtrat3rr3str3d.YLexer;
import mycompany.contacto_3xtrat3rr3str3d.YParser;
import mycompany.contacto_3xtrat3rr3str3d.ZetarianoLexer;
import mycompany.contacto_3xtrat3rr3str3d.ZetarianoParser;
import mycompany.contacto_3xtrat3rr3str3d.ast.ConstructorPigAST;
import mycompany.contacto_3xtrat3rr3str3d.ast.ConstructorYAST;
import mycompany.contacto_3xtrat3rr3str3d.ast.ConstructorZetarianoAST;
import mycompany.contacto_3xtrat3rr3str3d.c3d.GeneradorC3D;
import mycompany.contacto_3xtrat3rr3str3d.simbolos.TablaSimbolos;
import mycompany.contacto_3xtrat3rr3str3d.ui.ControladorErrores;
import mycompany.contacto_3xtrat3rr3str3d.visitors.*;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

public class GestorCompilacion
{
    public static void analizarCodigo(String nombreArchivo, String codigoTexto, TablaSimbolos tablaMemoria, JTextArea consolaSalida, JTextArea consolaC3D, JTextArea consolaAST)
    {
        consolaSalida.setText(" INICIANDO ANÁLISIS \n");
        consolaSalida.append("Archivo: " + nombreArchivo + "\n\n");
        consolaC3D.setText("");
        consolaAST.setText("");
        CharStream input = CharStreams.fromString(codigoTexto);
        ControladorErrores controlador = new ControladorErrores(consolaSalida);
        tablaMemoria.limpiar();
        GeneradorC3D.getInstancia().limpiar();
        try
        {
            if (nombreArchivo.endsWith(".z")) ejecutarZetariano(input, controlador, tablaMemoria, consolaSalida, consolaC3D, consolaAST);
            else if (nombreArchivo.endsWith(".y")) ejecutarY(input, controlador, tablaMemoria, consolaSalida, consolaC3D, consolaAST);
            else if (nombreArchivo.endsWith(".pig")) ejecutarPigLatin(input, controlador, tablaMemoria, consolaSalida, consolaC3D, consolaAST);
            else consolaSalida.append("No se reconoce la extensión para el análisis, debe ser .pig/.y/.z.\n");
        }
        catch (Exception e)
        {
            consolaSalida.append("Error crítico durante el análisis: " + e.getMessage() + "\n");
        }
    }
    
    private static void ejecutarZetariano(CharStream input, ControladorErrores controlador, TablaSimbolos tabla, JTextArea consola, JTextArea consolaC3D, JTextArea consolaAST)
    {
        ZetarianoLexer lexer = new ZetarianoLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        ZetarianoParser parser = new ZetarianoParser(tokens);
        configurarListeners(lexer, parser, controlador);
        ParseTree tree = parser.programa();
        if (!controlador.hayErrores)
        {
            consola.append("Análisis Sintáctico Zetariano completado con éxito.\n");
            ConstructorZetarianoAST astBuilder = new ConstructorZetarianoAST();
            consolaAST.setText(astBuilder.visit(tree).imprimirArbol());
            ZetarianoCustomVisitor visitor = new ZetarianoCustomVisitor(tabla, consola);
            visitor.visit(tree);
            validarSemanticaYC3D(visitor.isHayErroresSemanticos(), consola, consolaC3D);
        }
        else consola.append("Se encontraron errores en el código Zetariano. No se puede generar el AST.\n");
    }
    
    private static void ejecutarY(CharStream input, ControladorErrores controlador, TablaSimbolos tabla, JTextArea consola, JTextArea consolaC3D, JTextArea consolaAST)
    {
        YLexer lexer = new YLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        YParser parser = new YParser(tokens);
        configurarListeners(lexer, parser, controlador);
        ParseTree tree = parser.programa();
        if (!controlador.hayErrores)
        {
            consola.append("Análisis Sintáctico Y? completado con éxito.\n");
            ConstructorYAST astBuilder = new ConstructorYAST();
            consolaAST.setText(astBuilder.visit(tree).imprimirArbol());
            YCustomVisitor visitor = new YCustomVisitor(tabla, consola);
            visitor.visit(tree);
            validarSemanticaYC3D(visitor.isHayErroresSemanticos(), consola, consolaC3D);
        }
        else consola.append("Se encontraron errores en el código Y?. No se puede generar el AST.\n");
    }

    private static void ejecutarPigLatin(CharStream input, ControladorErrores controlador, TablaSimbolos tabla, JTextArea consola, JTextArea consolaC3D, JTextArea consolaAST)
    {
        PigLatinLexer lexer = new PigLatinLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        PigLatinParser parser = new PigLatinParser(tokens);
        configurarListeners(lexer, parser, controlador);
        ParseTree tree = parser.programa();
        if (!controlador.hayErrores)
        {
            consola.append("Análisis Sintáctico Pig Latin completado con éxito.\n");
            ConstructorPigAST astBuilder = new ConstructorPigAST();
            consolaAST.setText(astBuilder.visit(tree).imprimirArbol());
            PigLatinCustomVisitor visitor = new PigLatinCustomVisitor(tabla, consola);
            visitor.visit(tree);
            validarSemanticaYC3D(visitor.isHayErroresSemanticos(), consola, consolaC3D);
        }
        else consola.append("Se encontraron errores en el código Pig Latin. No se puede generar el AST.\n");
    }
    
    private static void configurarListeners(org.antlr.v4.runtime.Lexer lexer, org.antlr.v4.runtime.Parser parser, ControladorErrores controlador)
    {
        lexer.removeErrorListeners();
        parser.removeErrorListeners();
        lexer.addErrorListener(controlador);
        parser.addErrorListener(controlador);
    }

    private static void validarSemanticaYC3D(boolean erroresSemanticos, JTextArea consola, JTextArea consolaC3D)
    {
        if (!erroresSemanticos)
        {
            consola.append("Análisis Semántico completado con éxito.\n\nCódigo compilado y listo en las pestañas C3D y AST\n");
            consolaC3D.setText(GeneradorC3D.getInstancia().obtenerCodigoCompilable());
            consolaC3D.setCaretPosition(0);
        }
        else consola.append("Compilación detenida por errores semánticos.\n");
    }
    
    private static String formatearAST(String astCrudo)
    {
        StringBuilder sb = new StringBuilder();
        int indentacion = 0;
        for (char c : astCrudo.toCharArray())
        {
            if (c == '(')
            {
                sb.append('\n');
                for (int i = 0; i < indentacion; i++) sb.append("  |  ");
                sb.append(c);
                indentacion++;
            }
            else if (c == ')')
            {
                sb.append(c);
                indentacion--;
            }
            else
            {
                sb.append(c);
            }
        }
        return sb.toString().trim();
    }
}
