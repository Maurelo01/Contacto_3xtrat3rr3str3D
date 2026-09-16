package mycompany.contacto_3xtrat3rr3str3d.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Element;

public class NumeroLinea extends JPanel implements DocumentListener
{
    private final JTextPane editor;
    public NumeroLinea(JTextPane editor)
    {
        this.editor = editor;
        setBackground(new Color(240, 240, 240));
        setForeground(new Color(120, 120, 120));
        editor.getDocument().addDocumentListener(this);
    }

    @Override
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        g.setFont(editor.getFont());
        FontMetrics metrics = g.getFontMetrics();
        int ascenso = metrics.getAscent();
        Element root = editor.getDocument().getDefaultRootElement();
        int lineas = root.getElementCount();
        int ancho = metrics.stringWidth(String.valueOf(lineas)) + 15;
        int alto = editor.getHeight();
        if (getPreferredSize().width != ancho || getPreferredSize().height != alto)
        {
            setPreferredSize(new Dimension(ancho, alto));
            getParent().revalidate();
        }
        Rectangle clip = g.getClipBounds();
        for (int i = 0; i < lineas; i++)
        {
            try
            {
                Rectangle2D rect = editor.modelToView2D(root.getElement(i).getStartOffset());
                if (rect != null)
                {
                    int y = (int) rect.getY();
                    if (y + rect.getHeight() >= clip.y && y <= clip.y + clip.height)
                    {
                        String textoNumero = String.valueOf(i + 1);
                        int x = ancho - metrics.stringWidth(textoNumero) - 7;
                        g.drawString(textoNumero, x, y + ascenso);
                    }
                }
            }
            catch (Exception ex) {}
        }
    }
    @Override public void insertUpdate(DocumentEvent e) { repaint(); }
    @Override public void removeUpdate(DocumentEvent e) { repaint(); }
    @Override public void changedUpdate(DocumentEvent e) { repaint(); }
}