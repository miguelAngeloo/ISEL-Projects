library ieee;
use ieee.std_logic_1164.all;

entity SLCDC is
port(
		reset 	: in std_logic;
		Mclk		: in std_logic;
		SS		   : in std_logic;
		SCLK		: in std_logic;
		SDX		: in std_logic;
		E		   : out std_logic;
		Dout		: out std_logic_vector(4 downto 0)
		--serial_data		: out std_logic_vector(4 downto 0)
);
end SLCDC;

architecture Structural of SLCDC is

component Serial_Receiver is
	port (
	
		reset : IN STD_LOGIC;
		clk : IN STD_LOGIC;
		SS : IN STD_LOGIC;
		SCLK : IN STD_LOGIC;
		SDX : IN STD_LOGIC;
		accept : IN STD_LOGIC;
		DXval : OUT STD_LOGIC;
		Data : OUT STD_LOGIC_VECTOR(4 DOWNTO 0)
);

end component;


component LCD_Dispatcher is
	port(
		reset 	: in std_logic;
		clk		: in std_logic;
		Dval		: in std_logic;
		Din		: in std_logic_vector(4 downto 0);
		Wrl		: out std_logic;
		Dout		: out std_logic_vector(4 downto 0);
		done		: out std_logic
);

end component;


component clkDIV is
generic(div: natural := 100_000);
port(
		clk_in: in std_logic;
		clk_out: out std_logic
);

end component;


signal accept_signal : std_logic;
signal DXval_signal : std_logic;
signal Data_signal : std_logic_vector(4 downto 0);
signal clk_signal: std_logic;


begin
--serial_data <= Data_signal;

U_Serial_Receiver: Serial_Receiver port map (

	SS => SS,
	SCLK => SCLK,
	SDX => SDX,
	DXval => DXval_signal,
	Data => Data_signal,
	clk => Mclk,
	reset => reset,
	accept => accept_signal
);




U_LCD_Dispatcher: LCD_Dispatcher port map (

	Dval => DXval_signal,
	Din => Data_signal,
	Wrl => E,
	Dout => Dout,
	done => accept_signal,
	reset => reset,
	clk => clk_signal
);


Unit_clock_div: clkDiv
generic map(div => 50)
port map(

	clk_in => Mclk,
	clk_out => clk_signal
);



end Structural;
