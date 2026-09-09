package me.jackcw.jcore.storage;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.ScalarNode;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public final class YamlDefaultsMerger
{
    public boolean merge(File target, InputStream defaults)
    {
        if (target == null)
            throw new IllegalArgumentException(
                    "Target file cannot be null"
            );

        if (defaults == null)
            throw new IllegalArgumentException(
                    "Defaults input stream cannot be null"
            );

        LoaderOptions loaderOptions = new LoaderOptions();
        loaderOptions.setProcessComments(true);

        Yaml yaml = new Yaml(loaderOptions);

        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setProcessComments(true);
        dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        dumperOptions.setPrettyFlow(true);

        Yaml dumper = new Yaml(dumperOptions);

        Node targetNode;

        try (Reader reader = new InputStreamReader(Files.newInputStream(target.toPath()), StandardCharsets.UTF_8))
        {
            targetNode = yaml.compose(reader);
        }
        catch (Exception e)
        {
            throw new RuntimeException(
                    "Could not read target YAML: " + target.getName(), e
            );
        }

        Node defaultsNode;

        try (Reader reader = new InputStreamReader(defaults, StandardCharsets.UTF_8
        ))
        {
            defaultsNode = yaml.compose(reader);
        }
        catch (Exception e)
        {
            throw new RuntimeException(
                    "Could not read default YAML", e
            );
        }

        if (!(targetNode instanceof MappingNode targetMapping))
            throw new RuntimeException(
                    "Target YAML must contain a root mapping"
            );

        if (!(defaultsNode instanceof MappingNode defaultsMapping))
            throw new RuntimeException(
                    "Default YAML must contain a root mapping"
            );

        boolean changed = mergeMappings(targetMapping, defaultsMapping
        );

        if (!changed)
            return false;

        try (Writer writer = Files.newBufferedWriter(target.toPath(), StandardCharsets.UTF_8
        ))
        {
            dumper.serialize(targetMapping, writer
            );
        }
        catch (Exception e)
        {
            throw new RuntimeException(
                    "Could not save merged YAML: " + target.getName(), e
            );
        }

        return true;
    }

    private boolean mergeMappings(MappingNode target, MappingNode defaults)
    {
        boolean changed = false;

        for (NodeTuple defaultTuple : defaults.getValue())
        {
            Node defaultKey = defaultTuple.getKeyNode();
            Node defaultValue = defaultTuple.getValueNode();

            String key = getKey(defaultKey);

            Node existingValue = null;

            for (NodeTuple targetTuple : target.getValue())
            {
                Node targetKey = targetTuple.getKeyNode();

                if (getKey(targetKey).equals(key))
                {
                    existingValue = targetTuple.getValueNode();
                    break;
                }
            }

            if (existingValue == null)
            {
                target.getValue().add(defaultTuple);

                changed = true;
                continue;
            }

            if (existingValue instanceof MappingNode existingMapping && defaultValue instanceof MappingNode defaultMapping)
                if (mergeMappings(existingMapping, defaultMapping))
                    changed = true;
        }

        return changed;
    }

    private String getKey(Node node)
    {
        if (!(node instanceof ScalarNode scalar))
            throw new IllegalArgumentException(
                    "YAML mapping keys must be scalar values"
            );

        return scalar.getValue();
    }
}