# Contacto 3xtrat3rr3str3D

Compilador con interfaz gráfica desarrollado en Java y ANTLR4 que valida
léxica, sintáctica y semánticamente los lenguajes de alto nivel **Y?** (`.y`),
**Zetariano** (`.z`) y **Pig Latin** (`.pig`), y los traduce a **Código de Tres
Direcciones (C3D)** compilable en C, usando stack y heap.

Proyecto fase 1 de Compiladores 2.
Mauricio Joel Gómez Barrios - 202031478

## Requisitos

* **Java:** JDK 21 o superior.
* **Maven:** para la compilación y gestión de dependencias

## Cómo ejecutar

Ubicarse en la raíz del proyecto (`Contacto_3xtrat3rr3str3D/`, donde está el
`pom.xml`):

```bash
mvn clean package
o abrir el proyecto en NetBeans y usar Run (la clase principal es
mycompany.contacto_3xtrat3rr3str3d.Contacto_3xtrat3rr3str3D).
Uso rápido
1. Archivo > Abrir Proyecto y elegir la carpeta de trabajo.
2. Crear o abrir un archivo .pig, .y o .z (doble clic en el árbol).
3. Ejecutar > Analizar Código y revisar las pestañas:
- Consola de Análisis (errores léxicos, sintácticos y semánticos),
- Código 3 Direcciones,
- Árbol AST.
4. Archivo > Exportar C3D (.c) y compilar el resultado:
gcc salida.c -o salida -lm
5. Luego ejecutar con ./salida
Gestión del árbol de trabajo
Abrir proyecto, nuevo archivo, nueva carpeta, guardar, guardar como/descargar,
eliminar seleccionado, refrescar árbol y exportar C3D, todo desde el menú
Archivo. El coloreado de sintaxis es en tiempo real.
