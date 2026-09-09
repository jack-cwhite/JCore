package me.jackcw.jcore.message;

import me.jackcw.jcore.TestPlugin;
import me.jackcw.jcore.TestUtils;
import me.jackcw.jcore.storage.YamlFile;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MessageManagerTest
{
    private TestPlugin plugin;

    @BeforeEach
    void setup()
    {
        plugin = TestUtils.mockPlugin();
    }

    @AfterEach
    void cleanup()
    {
        TestUtils.cleanup();
    }

    @Test
    void getsMessage()
    {
        YamlFile file = TestUtils.createYaml(plugin);

        file.set(
                "core.no-permission",
                "&cYou do not have permission."
        );
        file.save();
        file.reload();

        MessageManager messageManager = new MessageManager(plugin, file);

        assertEquals(
                "&cYou do not have permission.",
                messageManager.get(CoreMessage.NO_PERMISSION)
        );
    }

    @Test
    void getsListMessage()
    {
        YamlFile file = TestUtils.createYaml(plugin);

        file.set(
                "core.help",
                List.of("Line one", "Line two")
        );
        file.save();
        file.reload();

        MessageManager messageManager = new MessageManager(plugin, file);

        assertEquals(
                List.of("Line one", "Line two"),
                messageManager.getList(() -> "core.help")
        );
    }

    @Test
    void getsPrefix()
    {
        YamlFile file = TestUtils.createYaml(plugin);

        file.set(
                "core.prefix",
                "&7[&aDuels&]"
        );
        file.save();
        file.reload();

        MessageManager messageManager = new MessageManager(plugin, file);

        assertEquals(
                "&7[&aDuels&]",
                messageManager.getPrefix()
        );
    }
}