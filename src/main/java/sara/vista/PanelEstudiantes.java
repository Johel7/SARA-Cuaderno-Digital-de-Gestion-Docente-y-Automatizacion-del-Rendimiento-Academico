package sara.vista;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import sara.dao.EstudianteDAO;
import sara.dao.GradoDAO;
import sara.modelo.Estudiante;
import sara.modelo.Grado;
import sara.modelo.Seccion;
import sara.modelo.Usuario;
import sara.util.Mensajes;

/*
 * Clase PanelEstudiantes
 * Registrar, buscar, modificar y dar de baja estudiantes dentro de cada
 * grupo. Dar de baja no borra nada: la matricula pasa a RETIRADA y las
 * notas se conservan.
 */
public class PanelEstudiantes extends JPanel {

    private Usuario usuario;
    private EstudianteDAO estudianteDAO = new EstudianteDAO();
    private GradoDAO gradoDAO = new GradoDAO();

    private PanelFiltros filtros = new PanelFiltros(true, false, false);
    private JTextField txtBuscar = new JTextField(14);
    private JCheckBox chkRetirados = new JCheckBox("Incluir retirados");
    private DefaultTableModel modelo;
    private JTable tabla;
    private JLabel lblTotal = new JLabel(" ");
    private List<Estudiante> lista = new ArrayList<>();

    public PanelEstudiantes(Usuario usuario) {
        this.usuario = usuario;
        setLayout(new BorderLayout(8, 8));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // parte de arriba: filtros y busqueda
        JPanel arriba = new JPanel(new BorderLayout());
        arriba.add(filtros, BorderLayout.NORTH);

        JPanel busqueda = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        JButton btnBuscar = new JButton("Buscar");
        JButton btnNuevo = new JButton("Nuevo estudiante");
        JButton btnEditar = new JButton("Modificar");
        JButton btnBaja = new JButton("Dar de baja / Reactivar");
        busqueda.add(new JLabel("Código o nombre:"));
        busqueda.add(txtBuscar);
        busqueda.add(chkRetirados);
        busqueda.add(btnBuscar);
        busqueda.add(btnNuevo);
        busqueda.add(btnEditar);
        busqueda.add(btnBaja);
        arriba.add(busqueda, BorderLayout.SOUTH);
        add(arriba, BorderLayout.NORTH);

        modelo = Tablas.modeloNoEditable(new String[] {
            "Código", "Nombres", "Apellidos", "Sexo", "Grado", "Sección", "Modalidad", "Estado" });
        tabla = Tablas.crear(modelo);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        add(lblTotal, BorderLayout.SOUTH);

        btnBuscar.addActionListener(e -> buscar());
        txtBuscar.addActionListener(e -> buscar());
        btnNuevo.addActionListener(e -> nuevo());
        btnEditar.addActionListener(e -> modificar());
        btnBaja.addActionListener(e -> cambiarEstado());

        buscar();
    }

    // Llena la tabla segun los filtros
    private void buscar() {
        try {
            lista = estudianteDAO.listar(filtros.getAnio(), filtros.getGrado().getId(),
                    filtros.getSeccion() == null ? 0 : filtros.getSeccion().getId(),
                    txtBuscar.getText(), chkRetirados.isSelected());

            modelo.setRowCount(0);
            for (Estudiante e : lista) {
                modelo.addRow(new Object[] {
                    e.getCodigo(), e.getNombres(), e.getApellidos(),
                    "F".equals(e.getSexo()) ? "Femenino" : "Masculino",
                    e.getNombreGrado(), e.getNombreSeccion(), e.getModalidad(),
                    e.estaRetirado() ? "Retirado" : "Activo" });
            }
            lblTotal.setText("Estudiantes encontrados: " + lista.size());
        } catch (SQLException ex) {
            Mensajes.errorBD(this, ex);
        }
    }

