package mycompany.contacto_3xtrat3rr3str3d.ast;

import java.util.ArrayList;
import java.util.List;

public class NodoAST
{
    private final String etiqueta;
    private final List<NodoAST> hijos;

    public NodoAST(String etiqueta)
    {
        this.etiqueta = etiqueta;
        this.hijos = new ArrayList<>();
    }
    
    public void agregarHijo(NodoAST hijo)
    {
        if (hijo != null) this.hijos.add(hijo);
    }
    public void agregarHijo(String etiquetaHijo)
    {
        this.hijos.add(new NodoAST(etiquetaHijo));
    }
    public String imprimirArbol()
    {
        return imprimirRecursivo("", true);
    }
    private String imprimirRecursivo(String prefijo, boolean esUltimo)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(prefijo).append(esUltimo ? "└── " : "├── ").append(etiqueta).append("\n");
        for (int i = 0; i < hijos.size(); i++)
        {
            boolean ultimoHijo = (i == hijos.size() - 1);
            String nuevoPrefijo = prefijo + (esUltimo ? "    " : "│   ");
            sb.append(hijos.get(i).imprimirRecursivo(nuevoPrefijo, ultimoHijo));
        }
        return sb.toString();
    }
}
