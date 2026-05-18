transcript on
if {[file exists rtl_work]} {
	vdel -lib rtl_work -all
}
vlib rtl_work
vmap work rtl_work

vcom -93 -work work {C:/ISEL/2_Semestre/LIC/Atividades/Projeto/Key_Scan/Decoder_Test/Decoder.vhd}