    private Estudiante estudianteSeleccionado() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            Mensajes.advertencia(this, "Seleccione un estudiante de la tabla.");
            return null;
        }
        return lista.get(fila);
    }

    // Registra estudiantes nuevos. Despues de guardar uno pregunta si se quiere
    // registrar otro en el mismo grupo (para crear una seccion completa rapido).
    private void nuevo() {
        int idGrado = (filtros.getGrado() != null) ? filtros.getGrado().getId() : 0;
        int idSeccion = (filtros.getSeccion() != null) ? filtros.getSeccion().getId() : 0;

        boolean registrarOtro = true;
        while (registrarOtro) {
            FormularioEstudiante formulario = new FormularioEstudiante(null);
            if (idGrado > 0) {
                formulario.seleccionarGrupo(idGrado, idSeccion);
            }

            boolean guardado = false;
            while (!guardado) {
                int r = JOptionPane.showConfirmDialog(this, formulario, "Nuevo estudiante (año " + filtros.getAnio() + ")",
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                if (r != JOptionPane.OK_OPTION) {
                    return;
                }

                String error = formulario.validar(0);
                if (error != null) {
                    Mensajes.advertencia(this, error);
                    continue; // se vuelve a mostrar con lo que ya escribio
                }

                try {
                    Estudiante e = formulario.construir(new Estudiante());
                    e.setIdUsuario(usuario.getIdUsuario());
                    estudianteDAO.registrar(e, formulario.getGrado().getId(), formulario.getSeccion().getId(),
                            filtros.getAnio(), formulario.getModalidad());
                    buscar();
                    guardado = true;
                } catch (SQLException ex) {
                    Mensajes.errorBD(this, ex);
                    return;
                }
            }

            // el siguiente estudiante se registra en el mismo grupo
            idGrado = formulario.getGrado().getId();
            idSeccion = formulario.getSeccion().getId();
            registrarOtro = Mensajes.confirmar(this,
                    "Estudiante registrado correctamente.\n¿Desea registrar otro estudiante en el mismo grupo?");
        }
    }

    private void modificar() {
        Estudiante seleccionado = estudianteSeleccionado();
        if (seleccionado == null) {
            return;
        }

        FormularioEstudiante formulario = new FormularioEstudiante(seleccionado);

        while (true) {
            int r = JOptionPane.showConfirmDialog(this, formulario, "Modificar estudiante",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (r != JOptionPane.OK_OPTION) {
                return;
            }

            String error = formulario.validar(seleccionado.getId());
            if (error != null) {
                Mensajes.advertencia(this, error);
                continue;
            }

            try {
                Estudiante e = formulario.construir(seleccionado);
                estudianteDAO.actualizar(e, formulario.getGrado().getId(), formulario.getSeccion().getId(),
                        formulario.getModalidad());
                buscar();
                Mensajes.info(this, "Datos actualizados.");
            } catch (SQLException ex) {
                Mensajes.errorBD(this, ex);
            }
            return;
        }
    }

    private void cambiarEstado() {
        Estudiante e = estudianteSeleccionado();
        if (e == null) {
            return;
        }

        try {
            if (e.estaRetirado()) {
                if (Mensajes.confirmar(this, "¿Reactivar la matrícula de " + e.getNombreCompleto() + "?")) {
                    estudianteDAO.cambiarEstado(e.getIdMatricula(), "ACTIVA");
                    buscar();
                }
            } else {
                if (Mensajes.confirmar(this, "¿Dar de baja a " + e.getNombreCompleto() + "?\n"
                        + "Sus notas se conservan y se puede reactivar después.")) {
                    estudianteDAO.cambiarEstado(e.getIdMatricula(), "RETIRADA");
                    buscar();
                }
            }
        } catch (SQLException ex) {
            Mensajes.errorBD(this, ex);
        }
    }

    // ---------------------------------------------------------------
    // Formulario para escribir los datos de un estudiante
    // ---------------------------------------------------------------
    private class FormularioEstudiante extends JPanel {

        private JTextField txtCodigo = new JTextField(18);
        private JTextField txtNombres = new JTextField(18);
        private JTextField txtApellidos = new JTextField(18);
        private JComboBox<String> cmbSexo = new JComboBox<>(new String[] { "Femenino", "Masculino" });
        private JTextField txtFecha = new JTextField(18);
        private JTextField txtDireccion = new JTextField(18);
        private JTextField txtTelefono = new JTextField(18);
        private JComboBox<Grado> cmbGrado = new JComboBox<>();
        private JComboBox<Seccion> cmbSeccion = new JComboBox<>();
        private JComboBox<String> cmbModalidad = new JComboBox<>(new String[] { "Regular", "Sabatina", "Dominical" });

        FormularioEstudiante(Estudiante existente) {
            setLayout(new GridLayout(0, 2, 6, 6));

            add(new JLabel("Código:"));
            add(txtCodigo);
            add(new JLabel("Nombres:"));
            add(txtNombres);
            add(new JLabel("Apellidos:"));
            add(txtApellidos);
            add(new JLabel("Sexo:"));
            add(cmbSexo);
            add(new JLabel("Fecha de nacimiento (aaaa-mm-dd):"));
            add(txtFecha);
            add(new JLabel("Dirección:"));
            add(txtDireccion);
            add(new JLabel("Teléfono:"));
            add(txtTelefono);
            add(new JLabel("Grado:"));
            add(cmbGrado);
            add(new JLabel("Sección:"));
            add(cmbSeccion);
            add(new JLabel("Modalidad:"));
            add(cmbModalidad);

            try {
                for (Grado g : gradoDAO.listarGrados()) {
                    cmbGrado.addItem(g);
                }
            } catch (SQLException ex) {
                Mensajes.errorBD(this, ex);
            }
            cargarSecciones();
            cmbGrado.addActionListener(e -> cargarSecciones());

            if (existente != null) {
                txtCodigo.setText(existente.getCodigo());
                txtNombres.setText(existente.getNombres());
                txtApellidos.setText(existente.getApellidos());
                cmbSexo.setSelectedIndex("F".equals(existente.getSexo()) ? 0 : 1);
                if (existente.getFechaNacimiento() != null) {
                    txtFecha.setText(existente.getFechaNacimiento().toString());
                }
                txtDireccion.setText(existente.getDireccion());
                txtTelefono.setText(existente.getTelefono());
                cmbModalidad.setSelectedItem(existente.getModalidad());
                seleccionarGrupo(existente.getIdGrado(), existente.getIdSeccion());
            }
        }

        private void cargarSecciones() {
            cmbSeccion.removeAllItems();
            Grado grado = (Grado) cmbGrado.getSelectedItem();
            if (grado == null) {
                return;
            }
            try {
                for (Seccion s : gradoDAO.listarSecciones(grado.getId())) {
                    cmbSeccion.addItem(s);
                }
            } catch (SQLException ex) {
                Mensajes.errorBD(this, ex);
            }
        }

        // Deja elegido un grado y (si se indica) una seccion
        void seleccionarGrupo(int idGrado, int idSeccion) {
            for (int i = 0; i < cmbGrado.getItemCount(); i++) {
                if (cmbGrado.getItemAt(i).getId() == idGrado) {
                    cmbGrado.setSelectedIndex(i);
                }
            }
            for (int i = 0; i < cmbSeccion.getItemCount(); i++) {
                if (cmbSeccion.getItemAt(i).getId() == idSeccion) {
                    cmbSeccion.setSelectedIndex(i);
                }
            }
        }

        Grado getGrado() {
            return (Grado) cmbGrado.getSelectedItem();
        }

        Seccion getSeccion() {
            return (Seccion) cmbSeccion.getSelectedItem();
        }

        String getModalidad() {
            return (String) cmbModalidad.getSelectedItem();
        }

        // Revisa los datos. Devuelve el mensaje de error, o null si todo esta bien.
        // idExcluir = id del estudiante que se esta modificando (0 si es nuevo).
        String validar(int idExcluir) {
            String codigo = txtCodigo.getText().trim();
            if (codigo.isEmpty() || codigo.length() > 20) {
                return "Escriba el código del estudiante (máximo 20 caracteres).";
            }
            if (txtNombres.getText().trim().isEmpty() || txtApellidos.getText().trim().isEmpty()) {
                return "Los nombres y los apellidos son obligatorios.";
            }

            String fecha = txtFecha.getText().trim();
            if (!fecha.isEmpty()) {
                try {
                    LocalDate nacimiento = LocalDate.parse(fecha);
                    if (!nacimiento.isBefore(LocalDate.now())) {
                        return "La fecha de nacimiento debe ser anterior a hoy.";
                    }
                } catch (DateTimeParseException ex) {
                    return "La fecha debe tener el formato aaaa-mm-dd (ejemplo: 2013-05-27).";
                }
            }

            String telefono = txtTelefono.getText().trim();
            if (!telefono.isEmpty() && !telefono.matches("[0-9+\\- ]{6,20}")) {
                return "El teléfono solo puede tener números, espacios, + y guiones (entre 6 y 20 caracteres).";
            }

            if (getGrado() == null || getSeccion() == null) {
                return "Elija el grado y la sección del estudiante.";
            }

            try {
                if (estudianteDAO.existeCodigo(codigo, idExcluir)) {
                    return "Ya existe un estudiante con el código " + codigo + ".";
                }
            } catch (SQLException ex) {
                return "No se pudo comprobar el código: " + ex.getMessage();
            }
            return null;
        }

        // Pasa lo escrito en el formulario al objeto Estudiante
        Estudiante construir(Estudiante e) {
            e.setCodigo(txtCodigo.getText().trim());
            e.setNombres(txtNombres.getText().trim());
            e.setApellidos(txtApellidos.getText().trim());
            e.setSexo(cmbSexo.getSelectedIndex() == 0 ? "F" : "M");

            String fecha = txtFecha.getText().trim();
            e.setFechaNacimiento(fecha.isEmpty() ? null : LocalDate.parse(fecha));

            e.setDireccion(txtDireccion.getText().trim().isEmpty() ? null : txtDireccion.getText().trim());
            e.setTelefono(txtTelefono.getText().trim().isEmpty() ? null : txtTelefono.getText().trim());
            return e;
        }
    }
}
