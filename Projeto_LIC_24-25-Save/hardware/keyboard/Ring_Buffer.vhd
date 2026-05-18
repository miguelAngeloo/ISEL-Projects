LIBRARY ieee;
USE ieee.std_logic_1164.ALL;

ENTITY Ring_Buffer IS
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
END Ring_Buffer;

ARCHITECTURE Structural OF Ring_Buffer IS

	COMPONENT Ring_Buffer_Control IS
		PORT (
			reset : IN STD_LOGIC;
			clk : IN STD_LOGIC;
			DAV : IN STD_LOGIC;
			CTS : IN STD_LOGIC;
			full : IN STD_LOGIC;
			empty : IN STD_LOGIC;
			Wr : OUT STD_LOGIC;
			selPG : OUT STD_LOGIC;
			incPutC : OUT STD_LOGIC;
			incGetC : OUT STD_LOGIC;
			Wreg : OUT STD_LOGIC;
			DAC : OUT STD_LOGIC
		);
	END COMPONENT;

	COMPONENT Memory_Address_Control IS
		PORT (
			clk : IN STD_LOGIC;
			reset : IN STD_LOGIC;
			putNGet : IN STD_LOGIC;
			incPut : IN STD_LOGIC;
			incGet : IN STD_LOGIC;
			address_out : OUT STD_LOGIC_VECTOR(3 DOWNTO 0);
			full : OUT STD_LOGIC;
			empty : OUT STD_LOGIC
		);
	END COMPONENT;

	COMPONENT RAM IS
		GENERIC (
			ADDRESS_WIDTH : NATURAL := 4;
			DATA_WIDTH : NATURAL := 4
		);
		PORT (
			address : IN STD_LOGIC_VECTOR(ADDRESS_WIDTH - 1 DOWNTO 0);
			wr : IN STD_LOGIC;
			din : IN STD_LOGIC_VECTOR(DATA_WIDTH - 1 DOWNTO 0);
			dout : OUT STD_LOGIC_VECTOR(DATA_WIDTH - 1 DOWNTO 0)
		);
	END COMPONENT;

	SIGNAL full_signal : STD_LOGIC;
	SIGNAL empty_signal : STD_LOGIC;
	SIGNAL selPG_signal : STD_LOGIC;
	SIGNAL incPUT_signal : STD_LOGIC;
	SIGNAL incGET_signal : STD_LOGIC;
	SIGNAL Wr_signal : STD_LOGIC;
	SIGNAL address_signal : STD_LOGIC_VECTOR(3 DOWNTO 0);

BEGIN

	Unit_Ring_Buffer_Control : Ring_Buffer_Control PORT MAP(
		reset => reset,
		clk => Mclk,
		DAV => DAV,
		CTS => CTS,
		full => full_signal,
		empty => empty_signal,
		Wr => Wr_signal,
		selPG => selPG_signal,
		incPutC => incPUT_signal,
		incGetC => incGET_signal,
		Wreg => Wreg,
		DAC => DAC
	);

	Unit_MAC_Address : Memory_Address_Control PORT MAP(
		reset => reset,
		clk => Mclk,
		putNGet => selPG_signal,
		incPut => incPUT_signal,
		incGet => incGET_signal,
		full => full_signal,
		empty => empty_signal,
		address_out => address_signal
	);

	Unit_RAM : RAM PORT MAP(

		address => address_signal,
		wr => Wr_signal,
		din => Data,
		dout => Q
	);

END ARCHITECTURE;