LIBRARY ieee;
USE ieee.std_logic_1164.all;

entity LCD_DISP is
		port( 
		clk   : in std_logic;
		Reset : in std_logic;
		Dval  : in std_logic;
		DIN   : in std_logic_vector(8 downto 0);
		DOUT  : out std_logic_vector(8 downto 0); 
		WrL   : out std_logic;
		done  : out std_logic
		);
	
end entity;

ARCHITECTURE behavioral OF LCD_DISP IS

    TYPE STATE_TYPE IS (IDLE, WRITING, ST_DONE);
    SIGNAL CurrentState, NextState : STATE_TYPE;

BEGIN
    CurrentState <= IDLE when Reset = '1' else NextState WHEN rising_edge(clk);

    GenerateNextState : PROCESS (CurrentState, Dval)
    BEGIN
        CASE CurrentState IS
        WHEN IDLE => IF (Dval = '1') THEN
                                    NextState <= WRITING;
                                ELSE
                                    NextState <= IDLE;
        END IF;
        WHEN WRITING => NextState <= ST_DONE;
        WHEN ST_DONE => NextState <= IDLE;
        END CASE;
    END PROCESS;

    WrL  <= '1' WHEN (CurrentState = WRITING) ELSE '0';
    done <= '1' WHEN (CurrentState = ST_DONE) ELSE '0';
	 DOUT <= DIN;
END ARCHITECTURE;