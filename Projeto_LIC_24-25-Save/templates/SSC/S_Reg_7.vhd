LIBRARY ieee;
USE ieee.std_logic_1164.all;

ENTITY S_Reg_7 IS
	PORT( 
	data    : in std_logic;
	CLK  : in std_logic;
	clr  : in std_logic;
	S_PL : in std_logic;
	E    : IN STD_LOGIC;
	Q_o  : out std_logic_VECTOR(6 downto 0)
	);
END S_Reg_7;

ARCHITECTURE logicFunction OF S_Reg_7 IS

component mux_1
	port
	(
	a		: in std_logic;
	b		: in std_logic;
	s		: in std_logic;
	y		: out std_logic
	); 
end component;

component FFD
	PORT( 
		CLK : in std_logic;
		RESET : in STD_LOGIC;
		SET : in std_logic;
		D : IN STD_LOGIC;
		EN : IN STD_LOGIC;
		Q : out std_logic
	);
end component;

signal Sin    : std_logic;
signal Q, DD  : std_logic_vector (6 downto 0);

BEGIN
mux_inst_7 : mux_1
	port map (
	a => '0',
	b => data,
	s => S_PL,
	y => DD(6)
	);
	
FFD_inst_7 : FFD
	port map (
	CLK => CLK,
	RESET => clr,
	SET => '0',
	D => DD(6),
	EN => E,
	Q => Q(6)
	);
	
mux_inst_6 : mux_1
	port map (
	a => Sin,
	b => Q(6),
	s => S_PL,
	y => DD(5)
	);
	
FFD_inst_6 : FFD
	port map (
	CLK => CLK,
	RESET => clr,
	SET => '0',
	D => DD(5),
	EN => E,
	Q => Q(5)
	);	
	
mux_inst_5 : mux_1
	port map (
	a => Sin,
	b => Q(5),
	s => S_PL,
	y => DD(4)
	);
	
FFD_inst_5 : FFD
	port map (
	CLK => CLK,
	RESET => clr,
	SET => '0',
	D => DD(4),
	EN => E,
	Q => Q(4)
	);

mux_inst_4 : mux_1
	port map (
	a => Sin,
	b => Q(4),
	s => S_PL,
	y => DD(3)
	);
	
FFD_inst_4 : FFD
	port map (
	CLK => CLK,
	RESET => clr,
	SET => '0',
	D => DD(3),
	EN => E,
	Q => Q(3)
	);

mux_inst_3 : mux_1
	port map (
	a => Sin,
	b => Q(3),
	s => S_PL,
	y => DD(2)
	);
	
FFD_inst_3 : FFD
	port map (
	CLK => CLK,
	RESET => clr,
	SET => '0',
	D => DD(2),
	EN => E,
	Q => Q(2)
	);
	
mux_inst_2 : mux_1
	port map (
	a => Sin,
	b => Q(2),
	s => S_PL,
	y => DD(1)
	);
	
FFD_inst_2 : FFD
	port map (
	CLK => CLK,
	RESET => clr,
	SET => '0',
	D => DD(1),
	EN => E,
	Q => Q(1)
	);
	
mux_inst_1 : mux_1
	port map (
	a => Sin,
	b => Q(1),
	s => S_PL,
	y => DD(0)
	);
	
FFD_inst_1 : FFD
	port map (
	CLK => CLK,
	RESET => clr,
	SET => '0',
	D => DD(0),
	EN => E,
	Q => Q(0)
	);
	
Q_o <= Q;
end architecture;