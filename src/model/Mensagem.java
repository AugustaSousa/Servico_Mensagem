package model;

import java.io.Serializable;
import java.util.Date;

public class Mensagem implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String remetente;
    private String destinatario;
    private String conteudo;
    private Date dataHora;
    private boolean lida;
    
    public Mensagem(String remetente, String destinatario, String conteudo) {
        this.remetente = remetente;
        this.destinatario = destinatario;
        this.conteudo = conteudo;
        this.dataHora = new Date();
        this.lida = false;
    }
    
    public String getRemetente() { return remetente; }
    public String getDestinatario() { return destinatario; }
    public String getConteudo() { return conteudo; }
    public Date getDataHora() { return dataHora; }
    public boolean isLida() { return lida; }
    public void setLida(boolean lida) { this.lida = lida; }
    
    @Override
    public String toString() {
        return "[" + dataHora + "] " + remetente + ": " + conteudo;
    }
}