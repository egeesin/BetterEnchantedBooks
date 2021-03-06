package dev.bernasss12.bebooks.mixin;

import dev.bernasss12.bebooks.BetterEnchantedBooks;
import dev.bernasss12.bebooks.client.gui.ModConfig;
import dev.bernasss12.bebooks.client.gui.TooltipDrawerHelper;
import dev.bernasss12.bebooks.util.NBTUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Comparator;
import java.util.List;

@Mixin(ItemStack.class)
@Environment(EnvType.CLIENT)
public abstract class ItemStackMixin {

    public ItemStackMixin(Item item) {
        super();
    }

    @Inject(at = @At(value = "HEAD"), method = "appendEnchantments")
    private static void appendEnchantmentsHead(List<Text> tooltip, ListTag enchantments, CallbackInfo info) {
        if (MinecraftClient.getInstance().currentScreen != null && MinecraftClient.getInstance().currentScreen instanceof HandledScreen) {
            if (ModConfig.configsFirstLoaded && ModConfig.sortingSetting != ModConfig.SortingSetting.DISABLED) {
                ListTag sortedEnchantments;
                try {
                    sortedEnchantments = NBTUtils.sort(enchantments, ModConfig.sortingSetting);
                    enchantments.clear();
                    enchantments.addAll(sortedEnchantments);
                    if (BetterEnchantedBooks.enchantedItemStack.get().isItemEqual(new ItemStack(Items.ENCHANTED_BOOK))) {
                        if (!BetterEnchantedBooks.cachedTooltipIcons.containsKey(BetterEnchantedBooks.enchantedItemStack.get())) {
                            BetterEnchantedBooks.cachedTooltipIcons.putIfAbsent(BetterEnchantedBooks.enchantedItemStack.get(), new TooltipDrawerHelper.TooltipQueuedEntry(tooltip.size(), enchantments));
                        }
                    }
                    TooltipDrawerHelper.currentTooltipWidth = MinecraftClient.getInstance().textRenderer.getWidth(tooltip.stream().max(Comparator.comparing(line -> MinecraftClient.getInstance().textRenderer.getWidth(line))).get());
                } catch (Exception ignored) {
                }
            }
        }
    }

    @Inject(at = @At(value = "HEAD"), method = "net/minecraft/item/ItemStack.method_17869(Ljava/util/List;Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/enchantment/Enchantment;)V")
    private static void method17869Head(List<Text> tooltip, CompoundTag tag, Enchantment enchantment, CallbackInfo info) {
        if(ModConfig.doShowEnchantmentMaxLevel){
            BetterEnchantedBooks.tooltipName.set(true);
        }
    }

    @Inject(at = @At(value = "TAIL"), method = "net/minecraft/item/ItemStack.method_17869(Ljava/util/List;Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/enchantment/Enchantment;)V")
    private static void method17869Tail(List<Text> tooltip, CompoundTag tag, Enchantment enchantment, CallbackInfo info) {
        // This will only run on HandledScreen subclasses because there is no need for it run elsewhere and also it can cause NPE crashes.
        if (MinecraftClient.getInstance().currentScreen != null && MinecraftClient.getInstance().currentScreen instanceof HandledScreen) {
            if (BetterEnchantedBooks.enchantedItemStack.get().isItemEqual(new ItemStack(Items.ENCHANTED_BOOK))) {
                switch (ModConfig.tooltipSetting) {
                    case ENABLED:
                        tooltip.addAll(TooltipDrawerHelper.getSpacerLines(enchantment, TooltipDrawerHelper.currentTooltipWidth));
                        break;
                    case ON_SHIFT:
                        if (Screen.hasShiftDown()) tooltip.addAll(TooltipDrawerHelper.getSpacerLines(enchantment, TooltipDrawerHelper.currentTooltipWidth));
                        break;
                    case DISABLED:
                        break;
                }
            }
        }
    }
}
