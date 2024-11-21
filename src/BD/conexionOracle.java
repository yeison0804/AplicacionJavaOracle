/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package BD;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author YEISON ANDRES MARIN
 */
public class conexionOracle {

    private Connection conn = null;
    private String url, user, pass;

    public conexionOracle() {
        conectar();

    }

    public Connection getConn() {
        return conn;
    }

    public String getUrl() {
        return url;
    }

    public String getUser() {
        return user;
    }

    public String getPass() {
        return pass;
    }

    public void conectar() {

        try {

            Class.forName("oracle.jdbc.OracleDriver"); // driver BD
            url = "jdbc:oracle:thin:@localhost:1522:XE";
            user = "SYSTEM";
            pass = "Marin0804";
            conn = DriverManager.getConnection(url, user, pass);
            System.out.println("Conectado");
        } catch (Exception e) {
            System.out.println("Error no se pudo conectar");
        }

    }

    public void desconectar() {

        try {
            conn.close();
            System.out.println("Desconectado");
        } catch (Exception e) {
            System.out.println("Error, no se pudo desconectar");
        }

    }

    public void registrarAfiliacion(int idVendedor, int idAfiliado) {
        CallableStatement cs = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            if (conn == null || conn.isClosed()) {
                throw new SQLException("Conexión con la base de datos no está activa.");
            }

            // Validar si el vendedor existe
            String consultaVendedor = "SELECT COUNT(*) FROM vendedores WHERE id_vendedor = ?";
            ps = conn.prepareStatement(consultaVendedor);
            ps.setInt(1, idVendedor);
            rs = ps.executeQuery();
            if (rs.next() && rs.getInt(1) == 0) {
                System.err.println("Error: El vendedor con ID " + idVendedor + " no existe.");
                return; // Finaliza la ejecución
            }

            // Validar si el afiliado existe
            String consultaAfiliado = "SELECT COUNT(*) FROM afiliados WHERE id_afiliado = ?";
            ps = conn.prepareStatement(consultaAfiliado);
            ps.setInt(1, idAfiliado);
            rs = ps.executeQuery();
            if (rs.next() && rs.getInt(1) == 0) {
                System.err.println("Error: El afiliado con ID " + idAfiliado + " no existe.");
                return; // Finaliza la ejecución
            }

            // Preparar la llamada al procedimiento almacenado
            String procedimiento = "{call PKG_Multinivel.registrar_afiliacion(?, ?)}";
            cs = conn.prepareCall(procedimiento);

            // Configurar los parámetros
            cs.setInt(1, idVendedor);
            cs.setInt(2, idAfiliado);

            // Ejecutar el procedimiento almacenado
            cs.execute();
            System.out.println("Afiliación registrada correctamente.");

            // Confirmar la transacción si no es autocommit
            if (!conn.getAutoCommit()) {
                conn.commit();
                System.out.println("Transacción confirmada.");
            }
        } catch (SQLException e) {
            System.err.println("SQL Error: Código=" + e.getErrorCode() + ", SQL State=" + e.getSQLState());
            System.err.println("Mensaje: " + e.getMessage());
        } finally {
            // Liberar recursos
            try {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
                if (cs != null) {
                    cs.close();
                }
            } catch (SQLException e) {
                System.err.println("Error al cerrar recursos: " + e.getMessage());
            }
        }
    }

    //login del vendedor llamado a la funcion
    public int loginVendedor(String nombreUsuario, String contraseña) {
        CallableStatement cs = null;
        int resultado = -1; // Inicializamos con un valor que indique error o no autenticado

        try {
            // Verificar la conexión antes de proceder
            if (conn == null || conn.isClosed()) {
                throw new SQLException("Conexión con la base de datos no está activa.");
            }

            // Preparar la llamada a la función
            String funcion = "{? = call PKG_Multinivel.login_vendedor(?, ?)}";
            cs = conn.prepareCall(funcion);

            // Configurar los parámetros
            cs.registerOutParameter(1, java.sql.Types.NUMERIC); // Parámetro de retorno
            cs.setString(2, nombreUsuario.trim());              // Parámetro 1: nombre_usuario (sin espacios adicionales)
            cs.setString(3, contraseña.trim());                 // Parámetro 2: contraseña (sin espacios adicionales)

            System.out.println("Ejecutando función con usuario: " + nombreUsuario);

            // Ejecutar la función
            cs.execute();

            // Obtener el resultado
            resultado = cs.getInt(1);

            // Verificar el resultado y registrar información relevante
            if (resultado == -1) {
                System.out.println("Login fallido: usuario o contraseña incorrectos.");
            } else {
                System.out.println("Login exitoso. ID del vendedor: " + resultado);
            }

        } catch (SQLException e) {
            System.err.println("Error al realizar el login: Código=" + e.getErrorCode() + ", Estado SQL=" + e.getSQLState());
            System.err.println("Mensaje: " + e.getMessage());
        } finally {
            // Cerrar el CallableStatement
            try {
                if (cs != null) {
                    cs.close();
                }
            } catch (SQLException e) {
                System.err.println("Error al cerrar CallableStatement: " + e.getMessage());
            }
        }

        return resultado;
    }

    public void registrarVendedor(String nombre, java.util.Date fechaIngreso, int nivel, String estado,
            String email, String telefono, String nombreUsuario, String contraseña) {
        CallableStatement cs = null;

        try {
            // Verificar la conexión antes de proceder
            if (conn == null || conn.isClosed()) {
                throw new SQLException("Conexión con la base de datos no está activa.");
            }

            // Preparar la llamada al procedimiento
            String procedimiento = "{call PKG_Multinivel.registrar_vendedor(?, ?, ?, ?, ?, ?, ?, ?)}";
            cs = conn.prepareCall(procedimiento);

            // Configurar los parámetros
            cs.setString(1, nombre);                               // p_nombre
            cs.setDate(2, new java.sql.Date(fechaIngreso.getTime())); // p_fecha_ingreso
            cs.setInt(3, nivel);                                   // p_nivel
            cs.setString(4, estado);                               // p_estado
            cs.setString(5, email);                                // p_email
            cs.setString(6, telefono);                             // p_telefono
            cs.setString(7, nombreUsuario);                        // p_nombre_usuario
            cs.setString(8, contraseña);                           // p_contraseña

            // Ejecutar el procedimiento
            cs.execute();
            System.out.println("Vendedor registrado correctamente.");

        } catch (SQLException e) {
            System.err.println("Error al registrar el vendedor: Código=" + e.getErrorCode() + ", Estado SQL=" + e.getSQLState());
            System.err.println("Mensaje: " + e.getMessage());
        } finally {
            // Cerrar el CallableStatement
            try {
                if (cs != null) {
                    cs.close();
                }
            } catch (SQLException e) {
                System.err.println("Error al cerrar CallableStatement: " + e.getMessage());
            }
        }
    }

    public boolean verificarVendedorExistente(int idVendedor) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            // Verificar la conexión antes de proceder
            if (conn == null || conn.isClosed()) {
                throw new SQLException("Conexión con la base de datos no está activa.");
            }

            // Consulta para verificar si el vendedor existe
            String sql = "SELECT COUNT(*) FROM vendedores WHERE id_vendedor = ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, idVendedor);

            rs = ps.executeQuery();
            if (rs.next()) {
                int count = rs.getInt(1);
                return count > 0; // Si count es mayor a 0, el vendedor existe
            }

        } catch (SQLException e) {
            System.err.println("Error al verificar el vendedor: Código=" + e.getErrorCode() + ", Estado SQL=" + e.getSQLState());
            System.err.println("Mensaje: " + e.getMessage());
        } finally {
            // Cerrar ResultSet y PreparedStatement
            try {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
            } catch (SQLException e) {
                System.err.println("Error al cerrar recursos: " + e.getMessage());
            }
        }
        return false; // Si no se encuentra el vendedor, devuelve false
    }

    public void actualizarVendedor(int idVendedor, String nombre, String estado, String email, String telefono) {
        if (!verificarVendedorExistente(idVendedor)) {
            System.out.println("El vendedor con ID " + idVendedor + " no existe.");
            return; // Detener la ejecución si el vendedor no existe
        }

        CallableStatement cs = null;

        try {
            // Verificar la conexión antes de proceder
            if (conn == null || conn.isClosed()) {
                throw new SQLException("Conexión con la base de datos no está activa.");
            }

            // Preparar la llamada al procedimiento
            String procedimiento = "{call PKG_Multinivel.actualizar_vendedor(?, ?, ?, ?, ?)}";
            cs = conn.prepareCall(procedimiento);

            // Configurar los parámetros
            cs.setInt(1, idVendedor); // p_id_vendedor
            cs.setString(2, nombre);  // p_nombre
            cs.setString(3, estado);  // p_estado
            cs.setString(4, email);   // p_email
            cs.setString(5, telefono);// p_telefono

            // Ejecutar el procedimiento
            cs.execute();
            System.out.println("Vendedor actualizado correctamente.");

        } catch (SQLException e) {
            System.err.println("Error al actualizar el vendedor: Código=" + e.getErrorCode() + ", Estado SQL=" + e.getSQLState());
            System.err.println("Mensaje: " + e.getMessage());
        } finally {
            // Cerrar el CallableStatement
            try {
                if (cs != null) {
                    cs.close();
                }
            } catch (SQLException e) {
                System.err.println("Error al cerrar CallableStatement: " + e.getMessage());
            }
        }
    }

    public void registrarAfiliado(int idVendedor, String nombreAfiliado) {

        CallableStatement cs = null;

        try {
            // Verificar la conexión antes de proceder
            if (conn == null || conn.isClosed()) {
                throw new SQLException("Conexión con la base de datos no está activa.");
            }

            // Preparar la llamada al procedimiento almacenado
            String procedimiento = "{call PKG_Multinivel.registrar_afiliado(?, ?)}";
            cs = conn.prepareCall(procedimiento);

            // Configurar los parámetros
            cs.setInt(1, idVendedor);       // p_id_vendedor
            cs.setString(2, nombreAfiliado); // p_nombre_afiliado

            // Ejecutar el procedimiento
            cs.execute();
            System.out.println("Afiliado registrado correctamente.");

        } catch (SQLException e) {
            // Manejo detallado de errores
            System.err.println("Error al registrar el afiliado: Código=" + e.getErrorCode() + ", Estado SQL=" + e.getSQLState());
            System.err.println("Mensaje: " + e.getMessage());

            // Opcional: muestra el error en un cuadro de diálogo en caso de usar GUI
            javax.swing.JOptionPane.showMessageDialog(null,
                    "Error al registrar el afiliado: " + e.getMessage(),
                    "Error", javax.swing.JOptionPane.ERROR_MESSAGE);

        } finally {
            // Cerrar el CallableStatement
            try {
                if (cs != null) {
                    cs.close();
                }
            } catch (SQLException e) {
                System.err.println("Error al cerrar CallableStatement: " + e.getMessage());
            }
        }
    }

    public static void cargarAfiliadosEnTabla(JTable tablaAfiliados) {
        DefaultTableModel modelo = (DefaultTableModel) tablaAfiliados.getModel();
        modelo.setRowCount(0); // Limpiar la tabla

        conexionOracle conexion = new conexionOracle();
        conexion.conectar();

        try {
            String query = "SELECT id_afiliado, id_vendedor, nombre FROM afiliados"; // Ajusta los nombres de columnas según tu tabla
            Statement stmt = conexion.getConn().createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                Object[] fila = {
                    rs.getInt("id_afiliado"),
                    rs.getInt("id_vendedor"),
                    rs.getString("nombre")
                };
                modelo.addRow(fila);
            }

            rs.close();
            stmt.close();
        } catch (SQLException e) {
            javax.swing.JOptionPane.showMessageDialog(null, "Error al cargar afiliados: " + e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        } finally {
            conexion.desconectar();
        }

    }

    public void registrarAfiliacionVendedor(int idVendedor, int idAfiliado) {

        CallableStatement cs = null;

        try {
            // Verificar la conexión antes de proceder
            if (conn == null || conn.isClosed()) {
                throw new SQLException("Conexión con la base de datos no está activa.");
            }

            // Preparar la llamada al procedimiento almacenado
            String procedimiento = "{call PKG_Multinivel.registrar_afiliacion(?, ?)}";
            cs = conn.prepareCall(procedimiento);

            // Configurar los parámetros
            cs.setInt(1, idVendedor); // Parámetro 1: p_id_vendedor
            cs.setInt(2, idAfiliado); // Parámetro 2: p_id_afiliado

            // Ejecutar el procedimiento
            cs.execute();
            System.out.println("Afiliación registrada correctamente.");

        } catch (SQLException e) {
            // Manejo detallado de errores
            System.err.println("Error al registrar la afiliación: Código=" + e.getErrorCode() + ", Estado SQL=" + e.getSQLState());
            System.err.println("Mensaje: " + e.getMessage());

            // Opcional: muestra el error en un cuadro de diálogo en caso de usar GUI
            javax.swing.JOptionPane.showMessageDialog(null,
                    "Error al registrar la afiliación: " + e.getMessage(),
                    "Error", javax.swing.JOptionPane.ERROR_MESSAGE);

        } finally {
            // Cerrar el CallableStatement
            try {
                if (cs != null) {
                    cs.close();
                }
            } catch (SQLException e) {
                System.err.println("Error al cerrar CallableStatement: " + e.getMessage());
            }
        }
    }

    public static void cargarAfiliacionesEnTabla(JTable tablaAfiliaciones) {
        // Obtener el modelo de la tabla
        DefaultTableModel modelo = (DefaultTableModel) tablaAfiliaciones.getModel();
        modelo.setRowCount(0); // Limpiar los datos actuales de la tabla

        // Conectar a la base de datos
        conexionOracle conexion = new conexionOracle();
        conexion.conectar();

        try {
            // Consulta para obtener las afiliaciones
            String query = "SELECT id_afiliacion, id_vendedor, id_afiliado FROM afiliaciones"; // Ajusta el nombre de la tabla si es necesario
            Statement stmt = conexion.getConn().createStatement();
            ResultSet rs = stmt.executeQuery(query);

            // Recorrer los resultados y agregarlos al modelo de la tabla
            while (rs.next()) {
                Object[] fila = {
                    rs.getInt("id_afiliacion"), // ID de la afiliación
                    rs.getInt("id_vendedor"), // ID del vendedor
                    rs.getInt("id_afiliado") // ID del afiliado
                };
                modelo.addRow(fila); // Agregar fila al modelo
            }

            // Cerrar ResultSet y Statement
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            // Manejo de errores en la carga de datos
            javax.swing.JOptionPane.showMessageDialog(null,
                    "Error al cargar las afiliaciones: " + e.getMessage(),
                    "Error",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        } finally {
            // Desconectar la conexión
            conexion.desconectar();
        }
    }

    public boolean validarCredenciales(String nombreUsuario, String contraseña) throws SQLException {
        String query = "SELECT COUNT(*) AS total FROM vendedores WHERE nombre_usuario = ? AND contraseña = ?";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setString(1, nombreUsuario);
        stmt.setString(2, contraseña);

        ResultSet rs = stmt.executeQuery();
        rs.next(); // Moverse al primer resultado
        int total = rs.getInt("total");

        rs.close();
        stmt.close();

        return total > 0; // Devuelve true si hay coincidencias
    }

    public void eliminarAfiliado(int idAfiliacion) {
        CallableStatement cs = null;
        conexionOracle conexion = new conexionOracle();
        conexion.conectar(); // Asegúrate de que este método establece la conexión

        try {
            // Verificar que la conexión está activa
            if (conexion.getConn() == null || conexion.getConn().isClosed()) {
                throw new SQLException("Conexión con la base de datos no está activa.");
            }

            // Llamada al procedimiento almacenado
            String procedimiento = "{call PKG_Multinivel.eliminar_afiliado(?)}"; // Ajusta el nombre del paquete y procedimiento
            cs = conexion.getConn().prepareCall(procedimiento);

            // Configurar los parámetros
            cs.setInt(1, idAfiliacion); // Parámetro para el ID de la afiliación

            // Ejecutar el procedimiento
            cs.execute();
            System.out.println("Afiliado eliminado correctamente.");

        } catch (SQLException e) {
            // Manejo de errores
            System.err.println("Error al eliminar afiliado: Código=" + e.getErrorCode() + ", Estado SQL=" + e.getSQLState());
            System.err.println("Mensaje: " + e.getMessage());
        } finally {
            try {
                if (cs != null) {
                    cs.close(); // Cerrar el CallableStatement
                }
                conexion.desconectar(); // Cerrar la conexión
            } catch (SQLException e) {
                System.err.println("Error al cerrar recursos: " + e.getMessage());
            }
        }
    }

    public String registrarProducto(int idProducto, String nombre, double precio, int stock) {
        CallableStatement cs = null;
        String resultado = null;

        try {
            // Verificar la conexión antes de proceder
            if (conn == null || conn.isClosed()) {
                throw new SQLException("Conexión con la base de datos no está activa.");
            }

            // Preparar la llamada a la función almacenada
            String funcion = "{? = call PKG_Multinivel.registrar_producto(?, ?, ?, ?)}";
            cs = conn.prepareCall(funcion);

            // Registrar el parámetro de salida (el valor de retorno de la función)
            cs.registerOutParameter(1, Types.VARCHAR);

            // Configurar los parámetros de entrada
            cs.setInt(2, idProducto);    // Parámetro 1: p_id_producto
            cs.setString(3, nombre);     // Parámetro 2: p_nombre
            cs.setDouble(4, precio);     // Parámetro 3: p_precio
            cs.setInt(5, stock);         // Parámetro 4: p_stock

            // Ejecutar la función
            cs.execute();

            // Obtener el resultado de la función
            resultado = cs.getString(1);
            System.out.println("Resultado: " + resultado);

        } catch (SQLException e) {
            // Manejo detallado de errores
            System.err.println("Error al registrar el producto: Código=" + e.getErrorCode() + ", Estado SQL=" + e.getSQLState());
            System.err.println("Mensaje: " + e.getMessage());

            // Opcional: muestra el error en un cuadro de diálogo en caso de usar GUI
            javax.swing.JOptionPane.showMessageDialog(null,
                    "Error al registrar el producto: " + e.getMessage(),
                    "Error", javax.swing.JOptionPane.ERROR_MESSAGE);

        } finally {
            // Cerrar el CallableStatement
            try {
                if (cs != null) {
                    cs.close();
                }
            } catch (SQLException e) {
                System.err.println("Error al cerrar CallableStatement: " + e.getMessage());
            }
        }

        return resultado;  // Devuelve el resultado de la función
    }

    public ArrayList<String[]> listarProductos() {
        ArrayList<String[]> productos = new ArrayList<>();
        CallableStatement cs = null;
        ResultSet rs = null;

        try {
            // Verificar la conexión antes de proceder
            if (conn == null || conn.isClosed()) {
                throw new SQLException("Conexión con la base de datos no está activa.");
            }

            // Preparar la llamada al procedimiento almacenado
            String procedimiento = "{call PKG_Multinivel.listar_productos}"; // Asegúrate de que el procedimiento esté bien definido
            cs = conn.prepareCall(procedimiento);
            rs = cs.executeQuery();

            // Obtener los datos de los productos y agregarlos a la lista
            while (rs.next()) {
                String[] producto = new String[4]; // Suponiendo que tienes 4 columnas: ID, Nombre, Precio, Stock
                producto[0] = rs.getString("ID_PRODUCTO");
                producto[1] = rs.getString("NOMBRE");
                producto[2] = rs.getString("PRECIO");
                producto[3] = rs.getString("STOCK");
                productos.add(producto);
            }

        } catch (SQLException e) {
            System.err.println("Error al listar productos: " + e.getMessage());
        } finally {
            // Cerrar recursos
            try {
                if (rs != null) {
                    rs.close();
                }
                if (cs != null) {
                    cs.close();
                }
            } catch (SQLException e) {
                System.err.println("Error al cerrar recursos: " + e.getMessage());
            }
        }

        return productos;
    }

    public static void cargarProductosEnTabla(JTable tablaProductos) {
        // Obtener el modelo de la tabla
        DefaultTableModel modelo = (DefaultTableModel) tablaProductos.getModel();
        modelo.setRowCount(0); // Limpiar los datos actuales de la tabla

        // Conectar a la base de datos
        conexionOracle conexion = new conexionOracle();
        conexion.conectar();

        try {
            // Consulta para obtener los productos
            String query = "SELECT id_producto, nombre_producto, precio, stock FROM productos"; // No es necesario usar comillas dobles si los nombres están en minúsculas
            PreparedStatement stmt = conexion.getConn().prepareStatement(query);
            ResultSet rs = stmt.executeQuery(); // Ejecutamos la consulta

            // Recorrer los resultados y agregarlos al modelo de la tabla
            while (rs.next()) {
                Object[] fila = {
                    rs.getInt("id_producto"), // ID del producto
                    rs.getString("nombre_producto"), // Nombre del producto
                    rs.getDouble("precio"), // Precio del producto
                    rs.getInt("stock") // Stock del producto
                };
                modelo.addRow(fila); // Agregar fila al modelo
            }

            // Cerrar ResultSet y PreparedStatement
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            // Manejo de errores en la carga de datos
            javax.swing.JOptionPane.showMessageDialog(null,
                    "Error al cargar los productos: " + e.getMessage(),
                    "Error",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        } finally {
            // Desconectar la conexión
            conexion.desconectar();
        }
    }

    public static int verificarDisponibilidad(int idProducto) {
        int disponibilidad = 0; // Valor por defecto en caso de error

        // Conectar a la base de datos
        conexionOracle conexion = new conexionOracle();
        conexion.conectar();

        CallableStatement cs = null;
        try {
            // Preparar la llamada a la función almacenada
            String query = "{? = call PKG_Multinivel.verificar_disponibilidad(?)}"; // La sintaxis para llamar a una función
            cs = conexion.getConn().prepareCall(query);

            // Registrar los parámetros de entrada y salida
            cs.registerOutParameter(1, java.sql.Types.INTEGER); // Parámetro de salida (tipo número)
            cs.setInt(2, idProducto); // Parámetro de entrada (ID del producto)

            // Ejecutar la función
            cs.execute();

            // Obtener el valor de salida
            disponibilidad = cs.getInt(1); // Recibir el valor de disponibilidad

        } catch (SQLException e) {
            // Manejo de errores
            javax.swing.JOptionPane.showMessageDialog(null,
                    "Error al verificar la disponibilidad: " + e.getMessage(),
                    "Error",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        } finally {
            // Cerrar recursos
            try {
                if (cs != null) {
                    cs.close();
                }
                conexion.desconectar();
            } catch (SQLException e) {
                System.err.println("Error al cerrar recursos: " + e.getMessage());
            }
        }

        return disponibilidad; // Retornar la disponibilidad (0 o 1, dependiendo de la lógica de la función)
    }

    public void agregarProductoInventario(int idProducto, int cantidad, String estado) throws SQLException {
        CallableStatement cs = null;

        try {
            // Preparar la llamada al procedimiento
            String query = "{call PKG_Multinivel.agregar_producto_inventario(?, ?, ?)}";
            cs = conn.prepareCall(query);

            // Configurar los parámetros
            cs.setInt(1, idProducto);       // Parámetro: p_id_producto
            cs.setInt(2, cantidad);         // Parámetro: p_cantidad
            if (estado == null || estado.isEmpty()) {
                cs.setNull(3, Types.VARCHAR); // Estado por defecto (si no se pasa)
            } else {
                cs.setString(3, estado);     // Parámetro: p_estado
            }

            // Ejecutar el procedimiento
            cs.execute();
            System.out.println("Producto agregado al inventario exitosamente.");
        } catch (SQLException e) {
            System.err.println("Error al agregar producto al inventario: " + e.getMessage());
            throw e;
        } finally {
            if (cs != null) {
                cs.close(); // Cerrar CallableStatement
            }
        }
    }

    public void registrarVenta(int idVendedor, int idProducto, int cantidad) throws SQLException {
        CallableStatement cs = null;

        try {
            // Preparar la llamada al procedimiento
            String query = "{call PKG_Multinivel.registrar_venta(?, ?, ?)}";
            cs = conn.prepareCall(query);

            // Configurar los parámetros
            cs.setInt(1, idVendedor);   // Parámetro: p_id_vendedor
            cs.setInt(2, idProducto);   // Parámetro: p_id_producto
            cs.setInt(3, cantidad);     // Parámetro: p_cantidad

            // Ejecutar el procedimiento
            cs.execute();
            System.out.println("Venta registrada exitosamente.");
        } catch (SQLException e) {
            System.err.println("Error al registrar la venta: " + e.getMessage());
            throw e; // Propagar el error
        } finally {
            if (cs != null) {
                cs.close(); // Cerrar CallableStatement
            }
        }
    }

    public static void cargarInventarioEnTabla(JTable tablaInventario) {
        // Obtener el modelo de la tabla
        DefaultTableModel modelo = (DefaultTableModel) tablaInventario.getModel();
        modelo.setRowCount(0); // Limpiar los datos actuales de la tabla

        // Conectar a la base de datos
        conexionOracle conexion = new conexionOracle();
        conexion.conectar();

        try {
            // Consulta SQL para obtener los datos del inventario
            String query = "SELECT id_inventario, id_producto, cantidad, estado, max_cantidad FROM inventario";
            Statement stmt = conexion.getConn().createStatement();
            ResultSet rs = stmt.executeQuery(query);

            // Recorrer los resultados y agregarlos al modelo de la tabla
            while (rs.next()) {
                Object[] fila = {
                    rs.getInt("id_inventario"), // ID del inventario
                    rs.getInt("id_producto"), // ID del producto
                    rs.getInt("cantidad"), // Cantidad disponible
                    rs.getString("estado"), // Estado del inventario
                    rs.getInt("max_cantidad") // Máxima cantidad permitida
                };
                modelo.addRow(fila); // Agregar fila al modelo
            }

            // Cerrar ResultSet y Statement
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            // Manejo de errores al cargar los datos
            javax.swing.JOptionPane.showMessageDialog(null,
                    "Error al cargar los datos del inventario: " + e.getMessage(),
                    "Error",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        } finally {
            // Desconectar la conexión
            conexion.desconectar();
        }
    }

    public static void cargarInventarioEnTabla1(JTable tablaInventario) {
        // Obtener el modelo de la tabla
        DefaultTableModel modelo = (DefaultTableModel) tablaInventario.getModel();
        modelo.setRowCount(0); // Limpiar los datos actuales de la tabla

        // Conectar a la base de datos
        conexionOracle conexion = new conexionOracle();
        conexion.conectar();

        try {
            // Consulta SQL para obtener los datos del inventario
            String query = "SELECT id_inventario, id_producto, cantidad, estado, max_cantidad FROM inventario";
            Statement stmt = conexion.getConn().createStatement();
            ResultSet rs = stmt.executeQuery(query);

            // Recorrer los resultados y agregarlos al modelo de la tabla
            while (rs.next()) {
                Object[] fila = {
                    rs.getInt("id_inventario"), // ID del inventario
                    rs.getInt("id_producto"), // ID del producto
                    rs.getInt("cantidad"), // Cantidad disponible
                    rs.getString("estado"), // Estado del inventario
                    rs.getInt("max_cantidad") // Máxima cantidad permitida
                };
                modelo.addRow(fila); // Agregar fila al modelo
            }

            // Cerrar ResultSet y Statement
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            // Manejo de errores al cargar los datos
            javax.swing.JOptionPane.showMessageDialog(null,
                    "Error al cargar los datos del inventario: " + e.getMessage(),
                    "Error",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        } finally {
            // Desconectar la conexión
            conexion.desconectar();
        }
    }

    public static void cargarVendedoresEnTabla(JTable tablaVendedores) {
        // Obtener el modelo de la tabla
        DefaultTableModel modelo = (DefaultTableModel) tablaVendedores.getModel();
        modelo.setRowCount(0); // Limpiar los datos actuales de la tabla

        // Conectar a la base de datos
        conexionOracle conexion = new conexionOracle();
        conexion.conectar();

        try {
            // Consulta SQL para obtener los datos de los vendedores
            String query = "SELECT id_vendedor, nombre, fecha_ingreso, nivel, estado, email, telefono, nombre_usuario, contraseña, es_vip, afiliados_reclutados, ganancias, descuentos FROM vendedores";
            Statement stmt = conexion.getConn().createStatement();
            ResultSet rs = stmt.executeQuery(query);

            // Recorrer los resultados y agregarlos al modelo de la tabla
            while (rs.next()) {
                Object[] fila = {
                    rs.getInt("id_vendedor"), // ID del vendedor
                    rs.getString("nombre"), // Nombre del vendedor
                    rs.getDate("fecha_ingreso"), // Fecha de ingreso
                    rs.getString("nivel"), // Nivel del vendedor
                    rs.getString("estado"), // Estado del vendedor
                    rs.getString("email"), // Email
                    rs.getString("telefono"), // Teléfono
                    rs.getString("nombre_usuario"), // Nombre de usuario
                    rs.getString("contraseña"), // Contraseña
                    rs.getString("es_vip"), // Si es VIP
                    rs.getInt("afiliados_reclutados"), // Afiliados reclutados
                    rs.getDouble("ganancias"), // Ganancias
                    rs.getDouble("descuentos") // Descuentos
                };
                modelo.addRow(fila); // Agregar fila al modelo
            }

            // Cerrar ResultSet y Statement
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            // Manejo de errores al cargar los datos
            javax.swing.JOptionPane.showMessageDialog(null,
                    "Error al cargar los datos de los vendedores: " + e.getMessage(),
                    "Error",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        } finally {
            // Desconectar la conexión
            conexion.desconectar();
        }
    }

    public static void cargarVentasEnTabla(JTable tablaVentas) {
        // Obtener el modelo de la tabla
        DefaultTableModel modelo = (DefaultTableModel) tablaVentas.getModel();
        modelo.setRowCount(0); // Limpiar los datos actuales de la tabla

        // Conectar a la base de datos
        conexionOracle conexion = new conexionOracle();
        conexion.conectar();

        try {
            // Consulta para obtener las ventas
            String query = "SELECT id_venta, id_vendedor, id_producto, cantidad, fecha_venta, total_venta, estado FROM ventas";
            Statement stmt = conexion.getConn().createStatement();
            ResultSet rs = stmt.executeQuery(query);

            // Recorrer los resultados y agregarlos al modelo de la tabla
            while (rs.next()) {
                Object[] fila = {
                    rs.getInt("id_venta"), // ID de la venta
                    rs.getInt("id_vendedor"), // ID del vendedor
                    rs.getInt("id_producto"), // ID del producto
                    rs.getInt("cantidad"), // Cantidad vendida
                    rs.getDate("fecha_venta"), // Fecha de la venta
                    rs.getDouble("total_venta"), // Total de la venta
                    rs.getString("estado") // Estado de la venta
                };
                modelo.addRow(fila); // Agregar fila al modelo
            }

            // Cerrar ResultSet y Statement
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            // Manejo de errores en la carga de datos
            javax.swing.JOptionPane.showMessageDialog(null,
                    "Error al cargar las ventas: " + e.getMessage(),
                    "Error",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        } finally {
            // Desconectar la conexión
            conexion.desconectar();
        }
    }

    public static void desvincularVendedor(int idVendedor) {
        conexionOracle conexion = new conexionOracle();
        conexion.conectar();

        CallableStatement cs = null;
        try {
            // Preparar el llamado al procedimiento almacenado
            String procedimiento = "{call PKG_Multinivel.desvincular_vendedor(?)}";
            cs = conexion.getConn().prepareCall(procedimiento);

            // Configurar el parámetro
            cs.setInt(1, idVendedor);

            // Ejecutar el procedimiento
            cs.execute();

            // Mensaje de éxito
            javax.swing.JOptionPane.showMessageDialog(null,
                    "Vendedor desvinculado exitosamente.",
                    "Éxito",
                    javax.swing.JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            // Manejo de errores
            javax.swing.JOptionPane.showMessageDialog(null,
                    "Error al desvincular el vendedor: " + e.getMessage(),
                    "Error",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        } finally {
            try {
                if (cs != null) {
                    cs.close(); // Cerrar CallableStatement
                }
            } catch (SQLException e) {
                System.err.println("Error al cerrar el CallableStatement: " + e.getMessage());
            }
            conexion.desconectar(); // Desconectar de la base de datos
        }
    }

    public void crearDespacho(int idDespacho, int idVenta, int idVendedor, java.sql.Date fechaDespacho, String estado) throws SQLException {
        CallableStatement cs = null;

        try {
            // Preparar la llamada al procedimiento
            String query = "{call PKG_Multinivel.crear_despacho(?, ?, ?, ?, ?)}";
            cs = conn.prepareCall(query);

            // Configurar los parámetros
            cs.setInt(1, idDespacho);        // Parámetro: p_id_despacho
            cs.setInt(2, idVenta);           // Parámetro: p_id_venta
            cs.setInt(3, idVendedor);        // Parámetro: p_id_vendedor
            cs.setDate(4, fechaDespacho);    // Parámetro: p_fecha_despacho
            cs.setString(5, estado);         // Parámetro: p_estado

            // Ejecutar el procedimiento
            cs.execute();
            System.out.println("Despacho creado exitosamente.");
        } catch (SQLException e) {
            System.err.println("Error al crear el despacho: " + e.getMessage());
            throw e; // Propagar el error
        } finally {
            if (cs != null) {
                cs.close(); // Cerrar CallableStatement
            }
        }
    }

    public static void cargarDespachosEnTabla(JTable tablaDespachos) {
        // Obtener el modelo de la tabla
        DefaultTableModel modelo = (DefaultTableModel) tablaDespachos.getModel();
        modelo.setRowCount(0); // Limpiar los datos actuales de la tabla

        // Conectar a la base de datos
        conexionOracle conexion = new conexionOracle();
        conexion.conectar();

        try {
            // Consulta SQL para obtener los datos de los despachos
            String query = "SELECT id_despacho, id_venta, id_vendedor, fecha_despacho, estado FROM despachos";
            Statement stmt = conexion.getConn().createStatement();
            ResultSet rs = stmt.executeQuery(query);

            // Recorrer los resultados y agregarlos al modelo de la tabla
            while (rs.next()) {
                Object[] fila = {
                    rs.getInt("id_despacho"), // ID del despacho
                    rs.getInt("id_venta"), // ID de la venta
                    rs.getInt("id_vendedor"), // ID del vendedor
                    rs.getDate("fecha_despacho"), // Fecha de despacho
                    rs.getString("estado") // Estado del despacho
                };
                modelo.addRow(fila); // Agregar fila al modelo
            }

            // Cerrar ResultSet y Statement
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            // Manejo de errores al cargar los datos
            javax.swing.JOptionPane.showMessageDialog(null,
                    "Error al cargar los datos de los despachos: " + e.getMessage(),
                    "Error",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        } finally {
            // Desconectar la conexión
            conexion.desconectar();
        }
    }

    public static void actualizarEstadoDespacho(int idDespacho, String nuevoEstado) throws SQLException {
        CallableStatement cs = null;
        conexionOracle conexion = null;

        try {
            // Crear la conexión a la base de datos
            conexion = new conexionOracle();
            conexion.conectar();

            // Preparar la llamada al procedimiento almacenado
            String query = "{call PKG_Multinivel.actualizar_estado_despacho(?, ?)}";
            cs = conexion.getConn().prepareCall(query); // Usar la conexión desde el objeto 'conexion'

            // Configurar los parámetros
            cs.setInt(1, idDespacho);         // Parámetro: p_id_despacho
            cs.setString(2, nuevoEstado);     // Parámetro: p_nuevo_estado

            // Ejecutar el procedimiento
            cs.execute();
            System.out.println("Estado del despacho actualizado exitosamente.");
        } catch (SQLException e) {
            // Manejo de excepciones en caso de error
            System.err.println("Error al actualizar el estado del despacho: " + e.getMessage());
            throw e; // Propagar el error
        } finally {
            if (cs != null) {
                cs.close(); // Cerrar CallableStatement
            }
            if (conexion != null) {
                conexion.desconectar(); // Desconectar la conexión
            }
        }
    }

    public static void actualizarCantidadProducto(int idProducto, int cantidad) throws SQLException {
        CallableStatement cs = null;
        conexionOracle conexion = null;

        try {
            // Crear la conexión a la base de datos
            conexion = new conexionOracle();
            conexion.conectar();

            // Preparar la llamada al procedimiento almacenado
            String query = "{call PKG_Multinivel.actualizar_cantidad_producto(?, ?)}";
            cs = conexion.getConn().prepareCall(query); // Usar la conexión desde el objeto 'conexion'

            // Configurar los parámetros
            cs.setInt(1, idProducto);     // Parámetro: p_id_producto
            cs.setInt(2, cantidad);       // Parámetro: p_cantidad

            // Ejecutar el procedimiento
            cs.execute();
            System.out.println("Cantidad del producto actualizada exitosamente.");
        } catch (SQLException e) {
            // Manejo de excepciones en caso de error
            System.err.println("Error al actualizar la cantidad del producto: " + e.getMessage());
            throw e; // Propagar el error
        } finally {
            if (cs != null) {
                cs.close(); // Cerrar CallableStatement
            }
            if (conexion != null) {
                conexion.desconectar(); // Desconectar la conexión
            }
        }
    }

}
