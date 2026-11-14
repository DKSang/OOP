package com.airlinereservation.gui;

import javafx.css.CssParser;
import javafx.css.Stylesheet;
import org.junit.jupiter.api.Test;

import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArbitrumThemeTest {

    @Test
    void bundlesAllArbitrumFontsAndValidStylesheetWithoutLegacyAnthropicCss() throws Exception {
        for (String fontResource : ArbitrumTheme.FONT_RESOURCES) {
            assertNotNull(ArbitrumTheme.class.getResource(fontResource), "Missing font: " + fontResource);
        }
        assertEquals(4, ArbitrumTheme.FONT_RESOURCES.size());

        ArbitrumTheme.loadFonts();

        URL stylesheetUrl = ArbitrumTheme.class.getResource(ArbitrumTheme.STYLESHEET_RESOURCE);
        assertNotNull(stylesheetUrl, "Missing arbitrum-theme.css");
        assertNull(ArbitrumTheme.class.getResource("anthropic-theme.css"), "Legacy CSS must be removed");

        CssParser.errorsProperty().clear();
        Stylesheet stylesheet = new CssParser().parse(stylesheetUrl);
        assertNotNull(stylesheet);
        assertTrue(stylesheet.getRules().size() > 20);
        assertTrue(CssParser.errorsProperty().isEmpty(), () -> "CSS parse errors: " + CssParser.errorsProperty());
    }
}
