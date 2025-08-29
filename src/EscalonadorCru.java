/**
 * Simula o escalonamento de processos com quantum fixo e operações de I/O.
 *
 * O algoritmo implementa um escalonador Round Robin com suporte a operações de I/O simuladas(ESQUECI DE COLOCAR PRIORIDADE '-').
 * Cada processo pode ser enviado para I/O aleatoriamente durante sua execução(TEMOS QUE CHECAR COM A PROFESSORA SE É ASSIM MESMO).
 * O estado dos processos é impresso no console a cada passo.
 */
public class EscalonadorCru {
    // -------------------
    // Configurações iniciais
    // -------------------

    // Nomes dos processos
    static String[] nomes = {"A", "B", "C"};
    // Durações totais de cada processo
    static int[] duracoes = {105,200,300};
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
    static int[] filaExecucao = new int[NUM_PROCESSOS]; 
    // Tamanho da fila de execução
    static int tamFilaExecucao = 0; 
    // Índices dos processos em I/O
    static int[] filaIO = new int[NUM_PROCESSOS]; 
    // Tempos restantes de I/O
    static int[] temposIO = new int[NUM_PROCESSOS]; 
    // Tamanho da fila de I/O
    static int tamFilaIO = 0; 
    // Tempo total decorrido
    static int tempoTotal = 0; 
    // Tempo total gasto em I/O
    static int tempoTotalIO = 0; 

    /**
     * Função principal: executa a simulação passo a passo até todos os processos terminarem.
     */
    public static void main(String[] args) {
        // Inicializa os arrays de controle
        for (int i = 0; i < NUM_PROCESSOS; i++) {
            // OBS: Durações é o valor inicial
            // Restantes é o valor que vai ser decrementado
            restantes[i] = duracoes[i];
            filaExecucao[i] = i;
        }
        tamFilaExecucao = NUM_PROCESSOS;

        // Executa a simulação até todos os processos terminarem
        while (tamFilaExecucao > 0 || tamFilaIO > 0) {
            avancarSimulacao();
        }
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
        // Atualiza fila de I/O: decrementa tempos e retorna processos prontos
        for (int i = tamFilaIO - 1; i >= 0; i--) {
            // Decrementa o tempo de I/O
            temposIO[i] -= QUANTUM;
            // Incrementa o tempo total de I/O
            tempoTotalIO += QUANTUM;
            // Se o tempo de I/O terminou, move o processo de volta para a fila de execução
            if (temposIO[i] <= 0) {
                // Indice do processo que terminou I/O
                int idx = filaIO[i];
                // Adiciona à fila de execução
                filaExecucao[tamFilaExecucao++] = idx;
                // Remove da fila de IO
                for (int j = i; j < tamFilaIO - 1; j++) {
                    filaIO[j] = filaIO[j + 1];
                    temposIO[j] = temposIO[j + 1];
                }
                tamFilaIO--;
            }
        }
        // Executa próximo processo 
        if (tamFilaExecucao > 0) {
            int idx = filaExecucao[0];
            // Remove do início da fila
            for (int i = 0; i < tamFilaExecucao - 1; i++) filaExecucao[i] = filaExecucao[i + 1];
            tamFilaExecucao--;
            // Simula chance de ir para I/O (20%)
            if (restantes[idx] > 0 && Math.random() < 0.2) {
                int tempoIO = 4 + (int)(Math.random() * 4); // 4 a 7
                // Adiciona o elemento a fila de I/O
                filaIO[tamFilaIO] = idx;
                // Adiciona o tempo de I/O
                temposIO[tamFilaIO] = tempoIO;
                // Incrementa o tamanho da fila de I/O
                tamFilaIO++;
                System.out.printf("Processo %s foi para I/O por %d unidades de tempo. Restante: %d\n", nomes[idx], tempoIO, restantes[idx]);
            } else if (restantes[idx] > 0) {
                // Se QUANTUM = 2 e restantes[idx] = 3 -> quantumExecutado = 2
                // Se QUANTUM = 2 e restantes[idx] = 1 -> quantumExecutado = 1
                // Evita ficar com restante negativo 
                int quantumExecutado = Math.min(QUANTUM, restantes[idx]);
                // Decrementa o restante do processo
                restantes[idx] -= quantumExecutado;
                // Incrementa o tempo total de processamento
                tempoTotal += quantumExecutado;
                System.out.printf("Processo %s executou %d unidades. Restante: %d\n", nomes[idx], quantumExecutado, restantes[idx]);
                // Se ainda resta algum trabalho no processo, volta para o final da fila de execução
                if (restantes[idx] > 0) {
                    filaExecucao[tamFilaExecucao++] = idx;
                } else {
                    System.out.printf("Processo %s terminou.\n", nomes[idx]);
                }
            }
        }
        // Imprime estado atual das filas
        imprimirEstado();
    }

    /**
     * Imprime o estado atual das filas de prontos e I/O(QUANDO COLOCARMOS PRIORIDADE TEM QUE ADICIONAR AQUI TAMBÉM).
     */
    static void imprimirEstado() {
        System.out.print("Fila de execução: ");
        for (int i = 0; i < tamFilaExecucao; i++) {
            System.out.print(nomes[filaExecucao[i]] + " ");
        }
        System.out.print("| Fila de I/O: ");
        for (int i = 0; i < tamFilaIO; i++) {
            System.out.print(nomes[filaIO[i]] + "(" + temposIO[i] + ") ");
        }
        System.out.println("\n");
    }
}
