LIBRARY ieee;
USE ieee.std_logic_1164.ALL;

ENTITY keyboard_reader_tb IS
END keyboard_reader_tb;

ARCHITECTURE behavioral OF keyboard_reader_tb IS

    COMPONENT KeyBoardReader IS
        PORT (
            reset : IN STD_LOGIC;
            Mclk : IN STD_LOGIC;
            Kack : IN STD_LOGIC;
            Lines_KBR : IN STD_LOGIC_VECTOR(3 DOWNTO 0);
            Dval : OUT STD_LOGIC;
            Columns_KBR : OUT STD_LOGIC_VECTOR(3 DOWNTO 0);
            Q : OUT STD_LOGIC_VECTOR(3 DOWNTO 0)
        );
    END COMPONENT;

    CONSTANT MCLK_PERIOD : TIME := 20 ns;
    CONSTANT MCLK_HALF_PERIOD : TIME := MCLK_PERIOD / 2;
    CONSTANT DIV_CLK : TIME := MCLK_PERIOD * 100_000;

    SIGNAL reset_tb : STD_LOGIC := '0';
    SIGNAL mclk_tb : STD_LOGIC := '0';
    SIGNAL kack_tb : STD_LOGIC := '0';
    SIGNAL lines_kbr_tb : STD_LOGIC_VECTOR(3 DOWNTO 0) := "0000";
    SIGNAL kval_tb : STD_LOGIC;
    SIGNAL columns_kbr_tb : STD_LOGIC_VECTOR(3 DOWNTO 0);
    SIGNAL k_tb : STD_LOGIC_VECTOR(3 DOWNTO 0);
    SIGNAL not_lines_kbr_tb : STD_LOGIC_VECTOR(3 DOWNTO 0);
    SIGNAL not_columns_kbr_tb : STD_LOGIC_VECTOR(3 DOWNTO 0);

