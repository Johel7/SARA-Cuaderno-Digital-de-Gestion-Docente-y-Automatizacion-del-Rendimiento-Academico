package sara.vista;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import sara.modelo.Usuario;
import sara.util.Mensajes;

/*
 * Clase MenuPrincipalFrame
 * Ventana principal. Segun el rol del usuario muestra un menu distinto:
 *   DOCENTE: grupos, estudiantes, evaluaciones, notas y consultas.
 *   ADMINISTRADOR: cuentas, asignaturas, grupos y respaldos.
 * Cada opcion cambia el panel que se ve en el centro de la ventana.
 */
public class MenuPrincipalFrame extends JFrame {

    private Usuario usuarioActual;
    private JPanel contenido = new JPanel(new BorderLayout());

    public MenuPrincipalFrame(Usuario usuarioActual) {
        this.usuarioActual = usuarioActual;

        setTitle("SARA - " + usuarioActual.getNombreCompleto() + " (" + usuarioActual.getRol() + ")");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(1100, 650));

        construirMenu();
        add(contenido, BorderLayout.CENTER);
        mostrarBienvenida();

        pack();
        setLocationRelativeTo(null);
    }

    private void construirMenu() {
        JMenuBar barra = new JMenuBar();

        // Menu "Sistema": lo ven todos
        JMenu menuSistema = new JMenu("Sistema");
        JMenuItem itemInicio = new JMenuItem("Inicio");
        JMenuItem itemCerrarSesion = new JMenuItem("Cerrar sesión");
        JMenuItem itemSalir = new JMenuItem("Salir");
        itemInicio.addActionListener(e -> mostrarBienvenida());
        itemCerrarSesion.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });
        itemSalir.addActionListener(e -> System.exit(0));
        menuSistema.add(itemInicio);
        menuSistema.add(itemCerrarSesion);
        menuSistema.add(itemSalir);
        barra.add(menuSistema);

        if (usuarioActual.esAdministrador()) {
            JMenu menuAdmin = new JMenu("Administración");
            agregarOpcion(menuAdmin, "Cuentas de usuario", () -> mostrar(new PanelCuentas(usuarioActual)));
            agregarOpcion(menuAdmin, "Asignaturas", () -> mostrar(new PanelAsignaturas()));
            agregarOpcion(menuAdmin, "Grupos y secciones", () -> mostrar(new PanelGrupos()));
            agregarOpcion(menuAdmin, "Respaldos", () -> mostrar(new PanelRespaldos()));
            barra.add(menuAdmin);
        } else {
            JMenu menuGrupos = new JMenu("Grupos y estudiantes");
            agregarOpcion(menuGrupos, "Grupos y secciones", () -> mostrar(new PanelGrupos()));
            agregarOpcion(menuGrupos, "Estudiantes", () -> mostrar(new PanelEstudiantes(usuarioActual)));
            barra.add(menuGrupos);

            JMenu menuEvaluacion = new JMenu("Evaluación");
            agregarOpcion(menuEvaluacion, "Configurar actividades", () -> mostrar(new PanelEvaluaciones(usuarioActual)));
            agregarOpcion(menuEvaluacion, "Registro de notas", () -> mostrar(new PanelNotas(usuarioActual)));
            barra.add(menuEvaluacion);

            JMenu menuResultados = new JMenu("Resultados");
            agregarOpcion(menuResultados, "Resultados, boletines y reportes", () -> mostrar(new PanelConsultas(usuarioActual)));
            barra.add(menuResultados);
        }

        JMenu menuAyuda = new JMenu("Ayuda");
        JMenuItem itemAcerca = new JMenuItem("Niveles de logro");
        itemAcerca.addActionListener(e -> Mensajes.info(this,
                "Niveles de logro del MINED:\n\n"
                + "AA  (90 - 100)  Aprendizaje Avanzado\n"
                + "AS  (76 - 89)    Aprendizaje Satisfactorio\n"
                + "AF  (60 - 75)    Aprendizaje Fundamental\n"
                + "AI  (menos de 60)  Aprendizaje Inicial"));
        menuAyuda.add(itemAcerca);
        barra.add(menuAyuda);

        setJMenuBar(barra);
    }

    // Agrega una opcion al menu que, al elegirla, ejecuta la accion indicada
    private void agregarOpcion(JMenu menu, String texto, Runnable accion) {
        JMenuItem item = new JMenuItem(texto);
        item.addActionListener(e -> accion.run());
        menu.add(item);
    }

    // Cambia el panel que se ve en el centro de la ventana
    private void mostrar(JPanel panel) {
        contenido.removeAll();
        contenido.add(panel, BorderLayout.CENTER);
        contenido.revalidate();
        contenido.repaint();
    }

    // Pantalla de inicio: saludo y botones con los pasos en el orden en que se usan
    private void mostrarBienvenida() {
        String texto = "<html><div style='text-align:center'><h1>SARA</h1>"
                + "<p>Cuaderno Digital de Gestión Docente</p>"
                + "<p>Bienvenido(a), <b>" + usuarioActual.getNombreCompleto() + "</b></p></div></html>";

        JPanel botones = new JPanel(new GridLayout(0, 1, 8, 8));
        botones.setBorder(new EmptyBorder(10, 250, 40, 250));

        if (usuarioActual.esAdministrador()) {
            agregarBotonInicio(botones, "1. Cuentas de usuario", () -> mostrar(new PanelCuentas(usuarioActual)));
            agregarBotonInicio(botones, "2. Asignaturas", () -> mostrar(new PanelAsignaturas()));
            agregarBotonInicio(botones, "3. Grupos y secciones", () -> mostrar(new PanelGrupos()));
            agregarBotonInicio(botones, "4. Respaldos de la base de datos", () -> mostrar(new PanelRespaldos()));
        } else {
            agregarBotonInicio(botones, "1. Crear o revisar secciones", () -> mostrar(new PanelGrupos()));
            agregarBotonInicio(botones, "2. Registrar estudiantes", () -> mostrar(new PanelEstudiantes(usuarioActual)));
            agregarBotonInicio(botones, "3. Configurar actividades evaluativas", () -> mostrar(new PanelEvaluaciones(usuarioActual)));
            agregarBotonInicio(botones, "4. Registrar notas", () -> mostrar(new PanelNotas(usuarioActual)));
            agregarBotonInicio(botones, "5. Ver resultados, boletines y reportes", () -> mostrar(new PanelConsultas(usuarioActual)));
        }

        JPanel bienvenida = new JPanel(new BorderLayout());
        bienvenida.add(new JLabel(texto, JLabel.CENTER), BorderLayout.NORTH);
        bienvenida.add(botones, BorderLayout.CENTER);
        mostrar(bienvenida);
    }

    private void agregarBotonInicio(JPanel panel, String texto, Runnable accion) {
        JButton boton = new JButton(texto);
        boton.setFont(boton.getFont().deriveFont(15f));
        boton.addActionListener(e -> accion.run());
        panel.add(boton);
    }
}
