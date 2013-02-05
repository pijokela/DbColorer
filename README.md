What is DbColorer?
==================

DbColorer allows you to categorize something like database columns
with colors. The project is also my first Scala Play 2 project, so
there will be some mistakes in the project structure early on.

How to run?
===========

DbColorer is very much in progress .. so this is not polished.

1. Get Play 2.0.4 (or newer, I hope)
  - http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html
  - http://www.scala-lang.org/downloads
  - http://www.playframework.org/download
2. Clone the repo
3. Go to the project directory
4. "play"
5. "~run"
6. In a browser go to "http://localhost:9000/createTestData". 
   This will create the H2 database with some data.
7. Go to "http://localhost:9000/".

Development
===========

I use Eclipse with Scala IDE for editing and some of the Eclipse files are 
in the repo. The idea is that the files should be usable for anybody working
with DbColorer.

1. Create an eclipse variable called PLAY_2_0 and point it to the directory
you got from unzipping Play. This will make the classpaths work.
2. Download the scala sources as a .tgz from 
3. Convert the .tgz to a .zip
4. Create and eclipse variable called SCALA_SOURCES and point it to the
ZIP file you just got. This will make Scala source navigation work - many
of the Scala IDE features will not work without it.
