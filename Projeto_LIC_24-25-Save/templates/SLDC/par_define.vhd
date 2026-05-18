LIBRARY ieee;
USE ieee.std_logic_1164.all;

entity par_define is
	port(
	S_in : in std_logic;
	par  : out std_logic
	);	
end entity;

architecture rtl of par_define is
 
 begin
 
 par <= S_in;
 end rtl;