package qa.rowing;
public final class SoundProbe {
    private static int count;
    public static void record(net.minecraft.client.resources.sounds.SoundInstance sound) {
        if(sound.getLocation().toString().contains("entity.boat.paddle")&&!sound.isRelative())count++;
    }
    public static int count(){return count;}
}
