package servidor;

import model.Mensagem;
import util.Constantes;
import javax.jms.*;
import org.apache.activemq.ActiveMQConnectionFactory;
import java.util.ArrayList;
import java.util.List;

public class GerenciadorFilas {
    
    private Connection connection;
    private Session session;
    
    public GerenciadorFilas() throws JMSException {
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(Constantes.BROKER_URL);
        
        factory.setTrustAllPackages(true);
        
        connection = factory.createConnection();
        connection.start();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    }

    public void criarFila(String nomeCliente) throws JMSException {
        String nomeFila = Constantes.PREFIXO_FILA + nomeCliente;
        Queue queue = session.createQueue(nomeFila);
        System.out.println("Fila criada: " + nomeFila);
    }
    
    public void enviarMensagemFila(String destinatario, Mensagem mensagem) throws JMSException {
        String nomeFila = Constantes.PREFIXO_FILA + destinatario;
        Queue queue = session.createQueue(nomeFila);
        MessageProducer producer = session.createProducer(queue);
        producer.setDeliveryMode(DeliveryMode.PERSISTENT);
        
        ObjectMessage msg = session.createObjectMessage();
        msg.setObject(mensagem);
        producer.send(msg);
        producer.close();
        
        System.out.println("Mensagem enviada para fila de " + destinatario);
    }
    
    public List<Mensagem> recuperarMensagensOffline(String nomeCliente) throws JMSException {
        List<Mensagem> mensagens = new ArrayList<>();
        String nomeFila = Constantes.PREFIXO_FILA + nomeCliente;
        Queue queue = session.createQueue(nomeFila);
        
        MessageConsumer consumer = session.createConsumer(queue);
        
        while (true) {
            Message msg = consumer.receive(1000);
            if (msg == null) break;
            
            if (msg instanceof ObjectMessage) {
                ObjectMessage objMsg = (ObjectMessage) msg;
                Mensagem mensagem = (Mensagem) objMsg.getObject();
                mensagens.add(mensagem);
            }
        }
        consumer.close();
        
        return mensagens;
    }
    
    public void fechar() throws JMSException {
        if (session != null) session.close();
        if (connection != null) connection.close();
    }
}