# CLAUDE.md

Instruções e plano de implementação do app. Leia este arquivo inteiro antes de qualquer alteração.

## 1. Visão geral

App Android local, pessoal, para capturar tarefas e anotações durante o dia, escolher focos diários na revisão noturna e acompanhar as realizações por área em um dashboard mensal.

Não há servidor, conta, sincronização nem telemetria. Os dados ficam no aparelho. O backup é responsabilidade do usuário e acontece por arquivos que ele escolhe, via Storage Access Framework (o Google Drive entra se o usuário tiver o provedor instalado, sem integração direta).

Idioma da interface: português do Brasil. Identificadores de código em inglês.

## 2. Stack

- Kotlin, Jetpack Compose, Material 3
- `minSdk 34` (Android 14)
- Room para dados, DataStore para preferências
- WorkManager para revisões, lembretes e backup automático
- Jetpack Glance para o widget
- Storage Access Framework para pastas de backup
- Sem Material You (cores dinâmicas ficam fora da v1)

## 3. Decisões fechadas (não reabrir sem consulta)

- **Prazo obrigatório** para tarefas, editável depois. Anotações não têm prazo.
- **Até 3 áreas** por registro.
- **Áreas fixas** após o onboarding. Lista inicial editável só no onboarding.
- **`criado_em` nunca muda**, mesmo com adiamento ou reordenação. A idade é a base da ordenação.
- **Focos no topo** da lista principal, depois o restante por idade (mais antiga primeiro).
- **Ordem manual** vale até o fim do dia. No dia seguinte, a regra de idade volta, exceto para focos.
- **Tarefas não concluídas** voltam automaticamente no dia seguinte.
- **Revisão diária às 21h**, com mínimo de 3 focos (pode ser mais).
- **Revisão de domingo às 19h**, ritual do app, não tarefa recorrente.
- **Sem gamificação** de nenhum tipo: sem sequências, selos, contadores de progresso ou planta que cresce.
- **Três temas visuais** (Caderno de Outono, Aconchego Noturno, Jardim de Bolso), cada um com claro e escuro. O tema altera só a estética. Textos e comportamento são iguais nos três.
- **Escolha de tema no primeiro uso**, alterável depois em Configurações.
- **Onboarding com resposta obrigatória sobre backup.** A escolha negativa é permitida, com aviso claro. Só então o onboarding é concluído.
- **Exportação e importação JSON** entram na v1, com `schemaVersion`.
- **Backup automático** via SAF, com os últimos 7 arquivos mantidos.
- **Exclusão do backup automático do Android** apenas para banco interno e preferências. O arquivo exportado pelo usuário não é excluído.
- **Sem cores dinâmicas, sem recorrência de tarefas, sem edição de áreas após o onboarding, sem compartilhamento.**

## 4. Regras de trabalho para o Claude Code

### 4.1 Commits
- **Um commit por etapa marcada.** Cada item `- [ ]` da seção 6 vira um commit quando concluído. Itens muito pequenos e do mesmo arquivo podem ir juntos, mas nunca dois itens de fases diferentes no mesmo commit.
- **Formato da mensagem:** Conventional Commits em português.
  - `feat(dados): ...`, `feat(onboarding): ...`, `feat(lista): ...`, `test(...)`, `fix(...)`, `chore(...)`, `docs(...)`.
  - Corpo curto, dizendo o que mudou e por quê, quando não for óbvio.
- **Marque o item como concluído (`- [x]`) no mesmo commit** em que ele foi implementado. O histórico do CLAUDE.md passa a ser o progresso real.
- **Antes de cada commit:** o projeto compila, os testes passam e o app abre sem crash no emulador ou aparelho.
- **Nunca commite código quebrado** para "avançar depois". Se um item não puder ser concluído, deixe-o `- [ ]`, explique o bloqueio na conversa e pare.
- Se o repositório git ainda não existir, inicialize-o na Fase 1, item 1.

### 4.2 Escopo
- Implemente apenas o que está no plano. Se algo parecer faltar, pergunte antes de criar.
- Não adicione funcionalidades fora da seção 3 ou do plano, mesmo que pareçam úteis.
- Não altere decisões da seção 3 sem consulta explícita ao usuário.
- Mudanças em arquivos existentes devem ser cirúrgicas: diff mínimo, sem reformatar código alheio à tarefa.

