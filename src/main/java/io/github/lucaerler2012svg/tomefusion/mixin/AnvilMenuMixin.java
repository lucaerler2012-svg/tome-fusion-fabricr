package io.github.lucaerler2012svg.tomefusion.mixin;

import io.github.lucaerler2012svg.tomefusion.BookMerger;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Greift in die Ergebnis-Berechnung des Ambosses ein. Zwei Faelle:
 *
 *  1) Buch + Buch:        Level werden addiert, Limit = Vanilla-Max x 2.
 *  2) Item + Buch:        (z. B. Schwert + Schaerfe-V-Buch) Level werden zu den vorhandenen
 *                         Verzauberungen des Items addiert, gleiches Limit.
 *
 * In allen anderen Faellen (oder wenn nichts passt) laeuft Vanilla unveraendert.
 *
 * TODO: Die Namen (createResult, cost, repairItemCountCost) sind Mojang-Namen aus 1.21.x und
 *       konnten fuer 26.3 nicht geprueft werden. Bei Mixin-Fehlern: ./gradlew genSources.
 */
@Mixin(AnvilMenu.class)
public abstract class AnvilMenuMixin {

	/** Die XP-Level-Kosten, die im Amboss angezeigt und beim Entnehmen abgezogen werden. */
	@Shadow @Final private DataSlot cost;

	/** Wie viele Reparatur-Materialien verbraucht werden (bei Buechern: 0). */
	@Shadow private int repairItemCountCost;

	@Inject(method = "createResult", at = @At("HEAD"), cancellable = true)
	private void tomefusion$combine(CallbackInfo ci) {
		AnvilMenu menu = (AnvilMenu) (Object) this;

		// Slots im Amboss: 0 = links, 1 = rechts, 2 = Ergebnis
		ItemStack left = menu.getSlot(0).getItem();
		ItemStack right = menu.getSlot(1).getItem();

		// Rechts muss ein verzaubertes Buch liegen
		if (!right.is(Items.ENCHANTED_BOOK)) {
			return; // Vanilla
		}
		ItemEnchantments rightEnchants = right.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
		if (rightEnchants.isEmpty() || left.isEmpty()) {
			return; // Vanilla
		}

		ItemStack result;

		if (left.is(Items.ENCHANTED_BOOK)) {
			// ---- Fall 1: Buch + Buch ----
			// Verzauberte Buecher speichern in STORED_ENCHANTMENTS (Werkzeuge in ENCHANTMENTS)
			ItemEnchantments leftEnchants = left.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
			if (leftEnchants.isEmpty()) {
				return; // Vanilla
			}
			result = left.copy();
			result.setCount(1);
			result.set(DataComponents.STORED_ENCHANTMENTS, BookMerger.merge(leftEnchants, rightEnchants));
		} else {
			// ---- Fall 2: Item (Schwert, Spitzhacke ...) + Buch ----
			if (left.getCount() != 1) {
				return; // Vanilla
			}
			// Nur Verzauberungen, die auf dieses Item passen (z. B. keine Schaerfe auf Spitzhacke)
			boolean anyApplicable = false;
			for (Holder<Enchantment> enchantment : rightEnchants.keySet()) {
				if (enchantment.value().canEnchant(left)) {
					anyApplicable = true;
					break;
				}
			}
			if (!anyApplicable) {
				return; // nichts passt -> Vanilla entscheiden lassen
			}
			ItemEnchantments itemEnchants = left.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
			ItemEnchantments merged = BookMerger.merge(itemEnchants, rightEnchants,
					enchantment -> enchantment.value().canEnchant(left));
			result = left.copy();
			result.set(DataComponents.ENCHANTMENTS, merged);
			// TODO: Beschaedigung/Reparaturkosten ("RepairCost") wie Vanilla behandeln?
		}

		// TODO (UMBENENNEN): Vanilla erlaubt Umbenennen beim Kombinieren (Feld "itemName"). Fehlt noch.

		// TODO (KOSTEN): Platzhalter - siehe BookMerger.PLACEHOLDER_COST
		this.cost.set(BookMerger.PLACEHOLDER_COST);
		this.repairItemCountCost = 0;

		menu.getSlot(2).set(result);
		menu.broadcastChanges();

		// TODO: Testen: Entnehmen verbraucht beide Items, Level werden abgezogen (Survival + Kreativ).

		ci.cancel(); // Vanilla-Berechnung ueberspringen
	}
}
