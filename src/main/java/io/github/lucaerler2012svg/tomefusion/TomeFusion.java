package io.github.lucaerler2012svg.tomefusion;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Haupteinstiegspunkt der Mod.
 *
 * Die eigentliche Arbeit passiert im Mixin
 * ({@link io.github.lucaerler2012svg.tomefusion.mixin.AnvilMenuMixin}) und in {@link BookMerger}.
 */
public class TomeFusion implements ModInitializer {

	public static final String MOD_ID = "tomefusion";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Tome Fusion geladen - Buecher werden im Amboss addiert.");

		// Eigener Kreativ-Tab mit allen Buechern (Level 1 bis Limit, der Reihe nach)
		TomeTabs.register();

		// TODO: Optional Config-Datei (Multiplikator, Kosten-Modus, Mod an/aus).
	}
}
