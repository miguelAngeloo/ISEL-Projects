LIBRARY IEEE;
USE IEEE.std_logic_1164.ALL;

ENTITY Parity_Check IS
    PORT (
        serialData : IN STD_LOGIC;
        serialClock : IN STD_LOGIC;
        init : IN STD_LOGIC;
        error : OUT STD_LOGIC
    );
END Parity_Check;

ARCHITECTURE arch OF Parity_Check IS
    COMPONENT FFD IS
        PORT (
            CLK : IN STD_LOGIC;
            RESET : IN STD_LOGIC;
            SET : IN STD_LOGIC;
            D : IN STD_LOGIC;
            EN : IN STD_LOGIC;
            Q : OUT STD_LOGIC
        );
    END COMPONENT;

    SIGNAL resultXor : STD_LOGIC;
    SIGNAL ffdOut : STD_LOGIC;

BEGIN
    flipFlop : FFD PORT MAP(
        serialClock,
        init,
        '0',
        resultXor,
        '1',
        ffdOut
    );

    resultXor <= serialData XOR ffdOut;
    error <= NOT ffdOut;
END ARCHITECTURE;