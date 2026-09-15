grammar PigLatin;

@header 
{
    package mycompany.contacto_3xtrat3rr3str3d;
}
// PARSER
    programa: importacion* seccionDeclaraciones? seccionCodigo FINIS_MAYUS PUNTOYCOMA EOF;

    // Secciones
        importacion: IMPORT ID (PUNTO ID)* PUNTO (Z | Y) PUNTOYCOMA?;
        seccionDeclaraciones: VARIABILES_GLOBAL declaracion*;
        seccionCodigo: MAIOR instruccion*;

    // Declaraciones
        declaracion: ESTO ID DOSPUNTOS tipo (valorInicial)? PUNTOYCOMA #DeclaracionVariable
                | ESTO ID DOSPUNTOS (VERUM | FALSUS) PUNTOYCOMA  #DeclaracionBoolSinTipo
                | ESTO ID DOSPUNTOS tipo agrupacionValores PUNTOYCOMA #DeclaracionEstructura
                | SERIES ID CORCH_IZQ expresion CORCH_DER DOSPUNTOS tipo (agrupacionValores)? PUNTOYCOMA  #DeclaracionArray
                | SERIES ID CORCH_IZQ expresion CORCH_DER DOSPUNTOS agrupacionValores PUNTOYCOMA #DeclaracionArrayBooleano
                | ESTO ID NOVUS ID PAREN_IZQ argumentos? PAREN_DER PUNTOYCOMA #DeclaracionObjeto;
                
        agrupacionValores: LLAVE_IZQ argumentos? LLAVE_DER;        
        valorInicial: expresion;

    // Instrucciones
        instruccion: asignacion PUNTOYCOMA
                | incremento PUNTOYCOMA 
                | expresion PUNTOYCOMA
                | condicional
                | bucle
                | interrupcion
                | impresion
                | lectura;
        
        acceso: ID (CORCH_IZQ expresion CORCH_DER | PUNTO ID)*;        
        asignacion: acceso IGUAL expresion #AsigGeneral;
        incremento: acceso (MAS_ABREVIADO | MENOS_ABREVIADO) #IncrementoGeneral;

        impresion: IMPRIMIR expresion (IMPRIMIR expresion)* PUNTOYCOMA;
        lectura: LECTURA #LecturaDescartar
                | ID LECTURA #LecturaAsignar;

    // Expresiones
        expresion: expresion (POR | DIVISION | MODULO) expresion #MultDiv
            | expresion (MAS | MENOS) expresion #SumaResta
            | expresion (MAYOR | MAYOR_IGUAL | MENOR | MENOR_IGUAL) expresion #Comparacion
            | expresion (IGUALIGUAL | DIFERENTEDE) expresion #Igualdad
            | expresion AND expresion #AndLogico
            | expresion OR expresion #OrLogico
            | factor #ToFactor;

        factor: MENOS factor #NegacionUnaria
            | NUMERO #NumLiteral
            | DECIMALES #DecLiteral
            | TEXTO #TextLiteral
            | CARACTER #CharLiteral
            | VERUM #TrueLiteral
            | FALSUS #FalseLiteral
            | NON factor #Negacion
            | PAREN_IZQ expresion PAREN_DER #Parentesis
            | NOVUS ID PAREN_IZQ argumentos? PAREN_DER #InstanciaObjeto
            | agrupacionValores #FactorEstructuraAnonima
            | acceso #AccesoVariableOAtributo
            | acceso PAREN_IZQ argumentos? PAREN_DER #LlamadaFuncionOMetodo;

        argumentos: expresion (COMA expresion)*;

    // Tipos
        tipo: tipoSimple | SERIES tipoSimple;
        tipoSimple: NUMERUS | TEXTUM | DECIMALIS | LITTERA | BOOL | ID;

    // Condicionales
        condicional: SI PAREN_IZQ expresion PAREN_DER bloque (ALITER PAREN_IZQ expresion PAREN_DER bloque)* (ALITER bloque)? FINIS PUNTOYCOMA;

        bloque: LLAVE_IZQ instruccion* LLAVE_DER;

    // Bucles
        bucle: bucleDum | bucleFacere | buclePer;
        bucleDum: DUM PAREN_IZQ expresion PAREN_DER bloque FINIS PUNTOYCOMA;
        bucleFacere: FACERE bloque DUM PAREN_IZQ expresion PAREN_DER PUNTOYCOMA;
        buclePer: PER PAREN_IZQ declaracion expresion PUNTOYCOMA actualizacion PUNTOYCOMA? PAREN_DER bloque;

        actualizacion: ID (MAS_ABREVIADO | MENOS_ABREVIADO)
                | asignacion;

        interrupcion: PERGE PUNTOYCOMA
                | INTERRUMPE PUNTOYCOMA;

// LEXER
    // EXPRESIONES
        // Palabras Reservadas de Secciones
            VARIABILES_GLOBAL: 'VARIABILES>';
            MAIOR: 'MAIOR>';

        // Palabras Reservadas
            ESTO: 'esto'; // Declaracion de variable
            SERIES: 'series'; // Arreglos
            SI: 'si';
            ALITER: 'aliter';
            DUM: 'dum';
            FACERE: 'facere';
            PER: 'per';
            PERGE: 'perge';
            INTERRUMPE: 'interrumpe';
            FINIS: 'finis';
            FINIS_MAYUS: 'FINIS';
            NON: 'non';
            IMPORT: 'import';
            NOVUS: 'novus';
            Z: 'z';
            Y: 'y';

        // Booleano
            VERUM: 'verum';
            FALSUS: 'falsus';

        // Tipos de datos
            NUMERUS: 'numerus';
            TEXTUM: 'textum';
            DECIMALIS: 'decimalis';
            LITTERA: 'littera';
            BOOL: 'bool';

        // Funciones
            IMPRIMIR: '>>';
            LECTURA: '<<';

        // Basicas
            ID: [a-zA-Z][a-zA-Z0-9_]* ;
            NUMERO: [0-9]+;
            DECIMALES: [0-9]+'.'[0-9]+;
            TEXTO: '"' .*? '"';
            CARACTER: '\'' . '\'';
            WS: [ \t\r]+ -> skip;
            FIN_LINEA: '\n' -> skip;        


    // COMENTARIOS
        COMENTARIO_LINEA: '//' ~[\r\n]* -> channel(HIDDEN);
        COMENTARIO_BLOQUE: '##' .*? '##' -> channel(HIDDEN);

    // OPERADORES
            // Aritmeticos
            MAS: '+';
            MENOS: '-';
            POR: '*';
            DIVISION: '/';
            MODULO: '%';
            MAS_ABREVIADO: '++';
            MENOS_ABREVIADO: '--';

            // Relacionales
            IGUALIGUAL: '==';
            DIFERENTEDE: '!=';
            MENOR: '<';
            MENOR_IGUAL: '<=';
            MAYOR: '>';
            MAYOR_IGUAL: '>=';

            // Logicos
            AND: '&&';
            OR: '||';

            // Declaratorio
            DOSPUNTOS: ':';
            IGUAL: '=';

            //Otro
            COMA: ',';
            PUNTOYCOMA: ';';
            PUNTO: '.';

            // Agrupacion
            LLAVE_IZQ: '{';
            LLAVE_DER: '}';
            CORCH_IZQ: '[';
            CORCH_DER: ']';
            PAREN_IZQ: '(';
            PAREN_DER: ')';
