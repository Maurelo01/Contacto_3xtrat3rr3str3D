package mycompany.contacto_3xtrat3rr3str3d.semantico;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class TablaSimbolos
{
    private Stack<Map<String, Simbolo>> pilaAmbitos;
    private int offsetGlobal;
    private int offsetLocal;
    public TablaSimbolos()
    {
        pilaAmbitos = new Stack<>();
        pilaAmbitos.push(new HashMap<>());
        offsetGlobal = 0;
        offsetLocal = 0;
    }

    public void entrarAmbito()
    {
        pilaAmbitos.push(new HashMap<>());
    }

    public void salirAmbito()
    {
        if (pilaAmbitos.size() > 1)
        {
            pilaAmbitos.pop();
        }
    }
    public void resetearOffsetLocal() 
    {
        this.offsetLocal = 0;
    }

    public boolean insertar(Simbolo simbolo)
    {
        Map<String, Simbolo> ambitoActual = pilaAmbitos.peek();
        if (ambitoActual.containsKey(simbolo.getNombre()))
        {
            return false;
        }
        if (simbolo.getCategoria().equals("Metodo") || simbolo.getCategoria().equals("Clase") || simbolo.getCategoria().equals("Constructor"))
        {
            simbolo.setOffset(-1);
            simbolo.setEnHeap(true);
        }
        else
        {
            if (pilaAmbitos.size() == 1)
            {
                simbolo.setOffset(offsetGlobal++);
                simbolo.setEnHeap(true);
            }
            else
            {
                simbolo.setOffset(offsetLocal++);
                simbolo.setEnHeap(false);
            }
        }
        ambitoActual.put(simbolo.getNombre(), simbolo);
        return true;
    }
    public Simbolo buscar(String nombre)
    {
        for (int i = pilaAmbitos.size() - 1; i >= 0; i--)
        {
            Map<String, Simbolo> ambito = pilaAmbitos.get(i);
            if (ambito.containsKey(nombre))
            {
                return ambito.get(nombre);
            }
        }
        return null;
    }
    public void limpiar()
    {
        pilaAmbitos.clear();
        pilaAmbitos.push(new HashMap<>());
        offsetGlobal = 0;
        offsetLocal = 0;
    }
    public String imprimirTabla()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("ESTADO TABLA DE SÍMBOLOS \n");
        for (int i = 0; i < pilaAmbitos.size(); i++)
        {
            String tipoAmbito = (i == 0) ? "GLOBAL Heap" : "LOCAL Nivel " + i + " (Stack)";
            sb.append("Ámbito ").append(tipoAmbito).append(":\n");
            for (Simbolo s : pilaAmbitos.get(i).values())
            {
                String ubicacion = s.isEnHeap() ? "Heap" : "Stack";
                String coord = s.getOffset() == -1 ? "N/A" : String.valueOf(s.getOffset());
                sb.append(String.format("  -> [%s] %s : %s (Línea: %d, Columna: %d) | Posición: %s[%s]\n", s.getCategoria(), s.getNombre(), s.getTipo(), s.getLinea(),
                        s.getColumna(), ubicacion, coord));
            }   
        }
        sb.append("\n");
        return sb.toString();
    }
    
    public boolean esGlobal(String nombre)
    {
        return !pilaAmbitos.isEmpty() && pilaAmbitos.get(0).containsKey(nombre);
    }
}