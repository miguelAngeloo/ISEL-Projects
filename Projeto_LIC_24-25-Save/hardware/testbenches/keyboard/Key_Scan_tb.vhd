LIBRARY ieee;
USE ieee.std_logic_1164.ALL;

ENTITY key_scan_tb IS
END key_scan_tb;

ARCHITECTURE behavioral OF key_scan_tb IS
    COMPONENT key_scan
        PORT (
            clk : IN STD_LOGIC;
            reset : IN STD_LOGIC;
            keyscan : IN STD_LOGIC;
            lines : IN STD_LOGIC_VECTOR(3 DOWNTO 0);
            keypress : OUT STD_LOGIC;
            keydata : OUT STD_LOGIC_VECTOR(3 DOWNTO 0);
            columns : OUT STD_LOGIC_VECTOR(3 DOWNTO 0)
        );
    END COMPONENT;

    CONSTANT MCLK_PERIOD : TIME := 20 ns;
    CONSTANT MCLK_HALF_PERIOD : TIME := MCLK_PERIOD / 2;

    SIGNAL clk_tb : STD_LOGIC := '0';
    SIGNAL reset_tb : STD_LOGIC := '0';
    SIGNAL keyscan_tb : STD_LOGIC := '0';
    SIGNAL lines_tb : STD_LOGIC_VECTOR(3 DOWNTO 0) := "0000";
    SIGNAL keypress_tb : STD_LOGIC;
    SIGNAL keydata_tb : STD_LOGIC_VECTOR(3 DOWNTO 0);
    SIGNAL columns_tb : STD_LOGIC_VECTOR(3 DOWNTO 0);
    SIGNAL not_lines_tb : STD_LOGIC_VECTOR(3 DOWNTO 0);
    SIGNAL not_columns_tb : STD_LOGIC_VECTOR(3 DOWNTO 0);

