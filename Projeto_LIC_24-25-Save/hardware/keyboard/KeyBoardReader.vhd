LIBRARY ieee;
USE ieee.std_logic_1164.ALL;

ENTITY KeyBoardReader IS
	PORT (
		reset : IN STD_LOGIC;
		Mclk : IN STD_LOGIC;
		Kack : IN STD_LOGIC;
		Lines_KBR : IN STD_LOGIC_VECTOR(3 DOWNTO 0);
		Dval : OUT STD_LOGIC;
		Columns_KBR : OUT STD_LOGIC_VECTOR(3 DOWNTO 0);
		Q : OUT STD_LOGIC_VECTOR(3 DOWNTO 0)
	);

END KeyBoardReader;

ARCHITECTURE Structural OF KeyBoardReader IS

	COMPONENT Key_Decode IS
		PORT (
			reset : IN STD_LOGIC;
			Mclk : IN STD_LOGIC;
			Kack_dec : IN STD_LOGIC;
			Lines_dec : IN STD_LOGIC_VECTOR(3 DOWNTO 0);
			Kval_dec : OUT STD_LOGIC;
			Columns_dec : OUT STD_LOGIC_VECTOR(3 DOWNTO 0);
			K : OUT STD_LOGIC_VECTOR(3 DOWNTO 0)
		);
	END COMPONENT;

	COMPONENT Ring_Buffer IS
		PORT (
			reset : IN STD_LOGIC;
			Mclk : IN STD_LOGIC;
			DAV : IN STD_LOGIC;
			CTS : IN STD_LOGIC;
			Data : IN STD_LOGIC_VECTOR(3 DOWNTO 0);
			Wreg : OUT STD_LOGIC;
			Q : OUT STD_LOGIC_VECTOR(3 DOWNTO 0);
			DAC : OUT STD_LOGIC
		);
	END COMPONENT;

	COMPONENT Output_Buffer IS
		PORT (
			reset : IN STD_LOGIC;
			clk : IN STD_LOGIC;
			Data : IN STD_LOGIC_VECTOR(3 DOWNTO 0);
			Load : IN STD_LOGIC;
			Ack : IN STD_LOGIC;
			Obfree : OUT STD_LOGIC;
			Dval : OUT STD_LOGIC;
			Q : OUT STD_LOGIC_VECTOR(3 DOWNTO 0)
		);
	END COMPONENT;

	SIGNAL DAC_to_KackDec : STD_LOGIC;
	SIGNAL Kval_to_DAV : STD_LOGIC;
	SIGNAL Wreg_to_Load : STD_LOGIC;
	SIGNAL OBfree_to_CTS : STD_LOGIC;

	SIGNAL K_Dec_Data : STD_LOGIC_VECTOR(3 DOWNTO 0);
	SIGNAL Ring_Buffer_Data : STD_LOGIC_VECTOR(3 DOWNTO 0);
	SIGNAL Output_Buffer_Data : STD_LOGIC_VECTOR(3 DOWNTO 0);


BEGIN

	Unit_key_decode : Key_Decode PORT MAP(
		Mclk => Mclk,
		reset => reset,
		Lines_dec => Lines_KBR,
		K => K_Dec_Data,
		Columns_dec => Columns_KBR,
		Kval_dec => Kval_to_DAV,
		Kack_dec => DAC_to_KackDec
	);

	Unit_ring_buffer : Ring_Buffer PORT MAP(
		Mclk => Mclk,
		reset => reset,
		DAV => Kval_to_DAV,
		CTS => OBfree_to_CTS,
		Data => K_Dec_Data,
		Wreg => Wreg_to_Load,
		Q => Ring_Buffer_Data,
		DAC => DAC_to_KackDec
	);

	Unit_output_buffer : Output_Buffer PORT MAP(
		reset => reset,
		clk => Mclk,
		Data => Ring_Buffer_Data,
		Load => Wreg_to_Load,
		Ack => Kack,
		Obfree => OBfree_to_CTS,
		Dval => Dval,
		Q => Output_Buffer_Data
	);

	Q <= Output_Buffer_Data;

END ARCHITECTURE;