library ieee;
use ieee.std_logic_1164.all;

entity Serial_Control is
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
end Serial_Control;

architecture behavioral of Serial_Control is

type STATE_TYPE is (STATE_INIT, STATE_WRITE, STATE_DFLAG, STATE_VALID_DATA, STATE_ACCEPT);

signal CurrentState, NextState : STATE_TYPE;

begin

-- Flip-Flop's 
CurrentState <= STATE_INIT when reset = '1' else NextState when rising_edge(clk);

-- Generate Next State 
GenerateNextState:
process (CurrentState, enRx, accept, pFlag, dFlag, RXerror)
begin
    case CurrentState is
        when STATE_INIT =>
            if (enRx = '0') then
                NextState <= STATE_WRITE;
            else
                NextState <= STATE_INIT;
            end if;

        when STATE_WRITE =>
				if (enRx = '1') then
					NextState <= STATE_INIT;
            elsif (enRx = '0' and dFlag = '1') then
					NextState <= STATE_DFLAG;
            else
                    NextState <= STATE_WRITE;
            end if;

        when STATE_DFLAG =>
            if (enRx = '1' and pFlag = '0') then
					NextState <= STATE_INIT;
				elsif (enRx = '1' and pFlag = '1' and RXerror = '0') then
					NextState <= STATE_VALID_DATA;
				else 
					NextState <= STATE_DFLAG;
				end if;
			
        when STATE_VALID_DATA =>
            if (accept = '1') then
                    NextState <= STATE_ACCEPT;
            else
                NextState <= STATE_VALID_DATA;
            end if;
				
		 when STATE_ACCEPT =>
				if (accept = '0') then
					NextState <= STATE_INIT;
					
				else
					NextState <= STATE_ACCEPT;
				end if;
    end case;
end process;

-- Generate outputs 
wr <= '1' when (CurrentState = STATE_WRITE) else '0';
					
init <= '1' when (CurrentState = STATE_INIT) else '0';

DXval <= '1' when (CurrentState = STATE_VALID_DATA) else '0';


end behavioral;
