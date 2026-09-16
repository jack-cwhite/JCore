# JCore

JCore is a Java 21 library for Paper plugins. It provides reusable command,
menu, YAML storage, serialization, task, countdown, player-state, and database
infrastructure. It is a library dependency, not a plugin to place in the
server's `plugins` folder.

## Build and use locally

```powershell
mvn clean install
```

Add it to a consuming plugin:

```xml
<dependency>
    <groupId>me.jackcw</groupId>
    <artifactId>JCore</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

The consuming plugin should shade JCore into its finished jar. Duels is the
reference implementation for the current API.

## Lifecycle

Create one `JCore` instance during `onEnable`, register serializers and
database migrations, then shut it down during `onDisable`:

```java
private JCore jCore;

@Override
public void onEnable()
{
    jCore = JCore.create(this, databaseConfiguration);
    jCore.initialize();

    // Register serializers, repositories, menus, commands, and migrations.
}

@Override
public void onDisable()
{
    if (jCore != null)
        jCore.shutdown();
}
```

Register every migration before the first call to `jCore.database()`. The
database connection and migrations are lazy and start on first use.

## Main services

- `files()` creates and caches `YamlFile` instances.
- `serializers()` converts domain objects to and from YAML-compatible data.
- `messages()` loads configurable messages and placeholders.
- `commands()` builds typed command trees with permissions.
- `menus()` creates configured, paginated, editable, and confirmation menus.
- `tasks()` runs Bukkit-thread and bounded background work.
- `migrations()` registers ordered SQL schema changes.
- `database()` returns the configured SQLite, MySQL, MariaDB, or PostgreSQL database.

## YAML repositories

A `YamlRepository<T>` stores objects under numeric keys and returns real domain
objects by invoking the registered serializer:

```java
jCore.serializers().register(Arena.class, new ArenaSerializer(jCore.serializers()));

YamlRepository<Arena> arenas = new YamlRepository<>(
        jCore.files().yaml("arenas.yml"),
        jCore.serializers(),
        "arenas",
        Arena.class,
        Arena::getId
);
```

Use `reserveId()` when creating an object, `save(object)` after mutations, and
`find`/`findAll` when loading. Reserved IDs are monotonic and survive deletion
and restart.

## Threading rules

Paper entities, inventories, and most Bukkit APIs belong on the server thread.
Use `tasks().submitAsync(...)` for blocking SQL or file-independent work, then
return to `tasks().runSync(...)` before interacting with a player or menu.

`JCore.shutdown()` closes owned menus first, drains queued background work,
then closes the database pool. A consuming plugin should stop its own gameplay
sessions before calling it so player restore work can finish cleanly.

## Tests

```powershell
mvn clean test
```

The suite covers commands, menus, countdowns, tasks, serialization, YAML,
migrations, and SQLite. Vendor-specific SQL should also be exercised against
real MySQL, MariaDB, and PostgreSQL instances in the consuming plugin's release
tests.
