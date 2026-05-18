LIBRARY IEEE;
USE IEEE.std_logic_1164.ALL;
ENTITY ADDER IS
    PORT (
        A : IN STD_LOGIC_VECTOR(3 DOWNTO 0);
        B : IN STD_LOGIC_VECTOR(3 DOWNTO 0);
        C0 : IN STD_LOGIC;
        S : OUT STD_LOGIC_VECTOR(3 DOWNTO 0);
        C4 : OUT STD_LOGIC);
END ADDER;
ARCHITECTURE ARCH_ADDER OF ADDER IS
    COMPONENT FULLADDER
        PORT (
            A : IN STD_LOGIC;
            B : IN STD_LOGIC;
            Cin : IN STD_LOGIC;
            R : OUT STD_LOGIC;
            Cout : OUT STD_LOGIC);
    END COMPONENT;
    SIGNAL C1, C2, C3 : STD_LOGIC;
BEGIN
    FA0 : FULLADDER PORT MAP(A => A(0), B => B(0), Cin => C0, R => S(0), Cout => C1);
    FA1 : FULLADDER PORT MAP(A => A(1), B => B(1), Cin => C1, R => S(1), Cout => C2);
    FA2 : FULLADDER PORT MAP(A => A(2), B => B(2), Cin => C2, R => S(2), Cout => C3);
    FA3 : FULLADDER PORT MAP(A => A(3), B => B(3), Cin => C3, R => S(3), Cout => C4);
END arch_adder;