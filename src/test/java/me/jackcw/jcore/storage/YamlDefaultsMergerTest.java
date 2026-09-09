package me.jackcw.jcore.storage;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

class YamlDefaultsMergerTest
{
    @Test
    void addsMissingDefaults() throws Exception
    {
        File target = Files.createTempFile("jcore-test-", ".yml").toFile();

        Files.writeString(target.toPath(), "name: Jack\n", StandardCharsets.UTF_8);

        String defaults = "name: Default\n" + "enabled: true\n" + "port: 3306\n";

        YamlDefaultsMerger merger = new YamlDefaultsMerger();
        boolean changed = merger.merge(target, new ByteArrayInputStream(defaults.getBytes(StandardCharsets.UTF_8)));

        assertTrue(changed);

        String result = Files.readString(target.toPath(), StandardCharsets.UTF_8);

        assertTrue(result.contains("name: Jack"));
        assertTrue(result.contains("enabled: true"));
        assertTrue(result.contains("port: 3306"));

        target.delete();
    }

    @Test
    void preservesExistingValues() throws Exception
    {
        File target = Files.createTempFile("jcore-test-", ".yml").toFile();
        Files.writeString(target.toPath(), "enabled: false\n", StandardCharsets.UTF_8);
        String defaults = "enabled: true\n";
        YamlDefaultsMerger merger = new YamlDefaultsMerger();
        boolean changed = merger.merge(target, new ByteArrayInputStream(defaults.getBytes(StandardCharsets.UTF_8)));

        assertFalse(changed);

        String result = Files.readString(target.toPath(), StandardCharsets.UTF_8);

        assertTrue(result.contains("enabled: false"));
        assertFalse(result.contains("enabled: true"));

        target.delete();
    }

    @Test
    void mergesNestedSections() throws Exception
    {
        File target = Files.createTempFile("jcore-test-", ".yml").toFile();
        Files.writeString(target.toPath(), "database:\n" + "  host: custom-host\n", StandardCharsets.UTF_8);
        String defaults = "database:\n" + "  host: localhost\n" + "  port: 3306\n";
        YamlDefaultsMerger merger = new YamlDefaultsMerger();
        boolean changed = merger.merge(target, new ByteArrayInputStream(defaults.getBytes(StandardCharsets.UTF_8)));

        assertTrue(changed);

        String result = Files.readString(target.toPath(), StandardCharsets.UTF_8);

        assertTrue(result.contains("host: custom-host"));
        assertTrue(result.contains("port: 3306"));

        target.delete();
    }

    @Test
    void returnsFalseWhenNothingChanges() throws Exception
    {
        File target = Files.createTempFile("jcore-test-", ".yml").toFile();
        String yaml = "enabled: true\n" + "port: 3306\n";
        Files.writeString(target.toPath(), yaml, StandardCharsets.UTF_8);
        YamlDefaultsMerger merger = new YamlDefaultsMerger();
        boolean changed = merger.merge(target, new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8)));

        assertFalse(changed);
        assertEquals(yaml, Files.readString(target.toPath(), StandardCharsets.UTF_8));

        target.delete();
    }

    @Test
    void rejectsNullTarget()
    {
        YamlDefaultsMerger merger = new YamlDefaultsMerger();

        assertThrows(IllegalArgumentException.class,
                () -> merger.merge(null, new ByteArrayInputStream("test: true".getBytes(StandardCharsets.UTF_8)))
        );
    }

    @Test
    void rejectsNullDefaults() throws Exception
    {
        File target = Files.createTempFile("jcore-test-", ".yml").toFile();
        YamlDefaultsMerger merger = new YamlDefaultsMerger();

        assertThrows(IllegalArgumentException.class, () -> merger.merge(target, null));

        target.delete();
    }

    @Test
    void noChangesReturnsFalse() throws IOException
    {
        File target = Files.createTempFile("jcore-test-", ".yml").toFile();
        Files.writeString(target.toPath(), "database:\n" + "  host: localhost\n", StandardCharsets.UTF_8);
        String defaults = "database:\n" + "  host: localhost\n";
        YamlDefaultsMerger merger = new YamlDefaultsMerger();
        boolean changed = merger.merge(target, new ByteArrayInputStream(defaults.getBytes(StandardCharsets.UTF_8)));

        assertFalse(changed);
    }

    @Test
    void deepNestedMergeWorks() throws IOException
    {
        File target = Files.createTempFile("jcore-test-", ".yml").toFile();
        Files.writeString(target.toPath(), "database:\n" + "  mysql:\n" + "    host: custom-host\n", StandardCharsets.UTF_8);

        String defaults = "database:\n" + "  mysql:\n" + "    host: localhost\n" + "    port: 3306\n" + "    username: root\n";
        YamlDefaultsMerger merger = new YamlDefaultsMerger();
        boolean changed = merger.merge(target, new ByteArrayInputStream(defaults.getBytes(StandardCharsets.UTF_8)));

        assertTrue(changed);

        String result = Files.readString(target.toPath(), StandardCharsets.UTF_8);

        assertTrue(result.contains("host: custom-host"));
        assertTrue(result.contains("port: 3306"));
        assertTrue(result.contains("username: root"));
    }
}