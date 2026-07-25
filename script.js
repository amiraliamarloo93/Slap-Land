/*
====================================
SLAP LAND
script.js
====================================
*/

const SERVER_HOST = "amir_kalam-srv-slap_landjava.play.mihancraft.com";
const SERVER_PORT = "25566";

const API =
`https://api.mcstatus.io/v2/status/java/${SERVER_HOST}:${SERVER_PORT}`;

const statusElement = document.getElementById("status");
const playersElement = document.getElementById("players");
const versionElement = document.getElementById("version");
const copyButton = document.getElementById("copyButton");
const refreshButton = document.getElementById("refreshButton");
const messageElement = document.getElementById("message");

/* -----------------------------
   دریافت اطلاعات سرور
----------------------------- */

async function loadServerStatus() {

    statusElement.className = "status loading";
    statusElement.textContent = "درحال دریافت...";

    try {

        const response = await fetch(API);

        if (!response.ok)
            throw new Error();

        const data = await response.json();

        if (data.online) {

            statusElement.className = "status online";
            statusElement.textContent = "🟢 آنلاین";

            playersElement.textContent =
                `${data.players.online} / ${data.players.max}`;

            versionElement.textContent =
                data.version.name_clean || data.version.name;

        } else {

            statusElement.className = "status offline";
            statusElement.textContent = "🔴 آفلاین";

            playersElement.textContent = "--";

            versionElement.textContent = "--";

        }

    }

    catch {

        statusElement.className = "status offline";

        statusElement.textContent = "خطا در اتصال";

        playersElement.textContent = "--";

        versionElement.textContent = "--";

    }

}

/* -----------------------------
   کپی آی پی
----------------------------- */

copyButton.addEventListener("click", async () => {

    const address =
`${SERVER_HOST}:${SERVER_PORT}`;

    try {

        await navigator.clipboard.writeText(address);

        messageElement.textContent =
        "✅ آی‌پی کپی شد.";

    }

    catch {

        messageElement.textContent =
        address;

    }

    setTimeout(() => {

        messageElement.textContent = "";

    }, 2500);

});

/* -----------------------------
   بروزرسانی دستی
----------------------------- */

refreshButton.addEventListener("click", () => {

    loadServerStatus();

});

/* -----------------------------
   بروزرسانی خودکار
----------------------------- */

loadServerStatus();

setInterval(loadServerStatus,30000);
