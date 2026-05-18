LIBRARY IEEE;
USE IEEE.std_logic_1164.ALL;
ENTITY MUX41 IS
    PORT (
        D : IN STD_LOGIC_VECTOR(3 DOWNTO 0);
        S : IN STD_LOGIC_VECTOR(1 DOWNTO 0);
        M : OUT STD_LOGIC
    );
END MUX41;
ARCHITECTURE arq_mux41 OF MUX41 IS
    COMPONENT MUX21
        PORT (
            X : IN STD_LOGIC_VECTOR(1 DOWNTO 0);
            S : IN STD_LOGIC;
            M : OUT STD_LOGIC);
    END COMPONENT;

    SIGNAL sa, sb : STD_LOGIC;
    BEGIN
        U0 : MUX21 PORT MAP(
            X(0) => D(0),
            X(1) => D(1),
            S => S(0),
            M => sa);
        U1 : MUX21 PORT MAP(
            X(0) => D(2),
            X(1) => D(3),
            S => S(0),
            M => sb);
        U2 : MUX21 PORT MAP(
            X(0) => sa,
            X(1) => sb,
            S => S(1),
            M => M);
END arq_mux41;