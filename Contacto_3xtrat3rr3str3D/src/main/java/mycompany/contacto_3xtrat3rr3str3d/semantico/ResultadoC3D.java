package mycompany.contacto_3xtrat3rr3str3d.semantico;

public class ResultadoC3D
{
    private TipoDato tipo;
    private String valorC3D;
    public ResultadoC3D(TipoDato tipo, String valorC3D)
    {
        this.tipo = tipo;
        this.valorC3D = valorC3D;
    }
    public TipoDato getTipo()
    {
        return tipo;
    }
    public String getValorC3D()
    {
        return valorC3D; 
    }
}