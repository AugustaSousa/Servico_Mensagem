package model;

import java.io.Serializable;

public class Contato implements Serializable {
    private static final long serialVersionUID = 1L;
    private String nome;
    private StatusCliente status;
    
    public Contato(String nome) {
        this.nome = nome;
        this.status = StatusCliente.OFFLINE;
    }
    
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public StatusCliente getStatus() { return status; }
    public void setStatus(StatusCliente status) { this.status = status; }
    
    @Override
    public String toString() {
        return nome + " (" + status + ")";
    }
}