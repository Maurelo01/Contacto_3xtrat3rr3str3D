package mycompany.contacto_3xtrat3rr3str3d.semantico;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class TablaSimbolos
{
    private Stack<Map<String, Simbolo>> pilaAmbitos;
    public TablaSimbolos()
    {
        pilaAmbitos = new Stack<>();
        pilaAmbitos.push(new HashMap<>());
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

    public boolean insertar(Simbolo simbolo)
    {
        Map<String, Simbolo> ambitoActual = pilaAmbitos.peek();
        if (ambitoActual.containsKey(simbolo.getNombre()))
        {
            return false;
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
                sb.append(String.format("  -> [%s] %s : %s (Línea: %d, Columna: %d)\n", s.getCategoria(), s.getNombre(), s.getTipo(), s.getLinea(), s.getColumna()));
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