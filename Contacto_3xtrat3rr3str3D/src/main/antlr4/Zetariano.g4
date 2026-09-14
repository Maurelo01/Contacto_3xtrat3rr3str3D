grammar Zetariano;

// PARSER
    programa: clase EOF;
    clase: PUBLIC CLASS ID LLAVE_IZQ miembro* LLAVE_DER;
    miembro: declaracion #MiembroDeclaracion
        | constructor #MiembroConstructor
        | metodo #MiembroMetodo;

    constructor: PUBLIC ID PAREN_IZQ parametros? PAREN_DER bloqueMetodo;
    metodo: PUBLIC tipo ID PAREN_IZQ parametros? PAREN_DER bloqueMetodo;
    parametros: parametro (COMA parametro)*;
    parametro: tipo ID;


    // Tipos de datos
    tipo: tipoBase (CORCH_IZQ CORCH_DER)*;
    tipoBase: INT | DOUBLE | BOOLEAN | CHAR | STRING | VOID | ID;

    // Declaraciones
    declaracion: tipo ID PUNTOYCOMA #DeclSinAsignar
        | tipo ID IGUAL expresion PUNTOYCOMA #DeclConAsignacion
        | tipo ID IGUAL LLAVE_IZQ argumentos? LLAVE_DER PUNTOYCOMA #DeclArrayLiteral;

    // Bloques y alcances
    bloqueMetodo: LLAVE_IZQ instruccion* LLAVE_DER;
    bloque: LLAVE_IZQ instruccion* LLAVE_DER | instruccion;

    // Instrucciones
    instruccion: declaracion
        | asignacion PUNTOYCOMA
        | incremento PUNTOYCOMA
        | acceso PAREN_IZQ argumentos? PAREN_DER PUNTOYCOMA
        | condicional
        | condicionalSwitch
        | bucle
        | funcionEspecial PUNTOYCOMA
        | BREAK PUNTOYCOMA
        | CONTINUE PUNTOYCOMA
        | RETURN expresion? PUNTOYCOMA;

    acceso: ID (CORCH_IZQ expresion CORCH_DER | PUNTO ID)*;
    asignacion: acceso (IGUAL | MAS_IGUAL | MENOS_IGUAL | POR_IGUAL) expresion;
    incremento: acceso (MAS_MAS | MENOS_MENOS);

    // Funciones Especiales de impresion
    funcionEspecial: PRINTLN PAREN_IZQ expresion? PAREN_DER
        | PRINT PAREN_IZQ expresion? PAREN_DER;

    // Estructuras de Control
    condicional: IF PAREN_IZQ expresion PAREN_DER bloque (ELSE IF PAREN_IZQ expresion PAREN_DER bloque)* (ELSE bloque)?;
    condicionalSwitch: SWITCH PAREN_IZQ expresion PAREN_DER LLAVE_IZQ caso* casoDefault? LLAVE_DER;
    caso: CASE expresion DOSPUNTOS instruccion*;
    casoDefault: DEFAULT DOSPUNTOS instruccion*;

    // Bucles
    bucle: FOR PAREN_IZQ declaracionFor? PUNTOYCOMA expresion? PUNTOYCOMA actualizacionFor? PAREN_DER bloque #BucleFor
        | WHILE PAREN_IZQ expresion PAREN_DER bloque #BucleWhile
        | DO bloqueMetodo WHILE PAREN_IZQ expresion PAREN_DER PUNTOYCOMA #BucleDoWhile;
    declaracionFor: tipo ID IGUAL expresion
        | asignacion;
    actualizacionFor: asignacion | incremento;

    // Expresiones
    expresion: expresion (POR | DIVISION | MODULO) expresion #MultDiv
        | expresion (MAS | MENOS) expresion #SumaResta
        | expresion (MAYOR | MAYOR_IGUAL | MENOR | MENOR_IGUAL) expresion #Comparacion
        | expresion (IGUALIGUAL | DIFERENTEDE) expresion #Igualdad
        | expresion AND expresion #AndLogico
        | expresion OR expresion #OrLogico
        | <assoc=right> expresion INTERROGACION expresion DOSPUNTOS expresion #Ternario
        | factor #ToFactor;

    factor: MENOS factor #NegacionUnaria
        | NO factor #NegacionLogica
        | NUMERO #NumLiteral
        | DECIMALES #DecLiteral
        | TEXTO #TextoLiteral
        | CARACTER #CharLiteral
        | TRUE #TrueLiteral
        | FALSE #FalseLiteral
        | NULL #NullLiteral
        | PAREN_IZQ expresion PAREN_DER #Parentesis
        | NEW ID PAREN_IZQ argumentos? PAREN_DER #InstanciaObjeto
        | NEW tipoBase (CORCH_IZQ expresion CORCH_DER)+ #InstanciaArray
        | READLN PAREN_IZQ PAREN_DER #LecturaConsola
        | acceso #AccesoVariableOAtributo
        | acceso PAREN_IZQ argumentos? PAREN_DER #LlamadaFuncionOMetodo;

    argumentos: expresion (COMA expresion)*;

// LEXER
    // Palabras Reservadas
        PUBLIC: 'public';
        CLASS: 'class';
        NEW: 'new';
        IF: 'if';
        ELSE: 'else';
        SWITCH: 'switch';
        CASE: 'case';
        DEFAULT: 'default';
        BREAK: 'break';
        FOR: 'for';
        WHILE: 'while';
        DO: 'do';
        CONTINUE: 'continue';
        RETURN: 'return';
        PRINTLN: 'println';
        PRINT: 'print';
        READLN: 'readln';
        NULL: 'null';
        TRUE: 'true';
        FALSE: 'false';

    // Tipos
        INT: 'int';
        DOUBLE: 'double';
        BOOLEAN: 'boolean';
        CHAR: 'char';
        STRING: 'String';
        VOID: 'void';

    // Basicas
        ID: [a-zA-Z_][a-zA-Z0-9_]*;
        NUMERO: [0-9]+;
        DECIMALES: [0-9]+'.'[0-9]+;
        TEXTO: '"' .*? '"';
        CARACTER: '\'' . '\'';
        WS: [ \t\r\n]+ -> skip;

    // Comentarios
        COMENTARIO_LINEA: '//' ~[\r\n]* -> channel(HIDDEN);
        COMENTARIO_BLOQUE: '/*' .*? '*/' -> channel(HIDDEN);

    // Operadores Aritmeticos
        MAS: '+';
        MENOS: '-';
        POR: '*';
        DIVISION: '/';
        MODULO: '%';
        MAS_MAS: '++';
        MENOS_MENOS: '--';

    // Operadores Asignacion
        IGUAL: '=';
        MAS_IGUAL: '+=';
        MENOS_IGUAL: '-=';
        POR_IGUAL: '*=';

    // Operadores relacionales y Logicos
        IGUALIGUAL: '==';
        DIFERENTEDE: '!=';
        MENOR: '<';
        MENOR_IGUAL: '<=';
        MAYOR: '>';
        MAYOR_IGUAL: '>=';
        AND: '&&';
        OR: '||';
        NO: '!';

    // Simbolos
        INTERROGACION: '?';
        DOSPUNTOS: ':';
        COMA: ',';
        PUNTOYCOMA: ';';
        PUNTO: '.';
        LLAVE_IZQ: '{';
        LLAVE_DER: '}';
        CORCH_IZQ: '[';
        CORCH_DER: ']';
        PAREN_IZQ: '(';
        PAREN_DER: ')'; 