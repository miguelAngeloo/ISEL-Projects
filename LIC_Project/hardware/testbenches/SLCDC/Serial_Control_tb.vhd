library ieee;
use ieee.std_logic_1164.all;

entity Serial_Control_tb is
end Serial_Control_tb;

architecture behavioral of Serial_Control_tb is
	
component Serial_Control is
	port(
				reset 		: in std_logic;
				clk			: in std_logic;
				enRx			: in std_logic;
				accept		: in std_logic;
				pFlag			: in std_logic;
				dFlag			: in std_logic;
				RXerror		: in std_logic;
				wr				: out std_logic;
				init			: out std_logic;
				DXval			: out std_logic
	);
end component;

constant mclk_period : time := 20 ns;
constant mclk_half_period : time := mclk_period / 2;


signal 		reset_tb 		: std_logic;
signal		clk_tb			: std_logic;
signal		enRx_tb			: std_logic;
signal		accept_tb		: std_logic;
signal		pFlag_tb		   : std_logic;
signal		dFlag_tb		   : std_logic;
signal		RXerror_tb		: std_logic;
signal		wr_tb				: std_logic;
signal		init_tb			: std_logic;
signal		DXval_tb			: std_logic;

begin
   uut : Serial_Control
	port map (
			reset => reset_tb,
			clk	=> clk_tb,
			enRx  => enRx_tb,
			accept=> accept_tb,
			pFlag => pFlag_tb,
			dFlag => dFlag_tb,
			RXerror=> RXerror_tb,
			wr    => wr_tb,
			init  => init_tb,
			DXval => DXval_tb
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

		  
		  
        enRx_tb <= '0';
        wait for mclk_period;
		  enRx_tb <= '0';
        dFlag_tb <= '0';
        wait for mclk_half_period;

		  enRx_tb <= '0';
        dFlag_tb <= '1';
        wait for mclk_half_period;

		  enRx_tb <= '1';
		  pFlag_tb <= '1';
		  RXerror_tb <= '0';
        wait for mclk_period;
		  
		  
		  

        accept_tb <= '1';
        wait for mclk_half_period;

        accept_tb <= '1';
        wait for mclk_half_period;
		  
		  accept_tb <= '0';
		  wait for mclk_half_period;
		  accept_tb <= '0';
		  wait for mclk_half_period;
		  

        wait;
    end process;

end behavioral;
	
			