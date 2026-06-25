package ui;

import cliente.ClienteMensagens;
import model.Contato;
import model.StatusCliente;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.Collection;

public class ClienteUI extends JFrame {
    
    private ClienteMensagens controller;
    
    private JList<String> listaContatos;
    private DefaultListModel<String> modelContatos;
    private JTextArea areaChat;
    private JTextField campoMensagem;
    private JButton btnEnviar;
    private JButton btnAdicionar;
    private JButton btnRemover;
    private JButton btnStatus;
    private JLabel lblStatus;
    
    private String contatoSelecionado;
    
    public ClienteUI(ClienteMensagens controller) {
        this.controller = controller;
        initComponents();
        setupListeners();
        
        setTitle("Chat - " + controller.getNome());
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                controller.desconectar();
            }
        });
    }
    
    private void initComponents() {
        setSize(700, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        ((JPanel) getContentPane()).setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JPanel panelTopo = new JPanel(new BorderLayout());
        lblStatus = new JLabel("Status: ONLINE");
        lblStatus.setFont(new Font("Arial", Font.BOLD, 14));
        lblStatus.setForeground(new Color(0, 150, 0));
        
        btnStatus = new JButton("Mudar Status");
        btnStatus.setPreferredSize(new Dimension(120, 30));
        
        panelTopo.add(lblStatus, BorderLayout.WEST);
        panelTopo.add(btnStatus, BorderLayout.EAST);
        
        JPanel panelContatos = new JPanel(new BorderLayout());
        panelContatos.setBorder(BorderFactory.createTitledBorder("Contatos"));
        panelContatos.setPreferredSize(new Dimension(200, 0));
        
        modelContatos = new DefaultListModel<>();
        listaContatos = new JList<>(modelContatos);
        listaContatos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane scrollContatos = new JScrollPane(listaContatos);
        
        JPanel panelBotoesContatos = new JPanel(new GridLayout(1, 2, 5, 5));
        btnAdicionar = new JButton("+");
        btnRemover = new JButton("-");
        panelBotoesContatos.add(btnAdicionar);
        panelBotoesContatos.add(btnRemover);
        
        panelContatos.add(scrollContatos, BorderLayout.CENTER);
        panelContatos.add(panelBotoesContatos, BorderLayout.SOUTH);
        
        JPanel panelChat = new JPanel(new BorderLayout());
        panelChat.setBorder(BorderFactory.createTitledBorder("Chat"));
        
        areaChat = new JTextArea();
        areaChat.setEditable(false);
        areaChat.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollChat = new JScrollPane(areaChat);
        
        JPanel panelEnvio = new JPanel(new BorderLayout(5, 5));
        campoMensagem = new JTextField();
        btnEnviar = new JButton("Enviar");
        btnEnviar.setPreferredSize(new Dimension(80, 30));
        panelEnvio.add(campoMensagem, BorderLayout.CENTER);
        panelEnvio.add(btnEnviar, BorderLayout.EAST);
        
        panelChat.add(scrollChat, BorderLayout.CENTER);
        panelChat.add(panelEnvio, BorderLayout.SOUTH);
        
        add(panelTopo, BorderLayout.NORTH);
        add(panelContatos, BorderLayout.WEST);
        add(panelChat, BorderLayout.CENTER);
    }
    
    private void setupListeners() {
        listaContatos.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selected = listaContatos.getSelectedValue();
                if (selected != null) {
                    String nomeLimpo = selected.replaceAll("[🟢🔴]", "").trim();
                    contatoSelecionado = nomeLimpo;
                    System.out.println("Contato selecionado: " + contatoSelecionado);
                }
            }
        });
        
        btnEnviar.addActionListener(e -> enviarMensagem());
        campoMensagem.addActionListener(e -> enviarMensagem());
        
        btnAdicionar.addActionListener(e -> {
            String nome = JOptionPane.showInputDialog(this, "Digite o nome do contato:");
            if (nome != null && !nome.trim().isEmpty()) {
                controller.adicionarContato(nome.trim());
            }
        });
        
        btnRemover.addActionListener(e -> {
            String selected = listaContatos.getSelectedValue();
            if (selected != null) {
                int confirm = JOptionPane.showConfirmDialog(this, 
                    "Remover contato " + selected + "?", "Confirmar", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    controller.removerContato(selected);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Selecione um contato para remover!");
            }
        });
        
        btnStatus.addActionListener(e -> {
            String[] opcoes = {"ONLINE", "OFFLINE"};
            int escolha = JOptionPane.showOptionDialog(this,
                "Mudar status", "Status",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, opcoes, opcoes[0]);
            
            if (escolha == 0) {
                controller.mudarStatus(StatusCliente.ONLINE);
            } else if (escolha == 1) {
                controller.mudarStatus(StatusCliente.OFFLINE);
            }
        });
    }
    
    private void enviarMensagem() {
        String texto = campoMensagem.getText().trim();
        if (texto.isEmpty()) return;
        if (contatoSelecionado == null) {
            JOptionPane.showMessageDialog(this, "Selecione um contato!");
            return;
        }
        
        controller.enviarMensagem(contatoSelecionado, texto);
        campoMensagem.setText("");
    }
    
    public void atualizarContatos(Collection<Contato> contatos) {
        SwingUtilities.invokeLater(() -> {
            modelContatos.clear();
            for (Contato c : contatos) {
                modelContatos.addElement(c.getNome());
            }
        });
    }
    
    public void atualizarContato(Contato contato) {
        SwingUtilities.invokeLater(() -> {
            for (int i = 0; i < modelContatos.size(); i++) {
                String item = modelContatos.get(i);
                if (item.contains(contato.getNome())) {
                    String novoItem = (contato.getStatus() == StatusCliente.ONLINE ? "🟢" : "🔴") + " " + contato.getNome();
                    modelContatos.set(i, novoItem);
                    break;
                }
            }
        });
    }
    
    public void adicionarMensagemChat(String remetente, String conteudo) {
        SwingUtilities.invokeLater(() -> {
            areaChat.append(remetente + ": " + conteudo + "\n");
            areaChat.setCaretPosition(areaChat.getDocument().getLength());
        });
    }
    
    public void atualizarStatus(StatusCliente status) {
        SwingUtilities.invokeLater(() -> {
            if (status == StatusCliente.ONLINE) {
                lblStatus.setText("Status: ONLINE");
                lblStatus.setForeground(new Color(0, 150, 0));
            } else {
                lblStatus.setText("Status: OFFLINE");
                lblStatus.setForeground(Color.RED);
            }
        });
    }
    
    public void mostrarErro(String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem, "Erro", JOptionPane.ERROR_MESSAGE);
    }
}