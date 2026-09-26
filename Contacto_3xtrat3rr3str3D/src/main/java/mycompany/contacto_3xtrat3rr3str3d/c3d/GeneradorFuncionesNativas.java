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
    
    public static String getNativaLeerNumero()
    {
        return """
               double nativa_leer_numero() {
                   double valor;
                   scanf("%lf", &valor);
                   char c;
                   while ((c = getchar()) != '\\n' && c != EOF);
                   return valor;
               }
               """;
    }

    public static String getNativaLeerCadena()
    {
        return """
               double nativa_leer_cadena() {
                   double punteroInicio = punteroHeap;
                   char c = getchar();
                   while(c == '\\n' || c == '\\r') { c = getchar(); }
                   while (c != '\\n' && c != '\\r' && c != EOF) {
                       heap[(int)punteroHeap] = c;
                       punteroHeap = punteroHeap + 1;
                       c = getchar();
                   }
                   heap[(int)punteroHeap] = -1;
                   punteroHeap = punteroHeap + 1;
                   return punteroInicio;
               }
               """;
    }

    public static String getNativaLeerDescartar()
    {
        return """
               void nativa_leer_descartar() {
                   char c = getchar();
                   while (c != '\\n' && c != '\\r' && c != EOF) {
                       c = getchar();
                   }
               }
               """;
    }
}
