kb::item(block).
kb::item(none).

// Rules to specify mutually exclusive locations
// 1. Perception Locations
kb::location(0, 1, Item)[prop] :- kb::item(Item).
kb::location(0, -1, Item)[prop] :- kb::item(Item).

// kb::location(1, 0, Item)[prop] :- kb::item(Item).
// kb::location(-1, 0, Item)[prop] :- kb::item(Item).

// Unknown locations (Outside percepts)
// kb::location(-1, 1, Item)[prop] :- kb::item(Item).
// kb::location(-1, -1, Item)[prop] :- kb::item(Item).
// kb::location(1, -1, Item)[prop] :- kb::item(Item).
// kb::location(1, 1, Item)[prop] :- kb::item(Item).


+know(location(0, -1, Item)) <- .print("The item must be ", Item).
