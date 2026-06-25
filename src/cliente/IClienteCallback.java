package cliente;

import model.Contato;
import model.Mensagem;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface IClienteCallback extends Remote {
    
    void receberMensagem(Mensagem mensagem) throws RemoteException;
    
    void notificarClienteOnline(String nome) throws RemoteException;
    
    void notificarClienteOffline(String nome) throws RemoteException;
    
    void atualizarListaContatos(List<Contato> contatos) throws RemoteException;
}