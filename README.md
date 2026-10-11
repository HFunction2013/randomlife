# Random Life

A Minecraft Forge 1.20.1 (47.3.0) mod that gives every living creature a **random max health** between 1 and 20000, and shows the **current/total health** floating above its head.

## Features

- Every creature gets a random max health in `[1, 20000]` when it spawns.
- The health is shown above the head as `current/max` (e.g. `Zombie 3421/15000`, players keep their name: `Steve 12000/20000`).
- Players get a **brand new random health pool on every respawn**.
- `/randomlife:set_life` command to manually (re)assign health.

## Command

```
/randomlife:set_life <targets> <life>
```

| Argument | Meaning |
| --- | --- |
| `<targets>` | Any entity selector, e.g. `@e`, `@a`, `@s` |
| `<life>` | `random` — re-roll a random max health; or a number between 1 and 20000 |

In both cases the **current health points are kept** and simply clamped to the new total, so an entity above its new max is capped back to full.

## Example

```
/randomlife:set_life @e random      # re-rolls the health of every creature
/randomlife:set_life @a 5000        # sets all players' max health to 5000
```

## Build

Requires JDK 17.

```
./gradlew build
```

The jar lands in `build/libs/` and can be dropped into `mods/`.

## Notes

- Health values are rolled by the server; the floating text is a custom nametag, so chat/death messages may include it.
- Very low rolls (e.g. 1 HP) make a creature extremely fragile — that is intentional.
