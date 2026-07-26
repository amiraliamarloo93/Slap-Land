/*
====================================
 SLAP LAND
 script.js
====================================
*/

const SERVER_HOST = "amir_kalam-srv-slap_landjava.play.mihancraft.com";
const SERVER_PORT = "25565";

const API =
`https://api.mcstatus.io/v2/status/java/${SERVER_HOST}:${SERVER_PORT}`;

const serverCard = document.getElementById("serverCard");

const status = document.getElementById("status");
const players = document.getElementById("players");
const version = document.getElementById("version");
const ip = document.getElementById("ip");

const copyButton = document.getElementById("copyButton");
const refreshButton = document.getElementById("refreshButton");
const message = document.getElementById("message");

/* ==============================
      دریافت وضعیت سرور
================================ */

async function loadServer(){

    status.className = "status loading";
    status.textContent = "درحال دریافت...";

    try{

        const response = await fetch(API);

        const data = await response.json();

        if(data.online){

            status.className = "status online";
            status.textContent = "🟢 آنلاین";

            players.textContent =
            `${data.players.online} / ${data.players.max}`;

            version.textContent =
            data.version.name_clean || data.version.name;

            serverCard.style.boxShadow =
            "0 0 35px rgba(50,255,120,.35)";

        }

        else{

            status.className = "status offline";
            status.textContent = "🔴 آفلاین";

            players.textContent = "--";

            version.textContent = "--";

            serverCard.style.boxShadow =
            "0 0 35px rgba(255,70,70,.35)";

        }

    }

    catch{

        status.className = "status offline";

        status.textContent = "خطا در اتصال";

        players.textContent = "--";

        version.textContent = "--";

    }

}

/* ==============================
         کپی IP
================================ */

copyButton.onclick = async ()=>{

    const address =
`${SERVER_HOST}:${SERVER_PORT}`;

    try{

        await navigator.clipboard.writeText(address);

        message.textContent =
        "✅ آی‌پی با موفقیت کپی شد.";

    }

    catch{

        message.textContent =
        address;

    }

    setTimeout(()=>{

        message.textContent="";

    },2500);

};

/* ==============================
      بروزرسانی
================================ */

refreshButton.onclick=()=>{

    loadServer();

};

/* ==============================
     بروزرسانی خودکار
================================ */

loadServer();

setInterval(loadServer,20000);

/* ==============================
      افکت سه بعدی کارت
================================ */

document.addEventListener("mousemove",(e)=>{

    const rect =
    serverCard.getBoundingClientRect();

    const x =
    e.clientX-rect.left;

    const y =
    e.clientY-rect.top;

    const rotateX =
    ((y-rect.height/2)/20);

    const rotateY =
    ((rect.width/2-x)/20);

    serverCard.style.transform =
    `rotateX(${rotateX}deg)
     rotateY(${rotateY}deg)`;

});

document.addEventListener("mouseleave",()=>{

    serverCard.style.transform =
    "rotateX(0deg) rotateY(0deg)";

});

/* ==============================
      افکت دکمه ها
================================ */

document.querySelectorAll("button").forEach(button=>{

    button.addEventListener("mouseenter",()=>{

        button.style.scale="1.05";

    });

    button.addEventListener("mouseleave",()=>{

        button.style.scale="1";

    });

});

/* ==============================
      انیمیشن عنوان
================================ */

const title =
document.querySelector(".server-name");

let glow = true;

setInterval(()=>{

    if(glow){

        title.style.textShadow =
        "0 0 20px #00c3ff,0 0 40px #ff8a00";

    }

    else{

        title.style.textShadow =
        "0 0 12px #00c3ff";

    }

    glow=!glow;

},1200);

/* ==============================
       نمایش IP
================================ */

ip.textContent =
SERVER_HOST;
