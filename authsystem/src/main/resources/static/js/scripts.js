document.addEventListener("DOMContentLoaded", function () {
    const toggle = document.getElementById("darkModeToggle");
    const icon = document.getElementById("modeIcon");

    
    if (localStorage.getItem("theme") === "dark") {
        document.body.classList.add("dark-mode");
        icon.textContent = "🌙";
    } else {
        icon.textContent = "☀️";
    }

    toggle.addEventListener("click", () => {
        document.body.classList.toggle("dark-mode");
        if (document.body.classList.contains("dark-mode")) {
            localStorage.setItem("theme", "dark");
            icon.textContent = "🌙";
        } else {
            localStorage.setItem("theme", "light");
            icon.textContent = "☀️";
        }
    });
});
