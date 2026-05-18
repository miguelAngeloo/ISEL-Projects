LIBRARY IEEE;
USE IEEE.std_logic_1164.ALL;
ENTITY ADDSUB IS
    PORT (
        A : IN STD_LOGIC_VECTOR(3 DOWNTO 0);
        B : IN STD_LOGIC_VECTOR(3 DOWNTO 0);
        Cbi : IN STD_LOGIC;
        OP : IN STD_LOGIC;
        R : OUT STD_LOGIC_VECTOR(3 DOWNTO 0);
        B3 : OUT STD_LOGIC;
        CBo : OUT STD_LOGIC);
END ADDSUB;
ARCHITECTURE ARCH_ADDSUB OF ADDSUB IS
    COMPONENT ADDER IS
        PORT (
            A : IN STD_LOGIC_VECTOR(3 DOWNTO 0);
            B : IN STD_LOGIC_VECTOR(3 DOWNTO 0);
            C0 : IN STD_LOGIC;
            S : OUT STD_LOGIC_VECTOR(3 DOWNTO 0);
            C4 : OUT STD_LOGIC);
    END COMPONENT;
    SIGNAL Cox, Cix : STD_LOGIC;
    SIGNAL Bx : STD_LOGIC_VECTOR(3 DOWNTO 0);
BEGIN
    A0 : ADDER PORT MAP(A => A, B => Bx, C0 => Cix, S => R, C4 => Cox);
    Cix <= Cbi XOR OP;
    Bx(0) <= B(0) XOR OP;
    Bx(1) <= B(1) XOR OP;
    Bx(2) <= B(2) XOR OP;
    Bx(3) <= B(3) XOR OP;
    Cbo <= Cox XOR OP;
    B3 <= Bx(3);
END ARCH_ADDSUB;