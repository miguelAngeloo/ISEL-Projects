LIBRARY ieee;
USE ieee.std_logic_1164.ALL;

ENTITY RouletteGame IS
	PORT (
		reset : IN STD_LOGIC;
		Mclk : IN STD_LOGIC;
		MSwitch : IN STD_LOGIC;
		CoinSwitch : IN STD_LOGIC;
		CoinIdSwitch : IN STD_LOGIC;
		-- Kack : in std_logic;
		Lines_Pins : IN STD_LOGIC_VECTOR(3 DOWNTO 0);
		-- Dval : out std_logic;
		Columns_Pins : OUT STD_LOGIC_VECTOR(3 DOWNTO 0);
		-- Q: out std_logic_vector(3 downto 0);
		CoinAcceptLED : OUT STD_LOGIC;
		LCD_DATA : OUT STD_LOGIC_VECTOR(7 DOWNTO 4);
		LCD_RS : OUT STD_LOGIC;
		LCD_E : OUT STD_LOGIC;
		HEX0 : OUT STD_LOGIC_VECTOR(7 DOWNTO 0);
		HEX1 : OUT STD_LOGIC_VECTOR(7 DOWNTO 0);
		HEX2 : OUT STD_LOGIC_VECTOR(7 DOWNTO 0);
		HEX3 : OUT STD_LOGIC_VECTOR(7 DOWNTO 0);
		HEX4 : OUT STD_LOGIC_VECTOR(7 DOWNTO 0);
		HEX5 : OUT STD_LOGIC_VECTOR(7 DOWNTO 0)
	);

END RouletteGame;

ARCHITECTURE Structural OF RouletteGame IS

	SIGNAL M : STD_LOGIC;
	SIGNAL Coin : STD_LOGIC;
	SIGNAL CoinId : STD_LOGIC;
	SIGNAL CoinAccept : STD_LOGIC;

	COMPONENT KeyBoardReader IS
		PORT (
			reset : IN STD_LOGIC;
			Mclk : IN STD_LOGIC;
			Kack : IN STD_LOGIC;
			Lines_KBR : IN STD_LOGIC_VECTOR(3 DOWNTO 0);
			Dval : OUT STD_LOGIC;
			Columns_KBR : OUT STD_LOGIC_VECTOR(3 DOWNTO 0);
			Q : OUT STD_LOGIC_VECTOR(3 DOWNTO 0)
		);

	END COMPONENT;

	COMPONENT UsbPort IS
		PORT (
			inputPort : IN STD_LOGIC_VECTOR(7 DOWNTO 0);
			outputPort : OUT STD_LOGIC_VECTOR(7 DOWNTO 0)
		);

	END COMPONENT;

	SIGNAL Dval_signal : STD_LOGIC;
	SIGNAL Data_signal : STD_LOGIC_VECTOR(3 DOWNTO 0);
	SIGNAL Kack_signal : STD_LOGIC;
	SIGNAL UsbPort_input : STD_LOGIC_VECTOR(7 DOWNTO 0) := "00000000";
	SIGNAL UsbPort_output : STD_LOGIC_VECTOR(7 DOWNTO 0);

	COMPONENT SLCDC IS
		PORT (
			reset : IN STD_LOGIC;
			Mclk : IN STD_LOGIC;
			SS : IN STD_LOGIC;
			SCLK : IN STD_LOGIC;
			SDX : IN STD_LOGIC;
			E : OUT STD_LOGIC;
			Dout : OUT STD_LOGIC_VECTOR(4 DOWNTO 0)
		);
	END COMPONENT;

	SIGNAL outLCDEnable : STD_LOGIC;
	SIGNAL Dout_signal : STD_LOGIC_VECTOR(4 DOWNTO 0);

	COMPONENT SRC IS
		PORT (
			reset : IN STD_LOGIC;
			Mclk : IN STD_LOGIC;
			SS : IN STD_LOGIC;
			SCLK : IN STD_LOGIC;
			SDX : IN STD_LOGIC;
			E : OUT STD_LOGIC;
			Dout : OUT STD_LOGIC_VECTOR(7 DOWNTO 0)
		);
	END COMPONENT;

	COMPONENT rouletteDisplay IS
		PORT (
			set : IN STD_LOGIC;
			cmd : IN STD_LOGIC_VECTOR(2 DOWNTO 0);
			data : IN STD_LOGIC_VECTOR(4 DOWNTO 0);
			HEX0 : OUT STD_LOGIC_VECTOR(7 DOWNTO 0);
			HEX1 : OUT STD_LOGIC_VECTOR(7 DOWNTO 0);
			HEX2 : OUT STD_LOGIC_VECTOR(7 DOWNTO 0);
			HEX3 : OUT STD_LOGIC_VECTOR(7 DOWNTO 0);
			HEX4 : OUT STD_LOGIC_VECTOR(7 DOWNTO 0);
			HEX5 : OUT STD_LOGIC_VECTOR(7 DOWNTO 0)
		);
	END COMPONENT;

	SIGNAL Display_DATA_signal : STD_LOGIC_VECTOR(7 DOWNTO 0);
	SIGNAL sdx_s, sclk_s, slcdcs_s, srcs_s, Display_E : STD_LOGIC;

BEGIN

	UsbPort_input(5) <= CoinId;
	UsbPort_input(6) <= Coin;
	UsbPort_input(7) <= M;
	CoinAccept <= UsbPort_output(6);
	
	M <= MSwitch;
	Coin <= CoinSwitch;
	CoinId <= CoinIdSwitch;
	CoinAcceptLED <= CoinAccept;

	Unit_keyboard_reader : KeyBoardReader PORT MAP(

		Mclk => Mclk,
		reset => reset,
		Lines_KBR => Lines_Pins,
		Q(0) => UsbPort_input(0),
		Q(1) => UsbPort_input(1),
		Q(2) => UsbPort_input(2),
		Q(3) => UsbPort_input(3),
		Columns_KBR => Columns_Pins,
		Dval => Dval_signal,
		Kack => Kack_signal
	);

	UsbPort_input(4) <= Dval_signal;
	Kack_signal <= UsbPort_output(7);

	Unit_usb_port : UsbPort PORT MAP(

		inputPort => UsbPort_input,
		outputPort => UsbPort_output

	);

	sdx_s <= UsbPort_output(0);
	sclk_s <= UsbPort_output(1);
	slcdcs_s <= UsbPort_output(2);
	srcs_s <= UsbPort_output(3);

	Unit_Serial_SRC : SRC PORT MAP(
		Mclk => Mclk,
		reset => reset,
		SDX => sdx_s,
		SCLK => sclk_s,
		SS => srcs_s,
		E => Display_E,
		Dout => Display_DATA_signal(7 DOWNTO 0)
	);

	Unit_Roulette_Display : rouletteDisplay PORT MAP(
		set => Display_E,
		cmd => Display_DATA_signal(2 DOWNTO 0),
		data => Display_DATA_signal(7 DOWNTO 3),
		HEX0 => HEX0,
		HEX1 => HEX1,
		HEX2 => HEX2,
		HEX3 => HEX3,
		HEX4 => HEX4,
		HEX5 => HEX5
	);

	Unit_SLCDC : SLCDC PORT MAP(
		Mclk => Mclk,
		reset => reset,
		SS => slcdcs_s,
		SCLK => sclk_s,
		SDX => sdx_s,
		E => outLCDEnable,
		Dout => Dout_signal(4 DOWNTO 0)
	);

	LCD_DATA <= Dout_signal(4 DOWNTO 1);
	LCD_RS <= Dout_signal(0);
	LCD_E <= outLCDEnable;

END Structural;