package mycompany.contacto_3xtrat3rr3str3d.semantico;

public class ControlTipos
{
    public static TipoDato normalizarTipo(String tipoBruto)
    {
        if (tipoBruto == null)
        {
            return TipoDato.ERROR;
        }

        switch (tipoBruto)
        {
            case "int":
            case "entero":
            case "numerus":
                return TipoDato.ENTERO;
            case "double":
            case "flotante":
            case "decimalis":
                return TipoDato.DECIMAL;
            case "String":
            case "cadena":
            case "textum":
                return TipoDato.CADENA;
            case "char":
            case "caracter":
            case "littera":
                return TipoDato.CARACTER;
            case "boolean":
            case "bool":
                return TipoDato.BOOLEANO;
            case "void":
                return TipoDato.VOID;
            default:
                return TipoDato.OBJETO;
        }
    }

    public static boolean esAsignacionValida(TipoDato destino, TipoDato origen)
    {
        if (destino == origen) return true;
        if (destino == TipoDato.DECIMAL && origen == TipoDato.ENTERO) return true;
        return false;
    }

    public static TipoDato resolverSuma(TipoDato t1, TipoDato t2)
    {
        if (t1 == TipoDato.CADENA || t2 == TipoDato.CADENA) return TipoDato.CADENA;
        if (t1 == TipoDato.ENTERO && t2 == TipoDato.ENTERO) return TipoDato.ENTERO;
        if ((t1 == TipoDato.ENTERO && t2 == TipoDato.DECIMAL) || (t1 == TipoDato.DECIMAL && t2 == TipoDato.ENTERO) || (t1 == TipoDato.DECIMAL && t2 == TipoDato.DECIMAL)) return TipoDato.DECIMAL;
        if ((t1 == TipoDato.CARACTER && t2 == TipoDato.ENTERO) || (t1 == TipoDato.ENTERO && t2 == TipoDato.CARACTER)) return TipoDato.ENTERO;
        if (t1 == TipoDato.CARACTER && t2 == TipoDato.CARACTER) return TipoDato.ENTERO;
        return TipoDato.ERROR;
    }
    public static TipoDato resolverAritmetica(TipoDato t1, TipoDato t2)
    {
        if (t1 == TipoDato.ENTERO && t2 == TipoDato.ENTERO) return TipoDato.ENTERO;
        if ((t1 == TipoDato.ENTERO && t2 == TipoDato.DECIMAL) || (t1 == TipoDato.DECIMAL && t2 == TipoDato.ENTERO) || (t1 == TipoDato.DECIMAL && t2 == TipoDato.DECIMAL)) return TipoDato.DECIMAL;
        if ((t1 == TipoDato.CARACTER && t2 == TipoDato.ENTERO) || (t1 == TipoDato.ENTERO && t2 == TipoDato.CARACTER)) return TipoDato.ENTERO;
        if (t1 == TipoDato.CARACTER && t2 == TipoDato.CARACTER) return TipoDato.ENTERO;
        return TipoDato.ERROR;
    }
    public static TipoDato resolverRelacional(TipoDato t1, TipoDato t2)
    {
        boolean esNum1 = (t1 == TipoDato.ENTERO || t1 == TipoDato.DECIMAL || t1 == TipoDato.CARACTER);
        boolean esNum2 = (t2 == TipoDato.ENTERO || t2 == TipoDato.DECIMAL || t2 == TipoDato.CARACTER);
        if (esNum1 && esNum2) return TipoDato.BOOLEANO;
        return TipoDato.ERROR;
    }
    public static TipoDato resolverIgualdad(TipoDato t1, TipoDato t2)
    {
        if (t1 == t2) return TipoDato.BOOLEANO;
        boolean esNum1 = (t1 == TipoDato.ENTERO || t1 == TipoDato.DECIMAL);
        boolean esNum2 = (t2 == TipoDato.ENTERO || t2 == TipoDato.DECIMAL);
        if (esNum1 && esNum2) return TipoDato.BOOLEANO;
        return TipoDato.ERROR;
    }

    public static TipoDato resolverLogica(TipoDato t1, TipoDato t2)
    {
        if (t1 == TipoDato.BOOLEANO && t2 == TipoDato.BOOLEANO) return TipoDato.BOOLEANO;
        return TipoDato.ERROR;
    }
    public static TipoDato resolverUnariaLogica(TipoDato t) 
    {
        if (t == TipoDato.BOOLEANO) return TipoDato.BOOLEANO;
        return TipoDato.ERROR;
    }
    public static TipoDato resolverUnariaAritmetica(TipoDato t) 
    {
        if (t == TipoDato.ENTERO || t == TipoDato.DECIMAL) return t;
        return TipoDato.ERROR;
    }
    public static String obtenerValorPorDefecto(TipoDato tipo) 
    {
        switch (tipo)
        {
            case ENTERO: return "0";
            case DECIMAL: return "0.0";
            case BOOLEANO: return "false";
            case CADENA: return "\"\"";
            case CARACTER: return "'\\0'";
            default: return "null";
        }
    }
}
