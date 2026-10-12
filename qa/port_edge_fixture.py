"""Port the native edge fixture through the branches' explicit Minecraft API adapters."""
from pathlib import Path
base=Path(__file__).resolve().parents[2]
relative=Path('qa/src/main/java/qa/rowing/EdgeScenario.java')
text=(base/'seamless-rowing'/relative).read_text()
for mc in ('26.2','1.21.1','1.20.1'):
    port=text
    if mc.startswith('1.'):
        port=port.replace('net.minecraft.world.entity.vehicle.boat.AbstractBoat','net.minecraft.world.entity.vehicle.Boat').replace('minecraft:oak_boat','minecraft:boat')
        native='mc.clearLevel(new TitleScreen());' if mc=='1.20.1' else 'mc.disconnect(new TitleScreen());'
        port=port.replace('mc.level.disconnect(net.minecraft.network.chat.Component.literal("QA disconnect"));mc.disconnect(new TitleScreen(),false);','mc.level.disconnect();'+native)
    (base/('seamless-rowing-'+mc)/relative).write_text(port)
print('Ported edge fixture to all four version branches')
