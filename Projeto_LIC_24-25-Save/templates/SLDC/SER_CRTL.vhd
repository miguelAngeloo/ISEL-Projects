LIBRARY ieee;
USE ieee.std_logic_1164.all;

entity SER_CRTL is
	port(
	clk    : in std_logic;
	Reset  : in std_logic;
	enRx   : in std_logic;
	accept : in std_logic; 
	pFlag  : in std_logic;
	dFlag  : in std_logic;
	RXerr  : in std_logic;
	wr     : out std_logic;
	init   : out std_logic;
	DXval  : out std_logic
	);
end SER_CRTL;


ARCHITECTURE behavioral OF SER_CRTL IS

    TYPE STATE_TYPE IS (WAITING, READING, ERROR_CHECK, VALID);
    SIGNAL CurrentState, NextState : STATE_TYPE;

BEGIN
    CurrentState <= WAITING when Reset = '1' else NextState WHEN rising_edge(clk);

    GenerateNextState : PROCESS (CurrentState, enRx, accept, pFlag, dFlag, RXerr, Reset)
    BEGIN
        CASE CurrentState IS
        WHEN WAITING => IF (enRx = '0') THEN
                                    NextState <= READING;
                                ELSE
                                    NextState <= WAITING;
        END IF;
        WHEN READING => IF (enRx = '1') THEN
                                    NextState <= WAITING;
                                ELSE
                                    IF (dFlag = '0') THEN
                                        NextState <= READING;
                                    ELSE
                                        NextState <= ERROR_CHECK;
                                    END IF;
        END IF;
        WHEN ERROR_CHECK => IF (enRX = '1') THEN
                                    NextState <= WAITING;
                                ELSE
                                    IF (pFlag = '0') THEN
                                        NextState <= ERROR_CHECK;
                                    ELSE
                                        IF (RXerr = '1') THEN
                                            NextState <= WAITING;
                                        ELSE
                                            NextState <= VALID;
                                        END IF;
                                    END IF;
        END IF;
        WHEN VALID => IF (enRX = '1') THEN
                                    NextState <= WAITING;
                                ELSE
                                    IF (accept = '1') THEN
                                        NextState <= WAITING;
                                    ELSE
                                        NextState <= VALID;
                                    END IF;
        END IF;
        END CASE;
    END PROCESS;

    init <= '1' WHEN (CurrentState = WAITING) ELSE '0';
    wr <= '1' WHEN (CurrentState = READING) ELSE '0';
    DXval <= '1' WHEN (CurrentState = VALID) ELSE '0';
END ARCHITECTURE;