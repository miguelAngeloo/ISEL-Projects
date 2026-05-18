LIBRARY ieee;
USE ieee.std_logic_1164.ALL;

ENTITY key_decode_tb IS
END key_decode_tb;

ARCHITECTURE behavioral OF key_decode_tb IS

    COMPONENT Key_Decode
        PORT (
            reset : IN STD_LOGIC;
            mclk : IN STD_LOGIC;
            kack_dec : IN STD_LOGIC;
            lines_dec : IN STD_LOGIC_VECTOR(3 DOWNTO 0);
            kval_dec : OUT STD_LOGIC;
            columns_dec : OUT STD_LOGIC_VECTOR(3 DOWNTO 0);
            k : OUT STD_LOGIC_VECTOR(3 DOWNTO 0)
        );
    END COMPONENT;

    CONSTANT MCLK_PERIOD : TIME := 20 ns;
    CONSTANT MCLK_HALF_PERIOD : TIME := MCLK_PERIOD / 2;
    CONSTANT DIV_CLK : TIME := MCLK_PERIOD * 100_000;

    SIGNAL reset_tb : STD_LOGIC := '0';
    SIGNAL mclk_tb : STD_LOGIC := '0';
    SIGNAL kack_dec_tb : STD_LOGIC := '0';
    SIGNAL lines_dec_tb : STD_LOGIC_VECTOR(3 DOWNTO 0) := "0000";
    SIGNAL kval_dec_tb : STD_LOGIC;
    SIGNAL columns_dec_tb : STD_LOGIC_VECTOR(3 DOWNTO 0);
    SIGNAL k_tb : STD_LOGIC_VECTOR(3 DOWNTO 0);
    SIGNAL not_lines_dec_tb : STD_LOGIC_VECTOR(3 DOWNTO 0);
    SIGNAL not_columns_dec_tb : STD_LOGIC_VECTOR(3 DOWNTO 0);

BEGIN
    uut : Key_Decode
    PORT MAP(
        reset => reset_tb,
        mclk => mclk_tb,
        kack_dec => kack_dec_tb,
        lines_dec => not_lines_dec_tb,
        kval_dec => kval_dec_tb,
        columns_dec => columns_dec_tb,
        k => k_tb
    );

    not_lines_dec_tb <= NOT lines_dec_tb;
    not_columns_dec_tb <= NOT columns_dec_tb;

    clk_gen : PROCESS
    BEGIN
        WHILE true LOOP
            mclk_tb <= '1';
            WAIT FOR MCLK_HALF_PERIOD;
            mclk_tb <= '0';
            WAIT FOR MCLK_HALF_PERIOD;
        END LOOP;
    END PROCESS;

    stimulus : PROCESS
        -- Total time of execution: 54 ms
    BEGIN
        -- 1 Clock cycle = 2 ms (100_000 Times slower than the real clock frequency - 20 ns)

        -- Initialize the inputs
        WAIT FOR DIV_CLK;
        kack_dec_tb <= '0';
        lines_dec_tb <= "0000";
        WAIT FOR DIV_CLK;
        -- Reset
        reset_tb <= '1';
        WAIT FOR DIV_CLK * 2;
        reset_tb <= '0';
        WAIT FOR DIV_CLK;

        -- Test 1
        lines_dec_tb <= "0010";
        WAIT FOR DIV_CLK * 2;
        lines_dec_tb <= "0000";
        WAIT FOR DIV_CLK * 2;
        kack_dec_tb <= '1';
        WAIT FOR DIV_CLK * 2;
        kack_dec_tb <= '0';
        WAIT FOR DIV_CLK;
        WAIT;
    END PROCESS;

END behavioral;