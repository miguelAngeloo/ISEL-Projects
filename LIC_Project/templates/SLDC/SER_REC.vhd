LIBRARY ieee;
USE ieee.std_logic_1164.all;

entity SER_REC is
		port(
		clk    : in std_logic;
		Reset  : in std_logic;
		NSS    : in std_logic;
		SCLK   : in std_logic;
		SDX    : in std_logic;
		accept : in std_logic;
		DXval  : out std_logic;
		DOUT   : out std_logic_vector(8 downto 0)
		);
	
end entity;

architecture rtl of SER_REC is

component SER_CRTL
	port(
	clk    : in std_logic;
	Reset  : in std_logic;
	enRx   : in std_logic;
	accept : in std_logic; 
	pFlag  : in std_logic;
	dFlag  : in std_logic;
	RXerr  : in std_logic;
	wr     : out std_logic;
	init   : out std_logic;
	DXval  : out std_logic
	);

end component;

component PAR_CHK is
	port(
	data  : in std_logic;
	clk   : in std_logic;
	init  : in std_logic;
	erro  : out std_logic
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

component equal
	port(
	terminalValue : in std_logic_vector (3 downto 0);
	Q: in std_logic_vector (3 downto 0);
	TC : out std_logic
	);

end component;	

component S_Reg_9
	port(
	data    : in std_logic;
	CLK  : in std_logic;
	clr  : in std_logic;
	S_PL : in std_logic;
	E    : IN STD_LOGIC;
	Q_o  : out std_logic_VECTOR(8 downto 0)
	);

end component;	

signal pF_out, dF_out, err_in, wr_in, init_in : std_logic;
signal EQ_in : std_logic_vector(3 downto 0);

begin

inst_CRTL :	SER_CRTL
	port map(
	clk    => clk,
	Reset  => Reset,
	enRx   => NSS,
	accept => accept,
	pFlag  => pF_out,
	dFlag  => dF_out,
	RXerr  => err_in,
	wr     => wr_in,
	init   => init_in,
	DXval  => DXval
	);
		
inst_Check :	PAR_CHK
	port map(
	data  => SDX,
	clk   => SCLK,
	init  => init_in,
	erro   => err_in
	);
	
inst_CNTRUP : CounterUP
	port map(
	K     => "0000",
	CLK   => SCLK,
	CE    => '1',
	RESET => init_in,
 	PL    => '0',
	Q     => EQ_in
	);
	
inst_EQ_9 : equal
	port map(
	terminalValue => "1001",
	Q             => EQ_in,
	TC            => dF_out
	);	
	
inst_EQ_10 : equal
	port map(
	terminalValue => "1010",
	Q             => EQ_in,
	TC            => pF_out
	);	
	
inst_SReg :	S_Reg_9
	port map(
	data => SDX,
	CLK  => SCLK,
	clr  => '0',
	S_PL => '1',
	E    => wr_in,
	Q_o  => DOUT
	);
	
	
	
end rtl;