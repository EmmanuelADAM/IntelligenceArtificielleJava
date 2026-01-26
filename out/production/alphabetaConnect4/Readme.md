# Game of Connect 4 with Alpha-Beta Pruning
This project implements the classic game of Connect 4 using the Alpha-Beta pruning algorithm to optimize the decision-making process of the AI player. The game is played on a 7x6 grid where two players take turns dropping colored discs into a column, aiming to connect four of their discs in a row either horizontally, vertically, or diagonally.
## Features
- Implementation of the Connect 4 game logic.
- AI player using the Alpha-Beta pruning algorithm for optimal moves.
- Simple console-based user interface for playing against the AI.
- Ability to play as either player (Red or Yellow).
- Comments and documentation for better code readability.
- Possibility to improve the AI by enhancing the evaluation function.

Example of board state:
```
   |   |   |   |   |   |
----------------------------
   |   |   |   |   |   |
----------------------------
   |   |   | R |   |   |
----------------------------
   |   | R | Y |   |   |
----------------------------
   | R | Y | Y | Y |   |
----------------------------
 R | Y | R | R | Y |   |
 
 R = Red player
 Y = Yellow player
 R wins
```

## Getting Started
Main class: `appli/Connect4Game.java`
- adapt the AI level (see `dangerValue` method in `Situation.java` (you can also change data in `DangerPattern.java`))