### 4.3 Dados e privacidade
- Nenhuma chamada de rede para serviços de terceiros. Nenhuma dependência de analytics ou crash reporting remoto.
- Nenhum dado sai do aparelho sem ação explícita do usuário (exportar ou escolher pasta de backup).
- Logs de depuração não devem conter conteúdo de tarefas ou anotações.

### 4.4 Testes mínimos por fase
- Fase 1: testes unitários do repositório (limite de 3 áreas, imutabilidade de `criado_em`, regras de foco, prazo obrigatório em tarefas).
- Fase 3: teste de ida e volta (exportar e importar produz os mesmos dados) e teste de validação de `schemaVersion`.
- Fase 5: teste de ordenação (focos no topo, depois idade, ordenações alternativas).
- Fase 9: teste de agregação do dashboard com dados de vários meses.

## 5. Modelo de dados (referência)

- **Registro:** `id`, `tipo` (TAREFA | ANOTACAO), `texto`, `criado_em`, `prazo` (nulo em anotação), `concluido_em` (nulo se pendente), `posicao_manual`, `status`.
- **RegistroArea:** `registro_id`, `area_id`. Máximo de 3 por registro.
- **Area:** `id`, `nome`, `ordem`.
- **Foco:** `data`, `registro_id`, `concluido` (booleano). Um registro por foco escolhido. Alimenta o dashboard.
- **Revisao:** `tipo` (DIARIA | DOMINGO), `data`, `concluida_em`.
- **Backup:** `ultimo_backup_em`, `ultimo_export_em`.
- **Preferências (DataStore):** tema, modo claro/escuro, horário da revisão diária, horário da revisão de domingo, pasta de backup (URI persistida), lembretes ativos, escolha de backup do onboarding.

---

## 6. Plano de implementação

Marque `- [x]` no mesmo commit em que o item for concluído.

### Fase 1: Estrutura de dados
- [x] Criar o projeto Kotlin com Jetpack Compose, Material 3 e `minSdk 34`. Inicializar o git.
- [x] Modelar no Room as entidades Registro, Area, RegistroArea, Foco e Revisao, conforme a seção 5.
- [x] Garantir que `criado_em` nunca seja alterado por adiamento ou reordenação.
- [x] Implementar a regra de prazo obrigatório para tarefas e prazo nulo para anotações.
- [x] Criar o DataStore para as preferências da seção 5.
- [x] Definir as áreas padrão (lista inicial, ainda não fixa).
- [x] Configurar `dataExtractionRules` e `fullBackupContent` para excluir do backup automático do Android o banco e as preferências.
- [x] Escrever os testes unitários da Fase 1 (seção 4.4).

### Fase 2: Tokens de tema
- [x] Criar o sistema de tokens de design: cores, formas, tipografia e espaçamento.
- [x] Implementar o tema Caderno de Outono, com claro e escuro.
- [x] Implementar o tema Aconchego Noturno, com claro e escuro.
- [x] Implementar o tema Jardim de Bolso, com claro e escuro, sem nenhum elemento de progresso.
- [x] Criar o seletor de tema com prévia dos três e transição suave entre eles.

### Fase 3: Backup, exportação e importação
- [x] Exportar para JSON com `schemaVersion`, pelo seletor "Salvar como" (`ACTION_CREATE_DOCUMENT`).
- [x] Importar com validação de `schemaVersion`, prévia das contagens e os modos mesclar (padrão) e substituir.
- [x] Escolher a pasta de backup via `ACTION_OPEN_DOCUMENT_TREE` e persistir a permissão com `takePersistableUriPermission`.
- [x] Implementar o backup diário com WorkManager, mantendo os últimos 7 arquivos.
- [x] Detectar permissão perdida ou provedor removido e avisar o usuário na próxima gravação.
- [x] Exibir o status do backup: último arquivo gravado e último export.
- [x] Escrever os testes da Fase 3 (seção 4.4).

