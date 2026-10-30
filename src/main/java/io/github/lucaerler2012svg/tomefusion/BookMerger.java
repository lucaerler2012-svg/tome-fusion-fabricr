package io.github.lucaerler2012svg.tomefusion;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.function.Predicate;
import net.minecraft.core.Holder;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

/**
 * Reine Rechenlogik (kein Mixin):
 *  - Level gleicher Verzauberungen werden ADDIERT
 *  - Ergebnis wird auf (Vanilla-Maximum x LIMIT_MULTIPLIER) begrenzt
 *  - Verzauberungen, die nur auf einem Buch vorkommen, werden uebernommen
 */
public final class BookMerger {

	/** Faktor fuer die Obergrenze: Limit = Vanilla-Max-Level x 2. */
	public static final int LIMIT_MULTIPLIER = 2;

	/**
	 * TODO (KOSTEN): Platzhalter! Hier entscheidest du spaeter, wie teuer das Kombinieren ist.
	 * Ideen: feste Kosten, Summe der Ergebnis-Level, 2^Level, Anzahl der Verzauberungen ...
	 * Vanilla zeigt im Survival "Zu teuer!" ab 40 Level. Soll das weiter gelten?
	 */
	public static final int PLACEHOLDER_COST = 3;

	private BookMerger() {}

	/** Kombiniert die gespeicherten Verzauberungen zweier Buecher. */
	public static ItemEnchantments merge(ItemEnchantments left, ItemEnchantments right) {
		return merge(left, right, enchantment -> true);
	}

	/**
	 * Wie oben, aber nur Verzauberungen von "right", die der Filter erlaubt, werden uebernommen.
	 * Wird fuer Buch auf Werkzeug/Waffe genutzt (Filter: passt die Verzauberung auf das Item?).
	 */
	public static ItemEnchantments merge(ItemEnchantments left, ItemEnchantments right,
			Predicate<Holder<Enchantment>> allowed) {
		ItemEnchantments.Mutable result = new ItemEnchantments.Mutable(left);

		for (Object2IntMap.Entry<Holder<Enchantment>> entry : right.entrySet()) {
			Holder<Enchantment> enchantment = entry.getKey();
			int rightLevel = entry.getIntValue();
			if (!allowed.test(enchantment)) {
				continue; // passt nicht auf dieses Item (z. B. Schaerfe auf Spitzhacke)
			}
			int leftLevel = result.getLevel(enchantment); // 0, falls nicht vorhanden

			if (leftLevel > 0) {
				// Gleiche Verzauberung auf beiden Buechern -> addieren + begrenzen
				result.set(enchantment, limit(enchantment, leftLevel + rightLevel));
			} else if (isCompatibleWithAll(enchantment, result)) {
				// Neue Verzauberung -> uebernehmen
				result.set(enchantment, limit(enchantment, rightLevel));
			}
			// TODO: Inkompatible Verzauberungen (z. B. Schaerfe + Bann der Gliederfuessler)
			//       werden aktuell stillschweigend verworfen. Alternative: Kombination verbieten.
		}
		return result.toImmutable();
	}

	/** Begrenzt ein Level auf Vanilla-Maximum x 2. */
	public static int limit(Holder<Enchantment> enchantment, int level) {
		int vanillaMax = enchantment.value().getMaxLevel();

		// TODO: Verzauberungen mit Max-Level 1 (Mending, Silk Touch ...) bleiben auf Level 1.
		if (vanillaMax <= 1) {
			return 1;
		}
		return Math.min(level, vanillaMax * LIMIT_MULTIPLIER);
	}

	private static boolean isCompatibleWithAll(Holder<Enchantment> candidate, ItemEnchantments.Mutable existing) {
		// TODO: Falls der Build hier fehlschlaegt: Enchantment.areCompatible(Holder, Holder)
		//       in der IDE (./gradlew genSources) nachschlagen und anpassen.
		for (Holder<Enchantment> other : existing.keySet()) {
			if (!other.equals(candidate) && !Enchantment.areCompatible(other, candidate)) {
				return false;
			}
		}
		return true;
	}
}
