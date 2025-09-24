package br.com.alura.bytebank.domain.conta;

import br.com.alura.bytebank.ConnectionFactory;
import br.com.alura.bytebank.domain.cliente.Cliente;
import br.com.alura.bytebank.domain.cliente.DadosCadastroCliente;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class ContaDAO {

    Connection conn;

    public ContaDAO(Connection conn) {
        this.conn = conn;
    }

    public Set<Conta> listar(){
        PreparedStatement ps;
        ResultSet resultSet;
        Set<Conta> contas = new HashSet<>();

        String sql = "SELECT * FROM conta";

        try{
            ps = conn.prepareStatement(sql);
            resultSet = ps.executeQuery();

            while (resultSet.next()){
                Integer numero = resultSet.getInt(1);
                BigDecimal saldo = resultSet.getBigDecimal(2);
                String nome = resultSet.getString(3);
                String cpf = resultSet.getString(4);
                String email = resultSet.getString(5);

                DadosCadastroCliente dados = new DadosCadastroCliente(nome,cpf,email);
                Cliente cliente = new Cliente(dados);
                contas.add(new Conta(numero,cliente,saldo));
            }
            resultSet.close();
            ps.close();
            conn.close();
        }catch (SQLException e){
            throw new RuntimeException(e);
        }
        return contas;
    }

    public Conta listarPorNumero(Integer numero){
        String sql = "SELECT * FROM conta WHERE numero = ?";
        PreparedStatement preparedStatement;
        ResultSet resultSet;
        Conta conta = null;

        try{
            preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setInt(1,numero);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()){
                Integer numeroDaBusca = resultSet.getInt(1);
                BigDecimal saldo = resultSet.getBigDecimal(2);
                String nome = resultSet.getString(3);
                String cpf = resultSet.getString(4);
                String email = resultSet.getString(5);

                DadosCadastroCliente dados = new DadosCadastroCliente(nome,cpf,email);
                Cliente cliente = new Cliente(dados);
                conta = new Conta(numero,cliente,saldo);
            }
            preparedStatement.close();
            resultSet.close();
            conn.close();
        }catch (SQLException e){
            throw new RuntimeException(e);
        }

        return conta;
    }

    public void alterar(Integer numDaConta,BigDecimal valor){
        String sql = "UPDATE conta SET saldo = ? WHERE numero = ?";
        PreparedStatement preparedStatement;

        try{
            conn.setAutoCommit(false);

            preparedStatement = conn.prepareStatement(sql);

            preparedStatement.setBigDecimal(1,valor);
            preparedStatement.setInt(2,numDaConta);

            preparedStatement.execute();
            conn.commit();

            preparedStatement.close();
            conn.close();
        }catch (SQLException e ){
            try{
                conn.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            throw new RuntimeException(e);
        }

    }
}
