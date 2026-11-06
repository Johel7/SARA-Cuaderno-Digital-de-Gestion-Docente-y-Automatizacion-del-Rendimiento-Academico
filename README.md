# SARA - Cuaderno Digital de Gestión Docente y Automatización del Rendimiento Académico

Este proyecto está desarrollado bajo Eclipse IDE y utiliza Maven para la gestión de dependencias y MySQL Workbench para la base de datos. Está configurado para ser ejecutado automáticamente con un solo clic.

### Requisitos previos:
1. Tener **MySQL Workbench** activo y corriendo localmente en el puerto 3306.
2. Tener configuradas las variables de entorno de **Java** y **Maven** en el sistema operativo.

### Cómo ejecutar (Un solo clic):
1. Descarga el repositorio como un archivo ZIP y descomprímelo.
2. Haz doble clic en el archivo **`ejecutar.bat`** que se encuentra en la carpeta raíz.
3. Introduce tu usuario y contraseña de MySQL Workbench cuando el script te lo solicite. El asistente creará la base de datos de manera automática, importará las tablas desde el archivo `schema.sql` e iniciará la aplicación Java de inmediato sin que debas configurar nada dentro de Eclipse.
