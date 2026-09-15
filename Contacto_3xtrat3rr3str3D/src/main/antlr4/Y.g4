grammar Y;

    @header {
        package mycompany.contacto_3xtrat3rr3str3d;
        import java.util.Stack;
        import java.util.Queue;
        import java.util.LinkedList;
    }

tokens { INDENT, DEDENT }
   
    @lexer::members
    {
        private Stack<Integer> indentStack = new Stack<>();
        private Queue<Token> pendingTokens = new LinkedList<>();
        {
            indentStack.push(0);
        }

        @Override
        public Token nextToken()
        {
            if (!pendingTokens.isEmpty())
            {
                return pendingTokens.poll();
            }
            Token token = super.nextToken();
            //  Cuando se termina el archivo se cierran todos los bloques abiertos con DEDENT
            if (token.getType() == EOF)
            {
                while (indentStack.size() > 1)
                {
                    indentStack.pop();
                    pendingTokens.add(new org.antlr.v4.runtime.CommonToken(YParser.DEDENT, "<DEDENT>"));
                }
                pendingTokens.add(token);
                return pendingTokens.poll();
            }
            return token;
        }

        private void procesarIndentacion(int espacios)
        {
            int indentActual = indentStack.peek();
            if (espacios > indentActual)
            {
                indentStack.push(espacios);
                pendingTokens.add(new org.antlr.v4.runtime.CommonToken(YParser.INDENT, "<INDENT>"));
            }
            else if (espacios < indentActual)
            {
                while (indentStack.size() > 1 && indentStack.peek() > espacios)
                {
                    indentStack.pop();
                    pendingTokens.add(new org.antlr.v4.runtime.CommonToken(YParser.DEDENT, "<DEDENT>"));
                }
            }
        }
    }

    // Mide espacios al inicio de la linea
    NUEVA_LINEA: ('\r'? '\n')+ [ \t]* 
    {
        String texto = getText();
        int espacios = 0;
        for (int i = texto.lastIndexOf('\n') + 1; i < texto.length(); i++)
        {
            if (texto.charAt(i) == '\t') espacios += 4;
            else if (texto.charAt(i) == ' ') espacios++;
        }
        procesarIndentacion(espacios);
    } -> channel(HIDDEN);

// PARSER
    programa: seccionEstructuras? seccionFunciones EOF;

    // Secciones
    seccionEstructuras: PORCENTAJE ESTRUCTURAS_KW definicionEstructura*;
    seccionFunciones: PORCENTAJE FUNCIONES_KW definicionFuncion+;

    // Estructuras
    definicionEstructura: ESTRUCTURA_KW ID DOSPUNTOS INDENT atributoEstructura+ DEDENT;
    atributoEstructura: tipo ID (CORCH_IZQ NUMERO CORCH_DER)? #AtributoNormal
                    | ID ID #AtributoEstructuraAnidada;

    // Funciones
    definicionFuncion: DEFINIR FUNCION ID PAREN_IZQ parametros? PAREN_DER (FLECHA tipo)? DOSPUNTOS bloque;
    parametros: parametro (COMA parametro)*;
    parametro: tipo ID #ParamValor
            | CORCH_IZQ CORCH_DER tipo ID #ParamArregloRef
            | LLAVE_IZQ ID ID #ParamStructRef;

    // Bloques
    bloque: INDENT instruccion+ DEDENT;

    // Instrucciones
    instruccion: declaracion #InstruccionDeclaracion
            | asignacion #InstruccionAsignacion
            | incremento #InstruccionIncremento
            | condicional #InstruccionCondicional
            | condicionalElegir #InstruccionCondicionalElegir
            | buclePara #InstruccionBuclePara
            | bucleMientras #InstruccionBucleMientras
            | bucleHacer #InstruccionBucleHacer
            | IMPRIMIR PAREN_IZQ expresion? PAREN_DER #InstruccionImprimir
            | RETORNAR expresion? #InstruccionRetornar
            | ROMPER #InstruccionRomper
            | CONTINUAR #InstruccionContinuar
            | llamadaFuncion #InstruccionLlamadaFuncion;

    // Declaraciones
    declaracion: tipo ID #DeclVariable
            | tipo ID IGUAL expresion #DeclVariableAsig
            | tipo ID CORCH_IZQ NUMERO? CORCH_DER #DeclArreglo
            | tipo ID CORCH_IZQ NUMERO? CORCH_DER IGUAL LLAVE_IZQ argumentos? LLAVE_DER #DeclArregloLiteral
            | tipo ID CORCH_IZQ CORCH_DER CORCH_IZQ CORCH_DER #DeclMatriz
            | ID ID #DeclEstructura
            | ID ID IGUAL LLAVE_IZQ argumentos? LLAVE_DER #DeclEstructuraAsig;

    acceso: ID (CORCH_IZQ expresion CORCH_DER | PUNTO ID)*;
    asignacion: acceso IGUAL expresion;
    incremento: acceso (MAS_MAS | MENOS_MENOS);
    llamadaFuncion: ID PAREN_IZQ argumentos? PAREN_DER;

    // Control de Flujo
    condicional: SI PAREN_IZQ expresion PAREN_DER ENTONCES bloque (SINO PAREN_IZQ expresion PAREN_DER ENTONCES bloque)* (CONTRARIO bloque)?;
    condicionalElegir: ELEGIR PAREN_IZQ expresion PAREN_DER INDENT caso* casoDefault? DEDENT;
    caso: CASO expresion DOSPUNTOS bloque ROMPER?;
    casoDefault: SIEMPRE DOSPUNTOS bloque ROMPER?;

    // Bucles
    buclePara: PARA PAREN_IZQ declaracion PUNTOYCOMA expresion PUNTOYCOMA actualizacionFor PAREN_DER DOSPUNTOS bloque;
    actualizacionFor: asignacion | incremento;
    bucleMientras: MIENTRAS PAREN_IZQ expresion PAREN_DER HACER bloque;
    bucleHacer: HACER DOSPUNTOS bloque MIENTRAS PAREN_IZQ expresion PAREN_DER;

    // Expresiones
    expresion: expresion (POR | DIVISION) expresion #MultDiv
            | expresion (MAS | MENOS) expresion #SumaResta
            | expresion (MAYOR | MAYOR_IGUAL | MENOR | MENOR_IGUAL) expresion #Comparacion
            | expresion (IGUALIGUAL | DIFERENTEDE) expresion #Igualdad
            | expresion AND expresion #AndLogico
            | expresion OR expresion #OrLogico
            | factor #ToFactor;

    factor: MENOS factor #NegacionUnaria
        | NO factor #NegacionLogica
        | NUMERO #NumLiteral
        | DECIMALES #DecLiteral
        | TEXTO #TextLiteral
        | CARACTER #CharLiteral
        | VERDADERO #TrueLiteral
        | FALSO #FalseLiteral
        | PAREN_IZQ expresion PAREN_DER #Parentesis
        | LEER PAREN_IZQ PAREN_DER #LecturaConsola
        | acceso #AccesoVariableOAtributo
        | llamadaFuncion #LlamadaFuncionExpr;

    argumentos: expresion (COMA expresion)*;
    tipo: ENTERO | CADENA | FLOTANTE | CARACTER_TIPO | BOOL;

