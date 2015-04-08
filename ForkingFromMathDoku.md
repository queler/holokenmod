# Introduction #

HoloKen may look quite different from MathDoku, but the guts of the game are mostly intact. There are still substantial changes that might discourage a direct merge back into MathDoku, especially with regards to save game files.


# Details #

The biggest changes made are in the UI and UX design.

## UI Design ##

  * A clean re-design based on the Android design guidelines, albeit not fully (ie no Action Bar etc on smaller screens)
  * Consistent colour scheme with Ice Cream Sandwich and above, with light and dark themes
  * Theme selector -- theme changes depending on device system, which guarantees backwards compatibility with Froyo and Gingerbread

## UX Design ##

  * Fixed gamescreen menu controls for New Game, Restart and Hints
  * Introduction of persistent Pen, Pencil and Eraser modes as a RadioGroup, no more Maybe's/Clear buttons
  * Selected cell changes colour depending on entry mode
  * OnLongClick listener for Pen button to fill in all possibles
  * Context menu entries made less confusing -- grouping of common hint items
  * Explicitly counts mistakes by the user, and gives user the option of highlighting mistakes or manually finding mistakes

## Gameplay options ##

  * Added default operations settings -- changed setArithmetic() and random chances for each operations to occur.
  * Added a timer Handler -- no need for second thread
  * One-time undo button -- need to deep copy cell state (possible future use of linked list to simulate limited stack)
  * Statistics tracking -- use of SharedPreferences to store game stats (less complicated than sqlite but also less comprehensive)

## Saving and sharing ##

  * Rewrote SaveGameListActivity layout to make ListView clearer
  * Save files now note playtime and cheated cells
  * Sharing of screenshots -- using drawingCacheEnabled and calling a share intent
