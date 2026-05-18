LIBRARY ieee;
USE ieee.std_logic_1164.ALL;
ENTITY Mux41_tb IS
END Mux41_tb;

ARCHITECTURE behavioral OF Mux41_tb IS

    COMPONENT MUX41 IS
        PORT (
            D : IN STD_LOGIC_VECTOR(3 DOWNTO 0);
				S : IN STD_LOGIC_VECTOR(1 DOWNTO 0);
				M : OUT STD_LOGIC
        );
    END COMPONENT;

    CONSTANT MCLK_PERIOD : TIME := 20 ns;
    CONSTANT MCLK_HALF_PERIOD : TIME := MCLK_PERIOD / 2;

    SIGNAL a, b, c, d, m : STD_LOGIC;
    SIGNAL s : STD_LOGIC_VECTOR(1 DOWNTO 0);

BEGIN
    Mux_test : MUX41
    PORT MAP(
        D(0) => a,
		  D(1) => b,
		  D(2) => c,
		  D(3) => d,
        S => s,
        M => m
    );
    stimulus : PROCESS
	 -- Total Time of execution: 180 ns
    BEGIN
		  WAIT FOR MCLK_PERIOD;
        -- Reset
        a <= '0';
        b <= '0';
        c <= '0';
        d <= '0';
        s <= "00";
        WAIT FOR MCLK_PERIOD;
        -- A Entrance Test
        a <= '1';
        WAIT FOR MCLK_PERIOD;
        -- S Position 2
        s <= "01";
        WAIT FOR MCLK_PERIOD;
        -- B Entrance Test
        a <= '0';
        b <= '1';
        WAIT FOR MCLK_PERIOD;
        -- S Position 3
        s <= "10";
        WAIT FOR MCLK_PERIOD;
        -- C Entrance Test
        b <= '0';
        c <= '1';
        WAIT FOR MCLK_PERIOD;
        -- S Position 4
        s <= "11";
        WAIT FOR MCLK_PERIOD;
        -- D Entrance Test
        c <= '0';
        d <= '1';
        WAIT FOR MCLK_PERIOD;

        WAIT;
    END PROCESS;
END ARCHITECTURE;