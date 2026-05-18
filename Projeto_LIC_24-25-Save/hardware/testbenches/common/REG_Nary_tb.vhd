LIBRARY IEEE;
USE IEEE.STD_LOGIC_1164.ALL;

ENTITY REG_Nary_tb IS
END REG_Nary_tb;

ARCHITECTURE behavior OF REG_Nary_tb IS

    -- Component declaration
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

    -- Signals to connect to DUT
    SIGNAL clk_tb : STD_LOGIC;
    SIGNAL reset_tb : STD_LOGIC;
    SIGNAL D_tb : STD_LOGIC_VECTOR(7 DOWNTO 0) := "00000000"; -- Adjusted for 8 bits
    SIGNAL E_tb : STD_LOGIC := '0';
    SIGNAL Sout_tb : STD_LOGIC_VECTOR(7 DOWNTO 0); -- Adjusted for 8 bits

    -- Clock period definition
    CONSTANT MCLK_PERIOD : TIME := 20 ns;
    CONSTANT MCLK_HALF_PERIOD : TIME := MCLK_PERIOD / 2;

BEGIN

    -- Instantiate the Unit Under Test (UUT)
    uut : REG_Nary
    GENERIC MAP(size => 8)
    PORT MAP(
        CLK => clk_tb,
        RESET => reset_tb,
        D => D_tb,
        EN => E_tb,
        Q => Sout_tb
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
    BEGIN
        -- Initial reset
        reset_tb <= '1';
        WAIT FOR MCLK_PERIOD;
        WAIT FOR MCLK_HALF_PERIOD;
        reset_tb <= '0';

        -- Enable the shift register and apply inputs
        E_tb <= '1';
        WAIT FOR MCLK_HALF_PERIOD;

        -- Apply 5 bits of input serially
        D_tb <= "00000000"; -- Initial value
        WAIT FOR MCLK_PERIOD;
        D_tb <= "11111111";
        WAIT FOR MCLK_PERIOD;
        D_tb <= "10101010";
        WAIT FOR MCLK_PERIOD;
        D_tb <= "01010101";
        WAIT FOR MCLK_HALF_PERIOD;
        E_tb <= '0'; -- Disable the register
        WAIT FOR MCLK_HALF_PERIOD;

        -- Finish simulation
        WAIT;
    END PROCESS;

END behavior;