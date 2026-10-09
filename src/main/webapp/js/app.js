(function () {
    "use strict";

    // Image fallback: if a product image fails to load, show a neutral placeholder.
    document.addEventListener("error", function (event) {
        var target = event.target;
        if (target && target.tagName === "IMG" && !target.dataset.fallbackApplied) {
            target.dataset.fallbackApplied = "true";
            target.src = "data:image/svg+xml;utf8," + encodeURIComponent(
                "<svg xmlns='http://www.w3.org/2000/svg' width='400' height='400'>" +
                "<rect width='400' height='400' fill='#eef1f6'/>" +
                "<text x='50%' y='50%' font-family='sans-serif' font-size='22' fill='#a0aec0' " +
                "text-anchor='middle' dominant-baseline='middle'>Giftora</text></svg>");
        }
    }, true);

    // Confirmation for destructive actions.
    document.addEventListener("click", function (event) {
        var el = event.target.closest("[data-confirm]");
        if (el) {
            var message = el.getAttribute("data-confirm") || "Are you sure?";
            if (!window.confirm(message)) {
                event.preventDefault();
                event.stopPropagation();
            }
        }
    });

    // Auto-dismiss alerts after a while.
    setTimeout(function () {
        document.querySelectorAll(".alert[data-auto-dismiss]").forEach(function (alert) {
            alert.style.transition = "opacity 0.4s ease";
            alert.style.opacity = "0";
            setTimeout(function () { alert.remove(); }, 400);
        });
    }, 4000);
})();
