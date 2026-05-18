library ieee;
use ieee.std_logic_1164.all;

entity equal is
	port (
	terminalValue : in std_logic_vector (3 downto 0);
	Q             : in std_logic_vector (3 downto 0);
	TC            : out std_logic
	);
	
end entity;

architecture rtl of equal is

component XOR_4
	port (
	terminalValue : in std_logic_vector (3 downto 0);
	Q: in std_logic_vector (3 downto 0);
	OR_o : out std_logic_vector (3 downto 0)
	);

end component;
	
component NOR_4
	port (
	OR_o: in std_logic_vector (3 downto 0);
	TC : out std_logic
	);
end component;

signal OR_out : std_logic_vector (3 downto 0);

begin 

XOR_4_inst : XOR_4
	port map(
	OR_o => OR_out,
	Q => Q,
	terminalValue => terminalValue
	);


NOR_4_inst : NOR_4
	port map(
	OR_o => OR_out,
	TC => TC
	);
	
end rtl;