### Fase 4: Onboarding
- [x] Tela de boas-vindas curta.
- [x] Tela de armazenamento e backup com resposta obrigatória: escolher pasta agora, ou seguir sem backup após a confirmação "Entendi, seguir sem backup". Só avança com uma das duas.
- [x] Escolha de tema com prévia dos três.
- [x] Áreas: manter, editar ou criar, com o aviso de que depois do onboarding elas são fixas.
- [x] Aviso de que o prazo pode ser alterado depois.
- [x] Horários das revisões com padrão 21h (diária) e 19h (domingo).
- [x] Marcar o onboarding como concluído somente após a resposta sobre backup.

### Fase 5: Lista principal
- [x] Focos do dia no topo, depois o restante por idade, da mais antiga para a mais recente.
- [x] Seletor de ordenação: "mais antigas" (padrão), "prazo mais próximo" e "mais recentes". Os focos permanecem no topo em qualquer ordenação.
- [x] Item com texto, áreas, indicador de prazo (vencido, hoje, próximos dias) e idade.
- [x] Seção separada para anotações, sem prazo.
- [x] Conclusão com animação discreta e feedback háptico.
- [x] Estado vazio com ilustração simples.
- [x] Tarefa vencida mantém a posição, com destaque visual.
- [x] Escrever os testes da Fase 5 (seção 4.4).

### Fase 6: Captura e edição
- [x] Captura rápida em bottom sheet, acessível de qualquer tela, com campo de texto e seletor "anotação" ou "tarefa".
- [x] Ao escolher tarefa, exibir prazo e áreas.
- [x] Converter anotação em tarefa.
- [x] Editar prazo e áreas a qualquer momento.

### Fase 7: Revisão diária (21h)
- [x] Listar pendentes ordenadas por idade.
- [x] Reordenação manual por arrastar. A ordem manual vale até o fim do dia.
- [x] Escolha de 3 focos para o dia seguinte, com possibilidade de mais.
- [x] Tarefas não concluídas voltam automaticamente no dia seguinte.
- [x] Notificação via WorkManager no horário configurado.

### Fase 8: Revisão de domingo (19h)
- [x] Resumo da semana: concluídas, adicionadas e focos cumpridos.
- [x] Triagem das pendentes mais antigas: manter, reagendar ou descartar.
- [x] Lembrete de backup, com atalho para fazer agora.
- [x] Notificação via WorkManager no horário configurado.

### Fase 9: Dashboard mensal
- [x] Total de tarefas concluídas no mês.
- [x] Concluídas por área, com proporção.
- [x] Dias em que os 3 focos foram cumpridos.
- [x] Tarefas extras além dos focos.
- [x] Distribuição por dia da semana de adicionadas e concluídas.
- [x] Navegação entre meses.
- [x] Escrever os testes da Fase 9 (seção 4.4).

### Fase 10: Widget e notificações
- [x] Widget com Jetpack Glance: focos do dia e atalho para captura rápida, com tipografia simplificada em cada tema.
- [ ] Lembrete de backup semanal, se o último backup tiver mais de 7 dias ou se o automático estiver desligado.
- [ ] Aviso de prazo opcional, desligado por padrão.
- [ ] Pedir a permissão de notificação quando a função for ativada, não no primeiro uso.

### Fase 11: Configurações
- [ ] Tema e modo claro/escuro.
- [ ] Horários das revisões.
- [ ] Lembretes e notificações.
- [ ] Dados e backup: exportar, importar, pasta automática, status e a escolha negativa do onboarding, com destaque discreto.

### Fase 12: Testes e QA
- [ ] Fluxo completo de formatação: instalar, importar o último arquivo e conferir os dados.
- [ ] Backup em pen drive, armazenamento interno e pasta do Google Drive via SAF.
- [ ] Perda de permissão de pasta: detecção e aviso.
- [ ] Tipografia do widget validada em cada tema.
- [ ] Dashboard testado com vários meses de dados reais.

## 7. Ao concluir cada fase

1. Rode a suíte de testes completa.
2. Confirme que todos os itens da fase estão `- [x]`.
3. Faça um commit de fechamento: `chore(fase-N): concluir fase N`.
4. Resuma para o usuário, em poucas linhas, o que foi entregue e o que ficou pendente. Não repita o plano.