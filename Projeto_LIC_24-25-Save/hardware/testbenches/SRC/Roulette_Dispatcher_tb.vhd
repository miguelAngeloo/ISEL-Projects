library ieee;
use ieee.std_logic_1164.all;

entity Roulette_Dispatcher_tb is
end Roulette_Dispatcher_tb;

architecture behavioral of Roulette_Dispatcher_tb is
	
component Roulette_Dispatcher is
port(
		reset 	: in std_logic;
		clk		: in std_logic;
		Dval		: in std_logic;
		Din		: in std_logic_vector(7 downto 0);
		WrD		: out std_logic;
		Dout		: out std_logic_vector(7 downto 0);
		done		: out std_logic
);
end component;

constant mclk_period : time := 20 ns;
constant mclk_half_period : time := mclk_period / 2;


signal 		reset_tb 		: std_logic;
signal		clk_tb			: std_logic;
signal		Dval_tb			: std_logic;
signal		Din_tb		   : std_logic_vector(7 downto 0);
signal		Wrl_tb		   : std_logic;
signal		Dout_tb		   : std_logic_vector(7 downto 0);
signal		done_tb			: std_logic;

begin
   uut : Roulette_Dispatcher
	port map (
			reset => reset_tb,
			clk	=> clk_tb,
			Dval  => Dval_tb,
			Din => Din_tb,
			Wrl => Wrl_tb,
			Dout => Dout_tb,
			done=> done_tb
	);
	
	clk_process : process
	begin
		while true loop
            clk_tb <= '1';
            wait for mclk_half_period;
            clk_tb <= '0';
            wait for mclk_half_period;
        end loop;
    end process;
	
	stimulus : process
	begin
		  reset_tb <= '1';
        wait for mclk_period;
        reset_tb <= '0';

		  
		  
        Dval_tb <= '0';
        wait for mclk_period;

		  Dval_tb <= '0';
        wait for mclk_half_period;
		  
		  Dval_tb <= '0';
		  wait for mclk_period;
		  

        Dval_tb <= '1';
        wait for mclk_half_period;

		  
		  Dval_tb <= '0';
		  wait for mclk_half_period;
		  
		  Dval_tb <= '1';
		  wait for mclk_half_period;
		  

        wait;
    end process;

end behavioral;
	