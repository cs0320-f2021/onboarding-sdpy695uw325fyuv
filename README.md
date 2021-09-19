# README

Project Name: Onboarding
Team: Chloe de Campos (cdecampo)
Total time: 18 hours :(

Design choices: I cheated? and looked at other students' code on Github. I stole the idea from PkR2JE77ps to make a new class in star.java that can store information about each star. I used a hashmap to track stars that have been loaded into the REPL.

Errors: The partition part of the quicksort function is getting stuck. I'm guessing it's a problem with the bounds of the for loop but I didn't have time to debug.

Unfinished: I also did not have time to write code for a nearest_neighbors <k> <star> command. In order to do this, I'd have to change the way the stars are stored in the hashmap, because right now they cannot be accessed using a name.

Tests: I'm really trying to not pass 18 hours. I should've written my tests first but, alas, I did not. I did not write many tests and I don't think I wrote them correctly either.


To build use:
`mvn package`

To run use:
`./run`

This will give you a barebones REPL, where you can enter text and you will be output at most 5 suggestions sorted alphabetically.

To start the server use:
`./run --gui [--port=<port>]`
