/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package mycompany.contacto_3xtrat3rr3str3d.ui;
import java.awt.Component;
import java.io.File;
import java.io.IOException;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.event.ChangeEvent;
import javax.swing.text.Element;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import mycompany.contacto_3xtrat3rr3str3d.servicios.GestorArchivos;
import mycompany.contacto_3xtrat3rr3str3d.servicios.GestorCompilacion;
import mycompany.contacto_3xtrat3rr3str3d.simbolos.TablaSimbolos;

/**
 *
 * @author mauricio
 */
public class VentanaPrincipal extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(VentanaPrincipal.class.getName());
    private File carpetaProyectoActual;
    private TablaSimbolos tablaMemoria = new TablaSimbolos();
    /**
     * Creates new form VentanaPrincipal
     */
    public VentanaPrincipal() {
        initComponents();
        agregarOpciones();
        panelPestañas.addChangeListener((ChangeEvent e) ->
        {
            actualizarIndicadorPosicion();
        });
    }

    private void agregarOpciones()
    {
        JMenuItem itemNuevaCarpeta = new JMenuItem("Nueva Carpeta");
        itemNuevaCarpeta.addActionListener(this::itemNuevaCarpetaActionPerformed);
        menuArchivo.add(itemNuevaCarpeta);
        JMenuItem itemGuardarComo = new JMenuItem("Guardar Como");
        itemGuardarComo.addActionListener(this::itemGuardarComoActionPerformed);
        menuArchivo.add(itemGuardarComo);
        JMenuItem itemEliminar = new JMenuItem("Eliminar Seleccionado");
        itemEliminar.addActionListener(this::itemEliminarActionPerformed);
        menuArchivo.add(itemEliminar);
        JMenuItem itemRefrescar = new JMenuItem("Refrescar Árbol");
        itemRefrescar.addActionListener(e -> refrescarArbol());
        menuArchivo.add(itemRefrescar);
    }

    private void refrescarArbol()
    {
        if (carpetaProyectoActual == null) return;
        DefaultMutableTreeNode nodoRaiz = new DefaultMutableTreeNode(new GestorArchivos.NodoArchivo(carpetaProyectoActual));
        GestorArchivos.llenarArbolDirectorios(carpetaProyectoActual, nodoRaiz);
        treeArchivos.setModel(new DefaultTreeModel(nodoRaiz));
    }

    private File obtenerDestinoCarpeta()
    {
        DefaultMutableTreeNode nodo = (DefaultMutableTreeNode) treeArchivos.getLastSelectedPathComponent();
        if (nodo != null && nodo.getUserObject() instanceof GestorArchivos.NodoArchivo na)
        {
            if (na.archivo.isDirectory()) return na.archivo;
            if (na.archivo.getParentFile() != null) return na.archivo.getParentFile();
        }
        return carpetaProyectoActual;
    }

    private void itemNuevaCarpetaActionPerformed(java.awt.event.ActionEvent evt)
    {
        File destino = obtenerDestinoCarpeta();
        if (destino == null)
        {
            JOptionPane.showMessageDialog(this, "Primero debes abrir un proyecto/carpeta.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String nombre = JOptionPane.showInputDialog(this, "Nombre de la nueva carpeta:", "Nueva Carpeta", JOptionPane.QUESTION_MESSAGE);
        if (nombre != null && !nombre.trim().isEmpty())
        {
            try
            {
                if (GestorArchivos.crearCarpeta(destino, nombre.trim()))
                {
                    JOptionPane.showMessageDialog(this, "Carpeta creada exitosamente.");
                    refrescarArbol();
                }
                else JOptionPane.showMessageDialog(this, "La carpeta ya existe.", "Error", JOptionPane.ERROR_MESSAGE);
            }
            catch (IOException e)
            {
                JOptionPane.showMessageDialog(this, "Error al crear la carpeta: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void itemGuardarComoActionPerformed(java.awt.event.ActionEvent evt)
    {
        Component tabActiva = panelPestañas.getSelectedComponent();
        if (!(tabActiva instanceof JScrollPane scroll))
        {
            JOptionPane.showMessageDialog(this, "No hay ningún archivo abierto.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        JTextPane editor = (JTextPane) scroll.getViewport().getView();
        JFileChooser buscador = new JFileChooser();
        buscador.setSelectedFile(new File("copia.pig"));
        if (buscador.showSaveDialog(this) == JFileChooser.APPROVE_OPTION)
        {
            try
            {
                File destino = buscador.getSelectedFile();
                GestorArchivos.guardarContenido(destino, editor.getText());
                editor.putClientProperty("archivoFisico", destino);
                panelPestañas.setTitleAt(panelPestañas.getSelectedIndex(), destino.getName());
                JOptionPane.showMessageDialog(this, "Archivo guardado como " + destino.getName(), "Éxito", JOptionPane.INFORMATION_MESSAGE);
                refrescarArbol();
            }
            catch (IOException e)
            {
                JOptionPane.showMessageDialog(this, "Error al descargar: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void itemEliminarActionPerformed(java.awt.event.ActionEvent evt)
    {
        DefaultMutableTreeNode nodo = (DefaultMutableTreeNode) treeArchivos.getLastSelectedPathComponent();
        if (nodo == null || !(nodo.getUserObject() instanceof GestorArchivos.NodoArchivo na))
        {
            JOptionPane.showMessageDialog(this, "Selecciona un archivo o carpeta en el árbol para eliminar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        File objetivo = na.archivo;
        if (objetivo.equals(carpetaProyectoActual))
        {
            JOptionPane.showMessageDialog(this, "No se puede eliminar la carpeta raíz del proyecto.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "¿Eliminar " + objetivo.getName() + " y todo su contenido?", "Confirmar eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;
        try
        {
            for (int i = 0; i < panelPestañas.getTabCount(); i++)
            {
                Component c = panelPestañas.getComponentAt(i);
                if (c instanceof JScrollPane s)
                {
                    JTextPane ed = (JTextPane) s.getViewport().getView();
                    File f = (File) ed.getClientProperty("archivoFisico");
                    if (f != null && (f.equals(objetivo) || f.getAbsolutePath().startsWith(objetivo.getAbsolutePath() + File.separator)))
                    {
                        panelPestañas.remove(i--);
                    }
                }
            }
            if (GestorArchivos.eliminarRecursivo(objetivo))
            {
                JOptionPane.showMessageDialog(this, "Eliminado correctamente.");
                refrescarArbol();
            }
            else JOptionPane.showMessageDialog(this, "No se pudo eliminar.", "Error", JOptionPane.ERROR_MESSAGE);
        }
        catch (IOException e)
        {
            JOptionPane.showMessageDialog(this, "Error al eliminar: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void abrirArchivoEnPestaña(File archivo)
    {
        String nombreArchivo = archivo.getName();
        for (int i = 0; i < panelPestañas.getTabCount(); i++)
        {
            if (panelPestañas.getTitleAt(i).equals(nombreArchivo))
            {
                panelPestañas.setSelectedIndex(i);
                return;
            }
        }
        try
        {
            String contenido = GestorArchivos.leerContenido(archivo);
            String extension = nombreArchivo.substring(nombreArchivo.lastIndexOf('.') + 1);
            ColoreadoSintaxis doc = new ColoreadoSintaxis(extension);
            JTextPane editor = new JTextPane(doc);
            editor.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 14));
            editor.setText(contenido);
            editor.putClientProperty("archivoFisico", archivo);
            editor.addCaretListener(e -> actualizarIndicadorPosicion());
            JScrollPane scroll = new JScrollPane(editor);
            scroll.setRowHeaderView(new NumeroLinea(editor));
            panelPestañas.addTab(nombreArchivo, scroll);
            panelPestañas.setSelectedComponent(scroll);
        }
        catch (IOException e)
        {
            JOptionPane.showMessageDialog(this, "Error al leer el archivo: " + e.getMessage());
        }
    }
    
    private void actualizarIndicadorPosicion()
    {
        Component tab = panelPestañas.getSelectedComponent();
        if (tab instanceof JScrollPane scroll)
        {
            JTextPane editor = (JTextPane) scroll.getViewport().getView();
            int dot = editor.getCaretPosition();
            Element root = editor.getDocument().getDefaultRootElement();
            int linea = root.getElementIndex(dot);
            int columna = dot - root.getElement(linea).getStartOffset();
            lblPosicion.setText("Línea: " + (linea + 1) + ", Columna: " + (columna + 1));
        }
        else lblPosicion.setText("Línea: 1, Columna: 1");
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        splitHorizontal = new javax.swing.JSplitPane();
        splitVertical = new javax.swing.JSplitPane();
        panelPestañas = new javax.swing.JTabbedPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        treeArchivos = new javax.swing.JTree();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jScrollPane2 = new javax.swing.JScrollPane();
        consolaSalida = new javax.swing.JTextArea();
        jScrollPane3 = new javax.swing.JScrollPane();
        consolaC3D = new javax.swing.JTextArea();
        jScrollPane4 = new javax.swing.JScrollPane();
        consolaAST = new javax.swing.JTextArea();
        lblPosicion = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        menuArchivo = new javax.swing.JMenu();
        itemNuevoArchivo = new javax.swing.JMenuItem();
        itemAbrirProyecto = new javax.swing.JMenuItem();
        itemGuardar = new javax.swing.JMenuItem();
        itemExportarC3D = new javax.swing.JMenuItem();
        itemCerrarArchivo = new javax.swing.JMenuItem();
        menuEjecutar = new javax.swing.JMenu();
        itemAnalizar = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("IDE Contacto 3xtrat3rr3str3D");

        splitHorizontal.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        splitVertical.setRightComponent(panelPestañas);

        treeArchivos.setPreferredSize(new java.awt.Dimension(80, 82));
        treeArchivos.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                treeArchivosMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(treeArchivos);

        splitVertical.setLeftComponent(jScrollPane1);

        splitHorizontal.setLeftComponent(splitVertical);

        consolaSalida.setEditable(false);
        consolaSalida.setColumns(20);
        consolaSalida.setRows(5);
        jScrollPane2.setViewportView(consolaSalida);

        jTabbedPane1.addTab("Consola de Análisis", jScrollPane2);

        consolaC3D.setEditable(false);
        consolaC3D.setColumns(20);
        consolaC3D.setRows(5);
        jScrollPane3.setViewportView(consolaC3D);

        jTabbedPane1.addTab("Código 3 Direcciones", jScrollPane3);

        consolaAST.setEditable(false);
        consolaAST.setColumns(20);
        consolaAST.setRows(5);
        jScrollPane4.setViewportView(consolaAST);

        jTabbedPane1.addTab("Árbol AST", jScrollPane4);

        splitHorizontal.setRightComponent(jTabbedPane1);

        lblPosicion.setText("Línea: 1, Columna: 1");

        menuArchivo.setText("Archivo");

        itemNuevoArchivo.setText("Nuevo Archivo");
        itemNuevoArchivo.addActionListener(this::itemNuevoArchivoActionPerformed);
        menuArchivo.add(itemNuevoArchivo);

        itemAbrirProyecto.setText("Abrir Proyecto");
        itemAbrirProyecto.addActionListener(this::itemAbrirProyectoActionPerformed);
        menuArchivo.add(itemAbrirProyecto);

        itemGuardar.setText("Guardar");
        itemGuardar.addActionListener(this::itemGuardarActionPerformed);
        menuArchivo.add(itemGuardar);

        itemExportarC3D.setText("Exportar C3D (.c)");
        itemExportarC3D.addActionListener(this::itemExportarC3DActionPerformed);
        menuArchivo.add(itemExportarC3D);

        itemCerrarArchivo.setText("Cerrar Pestaña Actual");
        itemCerrarArchivo.addActionListener(this::itemCerrarArchivoActionPerformed);
        menuArchivo.add(itemCerrarArchivo);

        jMenuBar1.add(menuArchivo);

        menuEjecutar.setText("Ejecutar");

        itemAnalizar.setText("Analizar Código");
        itemAnalizar.addActionListener(this::itemAnalizarActionPerformed);
        menuEjecutar.add(itemAnalizar);

        jMenuBar1.add(menuEjecutar);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(splitHorizontal, javax.swing.GroupLayout.DEFAULT_SIZE, 1122, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblPosicion)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(splitHorizontal, javax.swing.GroupLayout.DEFAULT_SIZE, 647, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblPosicion)
                .addContainerGap())
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void itemAbrirProyectoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemAbrirProyectoActionPerformed
        JFileChooser buscador = new JFileChooser();
        buscador.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (buscador.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
        {
            carpetaProyectoActual = buscador.getSelectedFile();
            DefaultMutableTreeNode nodoRaiz = new DefaultMutableTreeNode(new GestorArchivos.NodoArchivo(carpetaProyectoActual));
            GestorArchivos.llenarArbolDirectorios(carpetaProyectoActual, nodoRaiz);
            treeArchivos.setModel(new DefaultTreeModel(nodoRaiz));
        }
    }//GEN-LAST:event_itemAbrirProyectoActionPerformed

    private void treeArchivosMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_treeArchivosMouseClicked
        if (evt.getClickCount() == 2)
        {
            DefaultMutableTreeNode nodoSeleccionado = (DefaultMutableTreeNode) treeArchivos.getLastSelectedPathComponent();
            if (nodoSeleccionado != null && nodoSeleccionado.getUserObject() instanceof GestorArchivos.NodoArchivo nodoArchivo)
            {
                if (nodoArchivo.archivo.isFile()) abrirArchivoEnPestaña(nodoArchivo.archivo);
            }
        }
    }//GEN-LAST:event_treeArchivosMouseClicked

    private void itemGuardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemGuardarActionPerformed
        Component tabActiva = panelPestañas.getSelectedComponent();
        if (tabActiva instanceof JScrollPane scroll)
        {
            try
            {
                JTextPane editor = (JTextPane) scroll.getViewport().getView();
                File archivo = (File) editor.getClientProperty("archivoFisico");
                if (archivo != null)
                {
                    GestorArchivos.guardarContenido(archivo, editor.getText());
                    JOptionPane.showMessageDialog(this, "Archivo guardado correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                }
            }
            catch (IOException e)
            {
                JOptionPane.showMessageDialog(this, "Error al guardar: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        else
        {
            JOptionPane.showMessageDialog(this, "No hay ningún archivo abierto.", "Aviso", JOptionPane.WARNING_MESSAGE);
        }
    }//GEN-LAST:event_itemGuardarActionPerformed

    private void itemNuevoArchivoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemNuevoArchivoActionPerformed
        if (carpetaProyectoActual == null)
        {
            JOptionPane.showMessageDialog(this, "Primero debes abrir un proyecto/carpeta.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String nombreArchivo = JOptionPane.showInputDialog(this, "Ingrese el nombre del archivo con extensión: .pig/.y/.z:", "Nuevo Archivo", JOptionPane.QUESTION_MESSAGE);
        if (nombreArchivo != null && !nombreArchivo.trim().isEmpty())
        {
            if (nombreArchivo.endsWith(".z") || nombreArchivo.endsWith(".y") || nombreArchivo.endsWith(".pig"))
            {
                try
                {
                    File nuevoArchivo = new File(carpetaProyectoActual, nombreArchivo);
                    if (nuevoArchivo.createNewFile())
                    {
                        JOptionPane.showMessageDialog(this, "Archivo creado exitosamente.");
                        DefaultMutableTreeNode nodoRaiz = new DefaultMutableTreeNode(new GestorArchivos.NodoArchivo(carpetaProyectoActual));
                        GestorArchivos.llenarArbolDirectorios(carpetaProyectoActual, nodoRaiz);
                        treeArchivos.setModel(new DefaultTreeModel(nodoRaiz));
                        abrirArchivoEnPestaña(nuevoArchivo);
                    }
                    else JOptionPane.showMessageDialog(this, "El archivo ya existe.", "Error", JOptionPane.ERROR_MESSAGE);
                }
                catch (IOException e)
                {
                    JOptionPane.showMessageDialog(this, "Error al crear el archivo: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
            else JOptionPane.showMessageDialog(this, "Extensión inválida. Debe ser .z, .y o .pig", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_itemNuevoArchivoActionPerformed

    private void itemCerrarArchivoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemCerrarArchivoActionPerformed
        int indiceActivo = panelPestañas.getSelectedIndex();
        if (indiceActivo != -1) panelPestañas.remove(indiceActivo);
    }//GEN-LAST:event_itemCerrarArchivoActionPerformed

    private void itemAnalizarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemAnalizarActionPerformed
        Component tabActivo = panelPestañas.getSelectedComponent();
        if (tabActivo instanceof JScrollPane scroll)
        {
            JTextPane editor = (JTextPane) scroll.getViewport().getView();
            File archivo = (File) editor.getClientProperty("archivoFisico");
            if (archivo != null) GestorCompilacion.analizarCodigo(archivo.getName(), editor.getText(), tablaMemoria, consolaSalida, consolaC3D, consolaAST, archivo, carpetaProyectoActual);
            else JOptionPane.showMessageDialog(this, "El archivo no tiene ruta establecida, se debe guardar primero.", "Aviso", JOptionPane.WARNING_MESSAGE);
        }
        else JOptionPane.showMessageDialog(this, "No hay ningún archivo abierto para analizar.", "Aviso", JOptionPane.WARNING_MESSAGE);
    }//GEN-LAST:event_itemAnalizarActionPerformed

    private void itemExportarC3DActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemExportarC3DActionPerformed
        String codigoC3D = consolaC3D.getText();
        if (codigoC3D == null || codigoC3D.trim().isEmpty())
        {
            JOptionPane.showMessageDialog(this, "No hay código C3D para exportar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        JFileChooser buscador = new JFileChooser();
        buscador.setSelectedFile(new File("salida.c"));
        if (buscador.showSaveDialog(this) == JFileChooser.APPROVE_OPTION)
        {
            try
            {
                File archivoDestino = buscador.getSelectedFile();
                if (!archivoDestino.getName().endsWith(".c")) archivoDestino = new File(archivoDestino.getAbsolutePath() + ".c");
                GestorArchivos.guardarContenido(archivoDestino, codigoC3D);
                JOptionPane.showMessageDialog(this, "Código .c exportado exitosamente a " + archivoDestino.getName(), "Exportación Exitosa", JOptionPane.INFORMATION_MESSAGE);
            }
            catch (IOException e)
            {
                JOptionPane.showMessageDialog(this, "Error al exportar el archivo: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_itemExportarC3DActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        try
        {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels())
            {
                if ("Nimbus".equals(info.getName()))
                {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        }
        catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex)
        {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        java.awt.EventQueue.invokeLater(() -> new VentanaPrincipal().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea consolaAST;
    private javax.swing.JTextArea consolaC3D;
    private javax.swing.JTextArea consolaSalida;
    private javax.swing.JMenuItem itemAbrirProyecto;
    private javax.swing.JMenuItem itemAnalizar;
    private javax.swing.JMenuItem itemCerrarArchivo;
    private javax.swing.JMenuItem itemExportarC3D;
    private javax.swing.JMenuItem itemGuardar;
    private javax.swing.JMenuItem itemNuevoArchivo;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JLabel lblPosicion;
    private javax.swing.JMenu menuArchivo;
    private javax.swing.JMenu menuEjecutar;
    private javax.swing.JTabbedPane panelPestañas;
    private javax.swing.JSplitPane splitHorizontal;
    private javax.swing.JSplitPane splitVertical;
    private javax.swing.JTree treeArchivos;
    // End of variables declaration//GEN-END:variables
}
