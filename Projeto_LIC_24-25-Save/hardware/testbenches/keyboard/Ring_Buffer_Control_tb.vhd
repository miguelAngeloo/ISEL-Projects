library ieee;
use ieee.std_logic_1164.all;

entity Ring_Buffer_Control_tb is
end Ring_Buffer_Control_tb;

architecture behavioral of Ring_Buffer_Control_tb is
	
component Ring_Buffer_Control is
	port(
		reset 	: in std_logic;
		clk		: in std_logic;
		DAV		: in std_logic;
		CTS	   : in std_logic;
		full		: in std_logic;
		empty		: in std_logic;
		Wr		   : out std_logic;
		selPG		: out std_logic;
		incPutC	: out std_logic;
		incGetC	: out std_logic;
		Wreg	   : out std_logic;
		DAC	   : out std_logic
);
end component;

constant mclk_period : time := 20 ns;
constant mclk_half_period : time := mclk_period / 2;


signal 		reset_tb 		: std_logic;
signal		clk_tb			: std_logic;
signal		DAV_tb			: std_logic;
signal		CTS_tb		   : std_logic;
signal		full_tb		   : std_logic;
signal		empty_tb		   : std_logic;
signal		Wr_tb			   : std_logic;
signal		selPG_tb		   : std_logic;
signal		incPutC_tb		: std_logic;
signal		incGetC_tb		: std_logic;
signal		Wreg_tb		   : std_logic;
signal		DAC_tb	      : std_logic;

begin
   uut : Ring_Buffer_Control
	port map (
			reset => reset_tb,
			clk	=> clk_tb,
			DAV  => DAV_tb,
			CTS => CTS_tb,
			full => full_tb,
			empty => empty_tb,
			Wr => Wr_tb,
			selPG => selPG_tb,
			incPutC => incPutC_tb,
			incGetC => incGetC_tb,
			Wreg => Wreg_tb,
			DAC => DAC_tb
);
	
clk_gen : process
begin
		clk_tb <= '0';
		wait for mclk_half_period;
		clk_tb <= '1';
		wait for mclk_half_period;
end process;

stimulus: process
    begin
		  reset_tb <= '1';
        wait for mclk_period;
        reset_tb <= '0';

		  
		  
        DAV_tb <= '1';
        wait for mclk_period;

		  full_tb <= '0';
        wait for mclk_half_period*5;
		  
		  DAV_tb <= '1';
		  wait for mclk_period*5;
		  
        DAV_tb <= '0';
        wait for mclk_half_period*5;

		  
		  DAV_tb <= '0';
		  wait for mclk_half_period*5;
		  
		  empty_tb <= '1';
		  wait for mclk_half_period*5;
		  
		  empty_tb <= '0';
		  wait for mclk_half_period*5;
		  
		  CTS_tb <= '0';
		  wait for mclk_half_period*5;
		  
		  CTS_tb <= '1';
		  wait for mclk_half_period*7;
		  
		  DAV_tb <= '1';
		  full_tb <= '1';
		  wait for mclk_half_period*5;
		  
		  CTS_tb <= '1';
		  wait for mclk_half_period*5;
		  

        wait;
    end process;

end behavioral;
	