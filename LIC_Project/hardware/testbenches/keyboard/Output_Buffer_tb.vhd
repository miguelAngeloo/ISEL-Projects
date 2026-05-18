library ieee;
use ieee.std_logic_1164.all;

entity Output_Buffer_tb is
end Output_Buffer_tb;

architecture structural of Output_Buffer_tb is
	
component Output_Buffer is
	port(
		reset 	: in std_logic;
		clk		: in std_logic;
		Data     : in std_logic_vector(3 downto 0);
		Load		: in std_logic;
		Ack	   : in std_logic;
		Obfree	: out std_logic;
		Dval		: out std_logic;
		Q        : out std_logic_vector(3 downto 0)
);
end component;

constant mclk_period : time := 20 ns;
constant mclk_half_period : time := mclk_period / 2;


signal 		reset_tb 		: std_logic;
signal		clk_tb			: std_logic;
signal		Data_tb			: std_logic_vector(3 downto 0);
signal		Load_tb		   : std_logic;
signal		Obfree_tb		: std_logic;
signal		Dval_tb		   : std_logic;
signal		Q_tb			   : std_logic_vector(3 downto 0);
signal		Ack_tb		   : std_logic;

begin
   uut : Output_Buffer
	port map (
			reset => reset_tb,
			clk	=> clk_tb,
			Dval  => Dval_tb,
			Q => Q_tb,
			Load => Load_tb,
			Obfree => Obfree_tb,
			Ack => Ack_tb,
			Data => Data_tb
);
	
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
		  reset_tb <= '1';
        wait for mclk_period;
        reset_tb <= '0';

		  
		  
        Data_tb <= "0101";
        wait for mclk_period;

		  Load_tb <= '1';
        wait for mclk_half_period*5;
		  
		  Load_tb <= '1';
		  wait for mclk_period*5;
		  
        Load_tb <= '0';
        wait for mclk_half_period*5;

		  
		  Ack_tb <= '1';
		  wait for mclk_half_period*5;
		  
		  Ack_tb <= '0';
		  wait for mclk_half_period*7;
		  

        wait;
    end process;

end structural;
	