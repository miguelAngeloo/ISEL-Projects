LIBRARY ieee;
USE ieee.std_logic_1164.ALL;

ENTITY Ring_Buffer_tb IS
END Ring_Buffer_tb;

ARCHITECTURE behavioral OF Ring_Buffer_tb IS

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

	CONSTANT MCLK_PERIOD : TIME := 20 ns;
	CONSTANT MCLK_HALF_PERIOD : TIME := MCLK_PERIOD / 2;
	SIGNAL reset_tb : STD_LOGIC;
	SIGNAL Mclk_tb : STD_LOGIC;
	SIGNAL Data_tb : STD_LOGIC_VECTOR(3 DOWNTO 0) := "0000";
	SIGNAL CTS_tb : STD_LOGIC := '0';
	SIGNAL DAV_tb : STD_LOGIC := '0';
	SIGNAL Wreg_tb : STD_LOGIC;
	SIGNAL Q_tb : STD_LOGIC_VECTOR(3 DOWNTO 0);
	SIGNAL DAC_tb : STD_LOGIC;

BEGIN
	uut : Ring_Buffer
	PORT MAP(
		reset => reset_tb,
		Mclk => Mclk_tb,
		Data => Data_tb,
		Q => Q_tb,
		CTS => CTS_tb,
		DAV => DAV_tb,
		Wreg => Wreg_tb,
		DAC => DAC_tb
	);

	clk_process : PROCESS
	BEGIN
		WHILE true LOOP
			Mclk_tb <= '1';
			WAIT FOR MCLK_HALF_PERIOD;
			Mclk_tb <= '0';
			WAIT FOR MCLK_HALF_PERIOD;
		END LOOP;
	END PROCESS;

	-- Stimulus process
	stimulus : PROCESS
		-- Total Time of Execution = 4600 ns (230 clock cycles)
	BEGIN
		-- Reset the system
		reset_tb <= '1';
		WAIT FOR MCLK_PERIOD;
		reset_tb <= '0';
		WAIT FOR MCLK_PERIOD;

		-- Test writing to the buffer
		DAV_tb <= '1';
		Data_tb <= "1010";
		WAIT FOR MCLK_PERIOD * 6;
		DAV_tb <= '0';
		WAIT FOR MCLK_PERIOD * 4;

		-- Test reading from the buffer
		CTS_tb <= '1';
		WAIT FOR MCLK_PERIOD * 4;
		CTS_tb <= '0';
		WAIT FOR MCLK_PERIOD * 4;

		-- Test writing until full
		DAV_tb <= '1';
		Data_tb <= "1010";
		WAIT FOR MCLK_PERIOD * 4;
		DAV_tb <= '0';
		WAIT FOR MCLK_PERIOD * 2;

		DAV_tb <= '1';
		Data_tb <= "1111";
		WAIT FOR MCLK_PERIOD * 4;
		DAV_tb <= '0';
		WAIT FOR MCLK_PERIOD * 2;

		DAV_tb <= '1';
		Data_tb <= "0001";
		WAIT FOR MCLK_PERIOD * 4;
		DAV_tb <= '0';
		WAIT FOR MCLK_PERIOD * 2;

		DAV_tb <= '1';
		Data_tb <= "0010";
		WAIT FOR MCLK_PERIOD * 4;
		DAV_tb <= '0';
		WAIT FOR MCLK_PERIOD * 2;

		DAV_tb <= '1';
		Data_tb <= "0100";
		WAIT FOR MCLK_PERIOD * 4;
		DAV_tb <= '0';
		WAIT FOR MCLK_PERIOD * 2;

		DAV_tb <= '1';
		Data_tb <= "1000";
		WAIT FOR MCLK_PERIOD * 4;
		DAV_tb <= '0';
		WAIT FOR MCLK_PERIOD * 2;

		DAV_tb <= '1';
		Data_tb <= "0101";
		WAIT FOR MCLK_PERIOD * 4;
		DAV_tb <= '0';
		WAIT FOR MCLK_PERIOD * 2;

		DAV_tb <= '1';
		Data_tb <= "1100";
		WAIT FOR MCLK_PERIOD * 4;
		DAV_tb <= '0';
		WAIT FOR MCLK_PERIOD * 2;

		DAV_tb <= '1';
		Data_tb <= "1110";
		WAIT FOR MCLK_PERIOD * 4;
		DAV_tb <= '0';
		WAIT FOR MCLK_PERIOD * 2;

		DAV_tb <= '1';
		Data_tb <= "0000";
		WAIT FOR MCLK_PERIOD * 4;
		DAV_tb <= '0';
		WAIT FOR MCLK_PERIOD * 2;

		DAV_tb <= '1';
		Data_tb <= "0011";
		WAIT FOR MCLK_PERIOD * 4;
		DAV_tb <= '0';
		WAIT FOR MCLK_PERIOD * 2;

		DAV_tb <= '1';
		Data_tb <= "1101";
		WAIT FOR MCLK_PERIOD * 4;
		DAV_tb <= '0';
		WAIT FOR MCLK_PERIOD * 2;

		DAV_tb <= '1';
		Data_tb <= "0111";
		WAIT FOR MCLK_PERIOD * 4;
		DAV_tb <= '0';
		WAIT FOR MCLK_PERIOD * 2;

		DAV_tb <= '1';
		Data_tb <= "1011";
		WAIT FOR MCLK_PERIOD * 4;
		DAV_tb <= '0';
		WAIT FOR MCLK_PERIOD * 2;

		DAV_tb <= '1';
		Data_tb <= "1111";
		WAIT FOR MCLK_PERIOD * 4;
		DAV_tb <= '0';
		WAIT FOR MCLK_PERIOD * 2;

		DAV_tb <= '1';
		Data_tb <= "0000";
		WAIT FOR MCLK_PERIOD * 4;
		DAV_tb <= '0';
		WAIT FOR MCLK_PERIOD * 2;

		-- Test writing when full
		DAV_tb <= '1';
		Data_tb <= "1010";
		WAIT FOR MCLK_PERIOD * 4;
		DAV_tb <= '0';
		WAIT FOR MCLK_PERIOD * 2;

		-- Test reading until empty
		CTS_tb <= '1';
		WAIT FOR MCLK_PERIOD * 4;
		CTS_tb <= '0';
		WAIT FOR MCLK_PERIOD * 2;

		CTS_tb <= '1';
		WAIT FOR MCLK_PERIOD * 4;
		CTS_tb <= '0';
		WAIT FOR MCLK_PERIOD * 2;

		CTS_tb <= '1';
		WAIT FOR MCLK_PERIOD * 4;
		CTS_tb <= '0';
		WAIT FOR MCLK_PERIOD * 2;

		CTS_tb <= '1';
		WAIT FOR MCLK_PERIOD * 4;
		CTS_tb <= '0';
		WAIT FOR MCLK_PERIOD * 2;

		CTS_tb <= '1';
		WAIT FOR MCLK_PERIOD * 4;
		CTS_tb <= '0';
		WAIT FOR MCLK_PERIOD * 2;

		CTS_tb <= '1';
		WAIT FOR MCLK_PERIOD * 4;
		CTS_tb <= '0';
		WAIT FOR MCLK_PERIOD * 2;

		CTS_tb <= '1';
		WAIT FOR MCLK_PERIOD * 4;
		CTS_tb <= '0';
		WAIT FOR MCLK_PERIOD * 2;

		CTS_tb <= '1';
		WAIT FOR MCLK_PERIOD * 4;
		CTS_tb <= '0';
		WAIT FOR MCLK_PERIOD * 2;

		CTS_tb <= '1';
		WAIT FOR MCLK_PERIOD * 4;
		CTS_tb <= '0';
		WAIT FOR MCLK_PERIOD * 2;

		CTS_tb <= '1';
		WAIT FOR MCLK_PERIOD * 4;
		CTS_tb <= '0';
		WAIT FOR MCLK_PERIOD * 2;

		CTS_tb <= '1';
		WAIT FOR MCLK_PERIOD * 4;
		CTS_tb <= '0';
		WAIT FOR MCLK_PERIOD * 2;

		CTS_tb <= '1';
		WAIT FOR MCLK_PERIOD * 4;
		CTS_tb <= '0';
		WAIT FOR MCLK_PERIOD * 2;

		CTS_tb <= '1';
		WAIT FOR MCLK_PERIOD * 4;
		CTS_tb <= '0';
		WAIT FOR MCLK_PERIOD * 2;

		CTS_tb <= '1';
		WAIT FOR MCLK_PERIOD * 4;
		CTS_tb <= '0';
		WAIT FOR MCLK_PERIOD * 2;

		CTS_tb <= '1';
		WAIT FOR MCLK_PERIOD * 4;
		CTS_tb <= '0';
		WAIT FOR MCLK_PERIOD * 2;

		CTS_tb <= '1';
		WAIT FOR MCLK_PERIOD * 4;
		CTS_tb <= '0';
		WAIT FOR MCLK_PERIOD * 2;

		-- Test reading when empty
		CTS_tb <= '1';
		WAIT FOR MCLK_PERIOD * 4;
		CTS_tb <= '0';
		WAIT FOR MCLK_PERIOD * 2;

		WAIT;
	END PROCESS;

END ARCHITECTURE;