BEGIN
    uut : key_scan
    PORT MAP(
        clk => clk_tb,
        reset => reset_tb,
        keyscan => keyscan_tb,
        lines => not_lines_tb,
        keypress => keypress_tb,
        keydata => keydata_tb,
        columns => columns_tb
    );

    not_lines_tb <= NOT lines_tb;
    not_columns_tb <= NOT columns_tb;

    clk_gen : PROCESS
    BEGIN
        WHILE true LOOP
            clk_tb <= '1';
            WAIT FOR MCLK_HALF_PERIOD;
            clk_tb <= '0';
            WAIT FOR MCLK_HALF_PERIOD;
        END LOOP;
    END PROCESS;

    stimulus : PROCESS
        -- Total Time of execution: 1040 ns
    BEGIN
        WAIT FOR MCLK_PERIOD;
        -- Reset
        reset_tb <= '1';
        keyscan_tb <= '0';
        WAIT FOR MCLK_PERIOD * 2;
        reset_tb <= '0';
        WAIT FOR MCLK_PERIOD;

        -- For each key, one clock pressed, one clock released, one clock for scan

        -- NEW COLUMN (1)

        --  Line 1 (key 1)
        lines_tb <= "0001";
        WAIT FOR MCLK_PERIOD;
        lines_tb <= "0000";
        WAIT FOR MCLK_PERIOD;
        keyscan_tb <= '1';
        WAIT FOR MCLK_PERIOD;

        keyscan_tb <= '0';

        --  Line 2 (key 4)
        lines_tb <= "0010";
        WAIT FOR MCLK_PERIOD;
        lines_tb <= "0000";
        WAIT FOR MCLK_PERIOD;
        keyscan_tb <= '1';
        WAIT FOR MCLK_PERIOD;

        keyscan_tb <= '0';

        --  Line 3 (key 7)
        lines_tb <= "0100";
        WAIT FOR MCLK_PERIOD;
        lines_tb <= "0000";
        WAIT FOR MCLK_PERIOD;
        keyscan_tb <= '1';
        WAIT FOR MCLK_PERIOD;

        keyscan_tb <= '0';

        --  Line 4 (key *)
        lines_tb <= "1000";
        WAIT FOR MCLK_PERIOD;
        lines_tb <= "0000";
        WAIT FOR MCLK_PERIOD;
        keyscan_tb <= '1';
        WAIT FOR MCLK_PERIOD;

        -- NEW COLUMN (2)

        keyscan_tb <= '0';

        --  Line 1 (key 2)
        lines_tb <= "0001";
        WAIT FOR MCLK_PERIOD;
        lines_tb <= "0000";
        WAIT FOR MCLK_PERIOD;
        keyscan_tb <= '1';
        WAIT FOR MCLK_PERIOD;

        keyscan_tb <= '0';

        --  Line 2 (key 5)
        lines_tb <= "0010";
        WAIT FOR MCLK_PERIOD;
        lines_tb <= "0000";
        WAIT FOR MCLK_PERIOD;
        keyscan_tb <= '1';
        WAIT FOR MCLK_PERIOD;

        keyscan_tb <= '0';

        --  Line 3 (key 8)
        lines_tb <= "0100";
        WAIT FOR MCLK_PERIOD;
        lines_tb <= "0000";
        WAIT FOR MCLK_PERIOD;
        keyscan_tb <= '1';
        WAIT FOR MCLK_PERIOD;

        keyscan_tb <= '0';

        --  Line 4 (key 0)
        lines_tb <= "1000";
        WAIT FOR MCLK_PERIOD;
        lines_tb <= "0000";
        WAIT FOR MCLK_PERIOD;
        keyscan_tb <= '1';
        WAIT FOR MCLK_PERIOD;
        -- NEW COLUMN (3)

        keyscan_tb <= '0';

        --  Line 1 (key 3)
        lines_tb <= "0001";
        WAIT FOR MCLK_PERIOD;
        lines_tb <= "0000";
        WAIT FOR MCLK_PERIOD;
        keyscan_tb <= '1';
        WAIT FOR MCLK_PERIOD;

        keyscan_tb <= '0';

        --  Line 2 (key 6)
        lines_tb <= "0010";
        WAIT FOR MCLK_PERIOD;
        lines_tb <= "0000";
        WAIT FOR MCLK_PERIOD;
        keyscan_tb <= '1';
        WAIT FOR MCLK_PERIOD;

        keyscan_tb <= '0';

        --  Line 3 (key 9)
        lines_tb <= "0100";
        WAIT FOR MCLK_PERIOD;
        lines_tb <= "0000";
        WAIT FOR MCLK_PERIOD;
        keyscan_tb <= '1';
        WAIT FOR MCLK_PERIOD;

        keyscan_tb <= '0';

        --  Line 4 (key #)
        lines_tb <= "1000";
        WAIT FOR MCLK_PERIOD;
        lines_tb <= "0000";
        WAIT FOR MCLK_PERIOD;
        keyscan_tb <= '1';
        WAIT FOR MCLK_PERIOD;
        -- NEW COLUMN (4)

        keyscan_tb <= '0';

        --  Line 1 (key A)
        lines_tb <= "0001";
        WAIT FOR MCLK_PERIOD;
        lines_tb <= "0000";
        WAIT FOR MCLK_PERIOD;
        keyscan_tb <= '1';
        WAIT FOR MCLK_PERIOD;

        keyscan_tb <= '0';

        --  Line 2 (key B)
        lines_tb <= "0010";
        WAIT FOR MCLK_PERIOD;
        lines_tb <= "0000";
        WAIT FOR MCLK_PERIOD;
        keyscan_tb <= '1';
        WAIT FOR MCLK_PERIOD;

        keyscan_tb <= '0';

        --  Line 3 (key C)
        lines_tb <= "0100";
        WAIT FOR MCLK_PERIOD;
        lines_tb <= "0000";
        WAIT FOR MCLK_PERIOD;
        keyscan_tb <= '1';
        WAIT FOR MCLK_PERIOD;

        keyscan_tb <= '0';

        --  Line 4 (key D)
        lines_tb <= "1000";
        WAIT FOR MCLK_PERIOD;
        lines_tb <= "0000";
        WAIT FOR MCLK_PERIOD;

        -- Signal end of test
        keyscan_tb <= '1';
        WAIT FOR MCLK_HALF_PERIOD;
        keyscan_tb <= '0';
        WAIT FOR MCLK_HALF_PERIOD;
        WAIT;
    END PROCESS;

END behavioral;