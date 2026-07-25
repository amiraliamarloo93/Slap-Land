/*=========================================
        SLAP LAND ADMIN PANEL
=========================================*/

const SUPABASE_URL = "https://cpwjoibxdinpczaifecm.supabase.co";
const SUPABASE_KEY = "sb_publishable_Y0PWp2A0-T96kdHtT_hHmQ_b65g2xzK";

const supabase = window.supabase.createClient(
    SUPABASE_URL,
    SUPABASE_KEY
);

/*------------------ Elements ------------------*/

const loginPage = document.getElementById("loginPage");
const dashboard = document.getElementById("dashboard");

const adminName = document.getElementById("adminName");
const password = document.getElementById("password");

const loginButton = document.getElementById("loginButton");
const logoutButton = document.getElementById("logoutButton");

const loginMessage = document.getElementById("loginMessage");

const pcStatus = document.getElementById("pcStatus");
const requestStatus = document.getElementById("requestStatus");

const requestButton = document.getElementById("requestButton");

let currentUser = null;

/*=========================================
            Check Login
=========================================*/

async function checkLogin(){

    const { data } = await supabase.auth.getSession();

    if(data.session){

        currentUser = data.session.user;

        loginPage.style.display = "none";
        dashboard.style.display = "block";

        loadComputer();
        loadRequest();

        setInterval(loadComputer,5000);
        setInterval(loadRequest,5000);

    }

}

checkLogin();

/*=========================================
                Login
=========================================*/

loginButton.onclick = async ()=>{

    loginMessage.textContent="";

    if(adminName.value.trim()===""){

        loginMessage.textContent="نام مدیر را وارد کنید.";

        return;

    }

    const email =
    currentUser?.email || prompt("ایمیل مدیر را وارد کنید");

    const { error } =
    await supabase.auth.signInWithPassword({

        email:email,

        password:password.value

    });

    if(error){

        loginMessage.textContent="ورود ناموفق بود.";

        return;

    }

    localStorage.setItem(
        "adminName",
        adminName.value
    );

    location.reload();

};

/*=========================================
            Logout
=========================================*/

logoutButton.onclick = async ()=>{

    await supabase.auth.signOut();

    location.reload();

};

/*=========================================
      Computer Status
=========================================*/

async function loadComputer(){

    const { data } =
    await supabase

    .from("computer")

    .select("*")

    .eq("id",1)

    .single();

    if(!data){

        pcStatus.textContent="خطا";

        return;

    }

    if(data.online){

        pcStatus.className="online";

        pcStatus.textContent="🟢 کامپیوتر آنلاین";

    }

    else{

        pcStatus.className="offline";

        pcStatus.textContent="🔴 کامپیوتر خاموش";

    }

}

/*=========================================
        Last Request
=========================================*/

async function loadRequest(){

    const { data } =
    await supabase

    .from("requests")

    .select("*")

    .order("id",{ascending:false})

    .limit(1);

    if(!data.length){

        requestStatus.textContent=
        "درخواستی ثبت نشده است.";

        return;

    }

    const req=data[0];

    requestStatus.innerHTML=`

    مدیر:
    ${req.admin_name}

    <br><br>

    وضعیت:

    ${req.status}

    `;

}

/*=========================================
        Send Request
=========================================*/

requestButton.onclick = async ()=>{

    requestButton.disabled=true;

    requestButton.textContent=
    "درحال ارسال...";

    await supabase

    .from("requests")

    .insert({

        admin_name:
        localStorage.getItem("adminName"),

        status:"waiting"

    });

    requestButton.textContent=
    "✅ درخواست ثبت شد";

    loadRequest();

    setTimeout(()=>{

        requestButton.disabled=false;

        requestButton.textContent=
        "🟢 درخواست روشن کردن سرور";

    },3000);

};
