package codes.dreaming.dietcustomizer.mixin;

import com.illusivesoulworks.diet.api.type.IDietGroup;
import com.illusivesoulworks.diet.api.type.IDietResult;
import com.illusivesoulworks.diet.common.DietApiImpl;
import com.illusivesoulworks.diet.common.data.group.DietGroups;
import com.illusivesoulworks.diet.common.util.DietResult;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigValue;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;

import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import static codes.dreaming.dietcustomizer.DietCustomizer.CONFIG;

import java.util.*;


@Mixin(value = DietApiImpl.class, remap = false)
public abstract class DietApiImplMixin {
    @Inject(method = "get*", at = @At("HEAD"), cancellable = true)
    private void onGet(PlayerEntity player, ItemStack input, CallbackInfoReturnable<IDietResult> cir) {
        HashMap<IDietGroup, Float> diet = new HashMap<>();

        ConfigValue food = getFoodValues(input);
        if (food == null) {
            cir.setReturnValue(new DietResult(diet));
            return;
        }
        HashMap<String, Number> foodValues = (HashMap<String, Number>) food.unwrapped();

        Set<IDietGroup> groups =  DietGroups.getGroups(player.getWorld());
        for (IDietGroup group : groups) {
            Number nutritionValue = foodValues.get(group.getName());
            if (nutritionValue == null) {
                continue;
            }
            diet.put(group, nutritionValue.floatValue());
        }

        cir.setReturnValue(new DietResult(diet));
    }

    // Since we are not using the default diet system, we can cancel the calculation of the diet just to be sure that it won't interfere with our custom diet system
    @Inject(method = "calculate", at = @At("HEAD"), cancellable = true)
    private static void onCalculate(float healing, float saturation, Set<IDietGroup> groups, CallbackInfoReturnable<Map<IDietGroup, Float>> cir) {
        cir.setReturnValue(new HashMap<>());
    }

    @Unique
    private static ConfigValue getFoodValues(ItemStack itemStack) {
        Config foodValues = CONFIG.getConfig("foodValues");
        ConfigObject foodTagsValues = CONFIG.getObject("foodTagsValues");

        String escapedId = "\"%s\"".formatted(Registry.ITEM.getId(itemStack.getItem()).toString());
        if (foodValues.hasPath(escapedId)) {
            return foodValues.getValue(escapedId);
        }

        var stream = foodTagsValues.entrySet().stream().filter(ConfigValueEntry ->
            itemStack.isIn(TagKey.of(Registry.ITEM.getKey(), Identifier.tryParse(ConfigValueEntry.getKey())))
        ).findFirst();

        return stream.map(Map.Entry::getValue).orElse(null);
    }

}
