LIBRARY ieee;
USE ieee.std_logic_1164.all;

entity SCR_SRCRTL is
		port(
		MCLK   : in std_logic;
		Reset : in std_logic;
		NSS   : in std_logic;
		SCLK  : in std_logic;
		SDX   : in std_logic;
		Wrl   : out std_logic;
		DOUT  : out std_logic_vector(6 downto 0)
		);
	
end entity;

architecture rtl of SCR_SRCRTL is

component SER_REC_SCR 
	port(
	clk    : in std_logic;
	Reset  : in std_logic;
	NSS    : in std_logic;
	SCLK   : in std_logic;
	SDX    : in std_logic;
	accept : in std_logic;
	DXval  : out std_logic;
	DOUT   : out std_logic_vector(6 downto 0)
	);
	
end component;


component SCR_DISP 
	port(
	clk    : in std_logic;
	Reset  : in std_logic;
	Dval : in std_logic;
	DIN  : in std_logic_vector(6 downto 0);
	DOUT : out std_logic_vector(6 downto 0); 
	WrL  : out std_logic;
	done : out std_logic
	);

end component;

signal accept_in, DXval_in : std_logic; 
signal Do_in : std_logic_vector(6 downto 0);

begin

inst_REC : SER_REC_SCR
	port map(
	clk    => MCLK,
	Reset  => Reset,
	NSS    => NSS,
	SCLK   => SCLK,
	SDX    => SDX,
	accept => accept_in,
	DXval  => DXval_in,
	DOUT   => Do_in
	);

inst_DISP : SCR_DISP	
	port map(
	clk   => MCLK,
	Reset => Reset,
	Dval  => DXval_in,
	DIN   => Do_in,
	DOUT  => DOUT,
	WrL   => WrL,
	done  => accept_in
	);
	
end rtl;