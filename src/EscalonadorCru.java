/**
 * Simula o escalonamento de processos com quantum fixo e operações de I/O.
 *
 * O algoritmo implementa um escalonador Round Robin com suporte a operações de I/O simuladas.
 * Cada processo pode ser enviado para I/O aleatoriamente durante sua execução(TEMOS QUE CHECAR COM A PROFESSORA SE É ASSIM MESMO).
 * O estado dos processos é impresso no console a cada passo.
 */
public class EscalonadorCru {
    // PCB (Process Control Block)
    static class PCB {
        // Identificador do processo
        int pid;
        // Indetificador do processo pai
        int ppid;
        // Prioridade
        // 1 = alta, 0 = baixa
        int prioridade;
        // "PRONTO", "IO", "TERMINADO" 
        // "PRONTO" = pronto para execução
        // "IO" = em operação de I/O
        // "TERMINADO" = finalizado (Restante = 0)
        String status; 
        // Tempo restante de execução(Quantum)
        int restante;
        // Nome do processo (Geramos um sequêncial)
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
    static final int MAX_PROCESSOS = 10;
    // Quantum fixo para o escalonador
    static final int QUANTUM = 2;

    // Lista de PCBs
    static PCB[] pcbs = new PCB[MAX_PROCESSOS];
    static int numProcessos = 0;

    // Filas de prioridade(Alta)
    static int[] filaAlta = new int[MAX_PROCESSOS];
    static int tamFilaAlta = 0;

    // Filas de prioridade(Baixa)
    static int[] filaBaixa = new int[MAX_PROCESSOS];
    static int tamFilaBaixa = 0;
    
    // Fila de IO
    static int[] filaIO = new int[MAX_PROCESSOS];
    static int[] tiposIO = new int[MAX_PROCESSOS];
    static int[] temposIO = new int[MAX_PROCESSOS];
    static int tamFilaIO = 0;

    // Tempo total decorrido
    static int tempoTotal = 0;
    static int tempoTotalIO = 0;

    public static int geraNumeroIntervalo(int min, int max) {
        int numero = min + (int)(Math.random() * ((max - min) + 1));
        return numero;
    }

    /**
     * Função principal: executa a simulação passo a passo até todos os processos terminarem.
     */
    public static void main(String[] args) {

       

        // Inicializa processos (aleatórios)
        int quantidadeProcessosInicias =  geraNumeroIntervalo(3, MAX_PROCESSOS);
        for (int i = 0; i < quantidadeProcessosInicias; i++) {
            // Identificador do processo (Usa o índice)
            int pid = i;
            // Inicialmente nenhum processo tem pai
            int ppid = -1;
            // Inicialmente todos os processos são de alta prioridade
            int prioridade = 1; 
            // Sequencial para nome 
            // PA...PB...PC...
            String nome = "P" + (char)('A'+i);
            // Baseado no intervalo de geração da duração pode fazer o processamento durar bastante
            // Para mostrar em aula talvez seja interessante diminuir o intervalo
            int duracaoInicial = geraNumeroIntervalo(50, 300);
            pcbs[i] = new PCB(pid, ppid, prioridade, nome, duracaoInicial);
            filaAlta[tamFilaAlta++] = i;
            numProcessos++;
        }

        // Executa a simulação até todos os processos terminarem
        while (tamFilaAlta > 0 || tamFilaBaixa > 0 || tamFilaIO > 0) {
            avancarSimulacao();
        }

        // Mostra o tempo total de processamento e IO
        System.out.println("\nTodos os processos terminaram. Tempo total: " + tempoTotal);
        System.out.println("Tempo total incluindo I/O: " + (tempoTotal + tempoTotalIO));
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
            // Decrementa tempo de IO
            temposIO[i] -= QUANTUM;
            // Acrescenta tempo total de IO
            tempoTotalIO += QUANTUM;
            // Se o tempo de IO terminou
            if (temposIO[i] <= 0) {
                // Índice do processo que terminou o IO
                int idx = filaIO[i];
                // Tipo de IO que foi realizado
                int tipo = tiposIO[i];
                // PCB do processo
                PCB pcb = pcbs[idx];
                // Altera status para PRONTO
                // Lembrando que pronto é quando pode ser executado na fila de alta/baixa prioridade
                pcb.status = "PRONTO";
                // Remove da fila de IO
                for (int j = i; j < tamFilaIO - 1; j++) {
                    filaIO[j] = filaIO[j + 1];
                    tiposIO[j] = tiposIO[j + 1];
                    temposIO[j] = temposIO[j + 1];
                }
                // Decrementa tamanho da fila de IO
                tamFilaIO--;
                // Regras de retorno, retirei do PDF do trabalho
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
                System.out.printf("Processo %s voltou do I/O (%s) para fila %s. Restante: %d\n", pcb.nome, IO_NOMES[tipo], (tipo==0?"baixa":"alta"), pcb.restante);
            }
        }
        
