library ieee;
use ieee.std_logic_1164.all;

entity Shift_Reg is
port(
		Data : in std_logic;
		clk : in std_logic;
		E: in std_logic;
		reset: in std_logic;
		Sout: out std_logic;
		D: out std_logic_vector(4 downto 0)
);

end Shift_Reg;

architecture Structural of Shift_Reg is


component FFD is
PORT(	
		CLK : in std_logic;
		RESET : in STD_LOGIC;
		SET : in std_logic; 
		D : IN STD_LOGIC;
		EN : IN STD_LOGIC;
		Q : out std_logic
);

end component;


signal saida1, saida2, saida3, saida4, saida5 : std_logic ;
signal saida : std_logic; 


begin

U1: FFD port map (CLK => CLK, RESET => reset , SET => '0', D => Data , EN =>
E , Q => saida1 );

U2: FFD port map (CLK => CLK, RESET => reset , SET => '0', D => saida1 , EN =>
E, Q => saida2 );

U3: FFD port map (CLK => CLK, RESET => reset , SET => '0', D => saida2 , EN =>
E, Q => saida3 );

U4: FFD port map (CLK => CLK, RESET => reset, SET => '0', D => saida3 , EN =>
E, Q => saida4 );

U5: FFD port map (CLK => CLK, RESET => reset, SET => '0', D => saida4 , EN =>
E, Q => saida5 );


D(4) <= saida1 ;
D(3) <= saida2 ;
D(2) <= saida3 ;
D(1) <= saida4 ;
D(0) <= saida5 ;

Sout <= saida5;


end Structural;
