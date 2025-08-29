import javax.swing.*;
import java.awt.*;

/**
 * Simula o escalonamento de processos com quantum fixo e operações de I/O.
 *
 * O algoritmo implementa um escalonador Round Robin com suporte a operações de I/O simuladas(ESQUECI DE COLOCAR PRIORIDADE '-').
 * Cada processo pode ser enviado para I/O aleatoriamente durante sua execução(TEMOS QUE CHECAR COM A PROFESSORA SE É ASSIM MESMP).
 * O estado dos processos é impresso no console a cada passo.
 */
public class EscalonadorUI {
    // -------------------
    // Configurações iniciais
    // -------------------
    // Nomes dos processos
    static String[] nomes = {"A", "B", "C"};
    // Durações totais de cada processo
    static int[] duracoes = {10, 5, 2};
    // Quantum fixo para o escalonador
    static final int QUANTUM = 2;
    // Número de processos
    static final int NUM_PROCESSOS = nomes.length;

    // -------------------
    // Arrays de controle
    // -------------------
    // Tempo restante de cada processo
    static int[] restantes = new int[NUM_PROCESSOS];
    // Índices dos processos prontos para execução
    static int[] filaProntos = new int[NUM_PROCESSOS];
    // Tamanho da fila de prontos
    static int tamFilaProntos = 0;
    // Índices dos processos em I/O
    static int[] filaIO = new int[NUM_PROCESSOS];
    // Tempos restantes de I/O
    static int[] temposIO = new int[NUM_PROCESSOS];
    // Tamanho da fila de I/O
    static int tamFilaIO = 0;
    // Linha do tempo dos eventos
    static String[] linhaTempo = new String[100];
    static int[] blocosTamanhos = new int[100];
    static int tamLinhaTempo = 0;
    // Tempo total decorrido
    static int tempoTotal = 0;

    // Variaveis de interface (apenas para visualização)
    static JFrame frame;
    static JPanel timelinePanel;

    /**
     * Função principal: executa a simulação passo a passo até todos os processos terminarem.
     */
    public static void main(String[] args) {
        // Inicializa os arrays de controle
        for (int i = 0; i < NUM_PROCESSOS; i++) {
            // OBS: Durações é o valor inicial
            // Restantes é o valor que vai ser decrementado
            restantes[i] = duracoes[i];
            filaProntos[i] = i;
        }
        tamFilaProntos = NUM_PROCESSOS;
        SwingUtilities.invokeLater(EscalonadorUI::criarJanela);
    }

