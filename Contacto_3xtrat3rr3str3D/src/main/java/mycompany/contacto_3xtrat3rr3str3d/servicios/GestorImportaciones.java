package mycompany.contacto_3xtrat3rr3str3d.servicios;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JTextArea;
import mycompany.contacto_3xtrat3rr3str3d.PigLatinParser;
import mycompany.contacto_3xtrat3rr3str3d.YLexer;
import mycompany.contacto_3xtrat3rr3str3d.YParser;
import mycompany.contacto_3xtrat3rr3str3d.ZetarianoLexer;
import mycompany.contacto_3xtrat3rr3str3d.ZetarianoParser;
import mycompany.contacto_3xtrat3rr3str3d.simbolos.TablaSimbolos;
import mycompany.contacto_3xtrat3rr3str3d.ui.ControladorErrores;
import mycompany.contacto_3xtrat3rr3str3d.visitors.PigLatinCustomVisitor;
import mycompany.contacto_3xtrat3rr3str3d.visitors.YCustomVisitor;
import mycompany.contacto_3xtrat3rr3str3d.visitors.ZetarianoCustomVisitor;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

public class GestorImportaciones
{
    public static void procesarImportaciones(List<PigLatinParser.ImportacionContext> importaciones, PigLatinCustomVisitor visitorPig)
    {
        if (importaciones == null || importaciones.isEmpty()) return;
        TablaSimbolos tabla = visitorPig.getTabla();
        JTextArea consola = visitorPig.getConsola();
        File archivoActual = visitorPig.getArchivoActual();
        File carpetaProyecto = visitorPig.getCarpetaProyecto();
        Set<String> yaImportados = new HashSet<>();
        for (PigLatinParser.ImportacionContext imp : importaciones)
        {
            String rutaRelativa = reconstruirRuta(imp);
            if (rutaRelativa == null) continue;
            if (!yaImportados.add(rutaRelativa))
            {
                consola.append("Aviso: importación duplicada " + rutaRelativa + " ignorada.\n");
                continue;
            }
            File archivoImportado = resolverArchivo(rutaRelativa, archivoActual, carpetaProyecto);
            if (archivoImportado == null || !archivoImportado.exists())
            {
                consola.append("Error Semántico: no se encontró el archivo importado " + rutaRelativa + ". Verifique la ruta relativa al .pig o al proyecto.\n");
                visitorPig.setHayErroresSemanticos(true);
                continue;
            }
            consola.append("Importando: " + archivoImportado.getPath() + "\n");
            try
            {
                if (rutaRelativa.endsWith(".y")) importarArchivoY(archivoImportado, tabla, consola, visitorPig);
                else if (rutaRelativa.endsWith(".z")) importarArchivoZ(archivoImportado, tabla, consola, visitorPig);
                else
                {
                    consola.append("Error Semántico: extensión no soportada en " + rutaRelativa + ". Use .y o .z\n");
                    visitorPig.setHayErroresSemanticos(true);
                }
            }
            catch (Exception e)
            {
                consola.append("Error crítico al importar " + rutaRelativa + ": " + e.getMessage() + "\n");
                visitorPig.setHayErroresSemanticos(true);
            }
        }
        if (!visitorPig.isHayErroresSemanticos() && !yaImportados.isEmpty())
        {
            consola.append("Tablas de símbolos combinadas: " + yaImportados.size() + " archivo(s) importado(s).\n");
        }
    }

