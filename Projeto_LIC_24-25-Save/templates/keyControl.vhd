LIBRARY ieee;
USE ieee.std_logic_1164.ALL;

ENTITY KeyControl IS
    PORT(
        clk, Kack, Kpress, Reset : IN STD_LOGIC;
        Kval, Kscan : OUT STD_LOGIC
    );
END KeyControl;

ARCHITECTURE behavioral OF KeyControl IS

    TYPE STATE_TYPE IS (SCANNING_KEY, VALID_KEY);
    SIGNAL CurrentState, NextState : STATE_TYPE;

BEGIN
    CurrentState <= SCANNING_KEY when (Reset = '1') else NextState WHEN rising_edge(clk);

    GenerateNextState : PROCESS (CurrentState, Kpress, Kack)
    BEGIN
        CASE CurrentState IS
        WHEN SCANNING_KEY => IF (Kpress = '1') THEN
                                    NextState <= VALID_KEY;
                                ELSE
                                    NextState <= SCANNING_KEY;
        END IF;
        WHEN VALID_KEY => IF (Kack = '1') THEN
                                NextState <= SCANNING_KEY;
                            ELSE
                                NextState <= VALID_KEY;
        END IF;
        END CASE;
    END PROCESS;

    Kscan <= '1' WHEN (CurrentState = SCANNING_KEY) ELSE '0';
    Kval <= '1' WHEN (CurrentState = VALID_KEY) ELSE '0';
END ARCHITECTURE;