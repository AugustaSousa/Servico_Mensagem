package ui;

import cliente.ClienteMensagens;
import model.Contato;
import model.StatusCliente;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.Collection;

public class ClienteUI extends JFrame {
    
    private static final Color COR_PRIMARIA = new Color(255, 140, 0);
    private static final Color COR_PRIMARIA_ESCURA = new Color(200, 100, 0);
    private static final Color COR_PRIMARIA_CLARA = new Color(255, 180, 80);
    private static final Color COR_FUNDO = new Color(255, 248, 240);
    private static final Color COR_PAINEL = new Color(255, 255, 255);
    private static final Color COR_TEXTO = new Color(50, 45, 40);
    private static final Color COR_BORDA = new Color(255, 200, 150);
    private static final Color COR_HOVER = new Color(255, 160, 50);
    
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
    private JLabel lblTitulo;
    
    private String contatoSelecionado;
    
    public ClienteUI(ClienteMensagens controller) {
        this.controller = controller;
        initComponents();
        setupListeners();
        
        setTitle("💬 Chat - " + controller.getNome());
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                controller.desconectar();
            }
        });
    }
    
    private void initComponents() {
        setSize(800, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(COR_FUNDO);
        ((JPanel) getContentPane()).setBorder(new EmptyBorder(15, 15, 15, 15));
        
        JPanel panelTopo = new JPanel(new BorderLayout(15, 0));
        panelTopo.setBackground(COR_PAINEL);
        panelTopo.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(COR_BORDA, 1, true),
            new EmptyBorder(12, 20, 12, 20)
        ));
        
        // Título com ícone
        lblTitulo = new JLabel("Chat - " + controller.getNome());
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitulo.setForeground(COR_PRIMARIA_ESCURA);
        
        // Status com bolinha
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        statusPanel.setBackground(COR_PAINEL);
        
        lblStatus = new JLabel("● ONLINE");
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblStatus.setForeground(new Color(0, 180, 0));
        
        btnStatus = criarBotaoLaranja("Status", COR_PRIMARIA_CLARA);
        btnStatus.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnStatus.setPreferredSize(new Dimension(120, 32));
        
        statusPanel.add(lblStatus);
        statusPanel.add(btnStatus);
        
        panelTopo.add(lblTitulo, BorderLayout.WEST);
        panelTopo.add(statusPanel, BorderLayout.EAST);
        
        JPanel panelContatos = new JPanel(new BorderLayout(5, 5));
        panelContatos.setBackground(COR_PAINEL);
        panelContatos.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(COR_BORDA, 1, true),
            new EmptyBorder(10, 10, 10, 10)
        ));
        panelContatos.setPreferredSize(new Dimension(220, 0));
        
        JLabel lblContatos = new JLabel("CONTATOS");
        lblContatos.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblContatos.setForeground(COR_PRIMARIA_ESCURA);
        lblContatos.setBorder(new EmptyBorder(0, 5, 8, 0));
        panelContatos.add(lblContatos, BorderLayout.NORTH);
        
        modelContatos = new DefaultListModel<>();
        listaContatos = new JList<>(modelContatos);
        listaContatos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listaContatos.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        listaContatos.setBackground(COR_FUNDO);
        listaContatos.setForeground(COR_TEXTO);
        listaContatos.setFixedCellHeight(30);
        
        // Cores de seleção laranja
        listaContatos.setSelectionBackground(COR_PRIMARIA_CLARA);
        listaContatos.setSelectionForeground(COR_TEXTO);
        
        JScrollPane scrollContatos = new JScrollPane(listaContatos);
        scrollContatos.setBorder(new LineBorder(COR_BORDA, 1, true));
        scrollContatos.getViewport().setBackground(COR_FUNDO);
        
        // Botões de contato
        JPanel panelBotoesContatos = new JPanel(new GridLayout(1, 2, 5, 0));
        panelBotoesContatos.setBackground(COR_PAINEL);
        panelBotoesContatos.setBorder(new EmptyBorder(8, 0, 0, 0));
        
        btnAdicionar = criarBotaoLaranja("Adicionar", COR_PRIMARIA);
        btnRemover = criarBotaoLaranja("Remover", new Color(200, 80, 80));
        
        btnAdicionar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnRemover.setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        panelBotoesContatos.add(btnAdicionar);
        panelBotoesContatos.add(btnRemover);
        
        panelContatos.add(scrollContatos, BorderLayout.CENTER);
        panelContatos.add(panelBotoesContatos, BorderLayout.SOUTH);
        
        // ==========================================
        // PAINEL CENTRAL - CHAT
        // ==========================================
        JPanel panelChat = new JPanel(new BorderLayout(5, 5));
        panelChat.setBackground(COR_PAINEL);
        panelChat.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(COR_BORDA, 1, true),
            new EmptyBorder(10, 10, 10, 10)
        ));
        
        // Área de chat
        areaChat = new JTextArea();
        areaChat.setEditable(false);
        areaChat.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        areaChat.setBackground(COR_FUNDO);
        areaChat.setForeground(COR_TEXTO);
        areaChat.setBorder(new EmptyBorder(8, 8, 8, 8));
        areaChat.setLineWrap(true);
        areaChat.setWrapStyleWord(true);
        
        JScrollPane scrollChat = new JScrollPane(areaChat);
        scrollChat.setBorder(new LineBorder(COR_BORDA, 1, true));
        scrollChat.getViewport().setBackground(COR_FUNDO);
        
        JPanel panelEnvio = new JPanel(new BorderLayout(8, 8));
        panelEnvio.setBackground(COR_PAINEL);
        panelEnvio.setBorder(new EmptyBorder(8, 0, 0, 0));
        
        campoMensagem = new JTextField();
        campoMensagem.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        campoMensagem.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(COR_BORDA, 1, true),
            new EmptyBorder(8, 12, 8, 12)
        ));
        campoMensagem.setBackground(COR_FUNDO);
        campoMensagem.setForeground(COR_TEXTO);
        campoMensagem.setCaretColor(COR_PRIMARIA);
        
        btnEnviar = criarBotaoLaranja("Enviar", COR_PRIMARIA);
        btnEnviar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnEnviar.setPreferredSize(new Dimension(100, 40));
        
        btnEnviar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        panelEnvio.add(campoMensagem, BorderLayout.CENTER);
        panelEnvio.add(btnEnviar, BorderLayout.EAST);
        
        panelChat.add(scrollChat, BorderLayout.CENTER);
        panelChat.add(panelEnvio, BorderLayout.SOUTH);
        
        add(panelTopo, BorderLayout.NORTH);
        add(panelContatos, BorderLayout.WEST);
        add(panelChat, BorderLayout.CENTER);
        
        areaChat.append("Bem-vindo ao Chat Laranja!\n");
        areaChat.append("Adicione contatos para começar a conversar.\n\n");
    }
    
    private JButton criarBotaoLaranja(String texto, Color corFundo) {
        JButton botao = new JButton(texto);
        botao.setBackground(corFundo);
        botao.setForeground(Color.WHITE);
        botao.setFont(new Font("Segoe UI", Font.BOLD, 12));
        botao.setFocusPainted(false);
        botao.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(corFundo.darker(), 1, true),
            new EmptyBorder(8, 15, 8, 15)
        ));
        botao.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        botao.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                botao.setBackground(corFundo.brighter());
                botao.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(corFundo.darker().darker(), 2, true),
                    new EmptyBorder(7, 14, 7, 14)
                ));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                botao.setBackground(corFundo);
                botao.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(corFundo.darker(), 1, true),
                    new EmptyBorder(8, 15, 8, 15)
                ));
            }
        });
        
        return botao;
    }
    
    private void setupListeners() {
        listaContatos.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selected = listaContatos.getSelectedValue();
                if (selected != null) {
                    String nomeLimpo = selected.replaceAll("[🟢🔴]", "").trim();
                    contatoSelecionado = nomeLimpo;
                    System.out.println("Contato selecionado: " + contatoSelecionado);
                    setTitle("Chat - " + controller.getNome() + " → " + contatoSelecionado);
                } else {
                    setTitle("Chat - " + controller.getNome());
                }
            }
        });
        
        btnEnviar.addActionListener(e -> enviarMensagem());
        campoMensagem.addActionListener(e -> enviarMensagem());
        
        btnAdicionar.addActionListener(e -> {
            String nome = JOptionPane.showInputDialog(this, 
                "Digite o nome do contato:", 
                "Adicionar Contato", 
                JOptionPane.QUESTION_MESSAGE);
            if (nome != null && !nome.trim().isEmpty()) {
                controller.adicionarContato(nome.trim());
            }
        });
        
        btnRemover.addActionListener(e -> {
            String selected = listaContatos.getSelectedValue();
            if (selected != null) {
                String nomeLimpo = selected.replaceAll("[🟢🔴]", "").trim();
                int confirm = JOptionPane.showConfirmDialog(this, 
                    "Remover contato '" + nomeLimpo + "'?", 
                    "Confirmar", 
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
                if (confirm == JOptionPane.YES_OPTION) {
                    controller.removerContato(nomeLimpo);
                }
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Selecione um contato para remover!",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE);
            }
        });
        
        btnStatus.addActionListener(e -> {
            String[] opcoes = {"🟢 ONLINE", "🔴 OFFLINE"};
            int escolha = JOptionPane.showOptionDialog(this,
                "Mudar status do cliente",
                "Status",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                opcoes,
                opcoes[0]);
            
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
            JOptionPane.showMessageDialog(this, 
                "Selecione um contato antes de enviar!",
                "Aviso",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        controller.enviarMensagem(contatoSelecionado, texto);
        campoMensagem.setText("");
    }
    
    public void atualizarContatos(Collection<Contato> contatos) {
        SwingUtilities.invokeLater(() -> {
            modelContatos.clear();
            for (Contato c : contatos) {
                String icone = c.getStatus() == StatusCliente.ONLINE ? "🟢" : "🔴";
                modelContatos.addElement(icone + " " + c.getNome());
            }
            
            if (contatoSelecionado != null) {
                boolean contatoAindaExiste = false;
                for (Contato c : contatos) {
                    if (c.getNome().equals(contatoSelecionado)) {
                        contatoAindaExiste = true;
                        break;
                    }
                }
                if (!contatoAindaExiste) {
                    contatoSelecionado = null;
                    areaChat.setText("");
                    areaChat.append("Bem-vindo ao Chat Laranja!\n");
                    areaChat.append("Adicione contatos para começar a conversar.\n\n");
                    setTitle("💬 Chat - " + controller.getNome());
                }
            }
        });
    }
    
    public void atualizarContato(Contato contato) {
        SwingUtilities.invokeLater(() -> {
            for (int i = 0; i < modelContatos.size(); i++) {
                String item = modelContatos.get(i);
                if (item.contains(contato.getNome())) {
                    String icone = contato.getStatus() == StatusCliente.ONLINE ? "🟢" : "🔴";
                    String novoItem = icone + " " + contato.getNome();
                    modelContatos.set(i, novoItem);
                    break;
                }
            }
        });
    }
    
    public void adicionarMensagemChat(String remetente, String conteudo) {
        SwingUtilities.invokeLater(() -> {
            String prefixo = remetente.equals("Eu") ? "🧡" : "💬";
            areaChat.append(prefixo + " " + remetente + ": " + conteudo + "\n");
            areaChat.setCaretPosition(areaChat.getDocument().getLength());
        });
    }
    
    public void atualizarStatus(StatusCliente status) {
        SwingUtilities.invokeLater(() -> {
            if (status == StatusCliente.ONLINE) {
                lblStatus.setText("● ONLINE");
                lblStatus.setForeground(new Color(0, 180, 0));
                //campoMensagem.setEnabled(true);
                //btnEnviar.setEnabled(true);
            } else {
                lblStatus.setText("● OFFLINE");
                lblStatus.setForeground(Color.RED);
                //campoMensagem.setEnabled(false);
                //btnEnviar.setEnabled(false);
            }
        });
    }
    
    public void mostrarErro(String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem, "Erro", JOptionPane.ERROR_MESSAGE);
    }
}