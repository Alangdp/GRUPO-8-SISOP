import javax.swing.*;
import java.awt.*;

/**
 * Simula o escalonamento de processos com quantum fixo e operações de I/O.
 *
 * O algoritmo implementa um escalonador Round Robin com suporte a operações de I/O simuladas.
 * Cada processo pode ser enviado para I/O aleatoriamente durante sua execução (TEMOS QUE CHECAR COM A PROFESSORA SE É ASSIM MESMO).
 * O estado dos processos é impresso no console a cada passo.
 */

public class EscalonadorUI {
    // PCB (Process Control Block)
    static class PCB {
        // Identificador do processo
        int pid;
        // Identificador do processo pai
        int ppid;
        // Prioridade
        // 1 = alta, 0 = baixa
        int prioridade;
        // "PRONTO", "IO", "TERMINADO"
        // "PRONTO" = pronto para execução
        // "IO" = em operação de I/O
        // "TERMINADO" = finalizado (Restante = 0)
        String status;
        // Tempo restante de execução (Quantum)
        int restante;
        // Nome do processo (Geramos um sequencial)
        // PA...PB...PC...
        String nome;
        PCB(int pid, int ppid, int prioridade, String nome, int restante) {
            this.pid = pid;
            this.ppid = ppid;
            this.prioridade = prioridade;
            this.status = "PRONTO";
            this.nome = nome;
            this.restante = restante;
        }
    }

    // Tipos de IO
    static final int IO_DISCO = 0;
    static final int IO_FITA = 1;
    static final int IO_IMPRESSORA = 2;
    static final String[] IO_NOMES = {"Disco", "Fita", "Impressora"};
    // IO tem tempo fixo
    // OBS: Sequencia -> Disco, Fita, Impressora
    static final int[] IO_TEMPOS = {6, 8, 10};

    // -------------------
    // Configurações iniciais
    // -------------------

    // Limite máximo de processos
    static final int MAX_PROCESSOS = 5; // Para visualização, menos processos
    // Quantum fixo para o escalonador
    static final int QUANTUM = 2;

    // Lista de PCBs
    static PCB[] pcbs = new PCB[MAX_PROCESSOS];
    static int numProcessos = 0;

    // Filas de prioridade
    static int[] filaAlta = new int[MAX_PROCESSOS];
    static int tamFilaAlta = 0;
    static int[] filaBaixa = new int[MAX_PROCESSOS];
    static int tamFilaBaixa = 0;
    // Fila de IO
    static int[] filaIO = new int[MAX_PROCESSOS];
    static int[] tiposIO = new int[MAX_PROCESSOS];
    static int[] temposIO = new int[MAX_PROCESSOS];
    static int tamFilaIO = 0;

    // Linha do tempo dos eventos
    static String[] linhaTempo = new String[200];
    static int[] blocosTamanhos = new int[200];
    static int tamLinhaTempo = 0;
    // Tempo total decorrido
    static int tempoTotal = 0;
    static int tempoTotalIO = 0;

    // Variáveis de interface (apenas para visualização)
    static JFrame frame;
    static JPanel timelinePanel;

    /**
     * Gera um número aleatório no intervalo [min, max].
     */
    public static int geraNumeroIntervalo(int min, int max) {
        int numero = min + (int)(Math.random() * ((max - min) + 1));
        return numero;
    }

    /**
     * Função principal: executa a simulação passo a passo até todos os processos terminarem.
     */
    public static void main(String[] args) {
        // Inicializa processos (aleatórios)
        int quantidadeProcessosInicias = geraNumeroIntervalo(3, MAX_PROCESSOS);
        for (int i = 0; i < quantidadeProcessosInicias; i++) {
            // Identificador do processo (Usa o índice)
            int pid = i;
            // Inicialmente nenhum processo tem pai
            int ppid = -1;
            // Inicialmente todos os processos são de alta prioridade
            int prioridade = 1;
            // Sequencial para nome
            String nome = "P" + (char)('A'+i);
            // Baseado no intervalo de geração da duração pode fazer o processamento durar bastante
            // Dimunido para 5-20 para visualização
            int duracaoInicial = geraNumeroIntervalo(5, 15); 
            pcbs[i] = new PCB(pid, ppid, prioridade, nome, duracaoInicial);
            filaAlta[tamFilaAlta++] = i;
            numProcessos++;
        }
        SwingUtilities.invokeLater(EscalonadorUI::criarJanela);
    }

