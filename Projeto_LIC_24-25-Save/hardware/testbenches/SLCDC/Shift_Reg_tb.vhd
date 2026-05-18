library IEEE;
use IEEE.STD_LOGIC_1164.ALL;

entity Shift_Reg_tb is
end Shift_Reg_tb;

architecture behavior of Shift_Reg_tb is

    -- Component declaration
    component Shift_Reg
        port(
            Data : in std_logic;
            clk : in std_logic;
            E: in std_logic;
            reset: in std_logic;
            Sout: out std_logic;
            D: out std_logic_vector(4 downto 0)
        );
    end component;

    -- Signals to connect to DUT
    signal Data_tb   : std_logic := '0';
    signal clk_tb   : std_logic := '0';
    signal E_tb      : std_logic := '0';
    signal reset_tb  : std_logic := '0';
    signal Sout_tb   : std_logic;
    signal D_tb      : std_logic_vector(4 downto 0);

    -- Clock period definition
    CONSTANT MCLK_PERIOD : TIME := 20 ns;
    CONSTANT MCLK_HALF_PERIOD : TIME := MCLK_PERIOD / 2;

begin

    -- Instantiate the Unit Under Test (UUT)
    uut: Shift_Reg port map (
        Data => Data_tb,
        clk => clk_tb,
        E => E_tb,
        reset => reset_tb,
        Sout => Sout_tb,
        D => D_tb
    );

    -- Clock generation process
    clk_process : process
    begin
        while true loop
            clk_tb <= '1';
            wait for MCLK_HALF_PERIOD;
            clk_tb <= '0';
            wait for MCLK_HALF_PERIOD;
        end loop;
    end process;

    -- Stimulus process
    stimulus : process
    begin
        -- Initial reset
        reset_tb <= '1';
        wait for MCLK_PERIOD * 2;
        reset_tb <= '0';

        -- Enable the shift register and apply inputs
        E_tb <= '1';

        -- Apply 5 bits of input serially
        Data_tb <= '1';
		  wait for MCLK_PERIOD;
        Data_tb <= '0';
		  wait for MCLK_PERIOD;
        Data_tb <= '1';
		  wait for MCLK_PERIOD;
        Data_tb <= '1';
		  wait for MCLK_PERIOD;
        Data_tb <= '0';
		  wait for MCLK_PERIOD;

        -- Wait a few cycles to observe output
        E_tb <= '0';
        wait for MCLK_PERIOD * 5;

        -- Finish simulation
        wait;
    end process;

end behavior;