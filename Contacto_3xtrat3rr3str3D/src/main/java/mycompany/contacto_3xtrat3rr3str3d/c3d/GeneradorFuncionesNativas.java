package mycompany.contacto_3xtrat3rr3str3d.c3d;

public class GeneradorFuncionesNativas
{
    public static String getNativaImprimirString()
    {
        return """
               void nativa_imprimir_string(double puntero) {
                   double caracter = heap[(int)puntero];
                   L_Inicio:
                   if (caracter == -1) goto L_Fin;
                   printf("%c", (int)caracter);
                   puntero = puntero + 1;
                   caracter = heap[(int)puntero];
                   goto L_Inicio;
                   L_Fin:
                   return;
               }
               """;
    }
}
