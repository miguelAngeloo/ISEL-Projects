LIBRARY ieee;
USE ieee.std_logic_1164.ALL;
ENTITY Decoder_tb IS
END Decoder_tb;

ARCHITECTURE behavioral OF Decoder_tb IS

    COMPONENT Decoder IS
    PORT (
        I : IN STD_LOGIC_VECTOR(1 DOWNTO 0);
        S : OUT STD_LOGIC_VECTOR(3 DOWNTO 0)
    );
    END COMPONENT;

    CONSTANT MCLK_PERIOD : TIME := 20 ns;
    CONSTANT MCLK_HALF_PERIOD : TIME := MCLK_PERIOD / 2;

    SIGNAL i : STD_LOGIC_VECTOR(1 DOWNTO 0);
    SIGNAL s : STD_LOGIC_VECTOR(3 DOWNTO 0);

BEGIN
    Decoder_test : Decoder
    PORT MAP(
        i => I,
        s => S
    );
    stimulus : PROCESS
    BEGIN
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