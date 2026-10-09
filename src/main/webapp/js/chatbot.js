(function () {
    "use strict";

    var toggle = document.getElementById("chatToggle");
    var panel = document.getElementById("chatPanel");
    var closeBtn = document.getElementById("chatClose");
    var form = document.getElementById("chatForm");
    var input = document.getElementById("chatInput");
    var messages = document.getElementById("chatMessages");

    if (!toggle || !panel) {
        return;
    }

    function setOpen(open) {
        panel.classList.toggle("open", open);
        if (open && input) {
            input.focus();
        }
    }

    toggle.addEventListener("click", function () { setOpen(!panel.classList.contains("open")); });
    if (closeBtn) {
        closeBtn.addEventListener("click", function () { setOpen(false); });
    }

    function addMessage(text, kind) {
        var div = document.createElement("div");
        div.className = "chat-msg " + kind;
        div.textContent = text;
        messages.appendChild(div);
        messages.scrollTop = messages.scrollHeight;
        return div;
    }

    function sendMessage(message) {
        var loading = addMessage("...", "bot");
        loading.innerHTML = '<span class="spinner"></span>';
        fetch("api/v1/chatbot", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ message: message })
        }).then(function (response) {
            return response.json().then(function (body) {
                return { ok: response.ok, status: response.status, body: body };
            });
        }).then(function (result) {
            loading.remove();
            if (result.ok && result.body && result.body.data) {
                addMessage(result.body.data.reply, "bot");
            } else {
                var msg = result.body && result.body.error ? result.body.error : "Sorry, something went wrong.";
                addMessage(msg, "error");
            }
        }).catch(function () {
            loading.remove();
            addMessage("I could not connect right now. Please try again.", "error");
        });
    }

    if (form) {
        form.addEventListener("submit", function (event) {
            event.preventDefault();
            var value = input.value.trim();
            if (!value) {
                return;
            }
            if (value.length > 500) {
                addMessage("Please keep your message under 500 characters.", "error");
                return;
            }
            addMessage(value, "user");
            input.value = "";
            sendMessage(value);
        });
    }
})();
