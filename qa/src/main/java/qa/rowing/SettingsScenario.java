package qa.rowing;

import io.github.derkottersberg.seamlessrowing.client.*;
import io.github.derkottersberg.seamlessrowing.config.ClientSettings;
import net.minecraft.client.*;
import net.minecraft.client.gui.components.Button;
import java.nio.file.*;

public final class SettingsScenario {
    private static int tick,passed;
    public static void tick(Minecraft mc) {
        if(mc.player==null||mc.level==null) return;
        tick++;
        int stage=(tick-1)/40,local=(tick-1)%40;
        if(stage<3) {
            if(local==0) {
                mc.options.guiScale().set(new int[]{1,2,4}[stage]);mc.resizeDisplay();
                mc.setScreen(new RowingSettingsScreen(null));
            }
            if(local==12) Screenshot.grab(mc.gameDirectory,"settings-scale-"+new int[]{1,2,4}[stage]+".png",mc.getMainRenderTarget(),message->{});
            if(local==15) press(mc,"Rowing animation");
            if(local==20) {press(mc,"Cancel");check(RowingClient.settings().enabled(),"Cancel changed runtime settings");passed++;}
            if(local==25) {mc.setScreen(new RowingSettingsScreen(null));press(mc,"Rowing animation");press(mc,"Save");check(!RowingClient.settings().enabled(),"Save ignored draft");passed++;}
            if(local==30) {mc.setScreen(new RowingSettingsScreen(null));press(mc,"Reset");press(mc,"Save");check(RowingClient.settings().equals(ClientSettings.DEFAULT),"Reset ignored");passed++;}
        } else if(tick==121) {
            try {
                check(Files.exists(RowingClient.configPath().resolveSibling("seamless-rowing.json.bak")),"Missing backup");
                check(ClientSettings.load(RowingClient.configPath()).equals(ClientSettings.DEFAULT),"Persistence mismatch");
                Files.writeString(mc.gameDirectory.toPath().resolve("rowing-settings.json"),"{\"pass\":true,\"nativeUiAssertions\":"+passed+",\"guiScales\":[1,2,4]}");
            } catch(Exception failure){throw new RuntimeException(failure);}
            System.out.println("ROWING_SETTINGS_COMPLETE pass=true assertions="+passed);mc.stop();
        }
    }
    private static void press(Minecraft mc,String label) {
        var screen=mc.screen;check(screen instanceof RowingSettingsScreen,"Wrong screen");
        Button button=screen.children().stream().filter(c->c instanceof Button b&&b.getMessage().getString().startsWith(label)).map(c->(Button)c).findFirst().orElseThrow(()->new IllegalStateException("Missing button "+label));
        button.onPress();
    }
    private static void check(boolean condition,String failure){if(!condition)throw new IllegalStateException(failure);}
}
