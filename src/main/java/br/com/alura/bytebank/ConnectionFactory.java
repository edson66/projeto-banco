package br.com.alura.bytebank;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionFactory {

    public Connection fazerConexao(){
        try {
            return DriverManager
                    .getConnection("jdbc:mysql://localhost:3306/byte_bank?user=Edson&password=edson5427m");

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
