LIBRARY ieee;
USE ieee.std_logic_1164.all;

entity ERR is
	port(
	AI     : in std_logic_vector(3 downto 0);
	par    : in std_logic;
	EN     : in std_logic;
	clk    : in std_logic;
	reset  : in std_logic; 
	err    : out std_logic
	);
end ERR;

architecture rtl of ERR is

component Adder
	PORT ( 
	A: in std_logic_vector(3 downto 0);
	B: in std_logic_vector(3 downto 0);
	C0 : in std_logic;
	S: out std_logic_vector(3 downto 0);
	C4: out std_logic
	);
end component;

component FFD

	PORT( 
	CLK   : in std_logic;
	RESET : in STD_LOGIC;
	SET   : in std_logic;
	D     : IN STD_LOGIC;
	EN    : IN STD_LOGIC;
	Q     : out std_logic
	);
end component;

signal Q_out : std_logic;
signal S_out : std_logic_vector(3 downto 0);

begin

inst_Soma : Adder
	port map 
	(
	A             => AI,
	B(3 downto 1) => "000",
	B(0)          => par,
	S             => S_out,
	C0            => '0',
	C4            => open
	);
	
inst_FFD: FFD
port map(CLK => clk, reset => reset, set => '0', D => S_out(0), EN => EN, Q => Q_out);
	
err <= (Q_out and '1');

end rtl;