library ieee;
use ieee.std_logic_1164.all;

entity mux4 is
	port
	(
	a		: in std_logic_vector (3 downto 0);
	b		: in std_logic_vector (3 downto 0);
	s		: in std_logic;
	y		: out std_logic_vector (3 downto 0)
	); 
end entity;

architecture rtl of mux4 is
begin

	y(0) <= (a(0) and s) or (b(0) and not s);
	y(1) <= (a(1) and s) or (b(1) and not s);
	y(2) <= (a(2) and s) or (b(2) and not s);
	y(3) <= (a(3) and s) or (b(3) and not s);

end rtl;