# Trabalho Avaliativo 1 – Simulação de Escalonamento de Processos

## 1. Objetivo do Trabalho

Estimular a capacidade do aluno de trabalhar em equipe para organizar, projetar e desenvolver soluções para problemas formulados que envolvam o estudo e o conhecimento sobre gerenciamentos do sistema operacional.

## 2. Escopo do Trabalho

- Desenvolver um simulador que implementa o algoritmo de escalonamento.

## 7. Premissas a serem definidas pelo grupo para o Desenvolvimento do Simulador

- Limite máximo de processos criados: 10 PROCESSOS;
- Definição da fatia de tempo dada aos processos em execução: 2 QUANTUM; [ATENDIDO]
- Tempos de serviço e de I/O aleatórios para cada processo criado; [ATENDIDO]
- Tempos de duração de cada tipo de I/O (disco, fita magnética e impressora);
- Gerência de Processos: definição do PID, informações do PCB (prioridade, PID, PPID, status);
- Escalonador com pelo menos 3 filas: alta prioridade, baixa prioridade e fila(s) de I/O;
- Tipos de I/O:
  - Disco → fila baixa;
  - Fita magnética → fila alta;
  - Impressora → fila alta;
- Ordem de entrada na fila de prontos: novos processos → fila alta; processos de I/O → depende do tipo; preempção → fila baixa.

