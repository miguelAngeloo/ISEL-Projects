LIBRARY IEEE;
USE IEEE.std_logic_1164.ALL;

ENTITY DECODER IS
    PORT (
        S0 : IN STD_LOGIC;
        S1 : IN STD_LOGIC;
        EX0 : OUT STD_LOGIC;
        EX1 : OUT STD_LOGIC;
        EX2 : OUT STD_LOGIC;
        EX3 : OUT STD_LOGIC
    );
END DECODER;

ARCHITECTURE logicFuntion OF DECODER IS

BEGIN
    EX0 <= ((NOT S0) AND (NOT S1));
    EX1 <= ((S0) AND (NOT S1));
    EX2 <= ((NOT S0) AND (S1));
    EX3 <= ((S0) AND (S1));

END logicFuntion;