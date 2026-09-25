package mycompany.contacto_3xtrat3rr3str3d.c3d;

import java.util.List;

public class ConectorC3D
{
    public static String construirCodigoCompilable(List<String> codigoMain, int totalTemporales, List<String> funcionesNativas)
    {
        StringBuilder c3d = new StringBuilder();
        c3d.append("#include <stdio.h>\n\n");
        c3d.append("#include <math.h>\n\n");
        c3d.append("double heap[100000];\n");
        c3d.append("double stack[100000];\n");
        c3d.append("int punteroStack = 0;\n");
        c3d.append("int punteroHeap = 0;\n\n");

        if (totalTemporales > 0)
        {
            c3d.append("double ");
            for (int i = 0; i < totalTemporales; i++)
            {
                c3d.append("temp").append(i);
                if (i < totalTemporales - 1)
                {
                    c3d.append(", ");
                }
                if ((i + 1) % 100 == 0)
                {
                    c3d.append("\n");
                }
            }
            c3d.append(";\n\n");
        }

        if (funcionesNativas != null)
        {
            for (String nativa : funcionesNativas)
            {
                c3d.append(nativa).append("\n");
            }
        }

        c3d.append("void main() {\n");
        for (String inst : codigoMain)
        {
            c3d.append("    ").append(inst).append("\n");
        }
        c3d.append("\n    return;\n");
        c3d.append("}\n");
        return c3d.toString();
    }
}
