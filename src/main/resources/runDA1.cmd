@echo off
SETLOCAL ENABLEDELAYEDEXPANSION

for /l %%p in (0,1,3) do (
	
	start java Initiator "145.94.224.64" "1099" %%p "peer_config.txt" %%p

)