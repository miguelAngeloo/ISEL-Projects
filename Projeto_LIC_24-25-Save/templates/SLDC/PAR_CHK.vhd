LIBRARY ieee;
USE ieee.std_logic_1164.all;

entity PAR_CHK is
	port(
	data  : in std_logic;
	clk   : in std_logic;
	init  : in std_logic;
	erro  : out std_logic
	);
end PAR_CHK;

architecture rtl of PAR_CHK is

component registo
	PORT ( 
	CLK   : in std_logic;
	RESET : in STD_LOGIC;
	D     : IN STD_LOGIC_VECTOR(3 downto 0);
	EN    : IN STD_LOGIC;
	Q     : out std_logic_VECTOR(3 downto 0)
	);
end component;

component Adder
	PORT ( 
	A: in std_logic_vector(3 downto 0);
	B: in std_logic_vector(3 downto 0);
	C0 : in std_logic;
	S: out std_logic_vector(3 downto 0);
	C4: out std_logic
	);
end component;

component equal
	port(
	terminalValue : in std_logic_vector (3 downto 0);
	Q: in std_logic_vector (3 downto 0);
	TC : out std_logic
	);

end component;

component CounterUP
	port(
	K: in std_logic_vector (3 downto 0);
	CLK: in std_logic;
	CE: in std_logic;
	RESET: in std_logic;
	PL: in std_logic;
	Q: out std_logic_vector (3 downto 0)
	);

end component;	

component par_define is
	port(
	S_in : in std_logic;
	par  : out std_logic
	);	
end component;

component ERR 
	port(
	AI     : in std_logic_vector(3 downto 0);
	par    : in std_logic;
	EN     : in std_logic;
	clk    : in std_logic;
	reset  : in std_logic; 
	err    : out std_logic
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

signal rst, SCLK, init_in, par_out, err_out	 : std_logic;
signal S_out, EQ_in	   							 : std_logic_vector(3 downto 0);
signal Add_in 		         						 : std_logic_vector(3 downto 0):= "0000";

begin

Registo_inst : Registo
	port map 
	(
	CLK   => clk,
	RESET => rst,
	EN    => not rst,
	D     => S_out,
	Q     => Add_in
	);
	
Soma : Adder
	port map 
	(
	A(3 downto 1) => "000",
	A(0)          => data,
	B             => Add_in,
	S             => S_out,
	C0            => '0',
	C4            => open
	);
	
inst_CNTRUP : CounterUP
	port map(
	K     => "0000",
	CLK   => SCLK,
	CE    => '1',
	RESET => rst,
 	PL    => '0',
	Q     => EQ_in
	);

inst_EQ_10 : equal
	port map(
	terminalValue => "1010",
	Q             => EQ_in,
	TC            => rst
	);	
	

inst_FFD: FFD
port map(CLK => clk, reset => rst, set => '0', D => init, EN => init, Q => init_in);	
	
inst_pard : par_define
	port map(
	S_in => S_out(0),
	par  => par_out
	);
inst_Erro : ERR
	port map(
	AI    => S_out,
	par   => par_out,
	clk   => clk,
	reset => init_in,
	EN    => rst,
	err   => err_out
	);
	
erro <= err_out;	
end architecture;	