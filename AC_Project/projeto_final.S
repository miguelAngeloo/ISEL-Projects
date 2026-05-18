; Ficheiro:  projeto_final.S
; Descricao: Programa para a realização do Projeto Final de Arquitetura de Computadores.
; Autores:   Jessé Alencar - A51745,
;			 Miguel Ângelo - A51746,
;            Cátia Abrantes - A51747.
; Data:      05-06-2024
; Turma:     LEIC22D

; Definicao dos valores dos simbolos utilizados no programa
;
	.equ	CPSR_BIT_I, 0b010000          ; Mascara para o bit I do registo CPSR

	.equ	STACK_SIZE, 64                ; Dimensao do stack - 64 B

	; Definicoes do porto de entrada
	.equ	INPORT_ADDRESS, 0xFF80        ; Endereco do porto de entrada

	; Definicoes do porto de saida
	.equ	OUTPORT_ADDRESS, 0xFFC0       ; Endereco do porto de saida

	.equ	OUTPORT_INIT_VAL, 0           ; Valor inicial do porto de saida

	; Definicoes do circuito pTC
	.equ	PTC_ADDRESS,  0xFF40 ; sinal nCS_EXT1     ; Endereco do circuito pTC

	.equ	PTC_TCR, 0                    ; Deslocamento do registo TCR do pTC
	.equ	PTC_TMR, 2                    ; Deslocamento do registo TMR do pTC
	.equ	PTC_TC,  4                    ; Deslocamento do registo TC do pTC
	.equ	PTC_TIR, 6                    ; Deslocamento do registo TIR do pTC

	.equ	PTC_CMD_START, 0              ; Comando para iniciar a contagem no pTC
	.equ	PTC_CMD_STOP, 1               ; Comando para parar a contagem no pTC

    ; Configuracao da frequencia de Sysclk
	.equ	SYSCLK_FREQ, 0x63         ; Intervalo de contagem do circuito pTC
                                      ; que suporta a implementação do sysclk

    ; Configuracoes de tempo
	.equ	BLINK_TIME, 5                 ; Intervalo mínimo de tempo para alteração do estado dos LED's (Ligado/Desligado)
    .equ    CROSS_TIME, 100               ; Intervalo mínimo de tempo para a paddagem dos pedestres

    ;Entradas
    .equ PEDESTRIAN_BUTTON, 0x01
    .equ CONFIG, 0x10
    .equ TIME, 0xE0

    ;Saídas
    .equ TRAFFIC_LIGHT_R, 0x01
    .equ TRAFFIC_LIGHT_G, 0x02
    .equ PEDESTRIAN_LIGHT_R, 0x04
    .equ PEDESTRIAN_LIGHT_G, 0x08
    .equ MODE_R, 0x10
    .equ MODE_G, 0x20

    .equ CROSS_TIME_MASK, 0x07
    .equ CLEAR_LEDS_MASK, 0x3F


; Seccao:    startup
; Descricao: Guarda o código de arranque do sistema
;
	.section startup
	b	_start
	ldr	pc, isr_addr
_start:
	ldr	sp, stack_top_addr
    mov r0, pc
    add lr, r0, #4
	ldr	pc, main_addr
    b   .

stack_top_addr:
	.word	stack_top
main_addr:
	.word	main
isr_addr:
	.word	isr

times_addr:
	.word	Times

; Seccao:    text
; Descricao: Guarda o código do programa
;
	.text