    static void criarJanela() {
        frame = new JFrame("Linha do Tempo de Escalonamento");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 300);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());

        timelinePanel = new JPanel() {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(900, 200);
            }
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                desenharPainel(g);
            }
        };
        frame.add(timelinePanel, BorderLayout.CENTER);

        JButton btnProximo = new JButton("Próximo passo");
        btnProximo.addActionListener(e -> avancarSimulacao());
        frame.add(btnProximo, BorderLayout.SOUTH);

        frame.setVisible(true);
    }


    /**
     * Executa um passo da simulação:
     * - Atualiza a fila de I/O, retornando processos prontos para a fila de prontos.
     * - Executa o próximo processo pronto, podendo enviá-lo para I/O ou executar um quantum.
     * - Atualiza a visualização gráfica.
     * - (ESQUECI DE COLOCAR PRIORIDADE '-')
     * - (TEMOS QUE CHECAR COM A PROFESSORA SE É ASSIM MESMO)
     */
    static void avancarSimulacao() {
        // Atualiza fila de I/O: decrementa tempos e retorna processos prontos
        for (int i = tamFilaIO - 1; i >= 0; i--) {
            temposIO[i] -= QUANTUM;
            if (temposIO[i] <= 0) {
                int idx = filaIO[i];
                // Adiciona à fila de prontos
                filaProntos[tamFilaProntos++] = idx;
                // Remove da fila de IO
                for (int j = i; j < tamFilaIO - 1; j++) {
                    filaIO[j] = filaIO[j + 1];
                    temposIO[j] = temposIO[j + 1];
                }
                tamFilaIO--;
            }
        }
        // Executa próximo processo pronto
        if (tamFilaProntos > 0) {
            int idx = filaProntos[0];
            // Remove do início da fila
            for (int i = 0; i < tamFilaProntos - 1; i++) filaProntos[i] = filaProntos[i + 1];
            tamFilaProntos--;
            // Simula chance de ir para I/O (30%)
            if (restantes[idx] > 0 && Math.random() < 0.3) {
                int tempoIO = 4 + (int)(Math.random() * 4); // 4 a 7
                filaIO[tamFilaIO] = idx;
                temposIO[tamFilaIO] = tempoIO;
                tamFilaIO++;
                blocosTamanhos[tamLinhaTempo] = restantes[idx];
                linhaTempo[tamLinhaTempo++] = nomes[idx] + ":IO";
            } else if (restantes[idx] > 0) {
                int quantumExecutado = Math.min(QUANTUM, restantes[idx]);
                blocosTamanhos[tamLinhaTempo] = restantes[idx];
                restantes[idx] -= quantumExecutado;
                linhaTempo[tamLinhaTempo++] = nomes[idx] + ":" + quantumExecutado;
                tempoTotal += quantumExecutado;
                // Se ainda resta execução, volta para o final da fila de prontos
                if (restantes[idx] > 0) {
                    filaProntos[tamFilaProntos++] = idx;
                }
            }
        }
        timelinePanel.repaint();
    }

    /**
     * Desenha a linha do tempo dos processos e a fila de I/O.
     * (QUANDO COLOCARMOS PRIORIDADE TEM QUE ADICIONAR AQUI TAMBÉM)
     */
    static void desenharPainel(Graphics g) {
        // Topo: processos restantes e quantuns
        g.setFont(new Font("Arial", Font.BOLD, 16));
        StringBuilder status = new StringBuilder("Restantes: ");
        for (int i = 0; i < NUM_PROCESSOS; i++) {
            status.append(nomes[i]).append(": ").append(restantes[i]);
            if (i < NUM_PROCESSOS - 1) status.append(" | ");
        }
        int statusWidth = g.getFontMetrics().stringWidth(status.toString());
        g.drawString(status.toString(), (g.getClipBounds().width - statusWidth) / 2, 30);

        // Todos os blocos em uma única linha
        int alturaBloco = 40;
        int larguraUnidade = 20;
        int y = 100;
        int x = 100;
        int eixoY = y + alturaBloco + 10;
        int tempo = 0;
        for (int t = 0; t < tamLinhaTempo; t++) {
            String[] partes = linhaTempo[t].split(":");
            String nome = partes[0];
            if (partes[1].equals("IO")) {
                int largura = 40;
                g.setColor(Color.GRAY);
                g.fillRect(x, y, largura, alturaBloco);
                g.setColor(Color.WHITE);
                g.setFont(new Font("Arial", Font.BOLD, 18));
                g.drawString(nome + ":IO", x + 2, y + 28);
                g.setColor(Color.BLACK);
                g.drawString(Integer.toString(tempo), x - 5, eixoY + 20);
                g.drawLine(x, eixoY, x + largura, eixoY);
                x += largura;
            } else {
                int quantumExecutado = Integer.parseInt(partes[1]);
                int tempoRestanteAntes = blocosTamanhos[t];
                int largura = tempoRestanteAntes * larguraUnidade;
                Color cor = nome.equals("A") ? new Color(255, 140, 0) : nome.equals("B") ? new Color(200, 60, 60) : new Color(60, 120, 200);
                g.setColor(cor);
                g.fillRect(x, y, largura, alturaBloco);
                g.setColor(Color.WHITE);
                g.setFont(new Font("Arial", Font.BOLD, 18));
                g.drawString(nome, x + largura / 2 - 8, y + 28);
                g.setColor(Color.BLACK);
                g.drawString(Integer.toString(tempo), x - 5, eixoY + 20);
                g.drawLine(x, eixoY, x + largura, eixoY);
                x += largura;
                tempo += quantumExecutado;
            }
        }
        g.setColor(Color.BLACK);
        g.drawString(Integer.toString(tempo), x - 5, eixoY + 20);

        // Desenha fila de I/O
        int yIO = eixoY + 40;
        g.setFont(new Font("Arial", Font.BOLD, 14));
        g.setColor(Color.BLACK);
        g.drawString("Fila de I/O:", 100, yIO);
        int xIO = 200;
        for (int i = 0; i < tamFilaIO; i++) {
            int idx = filaIO[i];
            int tempoIO = temposIO[i];
            g.setColor(Color.GRAY);
            g.fillRect(xIO, yIO - 18, 60, 30);
            g.setColor(Color.WHITE);
            g.drawString(nomes[idx] + "(" + tempoIO + ")", xIO + 5, yIO);
            xIO += 70;
        }
    }
}
