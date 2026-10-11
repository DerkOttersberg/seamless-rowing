package io.github.derkottersberg.seamlessrowing.fabric;

import com.terraformersmc.modmenu.api.ModMenuApi;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import io.github.derkottersberg.seamlessrowing.client.RowingSettingsScreen;

public final class RowingModMenu implements ModMenuApi {
    public ConfigScreenFactory<?> getModConfigScreenFactory(){return RowingSettingsScreen::new;}
}
