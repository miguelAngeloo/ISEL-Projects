library ieee;
use ieee.std_logic_1164.all;

entity Ring_Buffer_Control is
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
end Ring_Buffer_Control;

architecture behavioral of Ring_Buffer_Control is

type STATE_TYPE is (STATE_INIT, STATE_SET_WRITE, STATE_WRITE, STATE_INC_PUT, STATE_ACK, STATE_READ, STATE_INC_GET);

signal CurrentState, NextState : STATE_TYPE;

begin

-- Flip-Flop's 
CurrentState <= STATE_INIT when reset = '1' else NextState when rising_edge(clk);

-- Generate Next State 
GenerateNextState:
process (CurrentState, DAV, CTS, full, empty)
	begin
		case CurrentState is
		
			when STATE_INIT  =>       if (DAV = '1' and full = '0') then 
														NextState <= STATE_SET_WRITE;
														
											  elsif (DAV = '1' and full = '1' and CTS = '0') then
														NextState <= STATE_INIT;
														
											  elsif (DAV = '1' and full = '1' and CTS = '1') then
														NextState <= STATE_READ;
														
											  elsif (DAV = '0' and empty = '1') then
														NextState <= STATE_INIT;
											
											  elsif (DAV = '0' and empty = '0' and CTS = '1') then
														NextState <= STATE_READ;
														
											  else
														NextState <= STATE_INIT;
														
											  end if;
											  
														
			when STATE_SET_WRITE =>   NextState <= STATE_WRITE;
												 
														
			when STATE_WRITE  =>      NextState <=  STATE_INC_PUT;
			
													
			when STATE_INC_PUT  =>    NextState <=  STATE_ACK;  
			
			
			when STATE_ACK  =>       if (DAV = '0') then 
														NextState <= STATE_INIT;
											 else 
														NextState <= STATE_ACK;
											 end if;
										
										 
										 
			when STATE_READ  => 	  if (CTS = '0') then
												NextState <= STATE_INC_GET;
										 else
												NextState <= STATE_READ;
										 end if;
											
			when STATE_INC_GET =>   NextState <= STATE_INIT; 
										
										
										
	end case;
end process;

-- Generate outputs 
selPG <= '1' when (CurrentState = STATE_SET_WRITE or CurrentState = STATE_WRITE or CurrentState = STATE_INC_PUT) else '0';
					
Wreg <= '1' when (CurrentState = STATE_READ) else '0';

incGetC <= '1' when (CurrentState = STATE_INC_GET) else '0';

incPutC <= '1' when (CurrentState = STATE_INC_PUT) else '0';

DAC <= '1' when (CurrentState = STATE_ACK) else '0';

Wr <= '1' when (CurrentState = STATE_WRITE) else '0';

end behavioral;
							