; Rotina:    main
; Descricao: Inicializa o sistema e entra no ciclo principal de funcionamento de acordo com o estado atual
; Entradas:  -
; Saidas:    -
; Efeitos:   Inicializa o porto de saida, o periferico pTC, a variavel global sysclk e habilita as interrupcoes.
;            Realiza a primeira leitura do porto de entrada e entra no estado de acordo com o valor lido.
main:
	mov	r0, #OUTPORT_INIT_VAL           ; Inicializar o porto de saida
	bl	outport_init
	mov	r0, #SYSCLK_FREQ                ; Inicializar o periferico pTC e a variavel global sysclk
	bl	sysclk_init
	mrs	r0, cpsr
	mov	r1, #CPSR_BIT_I                 ; Desativar as interrupcoes
	orr	r0, r0, r1
	msr	cpsr, r0
    bl inport_read
    ldr r4, prev_button_pos_addr        ; Verificar a posição inicial do botão dos pedestres
    mov r5, #PEDESTRIAN_BUTTON
    and r5,r5,r0
    strb r5, [r4,#0]                    ; Colocar a posição inicial na memória        
	bl to_config_button
    b op_state                          ; Verificar o botão de configuração

; Rotina: to_config_button
; Descricao: Rotina de entrada no estado de configuração caso o switch de configuração esteja ligado
to_config_button:
    push lr
	bl inport_read
	mov r1, #CONFIG
	and r0, r0, r1
	mov r4, #0
	cmp r0, r4
	bzc config_state            ;se o botão de configuração for '1', vai para o estado de configuração
    pop lr
    mov pc, lr                  ;caso contrario, volta onde estava antes de chamar a rotina

; Rotina: from_config_button
; Descricao: Rotina de saída do estado de configuração caso o switch de configuração esteja desligado
from_config_button:
    push lr
    bl inport_read
	mov r1, #CONFIG
	and r0, r0, r1
	mov r4, #0
	cmp r0, r4
	bzs op_state                ;se o botão de configuração for '0', vai para o estado de operação
    pop lr
    mov pc, lr                  ;caso contrário volta onde estava

; Rotina: p_button
; Descricao: Rotina de verificação de transição do botão de pedestres
p_button:
    push lr
    push r4
    push r5
    push r6
    push r7
    bl inport_read
    ldr r4, prev_button_pos_addr
    ldrb r7, [r4,#0]                  ; em r4 o valor da posição anterior do botão
    mov r5, #PEDESTRIAN_BUTTON
    and r5,r5,r0                      ; verificar a posição atual do botão de pedrestres, utilizando um 'and' entre a máscara do mesmo e o valor do porto de entrada
    sub r0, r5, r7                    ; valor de comparação entre a posição do botão atual e anterior (guardado em r0), se ocorrer uma transição de '0' - '1', então a diferença entre os dois é 1
    mov r6, #1
    cmp r6, r0                        ;verificar se houve transição: se a diferença for igual a zero, então houve transição, caso contrário não esxiste transição
    bne no_transaction
    strb r5, [r4, #0]                 ;passar o valor do botão atual para o valor do botão anterior
    pop r7
    pop r6
    pop r5
    pop r4
    pop lr  
    b cross_state

; Caso não haja transição do botão de pedestres a rotina retorna ao estado de operação
no_transaction:
    strb r5, [r4,#0]
    pop r7
    pop r6
    pop r5
    pop r4
    pop lr                            ;mesmo que não haja transição passa-se o valor do botão atual para o valor do botão anterior
    mov	pc, lr

prev_button_pos_addr:
    .word prev_button_pos

; Rotina: config_state
; Descricao: Estado de configuração onde se escolhe o tempo de espera para a passagem dos pedestres
; Entradas:  -
; Saidas:    -
; Efeitos:   Inicializa os LED's do modo de configuração e realiza a leitura do porto de entrada
;            para escolher o indice do array Times, alterando o estado dos LED's numa frequência de BLINK_TIME
config_state:
 	push r4
    push r5
	push r6
    mov r0, #MODE_G
	mov r1, #MODE_R
    orr r0, r0, r1
    mov r2, #TRAFFIC_LIGHT_G
    mov r3, #TRAFFIC_LIGHT_R
    orr r2, r2, r3
    mov r1, #PEDESTRIAN_LIGHT_G
    orr r8, r1, r2                  ;mover para r2 os LED's que terão que piscar
    orr r1, r0, r8                  ;mover para r0, as máscaras de todos os LED's a serem acesos
    mov r0, #CLEAR_LEDS_MASK 
    bl outport_write_bits           ;r0 = máscara, r1 = valor dos bits especificados pela máscara
    bl sysclk_get_ticks             ;marcar quando se ligaram os LED's (guardado em r0)
    ldr r4, prev_config_time_addr
    strb r0, [r4, #0]               ;guardar na memória o tempo anterior
    ldr r3, config_LED_state_addr
    mov r7, #1
    strb r7, [r3,#0]                ;estado dos LED's -> passa a '1', pois estes foram ligados
    
; Sub-rotina de configuração
; Loop que verifica se o botão de configuração foi alterado e faz a leitura do porto de entrada para 
; escolher o tempo de espera, que e guardado na variavel time_selected
config_state_do:
    bl inport_read
    mov r1, #TIME                   
    and r0, r0, r1                  ;utiliza os três bits mais significativos do porto de entrada para selecionar o índice do array Times
    lsr r0, r0, #4                  ;como cada índice do array Times é composto por 2 bytes, é necessário que os três bits mais significativos bits sejam deslocados em 1 bit para a esquerda:
    ldr r1, Times_addr              ;lsr r0, r0, #5  &  lsl r0, r0, #1             
    ldr r0, [r1, r0]                ;carregar para r1, o valor do array correspondente ao índice representado por r0
    ldr r1, time_selected_addr1
    str r0, [r1, #0]                ;carregar para time_selected, o valor correspondente ao tempo escolhido
    ldr r4, prev_config_time_addr
    ldrb r5, [r4,#0]                ;em r4, está guardado addr do tempo anterior (prev_config_time_addr)
    bl sysclk_get_ticks             ;verificar o tempo atual
    sub r6, r0, r5                  ;subtrair ao tempo atual o tempo anterior
    mov r7, #BLINK_TIME
    bl from_config_button           ;verificar se o botão de configuração foi alterado
    cmp r6, r7
    blo config_state_do             ;se ainda não passou o tempo volta a verificar 
    b config_lights_on_off

time_selected_addr1:
    .word time_selected

Times_addr:
    .word Times

; Sub-rotina de configuração dos LED's
; Mudança de estado dos LED's e atualização do estado dos LED's
config_lights_on_off:
    ldr r3, config_LED_state_addr
    ldrb r0, [r3,#0]                ;verificar o estado dos LED's
    mov r6, #1
    cmp r6, r0
    bne config_lights_on            ;se não estiverem acesos vão ser ligados, caso contrário são apagados
    mov r0, r8                      ;passar para r0, a máscara dos LED's
    bl outport_clear_bits           ;desligar os LED's
    bl sysclk_get_ticks             ;marcar o tempo em que forem desligados
    ldr r4, prev_config_time_addr
    strb r0, [r4,#0]                ;guardar na memória o novo tempo anterior
    mov r0, #0
    strb r0, [r3, #0]               ;mudar o estado dos LED's para apagados
    b config_state_do               ;vai voltar a verificar os tempos

; Sub-rotina de ativação dos LED's
config_lights_on:
    mov r0, r8                      ;passar para r0, a máscara dos LED's 
    bl outport_set_bits             ;ligar os LED's
    bl sysclk_get_ticks             ;marcar o tempo em que forem ligados
    ldr r4, prev_config_time_addr
    strb r0, [r4,#0]                ;guardar na memória o novo tempo anterior
    mov r0, #1
    ldr r3, config_LED_state_addr
    strb r0, [r3,#0]                ;mudar o estado dos LED's para acesos
    b config_state_do               ;vai voltar a verificar os tempos

prev_config_time_addr:
    .word prev_config_time

config_LED_state_addr:
    .word config_LED_state

; Rotina: op_state
; Descricao: Estado de operação onde os carros estão a passar
; Entradas:  -
; Saidas:    -
; Efeitos:   Inicializa os LED's do modo de operação e realiza a leitura do porto de entrada para
;            verificar se o switch de configuração foi alterado ou se o botão de pedestres foi pressionado
op_state:
    push r4
    push r5
	push r6
    mov r0, #PEDESTRIAN_LIGHT_R
    mov r1, #MODE_G
    orr r0, r0, r1
    mov r2, #TRAFFIC_LIGHT_G
    mov r3, #TRAFFIC_LIGHT_R
    orr r8, r2, r3
    orr r1, r0, r8
    mov r0, #CLEAR_LEDS_MASK        ;r0 = máscara, r1 = valor dos bits especificados pela máscara
    bl outport_write_bits
    bl sysclk_get_ticks             ;marcar quando se ligaram os LED's (guardado em r0)
    ldr r4, prev_op_time_addr
    strb r0, [r4,#0]                ;guardar na memória o tempo anterior
    ldr r3, traffic_LED_state_addr
    mov r5, #1
    strb r5, [r3,#0]                ;estado dos LED's -> passa a '1', pois estes foram ligados

; Sub-rotina de operação
; Loop que verifica se o switch de configuração foi alterado e se o botão de pedestres foi pressionado
op_state_do:
    ldr r4, prev_op_time_addr
    ldrb r5, [r4,#0]                ;em r4, está guardado addr do tempo anterior (prev_config_time_addr)
    bl sysclk_get_ticks             ;verificar o tempo atual
    sub r6, r0, r5                  ;subtrair ao tempo atual o tempo anterior
    mov r7, #BLINK_TIME
    bl to_config_button            ;verificar se o botão de configuração foi alterado
    bl p_button
    cmp r6, r7
    blo op_state_do              ;se ainda não passou o tempo volta a verificar 
    b lights_on_off

; Sub-rotina de mudança de estado dos LED's
lights_on_off:
    ldr r3, traffic_LED_state_addr
    ldrb r2, [r3,#0]                ;verificar o estado dos LED's
    mov r6, #1
    cmp r6, r2
    bne lights_on                   ;se não estiverem acesos vão ser ligados, caso contrário são apagados
    mov r0, r8                      ;passar para r0, a máscara dos LED's
    bl outport_clear_bits           ;desligar os LED's
    bl sysclk_get_ticks             ;marcar o tempo em que forem desligados
    ldr r4, prev_op_time_addr
    strb r0, [r4,#0]                ;guardar na memória o novo tempo anterior
    mov r0, #0
    strb r0, [r3, #0]               ;mudar o estado dos LED's para apagados
    b op_state_do                   ;vai voltar a verificar os tempos

; Sub-rotina de ativação dos LED's
lights_on:
    mov r0, r8                      ;passar para r0, a máscara dos LED's 
    bl outport_set_bits             ;ligar os LED's
    bl sysclk_get_ticks
    ldr r4, prev_op_time_addr       ;marcar o tempo em que forem ligados
    strb r0, [r4,#0]                ;guardar na memória o novo tempo anterior
    mov r0, #1
    ldr r3, traffic_LED_state_addr
    strb r0, [r3,#0]                ;mudar o estado dos LED's para acesos
    b op_state_do                   ;vai voltar a verificar os tempos

prev_op_time_addr:
    .word prev_op_time

traffic_LED_state_addr:
    .word traffic_LED_state

; Rotina: cross_state
; Descricao: Estado onde os pedestres estão a passar
; Entradas:  -
; Saidas:    -
; Efeitos:   Inicializa os LED's do modo de passagem dos pedestres e realiza a leitura do porto de entrada
;            para verificar se o switch de configuração foi alterado
cross_state:
    mov r0, #MODE_G
    mov r1, #TRAFFIC_LIGHT_R
    mov r2, #PEDESTRIAN_LIGHT_G
    orr r1, r0, r1
    orr r1, r1, r2                        ;juntar as duas máscaras
    mov r0, #CLEAR_LEDS_MASK              ;r0 = máscara, r1 = valor dos bits especificados pela máscara
    bl outport_write_bits                 ;usar write_bits para mudar apenas os LED's dos carros e pedestres
    bl sysclk_get_ticks                   ;guardar o momento, em r0, onde se ligaram os LED's
    ldr r6, prev_op_time_cross_addr
    strb r0, [r6, #0]                     ;previous time passa a ser o momento em que se ligaram os LED's

time_to_cross:
    ldr r6, prev_op_time_cross_addr     
    ldrb r4, [r6, #0] 
    bl sysclk_get_ticks                 ;guardar, em r0, o tempo atual               
    sub r5, r0, r4
    push r5
    ldr r5, time_selected_addr2         ;carrega a variàvel time_selected, que contém o tempo de espera utilizado para a passagem dos pedestres (ms) 
    ldr r7, [r5, #0]
    pop r5
    bl to_config_button                 ;verificar se o botão de configuração
    bl p_button				            ;verificar se houve uma nova transição '0'-'1'
    cmp r5, r7                          ;comparar o tempo que já passou com Crossing time
    blo time_to_cross                   ;se for maior ou igual a crossing time muda de estado, caso contrario volta a cross_state
    b op_state                          ;volta ao estado onde os carros estão a passar

prev_op_time_cross_addr:
    .word prev_op_time_cross

time_selected_addr2:
    .word time_selected

; Rotina:    isr
; Descricao: Incrementa o valor da variável global sysclk.
; Entradas:  -
; Saidas:    -
; Efeitos:   Limpa o pedido de interrupção pendente e incrementa o valor da variável global sysclk.
isr:
	push r0
    push r1
    push r2
    mov  r0, #PTC_ADDRESS & 0xFF
    movt r0, #(PTC_ADDRESS >> 8) & 0xFF
    strb r2, [r0, #PTC_TIR] ; Ativar o valor de TIR que é active LOW de modo a sair do pTC a saida nINT ativa que demonstra pedido de interrupção
    ldr r1, sysclk_addr
    ldr r0, [r1, #0]
    add r0, r0, #1
    str r0, [r1,#0]
    pop r2
    pop r1
    pop r0
    movs pc, lr
    

; Rotina:    sysclk_init
; Descricao: Inicia uma nova contagem no periferico pTC com o intervalo de
;            contagem recebido em R0, em ticks, limpando eventuais pedidos de
;            interrupcao pendentes e iniciando com o valor zero a variavel
;            global sysclk.
;            Interface exemplo: void sysclk_init( uint8_t interval );
; Entradas:  R0 - Valor do novo intervalo de contagem, em ticks.
; Saidas:    -
; Efeitos:   Inicia a contagem no periferico a partir do valor zero, limpando
;            eventuais pedidos de interrupcao pendentes e iniciando com o
;            valor zero a variavel global sysclk
sysclk_init:
		push lr
		push r4
		push r5 
		mov r4, #0 
		ldr r5, sysclk_addr
		str r4, [r5]
		bl ptc_init
		pop r4
		pop r5
		pop pc

; Rotina:    sysclk_get_ticks
; Descricao: Devolve o valor corrente da variável global sysclk.
;            Interface exemplo: uint16_t sysclk_get_ticks ( );
; Entradas:  -
; Saidas:    R0 - O valor corrente da variavel global sysclk.
; Efeitos:   -
sysclk_get_ticks:
	ldr r1 , sysclk_addr
	ldrb r0, [r1, #0] 
	mov pc, lr 

sysclk_addr:
	.word sysclk


; Gestor de periférico para o porto de entrada

; Rotina:    inport_read
; Descricao: Adquire e devolve o valor corrente do porto de entrada.
;            Interface exemplo: uint8_t inport_read( );
; Entradas:  -
; Saidas:    R0 - valor adquirido do porto de entrada
; Efeitos:   -
inport_read:
	ldr	r1, inport_addr
	ldrb	r0, [r1, #0]
	mov	pc, lr

inport_addr:
	.word	INPORT_ADDRESS


; Gestores de periférico para o porto de saída

; Rotina:    outport_set_bits
; Descricao: Atribui o valor logico 1 aos bits do porto de saida identificados
;            com o valor 1 em R0. O valor dos outros bits nao e alterado.
;            Interface exemplo: void outport_set_bits( uint8_t pins_mask );
; Entradas:  R0 - Mascara com a especificacao do indice dos bits a alterar.
; Saidas:    -
; Efeitos:   Altera o valor da variavel global outport_img.
outport_set_bits:
	push	lr
	ldr	r1, outport_img_addr
	ldrb	r2, [r1, #0]
	orr	r0, r2, r0
	strb	r0, [r1, #0]
	bl	outport_write
	pop	pc

; Rotina:    outport_write_bits
; Descricao: Atribui aos bits do porto de saída o valor dos bits
;            correspondentes do argumento do segundo parametro, no caso dos
;            bits correspondentes do argumento do primeiro parametro
;            apresentarem o valor um. O valor dos restantes bits do porto de
;            saida mantem-se inalterado.
;            Interface exemplo:
;                void outport_write_bits( uint8_t pins_mask, uint8_t value );
; Entradas:  r0 - valor a escrever no porto de saida
; Saidas:    -
; Efeitos:   Atualiza o valor da variavel global outport_img
outport_write_bits: 
    push    lr
    ldr    r2, outport_img_addr
    ldr    r3, [r2, #0] 
    and    r1, r0, r1 
    mvn    r0, r0
    and    r0, r3, r0 
    orr    r0, r0, r1 
    strb    r0, [r2, #0]
    bl    outport_write
    pop    pc

; Rotina:    outport_clear_bits
; Descricao: Atribui o valor logico 0 aos bits do porto de saida identificados
;            com o valor 1 em R0. O valor dos outros bits nao e alterado.
;            Interface exemplo: void outport_clear_bits( uint8_t pins_mask );
; Entradas:  R0 - Mascara com a especificacao do indice dos bits a alterar.
; Saidas:    -
; Efeitos:   Altera o valor da variavel global outport_img.
outport_clear_bits:
	push	lr
	ldr	r1, outport_img_addr
	ldrb	r2, [r1, #0]
	mvn	r0, r0
	and	r0, r2, r0
	strb	r0, [r1]
	bl	outport_write
	pop	pc

; Rotina:    outport_init
; Descricao: Faz a iniciacao do porto de saida, nele estabelecendo o valor
;            recebido em R0.
;            Interface exemplo: void outport_init( uint8_t value );
; Entradas:  R0 - Valor a atribuir ao porto de saida.
; Saidas:    -
; Efeitos:   Altera o valor da variavel global outport_img.
outport_init:
	push	lr
	ldr	r1, outport_img_addr
	strb	r0, [r1]
	bl	outport_write
	pop	pc

outport_img_addr:
	.word	outport_img

; Rotina:    outport_write
; Descricao: Escreve no porto de saida o valor recebido em R0.
;            Interface exemplo: void outport_write( uint8_t value );
; Entradas:  R0 - valor a atribuir ao porto de saida.
; Saidas:    -
; Efeitos:   -
outport_write:
	ldr	r1, outport_addr
	strb	r0, [r1, #0]
	mov	pc, lr

outport_addr:
	.word	OUTPORT_ADDRESS


; Gestor de periférico para o Pico Timer/Counter (pTC)

; Rotina:    ptc_init
; Descricao: Faz a iniciacao do periférico pTC, habilitando o seu funcionamento
;            em modo continuo e com o intervalo de contagem recebido em R0, em
;            ticks.
;            Interface exemplo: void ptc_init( uint8_t interval );
; Entradas:  R0 - Valor do novo intervalo de contagem, em ticks.
; Saidas:    -
; Efeitos:   Inicia a contagem no periferico a partir do valor zero, limpando
;            o pedido de interrupcao eventualmente pendente.
ptc_init:
    push    lr
	ldr	r1, PTC_ADDR
	mov	r2, #PTC_CMD_STOP
	strb	r2, [r1, #PTC_TCR]
	strb	r0, [r1, #PTC_TMR]
    bl  ptc_clr_irq
	mov	r2, #PTC_CMD_START
	strb	r2, [r1, #PTC_TCR]
	pop pc

; Rotina:    ptc_start
; Descricao: Habilita a contagem no periferico pTC.
;            Interface exemplo: void ptc_start( );
; Entradas:  -
; Saidas:    -
; Efeitos:   -
ptc_start:
	ldr	r0, PTC_ADDR
	mov	r1, #PTC_CMD_START
	strb	r1, [r0, #PTC_TCR]
	mov	pc, lr

; Rotina:    ptc_stop
; Descricao: Para a contagem no periferico pTC.
;            Interface exemplo: void ptc_stop( );
; Entradas:  -
; Saidas:    -
; Efeitos:   O valor do registo TC do periferico e colocado a zero.
ptc_stop:
	ldr	r0, PTC_ADDR
	mov	r1, #PTC_CMD_STOP
	strb	r1, [r0, #PTC_TCR]
	mov	pc, lr

; Rotina:    ptc_get_value
; Descricao: Devolve o valor corrente da contagem do periferico pTC.
;            Interface exemplo: uint8_t ptc_get_value( );
; Entradas:  -
; Saidas:    R0 - O valor corrente do registo TC do periferico.
; Efeitos:   -
ptc_get_value:
	ldr	r1, PTC_ADDR
	ldrb	r0, [r1, #PTC_TC]
	mov	pc, lr

; Rotina:    ptc_clr_irq
; Descricao: Sinaliza o periferico pTC que foi atendido um pedido de
;            interrupção.
;            Interface exemplo: void ptc_clr_irq( );
; Entradas:  -
; Saidas:    -
; Efeitos:   -
ptc_clr_irq:
	ldr	r0, PTC_ADDR
	strb	r1, [r0, #PTC_TIR]
	mov	pc, lr

PTC_ADDR:
	.word	PTC_ADDRESS


; Seccao:    data
; Descricao: Guarda as variáveis globais
	.data
outport_img:
	.space	1

prev_button_pos:
	.space 1

prev_op_time:
	.byte 1

prev_config_time:
	.byte 1

prev_op_time_cross:
	.byte 1

traffic_LED_state:
	.space 2

config_LED_state:
	.space 2

    .space 1         ;Corrigir o desalinhamento de variáveis na memória

time_selected:
   .space 2

Times:
	.word 100, 220, 350, 470, 600  ;array Times está implementado em décimas de segundo devido ao facto de que a unidade do sysclock ser 0,1s (uma décima de segundo)

;Times:
;    .word 10, 22, 35, 47, 60     -> exemplo do array Times em segundos

	.align
sysclk:
	.space	2


; Seccao:    stack
; Descricao: Implementa a pilha com o tamanho definido pelo simbolo STACK_SIZE
	.stack
	.space	STACK_SIZE
stack_top:
