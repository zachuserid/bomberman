===Bomberman Milestone 2 README===

===Running the program in Eclipse IDE: (RECOMMENDED)
	Open eclipse, go to File->Import and select
'Existing Project' under the 'General' directory. Browse to the Bomberman
directory and select Open. The project files are now imported into eclipse.
Run the server: BombermanGame.B_ServerMain 
and client: BombermanGame.B_ClientMain. The client should
be run four times, once per player up to the maximum allowed in the game.


===Running the program from the command line:
Note: The classpath does not seem to be recognized while testing here.
	Please refer to loading it in eclipse, as the lab Fedora images and Windows
	have it installed.

Note: Assuming you have `javac` and `java` in your path.

cd /path/to/bomberman/bin

Compile the server and client with the following:
javac -classpath path/to/bomberman/bin /path/to/bomberman/src/BombermanGame/B_ServerMain.java
and:
javac -classpath path/to/bomberman/bin /path/to/bomberman/src/BombermanGame/B_ClientMain.java

Execute the compiled server followed by the client four times, using the following:
java -Dfile.encoding=US-ASCII -classpath path/to/bomberman/bin /path/to/bomberman/src/BombermanGame.B_ServerMain
and:
java -Dfile.encoding=US-ASCII -classpath path/to/bomberman/bin /path/to/bomberman/src/BombermanGame.B_ClientMain

File locations:
-Performance and other testing is summarized in the INSTRUCTIONS.pdf file, located in the Testframe.zip file. This should also summarize our extensions.

-All of the idea files and code are located in code.zip. This is the bomberman repository and contains extra files. The src/ directory has the code and the Testframe/ directory contains the files that exist in Testframe.zip

----Things to note----:
	-When running the four client programs, you will initially see a grey window until all four players have joined the game.

	-Noted bug in the view: Occasionally, starting a client/spectator will result in a grey box that does not change with updates from server, or more commonly, a very small empty box. Research suggests that this is related to sometimes failing to pack() the components in the view. If this occurs, please try again. It seems to be much more common on linux and occurs infrequently on windows.

	-We interpreted the bomb count number as the total number of bombs that a player is allowed to drop in a game. This starts at a default number of 5 and is only incremented when the correct power up is picked up. We only realized on submission day that the this value was supposed to represent the number of bombs on the field at once.

----Playing the game----:
The game, as it stands now, is a free for all deathmatch. The objectives of the game that create a win scenario are being the last man standing, or being the first player to enter a door. The default bomb radius is two squares in four directions from the point of explosion. If a brick or a player is within the blast radius, it will be destroyed. Some powerups are hidden under bricks, while the door is also occasionally hidden under a brick.

