/*=====================================
        SLAP LAND ADMIN PANEL
======================================*/

*{
    margin:0;
    padding:0;
    box-sizing:border-box;
}

:root{

    --blue:#00c8ff;
    --orange:#ff8a00;

    --bg:#070d18;

    --card:rgba(18,25,42,.72);

    --text:#ffffff;

    --text2:#b7c1d8;

}

body{

    font-family:tahoma,sans-serif;

    background:var(--bg);

    color:var(--text);

    min-height:100vh;

    overflow:hidden;

    display:flex;

    justify-content:center;

    align-items:center;

}

/*====================
    Background
====================*/

.background{

    position:fixed;

    inset:0;

    overflow:hidden;

    z-index:-2;

}

.background span{

    position:absolute;

    border-radius:50%;

    filter:blur(100px);

    opacity:.25;

    animation:move 14s ease-in-out infinite;

}

.background span:nth-child(1){

    width:420px;

    height:420px;

    background:var(--blue);

    top:-120px;

    right:-120px;

}

.background span:nth-child(2){

    width:350px;

    height:350px;

    background:var(--orange);

    bottom:-80px;

    left:-80px;

    animation-delay:4s;

}

.background span:nth-child(3){

    width:180px;

    height:180px;

    background:#ffffff;

    opacity:.08;

    top:45%;

    left:50%;

}

@keyframes move{

    0%,100%{

        transform:translate(0,0);

    }

    50%{

        transform:translate(35px,-35px);

    }

}

/*====================
      Panel
====================*/

.panel{

    width:min(94%,500px);

    background:var(--card);

    backdrop-filter:blur(18px);

    border:1px solid rgba(255,255,255,.08);

    border-radius:28px;

    padding:35px;

    box-shadow:

    0 0 30px rgba(0,200,255,.25),

    0 0 45px rgba(255,138,0,.15);

    animation:show .7s ease;

}

@keyframes show{

    from{

        opacity:0;

        transform:translateY(30px);

    }

    to{

        opacity:1;

        transform:none;

    }

}

h1{

    text-align:center;

    font-size:40px;

    letter-spacing:2px;

    text-shadow:

    0 0 20px var(--blue),

    0 0 35px var(--orange);

}

.subtitle{

    text-align:center;

    margin-top:10px;

    color:var(--text2);

    margin-bottom:30px;

}

/*====================
      Inputs
====================*/

input{

    width:100%;

    padding:15px;

    margin-bottom:15px;

    border:none;

    outline:none;

    border-radius:14px;

    background:rgba(255,255,255,.06);

    color:white;

    font-size:15px;

    transition:.3s;

}

input::placeholder{

    color:#9da8bf;

}

input:focus{

    box-shadow:

    0 0 18px rgba(0,200,255,.35);

}

/*====================
      Buttons
====================*/

button{

    width:100%;

    border:none;

    cursor:pointer;

    padding:15px;

    margin-top:10px;

    border-radius:14px;

    color:white;

    font-size:15px;

    font-weight:bold;

    background:

    linear-gradient(135deg,

    var(--blue),

    var(--orange));

    transition:.25s;

}

button:hover{

    transform:translateY(-3px);

    box-shadow:

    0 0 22px rgba(0,200,255,.4),

    0 0 22px rgba(255,138,0,.3);

}

button:active{

    transform:scale(.97);

}

/*====================
      Cards
====================*/

.card{

    background:rgba(255,255,255,.05);

    border:1px solid rgba(255,255,255,.06);

    border-radius:18px;

    padding:18px;

    margin-bottom:18px;

}

.card h2{

    font-size:17px;

    margin-bottom:12px;

}

.card p{

    color:var(--text2);

    line-height:1.7;

}

/*====================
      Messages
====================*/

#loginMessage{

    margin-top:15px;

    text-align:center;

    min-height:20px;

    color:#ffc95d;

}

.online{

    color:#4cff84!important;

}

.offline{

    color:#ff5858!important;

}

.wait{

    color:#ffd75b!important;

}

/*====================
      Footer
====================*/

footer{

    position:fixed;

    bottom:18px;

    left:50%;

    transform:translateX(-50%);

    display:flex;

    align-items:center;

    gap:10px;

    padding:8px 18px;

    border-radius:40px;

    background:rgba(255,255,255,.05);

    border:1px solid rgba(255,255,255,.08);

    backdrop-filter:blur(10px);

    font-size:13px;

}

footer img{

    width:30px;

    height:30px;

    border-radius:50%;

    object-fit:cover;

    border:2px solid var(--blue);

    box-shadow:

    0 0 12px var(--blue),

    0 0 12px var(--orange);

}

/*====================
      Mobile
====================*/

@media(max-width:650px){

.panel{

    padding:25px;

}

h1{

    font-size:32px;

}

.subtitle{

    font-size:14px;

}

button{

    font-size:14px;

}

footer{

    font-size:12px;

    padding:8px 14px;

}

footer img{

    width:26px;

    height:26px;

}

}
