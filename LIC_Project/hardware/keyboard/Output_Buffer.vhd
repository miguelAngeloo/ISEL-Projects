LIBRARY ieee;
USE ieee.std_logic_1164.ALL;

ENTITY Output_Buffer IS
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
END Output_Buffer;

ARCHITECTURE Structural OF Output_Buffer IS

	COMPONENT REG_Nary IS
		GENERIC (size : NATURAL := 4);
		PORT (
			CLK : IN STD_LOGIC;
			RESET : IN STD_LOGIC;
			D : IN STD_LOGIC_VECTOR((size - 1) DOWNTO 0);
			EN : IN STD_LOGIC;
			Q : OUT STD_LOGIC_VECTOR((size - 1) DOWNTO 0)
		);
	END COMPONENT;

	COMPONENT Buffer_Control IS
		PORT (
			reset : IN STD_LOGIC;
			clk : IN STD_LOGIC;
			Load : IN STD_LOGIC;
			Ack : IN STD_LOGIC;
			Obfree : OUT STD_LOGIC;
			Dval : OUT STD_LOGIC;
			Wreg : OUT STD_LOGIC
		);

	END COMPONENT;

	SIGNAL Wreg_signal : STD_LOGIC;

BEGIN

	Unit_buffer_control : Buffer_Control PORT MAP(

		clk => clk,
		reset => reset,
		Ack => Ack,
		Load => Load,
		Wreg => Wreg_signal,
		Obfree => obfree,
		Dval => Dval

	);

	Unit_register_4b : REG_Nary PORT MAP(

		CLK => Wreg_signal,
		RESET => reset,
		D => DATA,
		EN => '1',
		Q => Q

	);

END ARCHITECTURE;