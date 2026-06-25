package cliente;

import model.Contato;
import model.Mensagem;
import model.StatusCliente;
import ui.ClienteUI;

import javax.swing.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ClienteMensagens {
    
    private String nome;
    private ClienteRMI clienteRMI;
    private StatusCliente status;
    private ConcurrentHashMap<String, Contato> contatos;
    private ConcurrentHashMap<String, List<Mensagem>> conversas;
    private ClienteUI ui;
    private String contatoAtual;
    
    public ClienteMensagens(String nome, String host) {
        this.nome = nome;
        this.status = StatusCliente.ONLINE;
        this.contatos = new ConcurrentHashMap<>();
        this.conversas = new ConcurrentHashMap<>();
        this.contatoAtual = null;
        
        this.ui = new ClienteUI(this);
        this.ui.setVisible(true);
        
        try {
            this.clienteRMI = new ClienteRMI(this, host, nome);
        } catch (Exception e) {
            e.printStackTrace();
            ui.mostrarErro("Erro ao conectar ao servidor: " + e.getMessage());
        }
    }
    
    public void mudarStatus(StatusCliente novoStatus) {
        this.status = novoStatus;
        if (clienteRMI != null) {
            clienteRMI.atualizarStatus(novoStatus);
        }
        ui.atualizarStatus(novoStatus);
    }
    
    public void selecionarContato(String nomeContato) {
        this.contatoAtual = nomeContato;
        
        conversas.putIfAbsent(nomeContato, new java.util.ArrayList<>());
        
        ui.mostrarConversa(nomeContato, conversas.get(nomeContato));
    }
    
    public List<Mensagem> getConversa(String nomeContato) {
        return conversas.getOrDefault(nomeContato, new java.util.ArrayList<>());
    }
    
    public void adicionarMensagem(String contato, String remetente, String conteudo) {
        Mensagem msg = new Mensagem(remetente, contato, conteudo);
        conversas.computeIfAbsent(contato, k -> new java.util.ArrayList<>()).add(msg);
        
        if (contato.equals(contatoAtual)) {
            ui.adicionarMensagemChat(remetente, conteudo);
        }
    }
    
    public void adicionarContato(String nomeContato) {
        if (clienteRMI != null) {
            clienteRMI.adicionarContato(nomeContato);
            conversas.putIfAbsent(nomeContato, new java.util.ArrayList<>());
        }
    }
    
    public void removerContato(String nomeContato) {
        if (clienteRMI != null) {
            clienteRMI.removerContato(nomeContato);
            conversas.remove(nomeContato);
            
            if (contatoAtual != null && contatoAtual.equals(nomeContato)) {
                contatoAtual = null;
                ui.limparChat();
            }
        }
    }
    
    public void enviarMensagem(String destinatario, String conteudo) {
        if (clienteRMI != null) {
            adicionarMensagem(destinatario, "Eu", conteudo);
            clienteRMI.enviarMensagem(destinatario, conteudo);
        }
    }
    
    public void receberMensagem(Mensagem mensagem) {
        SwingUtilities.invokeLater(() -> {
            adicionarMensagem(mensagem.getRemetente(), mensagem.getRemetente(), mensagem.getConteudo());
        });
    }
    
    public void notificarClienteOnline(String nome) {
        SwingUtilities.invokeLater(() -> {
            Contato c = contatos.get(nome);
            if (c != null) {
                c.setStatus(StatusCliente.ONLINE);
                ui.atualizarContato(c);
            }
        });
    }
    
    public void notificarClienteOffline(String nome) {
        SwingUtilities.invokeLater(() -> {
            Contato c = contatos.get(nome);
            if (c != null) {
                c.setStatus(StatusCliente.OFFLINE);
                ui.atualizarContato(c);
            }
        });
    }
    
    public void atualizarListaContatos(List<Contato> lista) {
        SwingUtilities.invokeLater(() -> {
            contatos.clear();
            for (Contato c : lista) {
                contatos.put(c.getNome(), c);
            }
            ui.atualizarContatos(contatos.values());
        });
    }
    
    public void desconectar() {
        if (clienteRMI != null) {
            clienteRMI.desconectar();
        }
        System.exit(0);
    }
    
    public String getNome() { return nome; }
    public StatusCliente getStatus() { return status; }
    public Contato getContato(String nome) { return contatos.get(nome); }
    
    public static void main(String[] args) {
        String host = "localhost";
        String nome = JOptionPane.showInputDialog(null, "Digite seu nome:", "Login", JOptionPane.QUESTION_MESSAGE);
        if (nome == null || nome.trim().isEmpty()) {
            nome = "Usuario" + System.currentTimeMillis();
        }
        
        new ClienteMensagens(nome, host);
    }
}