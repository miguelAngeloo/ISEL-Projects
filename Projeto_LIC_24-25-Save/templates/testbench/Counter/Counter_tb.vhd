LIBRARY ieee;
USE ieee.std_logic_1164.ALL;

ENTITY Counter_tb IS
END Counter_tb;

ARCHITECTURE behavioral OF Counter_tb IS

    COMPONENT Counter IS
        PORT (
            Reset : IN STD_LOGIC;
            Clock : IN STD_LOGIC;
            Enabled : IN STD_LOGIC;
            S : OUT STD_LOGIC_VECTOR(3 DOWNTO 0)
        );
    END COMPONENT;

    CONSTANT MCLK_PERIOD : TIME := 20 ns;
    CONSTANT MCLK_HALF_PERIOD : TIME := MCLK_PERIOD/2;

    SIGNAL r, clk, e : STD_LOGIC;
    SIGNAL s : STD_LOGIC_VECTOR(3 DOWNTO 0);

BEGIN
    clk_gen : PROCESS
    BEGIN
        clk <= '0';
        WAIT FOR MCLK_HALF_PERIOD;
        clk <= '1';
        WAIT FOR MCLK_HALF_PERIOD;
    END PROCESS;

    Counter_test : Counter
    PORT MAP(
        Reset => r,
        Clock => clk,
        Enabled => e,
        S => s
    );

    stimulus : PROCESS
    BEGIN
        -- Reset On
        r <= '1';
        e <= '0';
        WAIT FOR MCLK_HALF_PERIOD;
        -- Reset Off
        r <= '0';
        e <= '0';
        WAIT FOR MCLK_HALF_PERIOD;
        -- Enable On
        e <= '1';
        WAIT FOR MCLK_PERIOD * 16;

        WAIT;
    END PROCESS;
END ARCHITECTURE;