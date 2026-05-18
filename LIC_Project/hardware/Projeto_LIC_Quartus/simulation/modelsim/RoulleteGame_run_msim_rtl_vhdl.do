transcript on
if {[file exists rtl_work]} {
	vdel -lib rtl_work -all
}
vlib rtl_work
vmap work rtl_work

vcom -93 -work work {C:/ISEL/4_Semestre/LIC/Projeto_LIC_24-25/hardware/SLCDC/EQUALS.vhd}
vcom -93 -work work {C:/ISEL/4_Semestre/LIC/Projeto_LIC_24-25/hardware/keyboard/Memory_Address_Control.vhd}
vcom -93 -work work {C:/ISEL/4_Semestre/LIC/Projeto_LIC_24-25/hardware/keyboard/Buffer_Control.vhd}
vcom -93 -work work {C:/ISEL/4_Semestre/LIC/Projeto_LIC_24-25/hardware/keyboard/RAM.vhd}
vcom -93 -work work {C:/ISEL/4_Semestre/LIC/Projeto_LIC_24-25/hardware/keyboard/Output_Buffer.vhd}
vcom -93 -work work {C:/ISEL/4_Semestre/LIC/Projeto_LIC_24-25/hardware/keyboard/KeyBoardReader.vhd}
vcom -93 -work work {C:/ISEL/4_Semestre/LIC/Projeto_LIC_24-25/hardware/keyboard/KEY_SCAN.vhd}
vcom -93 -work work {C:/ISEL/4_Semestre/LIC/Projeto_LIC_24-25/hardware/keyboard/Key_Decode.vhd}
vcom -93 -work work {C:/ISEL/4_Semestre/LIC/Projeto_LIC_24-25/hardware/keyboard/Ring_Buffer_Control.vhd}
vcom -93 -work work {C:/ISEL/4_Semestre/LIC/Projeto_LIC_24-25/hardware/keyboard/Ring_Buffer.vhd}
vcom -93 -work work {C:/ISEL/4_Semestre/LIC/Projeto_LIC_24-25/hardware/keyboard/Key_Control.vhd}
vcom -93 -work work {C:/ISEL/4_Semestre/LIC/Projeto_LIC_24-25/hardware/keyboard/clkDIV.vhd}
vcom -93 -work work {C:/ISEL/4_Semestre/LIC/Projeto_LIC_24-25/hardware/common/REG_Nary.vhd}
vcom -93 -work work {C:/ISEL/4_Semestre/LIC/Projeto_LIC_24-25/hardware/common/REG_4B.vhd}
vcom -93 -work work {C:/ISEL/4_Semestre/LIC/Projeto_LIC_24-25/hardware/common/MUX41.vhd}
vcom -93 -work work {C:/ISEL/4_Semestre/LIC/Projeto_LIC_24-25/hardware/common/MUX21.vhd}
vcom -93 -work work {C:/ISEL/4_Semestre/LIC/Projeto_LIC_24-25/hardware/common/FULLADDER.vhd}
vcom -93 -work work {C:/ISEL/4_Semestre/LIC/Projeto_LIC_24-25/hardware/common/FFD.vhd}
vcom -93 -work work {C:/ISEL/4_Semestre/LIC/Projeto_LIC_24-25/hardware/common/DECODER.vhd}
vcom -93 -work work {C:/ISEL/4_Semestre/LIC/Projeto_LIC_24-25/hardware/common/COUNTER.vhd}
vcom -93 -work work {C:/ISEL/4_Semestre/LIC/Projeto_LIC_24-25/hardware/common/ADDER.vhd}

