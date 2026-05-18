library ieee;
use ieee.std_logic_1164.all;

entity SRC_tb is
end SRC_tb;

architecture tb of SRC_tb is

  -- DUT component
  component SRC is
    port(
      reset   : in std_logic;
      Mclk    : in std_logic;
      SS      : in std_logic;
      SCLK    : in std_logic;
      SDX     : in std_logic;
      E       : out std_logic;
      Dout    : out std_logic_vector(7 downto 0)
    );
  end component;

  -- Signals for testing
  signal reset_tb     : std_logic := '0';
  signal Mclk_tb      : std_logic := '0';
  signal SS_tb        : std_logic := '1';
  signal SCLK_tb      : std_logic := '0';
  signal SDX_tb       : std_logic := '0';
  signal E_tb         : std_logic;
  signal Dout_tb      : std_logic_vector(7 downto 0);

  constant CLK_PERIOD : time := 20 ns;
  constant SCLK_PERIOD : time := 40 ns;

begin

  -- Instantiate DUT
  DUT: SRC
    port map (
      reset   => reset_tb,
      Mclk    => Mclk_tb,
      SS      => SS_tb,
      SCLK    => SCLK_tb,
      SDX     => SDX_tb,
      E       => E_tb,
      Dout    => Dout_tb
    );

  -- Clock generator for Mclk
  clk_proc: process
  begin
    while true loop
      Mclk_tb <= '0';
      wait for CLK_PERIOD / 2;
      Mclk_tb <= '1';
      wait for CLK_PERIOD / 2;
    end loop;
  end process;

  -- Clock generator for SCLK
  sclk_proc: process
  begin
    while true loop
      SCLK_tb <= '0';
      wait for SCLK_PERIOD / 2;
      SCLK_tb <= '1';
      wait for SCLK_PERIOD / 2;
    end loop;
  end process;

  -- Stimulus process
  stim_proc: process
  begin
    -- Initial reset
    reset_tb <= '1';
    wait for 40 ns;
    reset_tb <= '0';

    -- Begin transmission (8-bit example: 1010_1101)
    SS_tb <= '0';
    wait for SCLK_PERIOD;

    SDX_tb <= '1'; wait for SCLK_PERIOD; -- bit 7
    SDX_tb <= '0'; wait for SCLK_PERIOD; -- bit 6
    SDX_tb <= '1'; wait for SCLK_PERIOD; -- bit 5
    SDX_tb <= '1'; wait for SCLK_PERIOD; -- bit 4
    SDX_tb <= '0'; wait for SCLK_PERIOD; -- bit 3
    SDX_tb <= '1'; wait for SCLK_PERIOD; -- bit 2
    SDX_tb <= '0'; wait for SCLK_PERIOD; -- bit 1
    SDX_tb <= '1'; wait for SCLK_PERIOD; -- bit 0

    -- End transmission
    SS_tb <= '1';

    wait for 200 ns;
    
    wait;
  end process;

end tb;
