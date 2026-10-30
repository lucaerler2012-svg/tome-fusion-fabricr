package io.github.lucaerler2012svg.tomefusion;

import java.util.Comparator;
import java.util.List;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

/**
 * Eigener Kreativ-Tab "Tome Fusion": zeigt JEDES verzauberte Buch fuer JEDE Verzauberung
 * der Reihe nach (z. B. Schaerfe 1, 2, 3 ... 10), danach die naechste Verzauberung.
 * Obergrenze pro Verzauberung = Vanilla-Maximum x 2 (siehe BookMerger.limit).
 */
public final class TomeTabs {

	private TomeTabs() {}

	public static void register() {
		// TODO: Falls "Identifier" nicht gefunden wird: in 26.3 per ./gradlew genSources nachsehen
		//       (frueher "ResourceLocation").
		Identifier id = Identifier.fromNamespaceAndPath(TomeFusion.MOD_ID, "books");

		CreativeModeTab tab = FabricItemGroup.builder()
				.title(Component.literal("Tome Fusion"))
				.icon(() -> new ItemStack(Items.ENCHANTED_BOOK))
				.displayItems((params, output) -> {
					HolderLookup.RegistryLookup<Enchantment> lookup =
							params.holders().lookupOrThrow(Registries.ENCHANTMENT);

					// Alphabetisch nach Verzauberungs-Name sortieren, damit die Reihenfolge stabil ist
					List<Holder.Reference<Enchantment>> enchantments = lookup.listElements()
							.sorted(Comparator.comparing(holder -> holder.key().toString()))
							.toList();

					for (Holder.Reference<Enchantment> enchantment : enchantments) {
						// Hoechstes erlaubtes Level (Max x 2, bei Max-Level 1 bleibt es 1)
						int cap = BookMerger.limit(enchantment, Integer.MAX_VALUE);
						// Level aufsteigend: 1, 2, 3 ... cap
						for (int level = 1; level <= cap; level++) {
							ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
							ItemEnchantments.Mutable stored = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
							stored.set(enchantment, level);
							book.set(DataComponents.STORED_ENCHANTMENTS, stored.toImmutable());
							output.accept(book);
						}
					}
				})
				.build();

		Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, id, tab);
	}
}
