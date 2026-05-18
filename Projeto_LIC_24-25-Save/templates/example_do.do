alias z (do do.do)

vlib work
vcom -reportprogress 300 -work work C:/work/altera/adder/adder4.vhd

vsim work.adder4
add wave -position insertpoint \
sim:/adder3/a \
sim:/adder/b \
sim:/adder/result

run 100 ns