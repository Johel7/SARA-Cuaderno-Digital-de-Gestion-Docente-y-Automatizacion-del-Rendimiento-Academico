package sara.modelo;

/*
 * Clase Usuario
 * Representa a un usuario del sistema (docente o administrador).
 * No guarda la clave: solo los datos que se necesitan despues del login.
 */
public class Usuario {

    public static final String ROL_ADMINISTRADOR = "ADMINISTRADOR";
    public static final String ROL_DOCENTE = "DOCENTE";

    private int idUsuario;
    private String nombres;
    private String apellidos;
    private String usuario;
    private String rol;
    private boolean activo = true;

    public Usuario(int idUsuario, String nombres, String apellidos, String usuario, String rol) {
        this.idUsuario = idUsuario;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.usuario = usuario;
        this.rol = rol;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public String getNombres() {
        return nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public String getUsuario() {
        return usuario;
    }

    public String getRol() {
        return rol;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public String getNombreCompleto() {
        return nombres + " " + apellidos;
    }

    public boolean esAdministrador() {
        return ROL_ADMINISTRADOR.equals(rol);
    }
}
