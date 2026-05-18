library ieee;
use ieee.std_logic_1164.all;

entity Key_Decode is
port(
		reset : in std_logic;
		Mclk : in std_logic;
		Kack_dec : in std_logic;
		Lines_dec: in std_logic_vector(3 downto 0);
		Kval_dec : out std_logic;
		Columns_dec: out std_logic_vector(3 downto 0);
		K: out std_logic_vector(3 downto 0)
);

end Key_Decode;

architecture Structural of Key_Decode is

component KEY_SCAN is
port (
        clk : IN STD_LOGIC;
        reset : IN STD_LOGIC;
        keyScan : IN STD_LOGIC;
        lines : IN STD_LOGIC_VECTOR(3 DOWNTO 0);
        keyPress : OUT STD_LOGIC;
        keyData : OUT STD_LOGIC_VECTOR(3 DOWNTO 0);
        columns : OUT STD_LOGIC_VECTOR(3 DOWNTO 0)
);
end component;

component Key_Control is
port(
		reset 	: in std_logic;
		clk		: in std_logic;
		Kack		: in std_logic;
		Kpress	: in std_logic;
		Kval		: out std_logic;
		Kscan		: out std_logic
);
end component;


component clkDIV is
port(
		clk_in: in std_logic;
		clk_out: out std_logic
);

end component;



signal Kpress_signal: std_logic;
signal Kscan_signal: std_logic;
signal clk_signal: std_logic;

begin

Unit_key_scan: KEY_SCAN port map (

	keyScan => Kscan_signal,
	keyPress => Kpress_signal,
	clk => clk_signal,
	reset => reset,
	lines => Lines_Dec,
	keyData => K,
	columns => Columns_dec
	
);

Unit_key_control: Key_Control port map(

	clk => clk_signal,
	reset => reset,
	Kval => Kval_dec,
	Kscan => Kscan_signal,
	Kpress => Kpress_signal,
	Kack => Kack_dec
	
);

Unit_clock_div: clkDiv port map(

	clk_in => Mclk,
	clk_out => clk_signal
);

end Structural;
	


		


