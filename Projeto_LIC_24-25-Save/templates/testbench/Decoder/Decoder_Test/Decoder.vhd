LIBRARY ieee;
USE ieee.std_logic_1164.ALL;

ENTITY Decoder IS
    PORT (
        I : IN STD_LOGIC_VECTOR(1 DOWNTO 0);
        S : OUT STD_LOGIC_VECTOR(3 DOWNTO 0)
    );
END Decoder;

ARCHITECTURE arch OF Decoder IS
BEGIN
    S <= "1000" WHEN I = "00" ELSE
        "0100" WHEN I = "01" ELSE
        "0010" WHEN I = "10" ELSE
        "0001" WHEN I = "11";
END ARCHITECTURE;