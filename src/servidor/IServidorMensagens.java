package servidor;

import cliente.IClienteCallback;
import model.Contato;
import model.Mensagem;
import model.StatusCliente;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface IServidorMensagens extends Remote {
    
    int conectar(IClienteCallback callback, String nome) throws RemoteException;
    void desconectar(int idCliente) throws RemoteException;
    
    void atualizarStatus(int idCliente, StatusCliente status) throws RemoteException;
    StatusCliente getStatusCliente(String nomeCliente) throws RemoteException;
    
    void adicionarContato(int idCliente, String nomeContato) throws RemoteException;
    void removerContato(int idCliente, String nomeContato) throws RemoteException;
    List<Contato> getContatos(int idCliente) throws RemoteException;
    List<String> getClientesOnline() throws RemoteException;
    
    void enviarMensagem(int idCliente, String destinatario, String conteudo) throws RemoteException;
    List<Mensagem> getMensagensOffline(int idCliente) throws RemoteException;
    void marcarMensagemComoLida(int idCliente, Mensagem mensagem) throws RemoteException;
}