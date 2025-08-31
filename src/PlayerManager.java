import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Simple player profile manager to enhance save/load per player.
 * Stores active profile and known profiles under saves/.players
 */
public class PlayerManager {
    private static final Path ROOT = Paths.get("saves/.players");
    private static final Path ACTIVE = ROOT.resolve("active.txt");

    public PlayerManager() {
        try { Files.createDirectories(ROOT); } catch (IOException ignored) {}
    }

    public String getActiveProfileId() {
        try {
            if (Files.exists(ACTIVE)) {
                String s = Files.readString(ACTIVE).trim();
                if (!s.isEmpty()) return s;
            }
        } catch (IOException ignored) {}
        return "default";
    }

    public void setActiveProfileId(String id) {
        try { Files.writeString(ACTIVE, id == null ? "default" : id); } catch (IOException ignored) {}
    }

    public List<String> listProfiles() {
        List<String> out = new ArrayList<>();
        try {
            if (!Files.isDirectory(ROOT)) return List.of("default");
            Files.list(ROOT.getParent())
                .filter(p -> Files.isDirectory(p) && !p.getFileName().toString().equals(".players"))
                .forEach(p -> out.add(p.getFileName().toString()));
        } catch (IOException ignored) {}
        if (out.isEmpty()) out.add("default");
        return out;
    }
}




