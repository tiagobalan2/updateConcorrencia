import java.sql.*;
import java.util.Scanner;


public class update {


    public static void main(String[] args) {
        Connection conn = null;
        PreparedStatement stmt = null;
        Scanner scanner = new Scanner(System.in);


        try {
            // Estabelece a conexão com o banco de dados
            String url = "jdbc:postgresql://localhost:5432/aularobson"; // Verifique a URL correta do seu banco
            conn = DriverManager.getConnection(url, "postgres", "labinfo123"); // Substitua pelas credenciais corretas
            conn.setAutoCommit(false);  // Desativa o autocommit para usar transações manualmente


            // Solicita ao usuário o ID do cliente e o aumento do limite
            System.out.print("Informe o ID do cliente: ");
            int idCliente = scanner.nextInt();  // Lê o ID do cliente


            // Tentamos bloquear o registro para garantir que o ID não seja atualizado simultaneamente
            String selectSql = "SELECT id FROM pessoas WHERE id = ? FOR UPDATE NOWAIT"; // "NOWAIT" para falhar imediatamente se o registro estiver bloqueado
            PreparedStatement selectStmt = conn.prepareStatement(selectSql);
            selectStmt.setInt(1, idCliente);


            try {
                ResultSet rs = selectStmt.executeQuery();
                if (!rs.next()) {
                    System.out.println("Erro: Nenhum cliente encontrado com o ID " + idCliente);
                    return;
                }
            } catch (SQLException e) {
                if (e.getSQLState().equals("55P03")) {  // Código de erro do PostgreSQL para "lock not available"
                    System.out.println("Erro: O cliente com o ID " + idCliente + " já está sendo atualizado por outra transação.");
                    return;  // Finaliza a execução se o bloqueio não puder ser obtido
                }
                throw e;  // Propaga o erro caso não seja relacionado a bloqueio
            }


            // Solicita o valor do aumento do limite
            System.out.print("Informe o valor do aumento do limite: ");
            double aumentoLimite = scanner.nextDouble();  // Lê o valor do aumento do limite


            // Prepara e executa o comando SQL de atualização
            String updateSql = "UPDATE pessoas SET limite_credito = limite_credito + ? WHERE id = ?";
            stmt = conn.prepareStatement(updateSql);
            stmt.setDouble(1, aumentoLimite);
            stmt.setInt(2, idCliente);


            // Executa a atualização e verifica o número de linhas afetadas
            int linhasAfetadas = stmt.executeUpdate();


            // Verifica se a atualização foi bem-sucedida
            if (linhasAfetadas > 0) {
                // Solicita confirmação do usuário
                System.out.print("A atualização foi realizada com sucesso. Deseja confirmar? (sim/não): ");
                String resposta = scanner.next();  // Lê a resposta do usuário


                // Se o usuário confirmar a atualização, realiza o commit
                if ("sim".equalsIgnoreCase(resposta)) {
                    conn.commit();
                    System.out.println("Limite atualizado e confirmado com sucesso!");
                } else {
                    // Caso o usuário escolha não confirmar, realiza o rollback
                    conn.rollback();
                    System.out.println("Atualização cancelada. Nenhuma alteração foi feita.");
                }
            } else {
                // Se não houverem linhas afetadas, faz o rollback
                conn.rollback();
                System.out.println("Erro: Nenhum cliente encontrado com o ID " + idCliente);
            }


        } catch (SQLException e) {
            // Em caso de erro, faz o rollback e exibe o erro
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException rollbackException) {
                System.out.println("Erro ao fazer rollback: " + rollbackException.getMessage());
            }
            System.out.println("Erro na atualização: " + e.getMessage());
        } finally {
            // Fecha os recursos
            try {
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
                if (scanner != null) scanner.close();  // Fecha o scanner
            } catch (SQLException e) {
                System.out.println("Erro ao fechar os recursos: " + e.getMessage());
            }
        }
    }
}
