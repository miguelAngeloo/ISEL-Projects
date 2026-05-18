LIBRARY ieee;
USE ieee.std_logic_1164.ALL;

ENTITY Memory_Address_Control_tb IS
END Memory_Address_Control_tb;

ARCHITECTURE behavioral OF Memory_Address_Control_tb IS

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

	CONSTANT MCLK_PERIOD : TIME := 20 ns;
	CONSTANT MCLK_HALF_PERIOD : TIME := MCLK_PERIOD / 2;
	SIGNAL clk_tb : STD_LOGIC;
	SIGNAL reset_tb : STD_LOGIC;
	SIGNAL putNGet_tb : STD_LOGIC;
	SIGNAL incPut_tb : STD_LOGIC;
	SIGNAL incGet_tb : STD_LOGIC;
	SIGNAL address_out_tb : STD_LOGIC_VECTOR(3 DOWNTO 0);
	SIGNAL full_tb : STD_LOGIC;
	SIGNAL empty_tb : STD_LOGIC;
BEGIN
	uut : Memory_Address_Control
	PORT MAP(
		clk => clk_tb,
		reset => reset_tb,
		putNGet => putNGet_tb,
		incPut => incPut_tb,
		incGet => incGet_tb,
		address_out => address_out_tb,
		full => full_tb,
		empty => empty_tb
	);

	clk_gen : PROCESS
	BEGIN
		clk_tb <= '1';
		WAIT FOR MCLK_HALF_PERIOD;
		clk_tb <= '0';
		WAIT FOR MCLK_HALF_PERIOD;
	END PROCESS;

	stimulus : PROCESS
	-- Total Time of Execution = 900 ns
	BEGIN
		reset_tb <= '1';
		putNGet_tb <= '0';
		incPut_tb <= '0';
		incGet_tb <= '0';
		WAIT FOR MCLK_PERIOD;

		-- Test Writing
		reset_tb <= '0';
		putNGet_tb <= '1';
		WAIT FOR MCLK_PERIOD;

		incPut_tb <= '1';
		WAIT FOR MCLK_PERIOD;
		incPut_tb <= '0';
		WAIT FOR MCLK_PERIOD;

		-- Test FULL Condition
		incPut_tb <= '1';
		WAIT FOR MCLK_PERIOD * 15;
		incPut_tb <= '0';
		WAIT FOR MCLK_PERIOD;

		-- Test Reading
		putNGet_tb <= '0';
		WAIT FOR MCLK_PERIOD;
		incGet_tb <= '1';
		WAIT FOR MCLK_PERIOD;
		incGet_tb <= '0';
		WAIT FOR MCLK_PERIOD;

		-- Test EMPTY Condition
		incGet_tb <= '1';
		WAIT FOR MCLK_PERIOD * 15;
		incGet_tb <= '0';
		WAIT FOR MCLK_PERIOD * 2;

		-- Test One Write Make Empty = '0'
		putNGet_tb <= '1';
		WAIT FOR MCLK_PERIOD;
		incPut_tb <= '1';
		WAIT FOR MCLK_PERIOD;
		incPut_tb <= '0';
		WAIT FOR MCLK_PERIOD;

		-- Test See Different Addresses for Write and Read
		putNGet_tb <= '0';
		WAIT FOR MCLK_PERIOD * 2;
		WAIT;
	END PROCESS;

END ARCHITECTURE;