BEGIN
    uut : KeyBoardReader
    PORT MAP(
        reset => reset_tb,
        Mclk => mclk_tb,
        Kack => kack_tb,
        Lines_KBR => not_lines_kbr_tb,
        Dval => kval_tb,
        Columns_KBR => columns_kbr_tb,
        Q => k_tb
    );

    not_lines_kbr_tb <= NOT lines_kbr_tb;
    not_columns_kbr_tb <= NOT columns_kbr_tb;

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
        -- Total time of execution: 290 ms (145 clocks)
    BEGIN
        -- 1 Clock cycle = 2 ms (100_000 Times slower than the real clock frequency - 20 ns)

        -- Initialize the inputs
        WAIT FOR DIV_CLK;
        kack_tb <= '0';
        lines_kbr_tb <= "0000";
        WAIT FOR DIV_CLK;
        -- Reset
        reset_tb <= '1';
        WAIT FOR DIV_CLK * 2;
        reset_tb <= '0';
        WAIT FOR DIV_CLK;

        -- Test 1
        lines_kbr_tb <= "0010";
        WAIT FOR DIV_CLK * 2;
        lines_kbr_tb <= "0000";
        WAIT FOR DIV_CLK * 2;
        kack_tb <= '1';
        WAIT FOR DIV_CLK * 2;
        kack_tb <= '0';
        WAIT FOR DIV_CLK;

        -- Key Stored on Output Buffer
        lines_kbr_tb <= "0010";
        WAIT FOR DIV_CLK * 2;
        lines_kbr_tb <= "0000";
        WAIT FOR DIV_CLK * 3;  -- 17 clk

        -- Keys Stored on Ring Buffer
        lines_kbr_tb <= "1000";
        WAIT FOR DIV_CLK * 2;
        lines_kbr_tb <= "0000";
        WAIT FOR DIV_CLK * 3;  -- 22 clk

        lines_kbr_tb <= "0010";
        WAIT FOR DIV_CLK * 2;
        lines_kbr_tb <= "0000";
        WAIT FOR DIV_CLK * 3;  -- 27 clk

        lines_kbr_tb <= "1000";
        WAIT FOR DIV_CLK * 2;
        lines_kbr_tb <= "0000";
        WAIT FOR DIV_CLK * 3;  -- 32 clk

        lines_kbr_tb <= "0010";
        WAIT FOR DIV_CLK * 2;
        lines_kbr_tb <= "0000";
        WAIT FOR DIV_CLK * 3;  -- 37 clk  -- 4th key

        lines_kbr_tb <= "1000";
        WAIT FOR DIV_CLK * 2;
        lines_kbr_tb <= "0000";
        WAIT FOR DIV_CLK * 3;  -- 42 clk

        lines_kbr_tb <= "0010";
        WAIT FOR DIV_CLK * 2;
        lines_kbr_tb <= "0000";
        WAIT FOR DIV_CLK * 3;  -- 47 clk

        lines_kbr_tb <= "1000";
        WAIT FOR DIV_CLK * 2;
        lines_kbr_tb <= "0000";
        WAIT FOR DIV_CLK * 3;  -- 52 clk

        lines_kbr_tb <= "0010";
        WAIT FOR DIV_CLK * 2;
        lines_kbr_tb <= "0000";
        WAIT FOR DIV_CLK * 3;  -- 57 clk  -- 8th key

        lines_kbr_tb <= "1000";
        WAIT FOR DIV_CLK * 2;
        lines_kbr_tb <= "0000";
        WAIT FOR DIV_CLK * 3;  -- 42 clk

        lines_kbr_tb <= "0010";
        WAIT FOR DIV_CLK * 2;
        lines_kbr_tb <= "0000";
        WAIT FOR DIV_CLK * 3;  -- 47 clk

        lines_kbr_tb <= "1000";
        WAIT FOR DIV_CLK * 2;
        lines_kbr_tb <= "0000";
        WAIT FOR DIV_CLK * 3;  -- 52 clk

        lines_kbr_tb <= "0010";
        WAIT FOR DIV_CLK * 2;
        lines_kbr_tb <= "0000";
        WAIT FOR DIV_CLK * 3;  -- 57 clk  -- 12th key

        lines_kbr_tb <= "1000";
        WAIT FOR DIV_CLK * 2;
        lines_kbr_tb <= "0000";
        WAIT FOR DIV_CLK * 3;  -- 42 clk

        lines_kbr_tb <= "0010";
        WAIT FOR DIV_CLK * 2;
        lines_kbr_tb <= "0000";
        WAIT FOR DIV_CLK * 3;  -- 47 clk

        lines_kbr_tb <= "1000";
        WAIT FOR DIV_CLK * 2;
        lines_kbr_tb <= "0000";
        WAIT FOR DIV_CLK * 3;  -- 52 clk

        lines_kbr_tb <= "0010";
        WAIT FOR DIV_CLK * 2;
        lines_kbr_tb <= "0000";
        WAIT FOR DIV_CLK * 3;  -- 57 clk  -- 16th key

        -- Key Waiting Process on Key Decode
        lines_kbr_tb <= "1000";
        WAIT FOR DIV_CLK * 2;
        lines_kbr_tb <= "0000";
        WAIT FOR DIV_CLK * 3;  -- 62 clk

        -- Key Lost on Key Decode
        lines_kbr_tb <= "0010";
        WAIT FOR DIV_CLK * 2;
        lines_kbr_tb <= "0000";
        WAIT FOR DIV_CLK * 3; -- 67 clk

        -- Reading Process on Key Decode

        kack_tb <= '1';
        WAIT FOR DIV_CLK;
        kack_tb <= '0';
        WAIT FOR DIV_CLK;

        kack_tb <= '1';
        WAIT FOR DIV_CLK;
        kack_tb <= '0';
        WAIT FOR DIV_CLK;

        kack_tb <= '1';
        WAIT FOR DIV_CLK;
        kack_tb <= '0';
        WAIT FOR DIV_CLK;

        kack_tb <= '1';
        WAIT FOR DIV_CLK;
        kack_tb <= '0';
        WAIT FOR DIV_CLK;

        kack_tb <= '1';
        WAIT FOR DIV_CLK;
        kack_tb <= '0';
        WAIT FOR DIV_CLK;

        kack_tb <= '1';
        WAIT FOR DIV_CLK;
        kack_tb <= '0';
        WAIT FOR DIV_CLK;

        kack_tb <= '1';
        WAIT FOR DIV_CLK;
        kack_tb <= '0';
        WAIT FOR DIV_CLK;

        kack_tb <= '1';
        WAIT FOR DIV_CLK;
        kack_tb <= '0';
        WAIT FOR DIV_CLK; -- 8th key

        kack_tb <= '1';
        WAIT FOR DIV_CLK;
        kack_tb <= '0';
        WAIT FOR DIV_CLK;

        kack_tb <= '1';
        WAIT FOR DIV_CLK;
        kack_tb <= '0';
        WAIT FOR DIV_CLK;

        kack_tb <= '1';
        WAIT FOR DIV_CLK;
        kack_tb <= '0';
        WAIT FOR DIV_CLK;

        kack_tb <= '1';
        WAIT FOR DIV_CLK;
        kack_tb <= '0';
        WAIT FOR DIV_CLK;

        kack_tb <= '1';
        WAIT FOR DIV_CLK;
        kack_tb <= '0';
        WAIT FOR DIV_CLK;

        kack_tb <= '1';
        WAIT FOR DIV_CLK;
        kack_tb <= '0';
        WAIT FOR DIV_CLK;

        kack_tb <= '1';
        WAIT FOR DIV_CLK;
        kack_tb <= '0';
        WAIT FOR DIV_CLK;

        kack_tb <= '1';
        WAIT FOR DIV_CLK;
        kack_tb <= '0';
        WAIT FOR DIV_CLK;  -- 16th key

        kack_tb <= '1';
        WAIT FOR DIV_CLK;
        kack_tb <= '0';
        WAIT FOR DIV_CLK;

        kack_tb <= '1';
        WAIT FOR DIV_CLK;
        kack_tb <= '0';
        WAIT FOR DIV_CLK;  -- Final key (18th key)

        -- Try Read With No Key
        kack_tb <= '1';
        WAIT FOR DIV_CLK;
        kack_tb <= '0';
        WAIT FOR DIV_CLK;

        WAIT;
    END PROCESS;

END behavioral;