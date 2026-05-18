LIBRARY ieee;
USE ieee.std_logic_1164.ALL;

ENTITY Key_Control_tb IS
END Key_Control_tb;

ARCHITECTURE behavioral OF Key_Control_tb IS

	COMPONENT Key_Control IS
		PORT (
			reset : IN STD_LOGIC;
			clk : IN STD_LOGIC;
			Kack : IN STD_LOGIC;
			Kpress : IN STD_LOGIC;
			Kval : OUT STD_LOGIC;
			Kscan : OUT STD_LOGIC
		);
	END COMPONENT;

	-- UUT signals
	CONSTANT MCLK_PERIOD : TIME := 20 ns;
	CONSTANT MCLK_HALF_PERIOD : TIME := MCLK_PERIOD / 2;

	SIGNAL reset_tb : STD_LOGIC;
	SIGNAL clk_tb : STD_LOGIC;
	SIGNAL Kack_tb : STD_LOGIC;
	SIGNAL Kpress_tb : STD_LOGIC;
	SIGNAL Kval_tb : STD_LOGIC;
	SIGNAL Kscan_tb : STD_LOGIC;
BEGIN

	-- Unit Under Test

	UUT : Key_Control
	PORT MAP(
		reset => reset_tb,
		clk => clk_tb,
		Kack => Kack_tb,
		Kpress => Kpress_tb,
		Kval => Kval_tb,
		Kscan => Kscan_tb
	);

	clk_gen : PROCESS
	BEGIN
		clk_tb <= '1';
		WAIT FOR MCLK_HALF_PERIOD;
		clk_tb <= '0';
		WAIT FOR MCLK_HALF_PERIOD;
	END PROCESS;

	stimulus : PROCESS
		-- Total time of execution: 300 ns
	BEGIN
		-- Initialize Inputs
		reset_tb <= '0';
		Kpress_tb <= '0';
		Kack_tb <= '0';

		WAIT FOR MCLK_PERIOD;
		-- reset
		reset_tb <= '1';
		WAIT FOR MCLK_PERIOD;
		reset_tb <= '0';
		WAIT FOR MCLK_PERIOD;

		-- Press Key (Change state from SCANNING to VALID_KEY)
		Kpress_tb <= '1';
		WAIT FOR MCLK_PERIOD * 3;
		Kpress_tb <= '0';
		WAIT FOR MCLK_PERIOD * 3;

		-- Acknowledge Key (Change state from VALID_KEY to WAITING_RELEASE)
		Kack_tb <= '1';
		WAIT FOR MCLK_PERIOD * 3;

		-- Finish Key Processing (Change state from WAITING_RELEASE to SCANNING)
		Kack_tb <= '0';
		WAIT FOR MCLK_PERIOD * 2;

		-- Signal end of test
		reset_tb <= '1';
		WAIT FOR MCLK_HALF_PERIOD;
		reset_tb <= '0';
		WAIT FOR MCLK_HALF_PERIOD;
		WAIT;

	END PROCESS;

END ARCHITECTURE;