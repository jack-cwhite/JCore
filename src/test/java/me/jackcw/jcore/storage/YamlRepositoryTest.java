package me.jackcw.jcore.storage;

import me.jackcw.jcore.TestPlugin;
import me.jackcw.jcore.TestUtils;
import me.jackcw.jcore.serialization.RepositorySerializer;
import me.jackcw.jcore.serialization.Serializer;
import me.jackcw.jcore.serialization.SerializerManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class YamlRepositoryTest
{
    private TestPlugin plugin;
    private SerializerManager serializerManager;
    private String fileName;
    private YamlRepository<Widget> repository;

    @BeforeEach
    void setup()
    {
        plugin = TestUtils.mockPlugin();
        serializerManager = TestUtils.createSerializerManager();
        serializerManager.register(Widget.class, new WidgetSerializer());

        fileName = "test-" + System.nanoTime() + ".yml";

        YamlFile file = TestUtils.createYaml(plugin, serializerManager, fileName);
        repository = new YamlRepository<>(file, serializerManager, "widgets", Widget.class, w -> w.id);
    }

    @AfterEach
    void cleanup()
    {
        TestUtils.cleanup();
    }

    @Test
    void savesAndFindsById()
    {
        repository.save(new Widget(1, "Alpha"));

        Optional<Widget> found = repository.find(1);

        assertTrue(found.isPresent());
        assertEquals("Alpha", found.get().name());
    }

    @Test
    void findReturnsEmptyForMissingId()
    {
        assertTrue(repository.find(404).isEmpty());
    }

    @Test
    void deleteRemovesEntry()
    {
        repository.save(new Widget(1, "Alpha"));
        repository.delete(1);

        assertTrue(repository.find(1).isEmpty());
    }

    @Test
    void findAllReturnsEveryEntry()
    {
        repository.save(new Widget(1, "Alpha"));
        repository.save(new Widget(2, "Beta"));

        List<Widget> all = repository.findAll();

        assertEquals(2, all.size());
    }

    @Test
    void findAllReturnsEmptyListWhenNothingStored()
    {
        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    void findAllSkipsNonNumericKeyInsteadOfThrowing()
    {
        repository.save(new Widget(1, "Alpha"));

        YamlFile file = TestUtils.createYaml(plugin, serializerManager, fileName);
        file.set("widgets.not-a-number", Map.of("name", "Bad"));
        file.save();

        List<Widget> all = repository.findAll();

        assertEquals(1, all.size());
        assertEquals("Alpha", all.get(0).name());
    }

    @Test
    void findAllSkipsUnreadableEntryInsteadOfThrowing()
    {
        repository.save(new Widget(1, "Alpha"));

        YamlFile file = TestUtils.createYaml(plugin, serializerManager, fileName);
        file.set("widgets.2", "not-a-map");
        file.save();

        List<Widget> all = repository.findAll();

        assertEquals(1, all.size());
        assertEquals("Alpha", all.get(0).name());
    }

    @Test
    void persistsAcrossReload()
    {
        repository.save(new Widget(1, "Alpha"));

        YamlFile reloadedFile = TestUtils.createYaml(plugin, serializerManager, fileName);
        YamlRepository<Widget> reloadedRepository = new YamlRepository<>(
                reloadedFile, serializerManager, "widgets", Widget.class, w -> w.id);

        Optional<Widget> found = reloadedRepository.find(1);

        assertTrue(found.isPresent());
        assertEquals("Alpha", found.get().name());
    }

    @Test
    void reservedIdsRemainMonotonicAfterDeletion()
    {
        int first = repository.reserveId();
        repository.save(new Widget(first, "Alpha"));
        repository.delete(first);

        assertEquals(first + 1, repository.reserveId());
    }

    @Test
    void reservedIdsContinueAcrossReload()
    {
        int first = repository.reserveId();

        YamlFile reloadedFile = TestUtils.createYaml(plugin, serializerManager, fileName);
        YamlRepository<Widget> reloadedRepository = new YamlRepository<>(
                reloadedFile, serializerManager, "widgets", Widget.class, w -> w.id);

        assertEquals(first + 1, reloadedRepository.reserveId());
    }

    @Test
    void rejectsNullConstructorArguments()
    {
        YamlFile file = TestUtils.createYaml(plugin, serializerManager);

        assertThrows(IllegalArgumentException.class,
                () -> new YamlRepository<>(null, serializerManager, "widgets", Widget.class, w -> 1));

        assertThrows(IllegalArgumentException.class,
                () -> new YamlRepository<>(file, null, "widgets", Widget.class, w -> 1));

        assertThrows(IllegalArgumentException.class,
                () -> new YamlRepository<>(file, serializerManager, null, Widget.class, w -> 1));

        assertThrows(IllegalArgumentException.class,
                () -> new YamlRepository<>(file, serializerManager, "widgets", null, w -> 1));

        assertThrows(IllegalArgumentException.class,
                () -> new YamlRepository<>(file, serializerManager, "widgets", Widget.class, null));
    }

    private record Widget(int id, String name)
    {
    }

    private static class WidgetSerializer implements RepositorySerializer<Widget>
    {
        @Override
        public Object serialize(Widget value)
        {
            return Map.of("name", value.name());
        }

        @Override
        public Widget deserialize(int id, Object value)
        {
            Map<?, ?> map = (Map<?, ?>) value;

            return new Widget(id, (String) map.get("name"));
        }
    }
}
