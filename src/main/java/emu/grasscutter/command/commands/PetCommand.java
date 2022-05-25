package emu.grasscutter.command.commands;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.command.Command;
import emu.grasscutter.command.CommandHandler;
import emu.grasscutter.data.GameData;
import emu.grasscutter.data.def.GadgetData;
import emu.grasscutter.data.def.ItemData;
import emu.grasscutter.data.def.MonsterData;
import emu.grasscutter.game.entity.EntityItem;
import emu.grasscutter.game.entity.EntityMonster;
import emu.grasscutter.game.entity.EntityVehicle;
import emu.grasscutter.game.entity.GameEntity;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.game.props.FightProperty;
import emu.grasscutter.game.world.Scene;
import emu.grasscutter.utils.Position;
import emu.grasscutter.utils.Utils;

import java.util.List;

import static emu.grasscutter.utils.Language.translate;

@Command(label = "pet", usage = "pet <entityId>",
        description = "Gain an pet near you", permission = "server.pet")
public final class PetCommand implements CommandHandler {


    @Override
    public void execute(Player sender, Player targetPlayer, List<String> args) {
        if (targetPlayer == null) {
            CommandHandler.sendMessage(sender, translate("commands.execution.need_target"));
            return;
        }

        int id = 0;
        int amount = 1;
        int level = 1;
        switch (args.size()) {
            case 1:
                try {
                    id = Integer.parseInt(args.get(0));
                } catch (NumberFormatException ignored) {
                    CommandHandler.sendMessage(sender, translate("commands.generic.error.entityId"));
                }
                break;
            default:
                CommandHandler.sendMessage(sender, translate("commands.pet.usage"));
                return;
        }

        MonsterData monsterData = GameData.getMonsterDataMap().get(id);
        GadgetData gadgetData = GameData.getGadgetDataMap().get(id);
        ItemData itemData = GameData.getItemDataMap().get(id);
        if (monsterData == null && gadgetData == null && itemData == null) {
            CommandHandler.sendMessage(sender, translate("commands.generic.error.entityId"));
            return;
        }
        Scene scene = targetPlayer.getScene();

        double maxRadius = Math.sqrt(amount * 0.2 / Math.PI);
        for (int i = 0; i < amount; i++) {
            Position pos = GetRandomPositionInCircle(targetPlayer.getPos(), maxRadius).addY(3);
            GameEntity entity = null;
            if (itemData != null) {
                entity = new EntityItem(scene, null, itemData, pos, 1, true);
            }
            if (gadgetData != null) {
                entity = new EntityVehicle(scene, targetPlayer.getSession().getPlayer(), gadgetData.getId(), 0, pos, targetPlayer.getRotation());  // TODO: does targetPlayer.getSession().getPlayer() have some meaning?
                int gadgetId = gadgetData.getId();
                switch (gadgetId) {
                    // TODO: Not hardcode this. Waverider (skiff)
                    case 45001001, 45001002 -> {
                        entity.addFightProperty(FightProperty.FIGHT_PROP_BASE_HP, 10000);
                        entity.addFightProperty(FightProperty.FIGHT_PROP_BASE_ATTACK, 100);
                        entity.addFightProperty(FightProperty.FIGHT_PROP_CUR_ATTACK, 100);
                        entity.addFightProperty(FightProperty.FIGHT_PROP_CUR_HP, 10000);
                        entity.addFightProperty(FightProperty.FIGHT_PROP_CUR_DEFENSE, 0);
                        entity.addFightProperty(FightProperty.FIGHT_PROP_CUR_SPEED, 0);
                        entity.addFightProperty(FightProperty.FIGHT_PROP_CHARGE_EFFICIENCY, 0);
                        entity.addFightProperty(FightProperty.FIGHT_PROP_MAX_HP, 10000);
                    }
                    default -> {}
                }
            }
            if (monsterData != null) {
                monsterData.setDefenseBase(0);
                monsterData.setAttackBase(0);
                monsterData.setElecSubHurt(0);
                monsterData.setFireSubHurt(0);
                monsterData.setGrassSubHurt(0);
                monsterData.setIceSubHurt(0);
                monsterData.setWaterSubHurt(0);
                monsterData.setRockSubHurt(0);
                monsterData.setWindSubHurt(0);
                monsterData.setPhysicalSubHurt(0);
                entity = new EntityMonster(scene, monsterData, pos, level);
            }

            scene.addEntity(entity);
            Grasscutter.playerPetMap.put(entity.getId(), targetPlayer.getUid());
            Grasscutter.getLogger().info("玩家 " + targetPlayer.getUid() + "获得了一只宠物 " + String.valueOf(entity.getId()) );
        }
        CommandHandler.sendMessage(sender, translate("commands.spawn.success", Integer.toString(amount), Integer.toString(id)));
    }

    private Position GetRandomPositionInCircle(Position origin, double radius){
        Position target = origin.clone();
        double angle = Math.random() * 360;
        double r = Math.sqrt(Math.random() * radius * radius);
        target.addX((float) (r * Math.cos(angle))).addZ((float) (r * Math.sin(angle)));
        return target;
    }
}
