library ieee;
use ieee.std_logic_1164.all;

entity mux_1 is
	port
	(
	a		: in std_logic;
	b		: in std_logic;
	s		: in std_logic;
	y		: out std_logic
	); 
end entity;

architecture rtl of mux_1 is
begin

	y <= (a and s) or (b and not s);
	
end rtl;