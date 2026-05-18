library ieee;
use ieee.std_logic_1164.all;

entity XOR_4 is
	port (
	terminalValue : in std_logic_vector (3 downto 0);
	Q: in std_logic_vector (3 downto 0);
	OR_o : out std_logic_vector (3 downto 0)
	);
	
end entity;

architecture rtl of XOR_4 is

begin

	OR_o(0)  <= Q(0) xor terminalValue(0);
	OR_o(1)  <= Q(1) xor terminalValue(1);
	OR_o(2)  <= Q(2) xor terminalValue(2);
	OR_o(3)  <= Q(3) xor terminalValue(3);
	
end rtl;