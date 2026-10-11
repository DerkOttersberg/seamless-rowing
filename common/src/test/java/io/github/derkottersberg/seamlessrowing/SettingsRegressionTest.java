package io.github.derkottersberg.seamlessrowing;

import io.github.derkottersberg.seamlessrowing.config.ClientSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;

class SettingsRegressionTest {
    @TempDir Path temp;
    @Test void savesAtomicallyAndKeepsPreviousSettingsBackup() throws Exception {
        Path file=temp.resolve("seamless-rowing.json");
        ClientSettings.DEFAULT.save(file); String original=Files.readString(file);
        var changed=new ClientSettings(false,true,false,false,true); changed.save(file);
        assertEquals(changed,ClientSettings.load(file));
        assertEquals(original,Files.readString(temp.resolve("seamless-rowing.json.bak")));
        try(var paths=Files.list(temp)) { assertEquals(2,paths.count()); }
    }
    @Test void malformedConfigIsPreservedAndMissingFieldsHaveDefaults() throws Exception {
        Path file=temp.resolve("settings.json"); Files.writeString(file,"{\"elbows\":\"false\"}");
        String original=Files.readString(file);
        assertThrows(IOException.class,()->ClientSettings.load(file)); assertEquals(original,Files.readString(file));
        Files.writeString(file,"{\"enabled\":false}");
        assertEquals(new ClientSettings(false,true,true,true,true),ClientSettings.load(file));
    }
    @Test void futureSchemaDoesNotGetSilentlyOverwritten() throws Exception {
        Path file=temp.resolve("settings.json"); Files.writeString(file,"{\"schema\":2}");
        assertThrows(IOException.class,()->ClientSettings.load(file));
        assertEquals("{\"schema\":2}",Files.readString(file));
    }
}
