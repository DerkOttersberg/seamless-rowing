package qa.rowing;
import java.util.function.Predicate;
public final class QaPlatform {
    private static Predicate<String> mods=id->false;
    public static void initialize(Predicate<String> loaded) {mods=loaded;}
    public static boolean isModLoaded(String id) {return mods.test(id);}
}
