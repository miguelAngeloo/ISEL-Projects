library ieee;
use ieee.std_logic_1164.all;

entity Test_Serial_Roulette is
port(
		reset 	: in std_logic;
		Mclk		: in std_logic;
		HEX0	: out std_logic_vector(7 downto 0);
		HEX1	: out std_logic_vector(7 downto 0);
		HEX2	: out std_logic_vector(7 downto 0);
		HEX3	: out std_logic_vector(7 downto 0);
		HEX4	: out std_logic_vector(7 downto 0);
		HEX5	: out std_logic_vector(7 downto 0)
);

end Test_Serial_Roulette;

architecture Structural of Test_Serial_Roulette is

component UsbPort is
PORT(
		inputPort:  IN  STD_LOGIC_VECTOR(7 DOWNTO 0);
		outputPort :  OUT  STD_LOGIC_VECTOR(7 DOWNTO 0)
);

end component;

component SRC is
port(
		reset 	: in std_logic;
		Mclk		: in std_logic;
		SS		   : in std_logic;
		SCLK		: in std_logic;
		SDX		: in std_logic;
		E		   : out std_logic;
		Dout		: out std_logic_vector(7 downto 0)
);

end component;



component REG_Nary is
GENERIC (size : NATURAL := 4);
    PORT (
        CLK : IN STD_LOGIC;
        RESET : IN STD_LOGIC;
        D : IN STD_LOGIC_VECTOR((size - 1) DOWNTO 0);
        EN : IN STD_LOGIC;
        Q : OUT STD_LOGIC_VECTOR((size - 1) DOWNTO 0)
    );
END component;


component rouletteDisplay is
port(	set	: in std_logic;
		cmd	: in std_logic_vector(2 downto 0);
		data	: in std_logic_vector(4 downto 0);
		HEX0	: out std_logic_vector(7 downto 0);
		HEX1	: out std_logic_vector(7 downto 0);
		HEX2	: out std_logic_vector(7 downto 0);
		HEX3	: out std_logic_vector(7 downto 0);
		HEX4	: out std_logic_vector(7 downto 0);
		HEX5	: out std_logic_vector(7 downto 0)
);

end component;


signal UsbPort_input : std_logic_vector(7 downto 0) :="00000000";
signal UsbPort_output : std_logic_vector(7 downto 0);
signal Display_DATA_signal : std_logic_vector(7 downto 0);
signal sdx_s, sclk_s, ss_s, Display_E : std_logic;

begin


Unit_Serial_SRC: SRC port map (

	Mclk => Mclk,
	reset => reset,
	SDX => sdx_s,
	SCLK => sclk_s,
	SS => ss_s,
	E => Display_E,
	Dout => Display_DATA_signal(7 downto 0)
);

 
 
 Unit_usb_port: UsbPort port map(

	inputPort => UsbPort_input,
	outputPort => UsbPort_output
	
);



Unit_Reg_NARY: REG_Nary generic map (size => 4)
port map (
	CLK => Mclk,
   RESET => reset,
	D => UsbPort_output (3 downto 0),
	EN => '1',
   Q(0) => sdx_s,
	Q(1) => sclk_s,
	Q(2) => OPEN,
	Q(3) => ss_s
	
);




Unit_Roulette_Display: rouletteDisplay port map(
	
	set => Display_E,
	cmd => Display_DATA_signal(2 downto 0),
	data => Display_DATA_signal(7 downto 3),
	HEX0 => HEX0,
	HEX1 => HEX1,
	HEX2 => HEX2,
	HEX3 => HEX3,
	HEX4 => HEX4,
	HEX5 => HEX5
);

end Structural;