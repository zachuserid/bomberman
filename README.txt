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