        // Índice do processo a ser executado
        int idx = -1;
        // Seleciona próximo processo a executar (prioridade alta primeiro)
        if (tamFilaAlta > 0) {
            idx = filaAlta[0];
            // Remove do início da fila e reorganiza os itens posteriores
            for (int i = 0; i < tamFilaAlta - 1; i++) filaAlta[i] = filaAlta[i + 1];
            tamFilaAlta--;
        // Se não houver na fila alta, tenta na baixa
        } else if (tamFilaBaixa > 0) {
            idx = filaBaixa[0];
            // Remove do início da fila e reorganiza os itens posteriores
            for (int i = 0; i < tamFilaBaixa - 1; i++) filaBaixa[i] = filaBaixa[i + 1];
            tamFilaBaixa--;
        }

        // Se encontrou um processo para executar
        if (idx != -1) {
            // PCB do processo
            PCB pcb = pcbs[idx];
            // Simula chance de ir para I/O (20%)
            if (pcb.restante > 0 && Math.random() < 0.2) {
                // O tipo de I/O é escolhido aleatoriamente
                int tipoIO = geraNumeroIntervalo(0, 2);
                // Tempo de I/O é o tempo fixo para o tipo de I/O + um acrescimo aleatório de 0 a 2
                int tempoIO = IO_TEMPOS[tipoIO] + geraNumeroIntervalo(0, 2); // base + 0~2
                // Adiciona à fila de IO
                filaIO[tamFilaIO] = idx;
                tiposIO[tamFilaIO] = tipoIO;
                temposIO[tamFilaIO] = tempoIO;
                tamFilaIO++;
                // Altera status para IO
                pcb.status = "IO";
                System.out.printf("Processo %s foi para I/O (%s) por %d unidades. Restante: %d\n", pcb.nome, IO_NOMES[tipoIO], tempoIO, pcb.restante);
            // Se o processo não foi para I/O
            } else if (pcb.restante > 0) {
                // O min é usado para não executar mais do que o restante
                // Quantum 2, resante 2 -> executa 2
                // Quantum 2, restante 1 -> executa 1
                int quantumExecutado = Math.min(QUANTUM, pcb.restante);
                // Decrementa o restante
                pcb.restante -= quantumExecutado;
                // Acrescenta ao tempo total
                tempoTotal += quantumExecutado;
                System.out.printf("Processo %s executou %d unidades. Restante: %d\n", pcb.nome, quantumExecutado, pcb.restante);
                // Se ainda resta execução 
                if (pcb.restante > 0) {
                    // Preempção: volta para fila baixa
                    filaBaixa[tamFilaBaixa++] = idx;
                    pcb.prioridade = 0;
                } else {
                    pcb.status = "TERMINADO";
                    System.out.printf("Processo %s terminou.\n", pcb.nome);
                }
            }
        }

        imprimirEstado();
    }

    /**
     * Imprime o estado atual das filas de prontos e I/O.
     */
    static void imprimirEstado() {
        System.out.print("Fila alta: ");
        for (int i = 0; i < tamFilaAlta; i++) {
            PCB pcb = pcbs[filaAlta[i]];
            System.out.print(pcb.nome + "(PID:"+pcb.pid+") ");
        }
        System.out.print("| Fila baixa: ");
        for (int i = 0; i < tamFilaBaixa; i++) {
            PCB pcb = pcbs[filaBaixa[i]];
            System.out.print(pcb.nome + "(PID:"+pcb.pid+") ");
        }
        System.out.print("| Fila de I/O: ");
        for (int i = 0; i < tamFilaIO; i++) {
            PCB pcb = pcbs[filaIO[i]];
            System.out.print(pcb.nome + "(" + IO_NOMES[tiposIO[i]] + ":" + temposIO[i] + ") ");
        }
        System.out.println("\n");
    }
}
