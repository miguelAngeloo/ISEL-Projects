LIBRARY IEEE;
USE IEEE.STD_LOGIC_1164.ALL;

ENTITY EQUALS_tb IS
END EQUALS_tb;

ARCHITECTURE behavior OF EQUALS_tb IS

    -- Component declaration
    COMPONENT EQUALS IS
        PORT (
            X : IN STD_LOGIC_VECTOR(3 DOWNTO 0);
            match : IN STD_LOGIC_VECTOR(3 DOWNTO 0);
            Q : OUT STD_LOGIC
        );
    END COMPONENT;

    -- Signals to connect to DUT
    SIGNAL X_tb : STD_LOGIC_VECTOR(3 DOWNTO 0);
    SIGNAL match_tb : STD_LOGIC_VECTOR(3 DOWNTO 0);
	 SIGNAL Q_tb : STD_LOGIC;

    -- Clock period definition
    CONSTANT MCLK_PERIOD : TIME := 20 ns;
    CONSTANT MCLK_HALF_PERIOD : TIME := MCLK_PERIOD / 2;

BEGIN

    -- Instantiate the Unit Under Test (UUT)
    uut : EQUALS
        PORT MAP (
            X => X_tb,
            match => match_tb, -- Example match pattern
            Q => Q_tb
        );
		  

    
    -- Stimulus process
    stimulus : PROCESS
	 BEGIN
       
        X_tb <= "1000";
		  match_tb <= "1001"; 
        WAIT FOR MCLK_PERIOD; 
        X_tb <= "1000";
		  match_tb <= "1011"; 
        WAIT FOR MCLK_PERIOD; 
        X_tb <= "0110";
		  match_tb <= "0110"; 
        WAIT FOR MCLK_PERIOD; 
        X_tb <= "1000";
		  match_tb <= "1111";
        WAIT FOR MCLK_PERIOD; 
        X_tb <= "0101";
		  match_tb <= "0101"; 
        WAIT FOR MCLK_PERIOD; 
        X_tb <= "1111";
		  match_tb <= "1111"; 
        WAIT FOR MCLK_PERIOD*5; 


        -- Finish simulation
        WAIT;
    END PROCESS;

END behavior;