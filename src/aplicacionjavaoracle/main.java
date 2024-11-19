/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aplicacionjavaoracle;

import BD.conexionOracle;
import graphics.LoginForm;
import graphics.MenuPrincipalForm;
import graphics.PanelVendedor;
import graphics.RegistrarVendedorForm;
import java.util.Date;

/**
 *
 * @author YEISON ANDRES MARIN
 */
public class main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        conexionOracle conn = new conexionOracle();
        /*     try {
            conn.registrarAfiliacion(23, 121);
        } finally  {
            
            conn.desconectar();
        }  */

 /*      // Parámetros del usuario
        String nombreUsuario = "yeison08";
        String contraseña = "12";

        try {
            // Llamar al método loginVendedor
            int resultado = conn.loginVendedor(nombreUsuario, contraseña);

            // Verificar el resultado
            if (resultado > 0) {
                System.out.println("Login exitoso. ID del vendedor: " + resultado);
            } else {
                System.out.println("Error en el sistema o usuario no encontrado.");
            }
        } catch (Exception e) {
            // Manejo de excepciones
            System.err.println("Ocurrió un error durante el proceso de login: " + e.getMessage());
        } finally {
            // Desconectar de la base de datos
            conn.desconectar();
        }
         */
      java.awt.EventQueue.invokeLater(() -> {
        new MenuPrincipalForm().setVisible(true);
    });  
        // Datos para registrar al vendedor
        /*        String nombre = "Juan Pérez";
            Date fechaIngreso = new Date(); // Fecha actual
            int nivel = 1; // Nivel inicial
            String estado = "ACTIVO";
            String email = "juan.perez@example.com";
            String telefono = "1234567890";
            String nombreUsuario = "juanp";
            String contrasena = "1234segura";

            // Llamar al método para registrar al vendedor
            conn.registrarVendedor(nombre, fechaIngreso, nivel, estado, email, telefono, nombreUsuario, contrasena);
  
         */
     /*   int idVendedor = 41;
        String nombre = "Juan Pérez";
        String estado = "ACTIVO";
        String email = "juan.perez@example.com";
        String telefono = "1234567890";
        
        
        conn.actualizarVendedor(idVendedor, nombre, estado, email, telefono);   */
        
        
        

    }
}
