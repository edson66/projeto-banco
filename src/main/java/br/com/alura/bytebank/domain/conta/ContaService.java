package br.com.alura.bytebank.domain.conta;

import br.com.alura.bytebank.ConnectionFactory;
import br.com.alura.bytebank.domain.RegraDeNegocioException;
import br.com.alura.bytebank.domain.cliente.Cliente;
import com.mysql.cj.xdevapi.PreparableStatement;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class ContaService {

    ConnectionFactory conexao;

    public ContaService(){
        conexao = new ConnectionFactory();
    }

    private Set<Conta> contas = new HashSet<>();

    public Set<Conta> listarContasAbertas() {
        Connection conn = conexao.fazerConexao();
        return new ContaDAO(conn).listar();
    }

    public BigDecimal consultarSaldo(Integer numeroDaConta) {
        var conta = buscarContaPorNumero(numeroDaConta);
        return conta.getSaldo();
    }

    public void abrir(DadosAberturaConta dadosDaConta) {
        var cliente = new Cliente(dadosDaConta.dadosCliente());
        var conta = new Conta(dadosDaConta.numero(), cliente,BigDecimal.ZERO);
        if (contas.contains(conta)) {
            throw new RegraDeNegocioException("Já existe outra conta aberta com o mesmo número!");
        }


        String sql = "INSERT INTO conta(numero,saldo,cliente_nome,cliente_cpf,cliente_email)" +
                "VALUES(?,?,?,?,?)";

        Connection con = conexao.fazerConexao();

        try{
            PreparedStatement preparedStatement = con.prepareStatement(sql);

            preparedStatement.setInt(1,conta.getNumero());
            preparedStatement.setDouble(2,0.0);
            preparedStatement.setString(3,dadosDaConta.dadosCliente().nome());
            preparedStatement.setString(4,dadosDaConta.dadosCliente().cpf());
            preparedStatement.setString(5,dadosDaConta.dadosCliente().email());

            preparedStatement.execute();

            preparedStatement.close();
            con.close();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public void realizarSaque(Integer numeroDaConta, BigDecimal valor) {
        var conta = buscarContaPorNumero(numeroDaConta);
        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraDeNegocioException("Valor do saque deve ser superior a zero!");
        }

        if (valor.compareTo(conta.getSaldo()) > 0) {
            throw new RegraDeNegocioException("Saldo insuficiente!");
        }
        Connection conn = conexao.fazerConexao();
        BigDecimal novoValor = conta.getSaldo().subtract(valor);
        new ContaDAO(conn).alterar(numeroDaConta,novoValor);

    }

    public void realizarDeposito(Integer numeroDaConta, BigDecimal valor) {
        var conta = buscarContaPorNumero(numeroDaConta);
        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraDeNegocioException("Valor do deposito deve ser superior a zero!");
        }

        Connection conn = conexao.fazerConexao();
        BigDecimal novoValor = conta.getSaldo().add(valor);
        new ContaDAO(conn).alterar(conta.getNumero(),novoValor);
    }

    public void encerrar(Integer numeroDaConta) {
        var conta = buscarContaPorNumero(numeroDaConta);
        if (conta.possuiSaldo()) {
            throw new RegraDeNegocioException("Conta não pode ser encerrada pois ainda possui saldo!");
        }

        contas.remove(conta);
    }

    public void transferir(Integer numContaOrigem, Integer numContaDestino,BigDecimal valor){
        this.realizarSaque(numContaOrigem,valor);
        this.realizarDeposito(numContaDestino,valor);
    }

    private Conta buscarContaPorNumero(Integer numero) {
        Connection conn = conexao.fazerConexao();
        Conta conta = new ContaDAO(conn).listarPorNumero(numero);
        if (conta != null){
            return conta;
        }else{
            throw new RegraDeNegocioException("Não existe conta com esse número!");
        }
    }
}
