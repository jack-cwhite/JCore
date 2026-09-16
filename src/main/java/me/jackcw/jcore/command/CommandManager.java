package me.jackcw.jcore.command;

import me.jackcw.jcore.message.CoreMessage;
import me.jackcw.jcore.message.MessageManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class CommandManager implements CommandExecutor
{
    private final JavaPlugin plugin;
    private final MessageManager messageManager;
    private final Map<String, CommandNode> commands = new HashMap<>();
    private PermissionPolicy permissionPolicy = PermissionPolicy.PARENT_AND_LEAF;

    public CommandManager(JavaPlugin plugin, MessageManager messageManager)
    {
        this.plugin = plugin;
        this.messageManager = messageManager;
    }

    public void setPermissionPolicy(PermissionPolicy permissionPolicy)
    {
        if (permissionPolicy == null)
            throw new IllegalArgumentException("Permission policy cannot be null");

        this.permissionPolicy = permissionPolicy;
    }

    public void register(CommandNode command)
    {
        registerName(command.getName(), command);

        for (String alias : command.getAliases())
            registerName(alias, command);

        PluginCommand bukkitCommand = plugin.getCommand(command.getName());

        if (bukkitCommand == null)
        {
            throw new IllegalStateException("Command '" + command.getName() + "' is not defined in plugin.yml");
        }

        bukkitCommand.setExecutor(this);
        bukkitCommand.setTabCompleter(this::tabComplete);
    }

    public void registerName(String name, CommandNode command)
    {
        String key = name.toLowerCase();

        if (commands.containsKey(key))
            throw new IllegalStateException("Command name or alias '" + name + "' is already registered");

        commands.put(key, command);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        CommandNode root = commands.get(command.getName().toLowerCase());

        if (root == null)
            return false;

        return executeNode(root, sender, args);
    }

    private boolean executeNode(CommandNode node, CommandSender sender, String[] args)
    {
        CommandNode child = args.length > 0 ? findChild(node, args[0]) : null;

        boolean requiresPermissionHere = permissionPolicy == PermissionPolicy.PARENT_AND_LEAF || child == null;

        if (requiresPermissionHere && !checkPermission(node, sender))
            return true;

        if (node.isPlayerOnly() && !(sender instanceof Player))
        {
            messageManager.send(sender, CoreMessage.PLAYER_ONLY);
            return true;
        }

        if (child != null)
        {
            String[] remaining = new String[args.length-1];
            System.arraycopy(args, 1, remaining, 0, remaining.length);

            return executeNode(child, sender, remaining);
        }

        if (node.getExecutor() != null)
            return execute(node, sender, args);

        if (node.getUsage() != null)
            messageManager.send(sender, CoreMessage.INCORRECT_USAGE, "usage", node.getUsage());

        return true;
    }

    private boolean execute(CommandNode node, CommandSender sender, String[] args)
    {
        Map<String, Object> parsedArguments = new HashMap<>();

        int argumentIndex = 0;
        for (CommandArgument<?> argument : node.getArguments())
        {
            if (argumentIndex >= args.length)
            {
                if (argument.isRequired())
                {
                    if (node.getUsage() != null)
                        messageManager.send(sender, CoreMessage.INCORRECT_USAGE, "usage", node.getUsage());

                    return true;
                }

                continue;
            }

            String rawValue = args[argumentIndex];

            try
            {
                Object value = argument.getType().parse(rawValue);
                parsedArguments.put(argument.getName(), value);
            }
            catch (CommandArgumentException e)
            {
                messageManager.send(sender, e.getMessageKey(), e.getPlaceholders());
                return true;
            }
            catch (IllegalArgumentException e)
            {
                sender.sendMessage(org.bukkit.ChatColor.RED + e.getMessage());
                return true;
            }

            argumentIndex++;
        }

        if (argumentIndex < args.length)
        {
            if (node.getUsage() != null)
                messageManager.send(sender, CoreMessage.INCORRECT_USAGE, "usage", node.getUsage());

            return true;
        }

        CommandContext context = new CommandContext(sender, parsedArguments);

        node.getExecutor().execute(context);

        return true;
    }

    private boolean checkPermission(CommandNode node, CommandSender sender)
    {
        if (hasPermission(node, sender))
            return true;

        messageManager.send(sender, CoreMessage.NO_PERMISSION, "permission", node.getPermission()
        );

        return false;
    }

    private boolean hasPermission(CommandNode node, CommandSender sender)
    {
        String permission = node.getPermission();

        return permission == null || permission.isBlank() || sender.hasPermission(permission);
    }

    private boolean isVisibleInCompletion(CommandNode node, CommandSender sender)
    {
        if (permissionPolicy == PermissionPolicy.PARENT_AND_LEAF || node.getChildren().isEmpty())
            return hasPermission(node, sender);

        return true;
    }

    private CommandNode findChild(CommandNode node, String name)
    {
        for (CommandNode child : node.getChildren())
        {
            if (child.getName().equalsIgnoreCase(name))
                return child;

            for (String alias : child.getAliases())
                if (alias.equalsIgnoreCase(name))
                    return child;
        }

        return null;
    }

    private List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args)
    {
        CommandNode root = commands.get(command.getName().toLowerCase());

        if (root == null)
            return Collections.emptyList();

        CommandNode node = root;
        int argumentIndex = 0;
        boolean consumingArguments = false;

        for (int i = 0; i < args.length - 1; i++)
        {
            if (!consumingArguments)
            {
                CommandNode child = findChild(node, args[i]);

                if (child != null && isVisibleInCompletion(child, sender))
                {
                    node = child;
                    continue;
                }

                consumingArguments = true;
            }

            argumentIndex++;
        }

        String current = args.length > 0 ? args[args.length - 1] : "";

        List<String> completions = new ArrayList<>();

        if (!consumingArguments)
            for (CommandNode child : node.getChildren())
                if (isVisibleInCompletion(child, sender) && child.getName().toLowerCase().startsWith(current.toLowerCase()))
                    completions.add(child.getName());

        List<CommandArgument<?>> arguments = node.getArguments();

        if (argumentIndex < arguments.size())
            completions.addAll(arguments.get(argumentIndex).getType().suggest(sender, current));

        return completions;
    }
}
