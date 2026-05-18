LIBRARY ieee;
USE ieee.std_logic_1164.ALL;
ENTITY Decoder_tb IS
END Decoder_tb;

ARCHITECTURE behavioral OF Decoder_tb IS

    COMPONENT Decoder IS
    PORT (
        S0 : IN STD_LOGIC;
        S1 : IN STD_LOGIC;
        EX0 : OUT STD_LOGIC;
        EX1 : OUT STD_LOGIC;
        EX2 : OUT STD_LOGIC;
        EX3 : OUT STD_LOGIC
    );
    END COMPONENT;

    CONSTANT MCLK_PERIOD : TIME := 20 ns;
    CONSTANT MCLK_HALF_PERIOD : TIME := MCLK_PERIOD / 2;

    SIGNAL i : STD_LOGIC_VECTOR(1 DOWNTO 0);
    SIGNAL s : STD_LOGIC_VECTOR(3 DOWNTO 0);

BEGIN
    Decoder_test : Decoder
    PORT MAP(
        S0 => i(0),
        S1 => i(1),
        EX0 => s(0),
        EX1 => s(1),
        EX2 => s(2),
        EX3 => s(3)
    );
    stimulus : PROCESS
    -- Total Time of execution: 100 ns
    BEGIN
        WAIT FOR MCLK_PERIOD;
        -- I Position 1
        i <= "00";
        WAIT FOR MCLK_PERIOD;
        -- I Position 2
        i <= "01";
        WAIT FOR MCLK_PERIOD;
        -- I Position 3
        i <= "10";
        WAIT FOR MCLK_PERIOD;
        -- I Position 4
        i <= "11";
        WAIT FOR MCLK_PERIOD;
        
        WAIT;
    END PROCESS;
END ARCHITECTURE;