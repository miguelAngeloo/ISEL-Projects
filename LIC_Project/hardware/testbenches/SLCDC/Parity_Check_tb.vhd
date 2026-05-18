LIBRARY IEEE;
USE IEEE.STD_LOGIC_1164.ALL;

ENTITY Parity_Check_tb IS
END Parity_Check_tb;

ARCHITECTURE behavior OF Parity_Check_tb IS

    -- Component declaration
    COMPONENT Parity_Check IS
        PORT (
            serialData : IN STD_LOGIC;
            serialClock : IN STD_LOGIC;
            init : IN STD_LOGIC;
            error : OUT STD_LOGIC
        );
    END COMPONENT;

    -- Signals to connect to DUT
    SIGNAL Data_tb : STD_LOGIC := '0';
    SIGNAL clk_tb : STD_LOGIC := '0';
    SIGNAL init_tb : STD_LOGIC := '0';
    SIGNAL error_tb : STD_LOGIC;

    -- Clock period definition
    CONSTANT MCLK_PERIOD : TIME := 20 ns;
    CONSTANT MCLK_HALF_PERIOD : TIME := MCLK_PERIOD / 2;

BEGIN

    -- Instantiate the Unit Under Test (UUT)
    uut : Parity_Check PORT MAP (
        serialData => Data_tb,
        serialClock => clk_tb,
        init => init_tb,
        error => error_tb
    );

    -- Clock generation process
    clk_process : PROCESS
    BEGIN
        WHILE true LOOP
            clk_tb <= '1';
            WAIT FOR MCLK_HALF_PERIOD;
            clk_tb <= '0';
            WAIT FOR MCLK_HALF_PERIOD;
        END LOOP;
    END PROCESS;

    -- Stimulus process
    stimulus : PROCESS
    -- Total Time of execution: 160 ns
    BEGIN
        -- Initial reset
        init_tb <= '1';
        WAIT FOR MCLK_PERIOD;
        WAIT FOR MCLK_HALF_PERIOD;
        init_tb <= '0';
        
        -- Test with data size of 6 bits
        -- Data pattern: 110010 (Odd parity)
        Data_tb <= '1'; -- D0
        WAIT FOR MCLK_PERIOD; -- ERROR = '0'
        Data_tb <= '1'; -- D1
        WAIT FOR MCLK_PERIOD; -- ERROR = '1'
        Data_tb <= '0'; -- D2
        WAIT FOR MCLK_PERIOD; -- ERROR = '1'
        Data_tb <= '0'; -- D3
        WAIT FOR MCLK_PERIOD; -- ERROR = '1'
        Data_tb <= '1'; -- D4
        WAIT FOR MCLK_PERIOD; -- ERROR = '0'
        Data_tb <= '0'; -- Parity bit (Odd parity => (3 + 0) % 2 = 1 <=> Parity Bit = 0)
        WAIT FOR MCLK_PERIOD; -- ERROR = '0'

        -- At the end of the data stream, the error signal should be '0' (odd parity)

        -- Finish simulation
        WAIT;
    END PROCESS;

END behavior;