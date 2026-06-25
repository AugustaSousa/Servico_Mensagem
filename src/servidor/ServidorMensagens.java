package servidor;

import cliente.IClienteCallback;
import model.Contato;
import model.Mensagem;
import model.StatusCliente;
import util.Constantes;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ServidorMensagens extends UnicastRemoteObject implements IServidorMensagens {
    
    private static final long serialVersionUID = 1L;
    
    private Map<Integer, IClienteCallback> clientesConectados;
    private Map<Integer, String> nomesClientes;
    private Map<String, StatusCliente> statusClientes;
    private Map<String, List<Contato>> listaContatos;
    private Map<String, List<Mensagem>> mensagensPendentes;
    private int proximoId = 1;
    
    private GerenciadorFilas gerenciadorFilas;
    
    public ServidorMensagens() throws RemoteException {
        super();
        this.clientesConectados = new ConcurrentHashMap<>();
        this.nomesClientes = new ConcurrentHashMap<>();
        this.statusClientes = new ConcurrentHashMap<>();
        this.listaContatos = new ConcurrentHashMap<>();
        this.mensagensPendentes = new ConcurrentHashMap<>();
        
        try {
            this.gerenciadorFilas = new GerenciadorFilas();
            System.out.println("Gerenciador de filas iniciado!");
        } catch (Exception e) {
            System.err.println("Erro ao iniciar gerenciador de filas: " + e.getMessage());
        }
        
        System.out.println("Servidor de Mensagens iniciado na porta " + Constantes.PORTA_RMI);
    }
    
    @Override
    public synchronized int conectar(IClienteCallback callback, String nome) throws RemoteException {
        if (nomesClientes.containsValue(nome)) {
            System.out.println("Nome " + nome + " já está em uso!");
            return -1;
        }
        
        int id = proximoId++;
        clientesConectados.put(id, callback);
        nomesClientes.put(id, nome);
        statusClientes.put(nome, StatusCliente.ONLINE);
        listaContatos.put(nome, new ArrayList<>());
        
        try {
            gerenciadorFilas.criarFila(nome);
            System.out.println("Fila criada para: " + nome);
        } catch (Exception e) {
            System.err.println("Erro ao criar fila para " + nome + ": " + e.getMessage());
        }
        
        System.out.println("Cliente conectado: " + nome + " (ID: " + id + ")");
        
        notificarNovoOnline(nome);
        
        return id;
    }
    
    private void notificarNovoOnline(String nome) throws RemoteException {
        for (Map.Entry<Integer, IClienteCallback> entry : clientesConectados.entrySet()) {
            try {
                entry.getValue().notificarClienteOnline(nome);
            } catch (RemoteException e) {
            }
        }
    }
    
    @Override
    public synchronized void desconectar(int idCliente) throws RemoteException {
        String nome = nomesClientes.get(idCliente);
        if (nome != null) {
            statusClientes.put(nome, StatusCliente.OFFLINE);
            clientesConectados.remove(idCliente);
            nomesClientes.remove(idCliente);
            
            for (Map.Entry<Integer, IClienteCallback> entry : clientesConectados.entrySet()) {
                try {
                    entry.getValue().notificarClienteOffline(nome);
                } catch (RemoteException e) {
                }
            }
            
            System.out.println("Cliente desconectado: " + nome);
        }
    }
    
    @Override
    public void atualizarStatus(int idCliente, StatusCliente status) throws RemoteException {
        String nome = nomesClientes.get(idCliente);
        if (nome != null) {
            statusClientes.put(nome, status);
            System.out.println("Cliente " + nome + " agora está " + status);
            
            if (status == StatusCliente.ONLINE) {
                processarMensagensPendentes(nome);
                
                entregarMensagensOffline(idCliente);
            }
        }
    }
    
    private void entregarMensagensOffline(int idCliente) throws RemoteException {
        String nome = nomesClientes.get(idCliente);
        if (nome == null) return;
        
        try {
            List<Mensagem> mensagens = gerenciadorFilas.recuperarMensagensOffline(nome);
            IClienteCallback callback = clientesConectados.get(idCliente);
            
            if (callback != null && !mensagens.isEmpty()) {
                for (Mensagem msg : mensagens) {
                    callback.receberMensagem(msg);
                }
                System.out.println("Entregues " + mensagens.size() + " mensagens offline para " + nome);
            }
        } catch (Exception e) {
            System.err.println("Erro ao entregar mensagens offline: " + e.getMessage());
        }
    }
    
    @Override
    public StatusCliente getStatusCliente(String nomeCliente) throws RemoteException {
        return statusClientes.getOrDefault(nomeCliente, StatusCliente.OFFLINE);
    }
    
    @Override
    public void adicionarContato(int idCliente, String nomeContato) throws RemoteException {
        String nome = nomesClientes.get(idCliente);
        if (nome == null) return;
        
        List<Contato> contatos = listaContatos.get(nome);
        if (contatos == null) {
            contatos = new ArrayList<>();
            listaContatos.put(nome, contatos);
        }
        
        for (Contato c : contatos) {
            if (c.getNome().equals(nomeContato)) {
                return;
            }
        }
        
        StatusCliente status = statusClientes.getOrDefault(nomeContato, StatusCliente.OFFLINE);
        Contato novoContato = new Contato(nomeContato);
        novoContato.setStatus(status);
        contatos.add(novoContato);
        
        System.out.println("Contato adicionado: " + nomeContato + " para " + nome);
    }
    
    @Override
    public void removerContato(int idCliente, String nomeContato) throws RemoteException {
        String nome = nomesClientes.get(idCliente);
        if (nome == null) return;
        
        List<Contato> contatos = listaContatos.get(nome);
        if (contatos != null) {
            contatos.removeIf(c -> c.getNome().equals(nomeContato));
            System.out.println("Contato removido: " + nomeContato + " de " + nome);
        }
    }
    
    @Override
    public List<Contato> getContatos(int idCliente) throws RemoteException {
        String nome = nomesClientes.get(idCliente);
        if (nome == null) return new ArrayList<>();
        
        List<Contato> contatos = listaContatos.get(nome);
        if (contatos == null) {
            contatos = new ArrayList<>();
            listaContatos.put(nome, contatos);
        }
        
        // Atualizar status dos contatos
        for (Contato c : contatos) {
            c.setStatus(statusClientes.getOrDefault(c.getNome(), StatusCliente.OFFLINE));
        }
        
        return contatos;
    }
    
    @Override
    public List<String> getClientesOnline() throws RemoteException {
        List<String> online = new ArrayList<>();
        for (Map.Entry<String, StatusCliente> entry : statusClientes.entrySet()) {
            if (entry.getValue() == StatusCliente.ONLINE) {
                online.add(entry.getKey());
            }
        }
        return online;
    }
    
    @Override
    public void enviarMensagem(int idCliente, String destinatario, String conteudo) throws RemoteException {
        String remetente = nomesClientes.get(idCliente);
        if (remetente == null) return;
        
        StatusCliente statusRemetente = statusClientes.get(remetente);
        if (statusRemetente == StatusCliente.OFFLINE) {
            armazenarMensagemPendente(remetente, destinatario, conteudo);
            return;
        }
        
        Mensagem mensagem = new Mensagem(remetente, destinatario, conteudo);
        
        StatusCliente statusDestinatario = statusClientes.get(destinatario);
        
        if (statusDestinatario == StatusCliente.ONLINE) {
            for (Map.Entry<Integer, IClienteCallback> entry : clientesConectados.entrySet()) {
                String nome = nomesClientes.get(entry.getKey());
                if (nome != null && nome.equals(destinatario)) {
                    try {
                        entry.getValue().receberMensagem(mensagem);
                        System.out.println("Mensagem entregue instantaneamente para " + destinatario);
                        return;
                    } catch (RemoteException e) {
                    }
                }
            }
        }
        
        try {
            gerenciadorFilas.enviarMensagemFila(destinatario, mensagem);
            System.out.println("Mensagem enviada para fila de " + destinatario + " (offline)");
        } catch (Exception e) {
            System.err.println("Erro ao enviar mensagem para fila: " + e.getMessage());
        }
    }
    
    @Override
    public List<Mensagem> getMensagensOffline(int idCliente) throws RemoteException {
        String nome = nomesClientes.get(idCliente);
        if (nome == null) return new ArrayList<>();
        
        try {
            return gerenciadorFilas.recuperarMensagensOffline(nome);
        } catch (Exception e) {
            System.err.println("Erro ao recuperar mensagens offline: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    @Override
    public void marcarMensagemComoLida(int idCliente, Mensagem mensagem) throws RemoteException {
    }
    
    public static void main(String[] args) {
        try {
            ServidorMensagens servidor = new ServidorMensagens();
            
            Registry registry = null;
            try {
                registry = LocateRegistry.getRegistry(Constantes.PORTA_RMI);
                registry.list();
            } catch (RemoteException e) {
                registry = LocateRegistry.createRegistry(Constantes.PORTA_RMI);
            }
            
            registry.rebind(Constantes.NOME_SERVIDOR, servidor);
            System.out.println("Servidor RMI registrado com sucesso!");
            System.out.println("Aguardando conexões...");
            System.out.println("ActiveMQ Broker: " + Constantes.BROKER_URL);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void armazenarMensagemPendente(String remetente, String destinatario, String conteudo) {
        Mensagem mensagem = new Mensagem(remetente, destinatario, conteudo);
        mensagensPendentes.computeIfAbsent(remetente, k -> new ArrayList<>()).add(mensagem);
    }

    private void processarMensagensPendentes(String remetente) {
        List<Mensagem> pendentes = mensagensPendentes.remove(remetente);
        if (pendentes == null || pendentes.isEmpty()) return;
        
        System.out.println("Processando " + pendentes.size() + " mensagens pendentes para " + remetente);
        
        for (Mensagem msg : pendentes) {
            String destinatario = msg.getDestinatario();
            StatusCliente statusDestinatario = statusClientes.get(destinatario);
            
            if (statusDestinatario == StatusCliente.ONLINE) {
                for (Map.Entry<Integer, IClienteCallback> entry : clientesConectados.entrySet()) {
                    String nome = nomesClientes.get(entry.getKey());
                    if (nome != null && nome.equals(destinatario)) {
                        try {
                            entry.getValue().receberMensagem(msg);
                            System.out.println("Mensagem pendente entregue para " + destinatario);
                        } catch (RemoteException e) {
                            try {
                                gerenciadorFilas.enviarMensagemFila(destinatario, msg);
                            } catch (Exception ex) {
                                System.err.println("Erro ao enviar mensagem pendente para fila: " + ex.getMessage());
                            }
                        }
                        break;
                    }
                }
            } else {
                try {
                    gerenciadorFilas.enviarMensagemFila(destinatario, msg);
                    System.out.println("Mensagem pendente enviada para fila de " + destinatario + " (offline)");
                } catch (Exception e) {
                    System.err.println("Erro ao enviar mensagem pendente para fila: " + e.getMessage());
                }
            }
        }
    }
}