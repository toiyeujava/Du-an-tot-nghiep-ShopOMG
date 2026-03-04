/**
 * Theme Toggle - Dark/Light Mode
 * Handles theme switching, localStorage persistence, and checkbox sync.
 */
(function () {
    'use strict';

    var STORAGE_KEY = 'theme';
    var DARK = 'dark';
    var LIGHT = 'light';

    /**
     * Get the current saved theme from localStorage.
     * Returns 'dark' or 'light' (defaults to 'light').
     */
    function getSavedTheme() {
        return localStorage.getItem(STORAGE_KEY) || LIGHT;
    }

    /**
     * Apply theme to the document and sync the checkbox.
     */
    function applyTheme(theme) {
        if (theme === DARK) {
            document.documentElement.setAttribute('data-theme', DARK);
        } else {
            document.documentElement.removeAttribute('data-theme');
        }

        // Sync checkbox state
        var checkbox = document.getElementById('themeToggleCheckbox');
        if (checkbox) {
            checkbox.checked = (theme === DARK);
        }
    }

    /**
     * Toggle between light and dark themes.
     */
    function toggleTheme() {
        var current = getSavedTheme();
        var next = (current === DARK) ? LIGHT : DARK;
        localStorage.setItem(STORAGE_KEY, next);
        applyTheme(next);
    }

    /**
     * Initialize theme on DOMContentLoaded.
     * The anti-FOUC script in <head> already sets data-theme,
     * so this just syncs the checkbox.
     */
    function init() {
        var theme = getSavedTheme();
        applyTheme(theme);

        // Attach event listener to the toggle checkbox
        var checkbox = document.getElementById('themeToggleCheckbox');
        if (checkbox) {
            checkbox.addEventListener('change', function () {
                toggleTheme();
            });
        }
    }

    // Run init when DOM is ready
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})();
