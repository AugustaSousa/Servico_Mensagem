package cliente;

import model.Contato;
import model.Mensagem;
import model.StatusCliente;
import servidor.IServidorMensagens;
import util.Constantes;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class ClienteRMI extends UnicastRemoteObject implements IClienteCallback {
    
    private static final long serialVersionUID = 1L;
    
    private int idCliente;
    private String nome;
    private IServidorMensagens servidor;
    private ClienteMensagens controller;
    
    public ClienteRMI(ClienteMensagens controller, String host, String nome) throws RemoteException {
        super();
        this.controller = controller;
        this.nome = nome;
        
        try {
            Registry registry = LocateRegistry.getRegistry(host, Constantes.PORTA_RMI);
            servidor = (IServidorMensagens) registry.lookup(Constantes.NOME_SERVIDOR);
            
            idCliente = servidor.conectar(this, nome);
            if (idCliente == -1) {
                System.err.println("Nome já em uso!");
                System.exit(1);
            }
            
            System.out.println("Conectado ao servidor! ID: " + idCliente);
            
            atualizarContatos();
            buscarMensagensOffline();
            
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erro ao conectar ao servidor!");
        }
    }
    
    public void atualizarStatus(StatusCliente status) {
        try {
            servidor.atualizarStatus(idCliente, status);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    
    public void adicionarContato(String nomeContato) {
        try {
            servidor.adicionarContato(idCliente, nomeContato);
            atualizarContatos();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    
    public void removerContato(String nomeContato) {
        try {
            servidor.removerContato(idCliente, nomeContato);
            atualizarContatos();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    
    public List<Contato> getContatos() {
        try {
            return servidor.getContatos(idCliente);
        } catch (RemoteException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public void enviarMensagem(String destinatario, String conteudo) {
        try {
            servidor.enviarMensagem(idCliente, destinatario, conteudo);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    
    private void buscarMensagensOffline() {
        try {
            List<Mensagem> mensagens = servidor.getMensagensOffline(idCliente);
            for (Mensagem m : mensagens) {
                controller.receberMensagem(m);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    
    private void atualizarContatos() {
        try {
            List<Contato> contatos = servidor.getContatos(idCliente);
            controller.atualizarListaContatos(contatos);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    
    public void desconectar() {
        try {
            servidor.desconectar(idCliente);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    
    
    @Override
    public void receberMensagem(Mensagem mensagem) throws RemoteException {
        controller.receberMensagem(mensagem);
    }
    
    @Override
    public void notificarClienteOnline(String nome) throws RemoteException {
        controller.notificarClienteOnline(nome);
    }
    
    @Override
    public void notificarClienteOffline(String nome) throws RemoteException {
        controller.notificarClienteOffline(nome);
    }
    
    @Override
    public void atualizarListaContatos(List<Contato> contatos) throws RemoteException {
        controller.atualizarListaContatos(contatos);
    }
    
    public int getIdCliente() { return idCliente; }
    public String getNome() { return nome; }
}