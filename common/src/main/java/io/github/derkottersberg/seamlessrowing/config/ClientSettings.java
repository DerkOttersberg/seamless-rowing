package io.github.derkottersberg.seamlessrowing.config;

import com.google.gson.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

public record ClientSettings(boolean enabled, boolean firstPerson, boolean thirdPerson,
                             boolean elbows, boolean torso) {
    public static final ClientSettings DEFAULT=new ClientSettings(true,true,true,true,true);
    private static final Gson GSON=new GsonBuilder().setPrettyPrinting().create();
    public static ClientSettings load(Path file) throws IOException {
        if (!Files.exists(file)) return DEFAULT;
        try {
            JsonObject json=JsonParser.parseString(Files.readString(file)).getAsJsonObject();
            if (json.has("schema") && (!json.get("schema").isJsonPrimitive()
                    || !json.get("schema").getAsJsonPrimitive().isNumber()
                    || json.get("schema").getAsBigDecimal().compareTo(java.math.BigDecimal.ONE)!=0))
                throw new IllegalArgumentException("Unsupported settings schema");
            return new ClientSettings(flag(json,"enabled"),flag(json,"firstPerson"),
                    flag(json,"thirdPerson"),flag(json,"elbows"),flag(json,"torso"));
        } catch (RuntimeException malformed) { throw new IOException("Invalid rowing settings; original preserved",malformed); }
    }
    private static boolean flag(JsonObject json,String key) {
        if (!json.has(key)) return true;
        JsonElement value=json.get(key);
        if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isBoolean())
            throw new IllegalArgumentException("Expected boolean: "+key);
        return value.getAsBoolean();
    }
    public void save(Path file) throws IOException {
        Files.createDirectories(file.toAbsolutePath().getParent());
        JsonObject json=new JsonObject(); json.addProperty("schema",1);
        json.addProperty("enabled",enabled); json.addProperty("firstPerson",firstPerson);
        json.addProperty("thirdPerson",thirdPerson); json.addProperty("elbows",elbows); json.addProperty("torso",torso);
        Path temp=Files.createTempFile(file.toAbsolutePath().getParent(),"rowing-",".tmp");
        try {
            Files.writeString(temp,GSON.toJson(json)+"\n",StandardCharsets.UTF_8);
            if (Files.exists(file)) Files.copy(file,file.resolveSibling(file.getFileName()+".bak"),StandardCopyOption.REPLACE_EXISTING);
            try { Files.move(temp,file,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING); }
            catch (AtomicMoveNotSupportedException unavailable) { Files.move(temp,file,StandardCopyOption.REPLACE_EXISTING); }
        } finally { Files.deleteIfExists(temp); }
    }
}
