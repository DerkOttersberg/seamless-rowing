package io.github.derkottersberg.seamlessrowing.client;

import io.github.derkottersberg.seamlessrowing.config.ClientSettings;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import java.io.IOException;

public final class RowingSettingsScreen extends Screen {
    private final Screen parent;
    private ClientSettings draft=RowingClient.settings();
    private String error="";
    public RowingSettingsScreen(Screen parent) { super(Component.translatable("seamlessrowing.settings.title")); this.parent=parent; }
    @Override protected void init() {
        int columns=height<250 ? 2 : 1;
        int total=Math.min(columns==2 ? 440 : 260,width-16),x=(width-total)/2,y=50;
        int buttonWidth=(total-(columns-1)*6)/columns;
        String[] labels={"enabled","firstPerson","thirdPerson","elbows","torso"};
        boolean[] flags={draft.enabled(),draft.firstPerson(),draft.thirdPerson(),draft.elbows(),draft.torso()};
        for(int i=0;i<labels.length;i++) {
            final int index=i;
            addRenderableWidget(Button.builder(Component.translatable("seamlessrowing.settings."+labels[i])
                    .append(": ").append(Component.translatable(flags[i] ? "options.on" : "options.off")),button -> {
                boolean[] next={draft.enabled(),draft.firstPerson(),draft.thirdPerson(),draft.elbows(),draft.torso()};
                next[index]=!next[index]; draft=new ClientSettings(next[0],next[1],next[2],next[3],next[4]); rebuildWidgets();
            }).bounds(x+(i%columns)*(buttonWidth+6),y+(i/columns)*24,buttonWidth,20).build());
        }
        int bottom=height-26,footerX=(width-Math.min(260,width-16))/2,footerWidth=(Math.min(260,width-16)-12)/3;
        addRenderableWidget(Button.builder(Component.translatable("controls.reset"),button->{draft=ClientSettings.DEFAULT;error="";rebuildWidgets();}).bounds(footerX,bottom,footerWidth,20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.cancel"),button->onClose()).bounds(footerX+footerWidth+6,bottom,footerWidth,20).build());
        addRenderableWidget(Button.builder(Component.translatable("seamlessrowing.settings.save"),button->{
            try { RowingClient.save(draft); onClose(); } catch(IOException failure) { error="Could not save; settings remain unchanged."; }
        }).bounds(footerX+2*(footerWidth+6),bottom,footerWidth,20).build());
    }
    @Override public void render(GuiGraphics graphics,int mouseX,int mouseY,float partial) {
        super.render(graphics,mouseX,mouseY,partial);
        graphics.drawCenteredString(font,title,width/2,18,0xFFFFFFFF);
        graphics.drawCenteredString(font,Component.translatable("seamlessrowing.settings.scope"),width/2,34,0xFFB9C9D2);
        graphics.drawWordWrap(font,Component.literal(error.isEmpty()?RowingClient.compatibilityStatus():error),
                Math.max(8,(width-300)/2),height-(height<250?56:66),Math.min(300,width-16),error.isEmpty()?0xFFB9C9D2:0xFFFF8888);
    }
    @Override public void onClose() { minecraft.setScreen(parent); }
}
