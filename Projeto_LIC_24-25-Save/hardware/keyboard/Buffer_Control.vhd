library ieee;
use ieee.std_logic_1164.all;

entity Buffer_Control is
port(
		reset 	: in std_logic;
		clk		: in std_logic;
		Load		: in std_logic;
		Ack	: in std_logic;
		Obfree		: out std_logic;
		Dval		: out std_logic;
		Wreg		: out std_logic
);
end Buffer_Control;

architecture behavioral of Buffer_Control is

type STATE_TYPE is (STATE_INIT, STATE_WRITTING, STATE_VALID_DATA, STATE_WATTING);

signal CurrentState, NextState : STATE_TYPE;

begin

-- Flip-Flop's 
CurrentState <= STATE_INIT when reset = '1' else NextState when rising_edge(clk);

-- Generate Next State 
GenerateNextState:
process (CurrentState, Load, Ack)
	begin
		case CurrentState is
		
			when STATE_INIT  =>       if (Load = '1') then 
														NextState <= STATE_WRITTING;
											      else 
														NextState <= STATE_INIT;
											      end if;
														
			when STATE_WRITTING  =>   if (Load = '0') then
														NextState <= STATE_VALID_DATA;
												   else 
														NextState <= STATE_WRITTING;
												   end if;
														
			when STATE_VALID_DATA => if (Ack = '1') then
														NextState <= STATE_WATTING;
													else
														NextState <= STATE_VALID_DATA;
													end if;
													
			when STATE_WATTING  =>       if (Ack = '0') then 
														NextState <= STATE_INIT;
											      else 
														NextState <= STATE_WATTING;
											      end if;							
										
										
										
	end case;
end process;

-- Generate outputs 
Obfree <= '1' when (CurrentState = STATE_INIT) else '0';
					
Wreg <= '1' when (CurrentState = STATE_WRITTING) else '0';

Dval <= '1' when (CurrentState = STATE_VALID_DATA) else '0';

end behavioral;
								