// LEXER
    // Palabras Reservadas
        ESTRUCTURAS_KW: 'estructuras';
        FUNCIONES_KW: 'funciones';
        ESTRUCTURA_KW: 'estructura';
        DEFINIR: 'definir';
        FUNCION: 'funcion';
        SI: 'si';
        ENTONCES: 'entonces';
        SINO: 'sino';
        CONTRARIO: 'contrario';
        ELEGIR: 'elegir';
        CASO: 'caso';
        SIEMPRE: 'siempre';
        ROMPER: 'romper';
        PARA: 'para';
        MIENTRAS: 'mientras';
        HACER: 'hacer';
        CONTINUAR: 'continuar';
        IMPRIMIR: 'imprimir';
        LEER: 'leer';
        RETORNAR: 'retornar';

    // Tipos y Booleanos
        ENTERO: 'entero';
        CADENA: 'cadena';
        FLOTANTE: 'flotante';
        CARACTER_TIPO: 'caracter';
        BOOL: 'bool';
        VERDADERO: 'verdadero';
        FALSO: 'falso';

    // Basicas
        ID: [a-zA-Z_][a-zA-Z0-9_]*;
        NUMERO: [0-9]+;
        DECIMALES: [0-9]+'.'[0-9]+;
        TEXTO: '"' .*? '"';
        CARACTER: '\'' . '\'';
        WS: [ \t]+ -> skip;

    // Comentarios
        COMENTARIO_LINEA: '//' ~[\r\n]* -> channel(HIDDEN);
        COMENTARIO_BLOQUE: '/*' .*? '*/' -> channel(HIDDEN);

    // Operadores
        MAS: '+';
        MENOS: '-';
        POR: '*';
        DIVISION: '/';
        MAS_MAS: '++';
        MENOS_MENOS: '--';
        IGUALIGUAL: '==';
        DIFERENTEDE: '!=';
        MENOR: '<';
        MENOR_IGUAL: '<=';
        MAYOR: '>';
        MAYOR_IGUAL: '>=';
        AND: '&&';
        OR: '||';
        NO: '!';
        IGUAL: '=';

    // Simbolos
        PORCENTAJE: '%';
        DOSPUNTOS: ':';
        FLECHA: '->';
        COMA: ',';
        PUNTOYCOMA: ';';
        PUNTO: '.';
        LLAVE_IZQ: '{';
        LLAVE_DER: '}';
        CORCH_IZQ: '[';
        CORCH_DER: ']';
        PAREN_IZQ: '(';
        PAREN_DER: ')';