    public static String reconstruirRuta(PigLatinParser.ImportacionContext ctx)
    {
        List<TerminalNode> ids = ctx.ID();
        if (ids == null || ids.isEmpty()) return null;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ids.size(); i++)
        {
            if (i > 0) sb.append(File.separator);
            sb.append(ids.get(i).getText());
        }
        String ext = (ctx.Y() != null) ? ".y" : ".z";
        sb.append(ext);
        return sb.toString();
    }

    public static File resolverArchivo(String rutaRelativa, File archivoActual, File carpetaProyecto)
    {
        File candidato = new File(rutaRelativa);
        if (candidato.isAbsolute() && candidato.exists()) return candidato;
        if (archivoActual != null && archivoActual.getParentFile() != null)
        {
            File juntoAlPig = new File(archivoActual.getParentFile(), rutaRelativa);
            if (juntoAlPig.exists()) return juntoAlPig;
        }
        if (carpetaProyecto != null)
        {
            File enProyecto = new File(carpetaProyecto, rutaRelativa);
            if (enProyecto.exists()) return enProyecto;
        }
        File relativoCwd = new File(System.getProperty("user.dir"), rutaRelativa);
        if (relativoCwd.exists()) return relativoCwd;
        return candidato;
    }

    private static void importarArchivoY(File archivo, TablaSimbolos tabla, JTextArea consola, PigLatinCustomVisitor visitorPig) throws Exception
    {
        String codigo = GestorArchivos.leerContenido(archivo);
        CharStream input = CharStreams.fromString(codigo);
        ControladorErrores controlador = new ControladorErrores(consola);
        YLexer lexer = new YLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        YParser parser = new YParser(tokens);
        lexer.removeErrorListeners();
        parser.removeErrorListeners();
        lexer.addErrorListener(controlador);
        parser.addErrorListener(controlador);
        ParseTree tree = parser.programa();
        if (controlador.hayErrores)
        {
            consola.append("Error: el archivo importado " + archivo.getName() + " tiene errores léxicos/sintácticos.\n");
            visitorPig.setHayErroresSemanticos(true);
            return;
        }
        YCustomVisitor visitorY = new YCustomVisitor(tabla, consola, false);
        visitorY.visit(tree);
        if (visitorY.isHayErroresSemanticos())
        {
            consola.append("Error: el archivo importado " + archivo.getName() + " tiene errores semánticos.\n");
            visitorPig.setHayErroresSemanticos(true);
        }
        else consola.append("  OK: estructuras/funciones de " + archivo.getName() + " disponibles.\n");
    }

    private static void importarArchivoZ(File archivo, TablaSimbolos tabla, JTextArea consola, PigLatinCustomVisitor visitorPig) throws Exception
    {
        String codigo = GestorArchivos.leerContenido(archivo);
        String nombreBase = archivo.getName().replaceFirst("\\.z$", "");
        CharStream input = CharStreams.fromString(codigo);
        ControladorErrores controlador = new ControladorErrores(consola);
        ZetarianoLexer lexer = new ZetarianoLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        ZetarianoParser parser = new ZetarianoParser(tokens);
        lexer.removeErrorListeners();
        parser.removeErrorListeners();
        lexer.addErrorListener(controlador);
        parser.addErrorListener(controlador);
        ParseTree tree = parser.programa();
        if (controlador.hayErrores)
        {
            consola.append("Error: el archivo importado " + archivo.getName() + " tiene errores léxicos/sintácticos.\n");
            visitorPig.setHayErroresSemanticos(true);
            return;
        }
        if (tree instanceof ZetarianoParser.ProgramaContext prog && prog.clase() != null)
        {
            String nombreClase = prog.clase().ID().getText();
            if (!nombreClase.equals(nombreBase))
            {
                consola.append("Advertencia: el archivo " + archivo.getName() + " debería llamarse como la clase " + nombreClase + ".z.\n");
            }
        }
        ZetarianoCustomVisitor visitorZ = new ZetarianoCustomVisitor(tabla, consola, false);
        visitorZ.visit(tree);
        if (visitorZ.isHayErroresSemanticos())
        {
            consola.append("Error: el archivo importado " + archivo.getName() + " tiene errores semánticos.\n");
            visitorPig.setHayErroresSemanticos(true);
        }
        else consola.append("  OK: clase/métodos de " + archivo.getName() + " disponibles.\n");
    }
}