    /**
     * Cria a janela da interface gráfica e inicializa os componentes.
     */
    static void criarJanela() {
        frame = new JFrame("Linha do Tempo de Escalonamento (Round Robin com Prioridade)");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1100, 400);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());

        timelinePanel = new JPanel() {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(1100, 300);
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
     * - Imprime o estado atual dos processos no console.
     */
    static void avancarSimulacao() {
        // Loop da fila de IO
        // Loop feito iniciando no final para facilitar remoção
        // Caso fosse do início teria que ajustar o índice ao remover
        for (int i = tamFilaIO - 1; i >= 0; i--) {
            temposIO[i] -= QUANTUM;
            tempoTotalIO += QUANTUM;
            if (temposIO[i] <= 0) {
                int idx = filaIO[i];
                int tipo = tiposIO[i];
                PCB pcb = pcbs[idx];
                pcb.status = "PRONTO";
                int tempoRestanteAntes = pcb.restante;
                // Remove da fila de IO
                for (int j = i; j < tamFilaIO - 1; j++) {
                    filaIO[j] = filaIO[j + 1];
                    tiposIO[j] = tiposIO[j + 1];
                    temposIO[j] = temposIO[j + 1];
                }
                tamFilaIO--;
                // Regras de retorno
                // - Tipos de I/O:
                //  - Disco → fila baixa;
                //  - Fita magnética → fila alta;
                //  - Impressora → fila alta;
                if (tipo == IO_DISCO) {
                    filaBaixa[tamFilaBaixa++] = idx;
                    pcb.prioridade = 0;
                } else {
                    filaAlta[tamFilaAlta++] = idx;
                    pcb.prioridade = 1;
                }
                blocosTamanhos[tamLinhaTempo] = tempoRestanteAntes;
                linhaTempo[tamLinhaTempo++] = pcb.nome + ":IOFIM" + IO_NOMES[tipo];
            }
        }

        // Índice do processo a ser executado
        int idx = -1;
        // Seleciona próximo processo a executar (prioridade alta primeiro)
        if (tamFilaAlta > 0) {
            idx = filaAlta[0];
            for (int i = 0; i < tamFilaAlta - 1; i++) filaAlta[i] = filaAlta[i + 1];
            tamFilaAlta--;
        } else if (tamFilaBaixa > 0) {
            idx = filaBaixa[0];
            for (int i = 0; i < tamFilaBaixa - 1; i++) filaBaixa[i] = filaBaixa[i + 1];
            tamFilaBaixa--;
        }

        // Se encontrou um processo para executar
        if (idx != -1) {
            PCB pcb = pcbs[idx];
            int tempoRestanteAntes = pcb.restante;
            // Simula chance de ir para I/O (20%)
            if (pcb.restante > 0 && Math.random() < 0.2) {
                int tipoIO = geraNumeroIntervalo(0, 2);
                int tempoIO = IO_TEMPOS[tipoIO] + geraNumeroIntervalo(0, 2);
                filaIO[tamFilaIO] = idx;
                tiposIO[tamFilaIO] = tipoIO;
                temposIO[tamFilaIO] = tempoIO;
                tamFilaIO++;
                pcb.status = "IO";
                blocosTamanhos[tamLinhaTempo] = tempoRestanteAntes;
                linhaTempo[tamLinhaTempo++] = pcb.nome + ":IO" + IO_NOMES[tipoIO];
            } else if (pcb.restante > 0) {
                int quantumExecutado = Math.min(QUANTUM, pcb.restante);
                pcb.restante -= quantumExecutado;
                tempoTotal += quantumExecutado;
                blocosTamanhos[tamLinhaTempo] = tempoRestanteAntes;
                linhaTempo[tamLinhaTempo++] = pcb.nome + ":Q" + quantumExecutado;
                if (pcb.restante > 0) {
                    filaBaixa[tamFilaBaixa++] = idx;
                    pcb.prioridade = 0;
                } else {
                    pcb.status = "TERMINADO";
                }
            }
        }
        timelinePanel.repaint();
    }

    /**
     * Desenha a linha do tempo dos processos e a fila de I/O.
     */
    static void desenharPainel(Graphics g) {
        // Topo: processos restantes e quantuns
        g.setFont(new Font("Arial", Font.BOLD, 16));
        StringBuilder status = new StringBuilder("Restantes: ");
        for (int i = 0; i < numProcessos; i++) {
            status.append(pcbs[i].nome).append(": ").append(pcbs[i].restante);
            if (i < numProcessos - 1) status.append(" | ");
        }
        int statusWidth = g.getFontMetrics().stringWidth(status.toString());
        g.drawString(status.toString(), (g.getClipBounds().width - statusWidth) / 2, 30);

        // Todos os blocos em uma única linha
        int alturaBloco = 40;
        int larguraUnidade = 10;
        int y = 100;
        int x = 100;
        int eixoY = y + alturaBloco + 10;
        int tempo = 0;
        for (int t = 0; t < tamLinhaTempo; t++) {
            String[] partes = linhaTempo[t].split(":");
            String nome = partes[0];
            int tempoRestanteAntes = blocosTamanhos[t];
            if (partes[1].startsWith("IO")) {
                if (partes[1].startsWith("IOFIM")) {
                    // Fim de IO, bloco escuro pequeno
                    int largura = 20;
                    g.setColor(Color.DARK_GRAY);
                    g.fillRect(x, y, largura, alturaBloco);
                    g.setColor(Color.WHITE);
                    g.setFont(new Font("Arial", Font.BOLD, 14));
                    g.drawString(nome + ":FIM-" + partes[1].substring(5), x + 2, y + 28);
                    g.setColor(Color.BLACK);
                    g.drawString(Integer.toString(tempo), x - 5, eixoY + 20);
                    g.drawLine(x, eixoY, x + largura, eixoY);
                    x += largura;
                } else {
                    // IO normal, largura proporcional ao tempo restante antes do IO
                    int largura = tempoRestanteAntes * larguraUnidade;
                    String tipo = partes[1].substring(2);
                    g.setColor(Color.GRAY);
                    g.fillRect(x, y, largura, alturaBloco);
                    g.setColor(Color.WHITE);
                    g.setFont(new Font("Arial", Font.BOLD, 16));
                    g.drawString(nome + ":IO-" + tipo, x + 2, y + 28);
                    g.setColor(Color.BLACK);
                    g.drawString(Integer.toString(tempo), x - 5, eixoY + 20);
                    g.drawLine(x, eixoY, x + largura, eixoY);
                    x += largura;
                    tempo += QUANTUM; 
                }
            } else if (partes[1].startsWith("Q")) {
                int largura = tempoRestanteAntes * larguraUnidade;
                Color cor = nome.equals("PA") ? new Color(255, 140, 0) : nome.equals("PB") ? new Color(200, 60, 60) : nome.equals("PC") ? new Color(60, 120, 200) : nome.equals("PD") ? new Color(60, 200, 120) : new Color(120, 60, 200);
                g.setColor(cor);
                g.fillRect(x, y, largura, alturaBloco);
                g.setColor(Color.WHITE);
                g.setFont(new Font("Arial", Font.BOLD, 18));
                g.drawString(nome, x + largura / 2 - 8, y + 28);
                g.setColor(Color.BLACK);
                g.drawString(Integer.toString(tempo), x - 5, eixoY + 20);
                g.drawLine(x, eixoY, x + largura, eixoY);
                x += largura;
                int quantumExecutado = Integer.parseInt(partes[1].substring(1));
                tempo += quantumExecutado;
            }
        }
        g.setColor(Color.BLACK);
        g.drawString(Integer.toString(tempo), x - 5, eixoY + 20);

        // Desenha filas
        int yFila = eixoY + 40;
        g.setFont(new Font("Arial", Font.BOLD, 14));
        g.setColor(Color.BLACK);
        g.drawString("Fila alta:", 100, yFila);
        int xFila = 200;
        for (int i = 0; i < tamFilaAlta; i++) {
            PCB pcb = pcbs[filaAlta[i]];
            g.setColor(new Color(255, 140, 0));
            g.fillRect(xFila, yFila - 18, 60, 30);
            g.setColor(Color.WHITE);
            g.drawString(pcb.nome, xFila + 5, yFila);
            xFila += 70;
        }
        yFila += 40;
        g.setColor(Color.BLACK);
        g.drawString("Fila baixa:", 100, yFila);
        xFila = 200;
        for (int i = 0; i < tamFilaBaixa; i++) {
            PCB pcb = pcbs[filaBaixa[i]];
            g.setColor(new Color(60, 120, 200));
            g.fillRect(xFila, yFila - 18, 60, 30);
            g.setColor(Color.WHITE);
            g.drawString(pcb.nome, xFila + 5, yFila);
            xFila += 70;
        }
        yFila += 40;
        g.setColor(Color.BLACK);
        g.drawString("Fila de I/O:", 100, yFila);
        xFila = 200;
        for (int i = 0; i < tamFilaIO; i++) {
            PCB pcb = pcbs[filaIO[i]];
            int tempoIO = temposIO[i];
            String tipo = IO_NOMES[tiposIO[i]];
            g.setColor(Color.GRAY);
            g.fillRect(xFila, yFila - 18, 80, 30);
            g.setColor(Color.WHITE);
            g.drawString(pcb.nome + "(" + tipo + ":" + tempoIO + ")", xFila + 5, yFila);
            xFila += 90;
        }
    }
}
