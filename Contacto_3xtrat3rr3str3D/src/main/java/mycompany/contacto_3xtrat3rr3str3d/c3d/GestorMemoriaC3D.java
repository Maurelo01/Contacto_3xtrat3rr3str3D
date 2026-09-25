package mycompany.contacto_3xtrat3rr3str3d.c3d;

public class GestorMemoriaC3D
{
    private int contadorTemporales;
    private int contadorEtiquetas;
    private int punteroStack;
    private int punteroHeap;
    public GestorMemoriaC3D()
    {
        limpiar();
    }

    public void limpiar()
    {
        this.contadorTemporales = 0;
        this.contadorEtiquetas = 0;
        this.punteroStack = 0;
        this.punteroHeap = 0;
    }

    public String generarTemporal()
    {
        return "temp" + (contadorTemporales++);
    }

    public String generarEtiqueta()
    {
        return "et" + (contadorEtiquetas++);
    }

    public int moverPunteroStack(int espacios)
    {
        int actual = punteroStack;
        punteroStack += espacios;
        return actual;
    }

    public int moverPunteroHeap(int espacios)
    {
        int actual = punteroHeap;
        punteroHeap += espacios;
        return actual;
    }

    public int getContadorTemporales()
    {
        return contadorTemporales;
    }
}
